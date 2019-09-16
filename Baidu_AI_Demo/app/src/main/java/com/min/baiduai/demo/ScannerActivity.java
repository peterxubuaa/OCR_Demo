package com.min.baiduai.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.min.baiduai.demo.baidu.ocr.RecognizeService;
import com.min.baiduai.demo.baidu.speech.Recognizer;
import com.min.baiduai.demo.baidu.speech.listener.IRecogListener;
import com.min.baiduai.demo.baidu.speech.listener.MessageStatusRecogListener;
import com.min.baiduai.demo.baidu.tts.OfflineResource;
import com.min.baiduai.demo.baidu.tts.control.InitConfig;
import com.min.baiduai.demo.baidu.tts.control.MySyntherizer;
import com.min.baiduai.demo.baidu.tts.control.NonBlockSyntherizer;
import com.min.baiduai.demo.baidu.tts.listener.UiMessageListener;
import com.min.baiduai.demo.camera.CameraManager;
import com.min.baiduai.demo.decode.CaptureActivityHandler;
import com.min.baiduai.demo.utils.BmpTools;
import com.min.baiduai.demo.utils.CommonTools;
import com.min.baiduai.demo.view.ImageDialog;
import com.min.baiduai.demo.view.ScannerFinderView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static com.min.baiduai.demo.baidu.speech.IStatus.*;
import static com.min.baiduai.demo.baidu.tts.MainHandlerConstant.*;

public class ScannerActivity extends Activity implements Callback, Camera.PictureCallback,
        ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = ScannerActivity.class.getSimpleName();

    private static final int RECOGNIZE_SUCCESS = 1001;
    private final int SETTING_REQUEST_CODE = 0;
    private final int REQUEST_MULTIPLE_PERMISSION = 100;
    private final int OCR_INIT_EXIT_MASK = 0x1;
    private final int OCR_START_STOP_MASK = 0x2;
    private final int SPEECH_INIT_EXIT_MASK = 0x10;
    private static final int SPEECH_START_STOP_MASK = 0x20;
    private final int TTS_INIT_EXIT_MASK = 0x100;
    private static final int TTS_START_STOP_MASK = 0x200;

    private CaptureActivityHandler mCaptureActivityHandler;
    private boolean mHasSurface;
    private ScannerFinderView mFinderView;
    private SurfaceView mSurfaceView;
    private Button mRecognizeButton;

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;
    private ImageDialog mImageDialog;
    private Bitmap mBmp = null;
    private int mOrientation = 0;
    private boolean mAllApkPermission = false;
    private SettingActivity.SettingResults mSettingResults = null;
    private int mWorkState = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

//        Point pt1 = CommonTools.getScreenSize(this);
//        Point pt2 = CommonTools.getDisplaySize(this);
        applyApkPermissions();

        initView();
        //init setting params
        mSettingResults = SettingActivity.getSettingResults(this);
        //1. baidu ocr
        initBaiduOCR();
        //2. baidu speech
        initBaiduSpeech();
        //3. baidu tts
        initBaiduTTS();
    }

    //ViewTreeObserver.OnGlobalLayoutListener
    @Override
    public void onGlobalLayout() {
        mSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        mFinderView = findViewById(R.id.view_finder);
        mFinderView.setOrientation(mOrientation);
        int h1 = findViewById(R.id.header_bt).getHeight();
        int h2 = CommonTools.getStatusBarHeight(this);
        int h3 = findViewById(R.id.recognize_bt).getHeight();
        int h4 = CommonTools.getNavigationBarHeight(this);
        mFinderView.setTopBottomLimit(h1, h2 + h3 + h4);
    }

    private void applyApkPermissions() {
        final String[] requiredPermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA};
        ArrayList<String> denyPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
                continue;
            denyPermissions.add(permission);
        }
        if (denyPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, denyPermissions.toArray(new String[0]), REQUEST_MULTIPLE_PERMISSION);
            mAllApkPermission = false;
        } else {
            mAllApkPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_MULTIPLE_PERMISSION == requestCode) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    mAllApkPermission = false;
                    alertText(getResources().getString(R.string.notification), getResources().getString(R.string.permission_allow));
                    SystemClock.sleep(2000);
                    applyApkPermissions();
//                    Toast.makeText(this, "Must allow all permissions", Toast.LENGTH_LONG).show();
//                    SystemClock.sleep(5000);
//                    finish();
                    return;
                }
            }
            mAllApkPermission = true;
            initView();
            if (mSettingResults.mOCREnable) {
                CameraManager.init();
                initCamera();
            }

            if (mSettingResults.mSpeechEnable) {
                startBaiduSpeechRecognize();
            }
        }
    }

    private void initView() {
        //获取当前Activity的屏幕方向
        int orientation = getRequestedOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mOrientation = 90;
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mOrientation = 0;
        }

        mHasSurface = false;

        findViewById(R.id.header_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScannerActivity.this, SettingActivity.class);
                startActivityForResult(intent, SETTING_REQUEST_CODE);
            }
        });

        mRecognizeButton = findViewById(R.id.recognize_bt);
        resetRecognizeBTSize(mRecognizeButton);
        mRecognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSettingResults.mVoiceEnable) {
                    if (stopTTSSpeak()) return;
                }
                if (mSettingResults.mSpeechEnable) {
                    if (isBaiduSpeechRecognizeRunning()) {
                        stopBaiduSpeechRecognize();
                    } else {
                        startBaiduSpeechRecognize();
                    }
                } else if (mSettingResults.mOCREnable) {
                    startBaiduOCR();
                }
            }
        });

        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setCancelable(true);
        mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartPreview();
            }
        });

        mImageDialog = new ImageDialog(this);
        mImageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartPreview();
            }
        });

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartPreview();
            }
        });
    }

    private void resetRecognizeBTSize(Button bt) {
        Point scrRes = CommonTools.getDisplaySize(this);
//        float testSize = scrRes.x / 10;
//        bt.setTextSize(testSize);
        ViewGroup.LayoutParams params = bt.getLayoutParams();
        params.width = scrRes.x;
        params.height = scrRes.y / 5;
        bt.setLayoutParams(params);
    }

    public Rect getCropRect() {
        return mFinderView.getRect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SETTING_REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            mSettingResults = SettingActivity.getSettingResults(this);
            initBaiduOCRSettings();
            initBaiduSpeechSettings();
            initBaiduTTSSettings();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAllApkPermission) {
            if (mSettingResults.mOCREnable) {
                CameraManager.init();
                initCamera();
            }
            if (mSettingResults.mSpeechEnable) {
//                startBaiduSpeechRecognize();
            }
        }
    }

    @Override
    protected void onPause() {
        if (mSettingResults.mOCREnable) {
            if (null != mFinderView) mFinderView.setVisibility(View.GONE);
            if (mCaptureActivityHandler != null) {
                try {
                    mCaptureActivityHandler.quitSynchronously();
                    mCaptureActivityHandler = null;
                    if (null != mSurfaceView && !mHasSurface) {
                        mSurfaceView.getHolder().removeCallback(this);
                    }
                    if (!CameraManager.get().closeDriver()) {
                        finish();
                    }
                } catch (Exception e) {
                    // 关闭摄像头失败的情况下,最好退出该Activity,否则下次初始化的时候会显示摄像头已占用.
                    finish();
                }
            }
        }

        if (mSettingResults.mSpeechEnable) {
            stopBaiduSpeechRecognize();
        }

        if (mSettingResults.mVoiceEnable) {

        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseBaiduOCR();
        releaseBaiduSpeech();
        releaseBaiduTTS();
    }

    private void initCamera() {
        if (null == mSurfaceView) {
            ViewStub viewStub = findViewById(R.id.view_stub);
            viewStub.setLayoutResource(R.layout.layout_surface_view);
            mSurfaceView = (SurfaceView) viewStub.inflate();
        }
        mSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            if (!CameraManager.get().openDriver(this, surfaceHolder, mOrientation)) {
                finish();
                return;
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        mFinderView.setVisibility(View.VISIBLE);
        if (mCaptureActivityHandler == null) {
            mCaptureActivityHandler = new CaptureActivityHandler(mBaiduOCR_Flash);
        }
    }

    public void restartPreview() {
        if (null != mCaptureActivityHandler) {
            try {
                mCaptureActivityHandler.restartPreviewAndDecode(mBaiduOCR_Flash);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            if (mSettingResults.mOCREnable) {
                initCamera(holder);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (null != mBmp) mBmp.recycle();
        mBmp = null;
        if (null == data) return;

        mCaptureActivityHandler.onPause();
        mBmp = BmpTools.getFocusedBitmap(this, camera, data, getCropRect(), mOrientation);

        baiduOCRText(mBmp);
    }

    private void OCRSuccess(String result, Bitmap bitmap) {
        String filterStr = "";
        if (mSettingResults.mChessEnable) {
            filterStr = CommonTools.leftChineseCharacter(result);
            if (!TextUtils.isEmpty(filterStr)) {
                compareRank(filterStr);
            } else {
//            playHintVoice("military_chess_voice/compare_fail.m4a", 3000);
                if (mSettingResults.mVoiceEnable) {
                    startTTSSpeak("比较失败,请重试", 2000);
                }
            }
        } else {
            if (mBaiduOCR_LanguageType.equals("CHN")) {
                filterStr = CommonTools.filterEnglishCharacter(result);
            } else {
                filterStr = result;
            }
            if (mSettingResults.mVoiceEnable) {
                startTTSSpeak(filterStr, -1);
            }
        }

        if (mSettingResults.mOCREnable_Debug) {
            mImageDialog.addBitmap(bitmap);
            if (TextUtils.isEmpty(result)) {
                mImageDialog.addTitle(getResources().getString(R.string.recognize_fail), "");
            } else {
                mImageDialog.addTitle(result, filterStr);
            }
            mImageDialog.show();
        } else {
            restartPreview();
        }
    }

    private void compareRank(String content) {
        CompareRank compareRank = new CompareRank();
        int result = compareRank.compare(content);

        switch (result) {
            case CompareRank.RANK_WIN:
//                playHintVoice("military_chess_voice/left_win.m4a", 2000);
                if (mSettingResults.mVoiceEnable) startTTSSpeak("左边获胜", 1000);
                break;
            case CompareRank.RANK_DRAW:
//                playHintVoice("military_chess_voice/both_dead.m4a", 2000);
                if (mSettingResults.mVoiceEnable) startTTSSpeak("双方同归于尽", 1000);
                break;
            case CompareRank.RANK_LOSE:
//                playHintVoice("military_chess_voice/right_win.m4a", 2000);
                if (mSettingResults.mVoiceEnable) startTTSSpeak("右边获胜", 1000);
                break;
            case CompareRank.GAME_WIN:
//                playHintVoice("military_chess_voice/left_win.m4a", 2000);
//                playHintVoice("military_chess_voice/game_over.m4a", 4000);
                if (mSettingResults.mVoiceEnable) startTTSSpeak("左边获胜,军旗被扛游戏结束", 2000);
                break;
            case CompareRank.GAME_LOSE:
//                playHintVoice("military_chess_voice/right_win.m4a", 2000);
//                playHintVoice("military_chess_voice/game_over.m4a", 4000);
                if (mSettingResults.mVoiceEnable) startTTSSpeak("右边获胜,军旗被扛游戏结束", 2000);
                break;
            case CompareRank.INVALID:
//                playHintVoice("military_chess_voice/compare_fail.m4a", 4000);
                if (mSettingResults.mVoiceEnable) startTTSSpeak("比较失败请重试", 2000);
                break;
        }
    }

/*    void playHintVoice(String assertName, long delayMs) {
        if (mSettingResults.mVoiceEnable) {
            CommonTools.playVoice(this, assertName);
            SystemClock.sleep(delayMs);
        }
    }*/

    private void buildProgressDialog() {
        mProgressDialog.setMessage(getResources().getString(R.string.recognizing));
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

//    private void cancelProgressDialog() {
//        updateRecognizeButton(true);
//        if (mProgressDialog.isShowing()) {
//            mProgressDialog.dismiss();
//        }
//    }

    private void alertText(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAlertDialog.setTitle(title);
                mAlertDialog.setMessage(message);
//                mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", null);
                mAlertDialog.show();
            }
        });
    }

    private void updateRecognizeButton() {
        if (mSettingResults.mSpeechEnable) {
            if ((mWorkState & SPEECH_START_STOP_MASK) > 0) {
                mRecognizeButton.setTextColor(Color.GREEN);
                mRecognizeButton.setText(getResources().getString(R.string.speech_controlling));
            } else {
                mRecognizeButton.setTextColor(Color.RED);
                mRecognizeButton.setText(getResources().getString(R.string.recognize));
            }
        } else if (mSettingResults.mOCREnable) {
            if ((mWorkState & OCR_START_STOP_MASK) > 0) {
                mRecognizeButton.setTextColor(Color.GRAY);
                mRecognizeButton.setEnabled(false);
            } else {
                mRecognizeButton.setTextColor(Color.RED);
                mRecognizeButton.setEnabled(true);
            }
        }
    }

    private void dismissAllDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        if (mAlertDialog.isShowing()) mAlertDialog.dismiss();
        if (mImageDialog.isShowing()) mImageDialog.dismiss();;
    }

    //1. baidu OCR
    private boolean mBaiduOCR_HasToken = false;
    private Handler mBaiduOCR_Handler = new BaiduOCRHandler(this);
    private boolean mBaiduOCR_Notification = false;
    private boolean mBaiduOCR_Flash = false;
    private boolean mBaiduOCR_DetectDirection = true;
    private String mBaiduOCR_LanguageType = GeneralBasicParams.CHINESE_ENGLISH;
    private int mBaiduOCR_Rotation = 0;

    static class BaiduOCRHandler extends Handler {
        WeakReference<ScannerActivity> mTheActivity;

        BaiduOCRHandler(ScannerActivity activity) {
            mTheActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ScannerActivity theActivity = mTheActivity.get();
            if (ScannerActivity.RECOGNIZE_SUCCESS == msg.what) {
                theActivity.dismissAllDialog();
                theActivity.OCRSuccess((String) msg.obj, theActivity.mBmp);
            }
        }
    }

    private void initBaiduOCR() {
        initBaiduOCRSettings();
//        用明文ak，sk初始化
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
//                String token = result.getAccessToken();
                mBaiduOCR_HasToken = true;
                mWorkState |= OCR_INIT_EXIT_MASK;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK，SK方式获取OCR token失败", error.getMessage());
            }
        }, getApplicationContext(), "13MaYeYFLEYqZt8K3yMe8CGN", "b3CGEBw4W0np2TWquO08GAh0Q8HXcIMV");
    }

    private void initBaiduOCRSettings() {
        mBaiduOCR_Notification = mSettingResults.mOCREnable_Notification;
        mBaiduOCR_Flash = mSettingResults.mOCREnable_Flash;
        mBaiduOCR_DetectDirection = mSettingResults.mOCREnable_DetectDirection;
        mBaiduOCR_LanguageType = mSettingResults.mOCREnable_LanguageType;
        mBaiduOCR_Rotation = mSettingResults.mOCREnable_Rotation;
    }

    private void releaseBaiduOCR() {
        mWorkState &= ~OCR_INIT_EXIT_MASK;
    }

    private boolean checkTokenStatus() {
        if (!mBaiduOCR_HasToken) {
            Toast.makeText(getApplicationContext(), "OCR token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return mBaiduOCR_HasToken;
    }

    private void startBaiduOCR() {
        if (mBaiduOCR_Notification) CommonTools.playRingtone(this);
        mWorkState |= OCR_START_STOP_MASK;
        updateRecognizeButton();
        buildProgressDialog();
        if (mSettingResults.mChessEnable) {
//                playHintVoice("compare_start.m4a", 3000);
            if (mSettingResults.mVoiceEnable) startTTSSpeak("比较开始", 2000);
        }
        CameraManager.get().takeShot(null, //设置为空关闭拍照提示音
                null, ScannerActivity.this);
    }

    private void OCRResult(final String result) {
        mWorkState &= ~OCR_START_STOP_MASK;
        updateRecognizeButton();

        Message message = Message.obtain();
        message.what = ScannerActivity.RECOGNIZE_SUCCESS;
        message.obj = result;
        mBaiduOCR_Handler.sendMessage(message);
    }

    private void baiduOCRText(Bitmap bmp) {
        if (!checkTokenStatus()) return;

        try {
            Bitmap newBmp = bmp.copy(bmp.getConfig(), false);
            if (mBaiduOCR_Rotation > 0) {
                newBmp = BmpTools.rotateBitmap(newBmp, mBaiduOCR_Rotation);
            }
            File outputFile = new File(getFilesDir(), "pic.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            newBmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            newBmp.recycle();
            fileOutputStream.close();

            GeneralBasicParams param = new GeneralBasicParams();
            if ("CHN".equals(mBaiduOCR_LanguageType)) {
                param.setLanguageType(GeneralBasicParams.CHINESE_ENGLISH);
            } else {
                param.setLanguageType(mBaiduOCR_LanguageType);
            }
            param.setDetectDirection(mBaiduOCR_DetectDirection);
            param.setImageFile(outputFile);

            RecognizeService.recGeneralBasic(this, param,
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            OCRResult(result);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //2. baidu speech
    //baidu speech
    private Recognizer mBaiduSpeech_Recognizer = null;
    private int mBaiduSpeech_VADTimeout = 0; //0: 表示开启长语音； >0: 连续xxxms静音，断句间隔时间，表示一句话结束，比如800ms适合短句输入，2000ms适合长句输入;
    private int mBaiduSpeech_LanguageID = 15362;//1536: 默认普通话；15362：普通话模型，加强标点；1936：普通话远场模型； 1737：英语； 1837： 四川话

    static class BaiduSpeechHandler extends Handler {
        WeakReference<ScannerActivity> mTheActivity;

        BaiduSpeechHandler(ScannerActivity activity) {
            mTheActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
//                处理MessageStatusRecogListener中的状态回调
//                STATUS_NONE 初始状态
//                STATUS_READY 引擎准备完毕
//                STATUS_SPEAKING 用户开始说话到用户说话完毕前
//                STATUS_RECOGNITION 用户说话完毕后，识别结束前
//                STATUS_FINISHED 获得最终识别结果
                case STATUS_FINISHED:
                    if (1 == msg.arg2) {
                        mTheActivity.get().speechResult(msg.obj.toString());
                    }
                    break;
                case STATUS_LONG_SPEECH_FINISHED:
                    Log.d(TAG, "asr long speech finish");
                    break;
                case STATUS_STOPPED:
                    Log.d(TAG, "asr stop");
                    break;
                case STATUS_NONE:
                    Log.d(TAG, "asr initial status");
                    break;
                case STATUS_READY:
                    Log.d(TAG, "asr engine ready");
                    break;
                case STATUS_SPEAKING:
                    Log.d(TAG, "asr speaking");
                    break;
                case STATUS_RECOGNITION:
                    Log.d(TAG, "asr recognizing");
                    break;
                default:
                    break;
            }
        }
    }

    private void initBaiduSpeech() {
        initBaiduSpeechSettings();
        //baidu speech
        IRecogListener listener = new MessageStatusRecogListener(new BaiduSpeechHandler(this));
        mBaiduSpeech_Recognizer = new Recognizer(this, listener);
        mWorkState |= SPEECH_INIT_EXIT_MASK;
    }

    private void initBaiduSpeechSettings() {
        mBaiduSpeech_VADTimeout = mSettingResults.mSpeechEnable_VADTimeout;
        mBaiduSpeech_LanguageID = mSettingResults.mSpeechEnable_LanguageID;
    }

    private void releaseBaiduSpeech() {
        if (null != mBaiduSpeech_Recognizer) {
            mBaiduSpeech_Recognizer.release();
        }
        mWorkState &= ~SPEECH_INIT_EXIT_MASK;
    }

    private void speechResult(String result) {
        dismissAllDialog();
        if (mBaiduSpeech_VADTimeout > 0) {
            mWorkState &= ~SPEECH_START_STOP_MASK;
            updateRecognizeButton();
        }
        Log.d(TAG, result + "\n");
        if (mSettingResults.mSpeechEnable_Debug) {
            alertText(getResources().getString(R.string.recognize_result), result + "\n");
        }

        if (mSettingResults.mSpeechEnable_Control) {
            result = CommonTools.leftChineseCharacter(result);
            voiceControl(result);
        }
    }

    //开始录音，点击“开始”按钮后调用。
    private void startBaiduSpeechRecognize() {
        if (null == mBaiduSpeech_Recognizer) return;

        if (!isBaiduSpeechRecognizeRunning()) {
            JSONObject json = new JSONObject();
            try {
                json.put("accept-audio-data", false);
                json.put("disable-punctuation", false);
                json.put("accept-audio-volume", false);
                json.put("vad.endpoint-timeout", mBaiduSpeech_VADTimeout);
                json.put("pid", mBaiduSpeech_LanguageID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            String json = "{\"accept-audio-data\":false,\"disable-punctuation\":false,\"accept-audio-volume\":false,\"vad.endpoint-timeout\":0}";
            mWorkState |= SPEECH_START_STOP_MASK;
            updateRecognizeButton();
            String param = json.toString();
            mBaiduSpeech_Recognizer.start(param);
        }
    }

    private void stopBaiduSpeechRecognize() {
        mWorkState &= ~SPEECH_START_STOP_MASK;
        updateRecognizeButton();
        if (null == mBaiduSpeech_Recognizer) return;

        mBaiduSpeech_Recognizer.stop();
    }

    private boolean isBaiduSpeechRecognizeRunning() {
        if (null == mBaiduSpeech_Recognizer) return false;

        return (mWorkState & SPEECH_START_STOP_MASK) > 0;
    }

    private void voiceControl(String voiceCmd) {
        if ("开始".equals(voiceCmd) || "读书".equals(voiceCmd) || "比较".equals(voiceCmd)) {
            if (mSettingResults.mOCREnable) startBaiduOCR();
        } else if ("结束".equals(voiceCmd)) {
            stopBaiduSpeechRecognize();
            startBaiduSpeechRecognize();
        } else if ("关闭".equals(voiceCmd) || "退出".equals(voiceCmd)) {
            stopBaiduSpeechRecognize();
        }
    }

    //3. baidu TTS
    // 主控制类，所有合成控制方法从这个类开始
    private MySyntherizer mBaiduTTS_Synthesizer = null;
    private int mBaiduTTS_Speaker = 0; //0: 标准女声; 1:标准男声; 3: 情感男声; 4:情感女童声; 5:情感女声; 103；106；110: 情感男童声；111
    private int mBaiduTTS_Volume = 5; //在线及离线合成的音量 。范围["0" - "15"], 不支持小数。 "0" 最轻，"15" 最响。
    private int mBaiduTTS_Speed = 5; //在线及离线合成的语速 。范围["0" - "15"], 不支持小数。 "0" 最慢，"15" 最快
    private int mBaiduTTS_Pitch = 5; //在线及离线合成的语调 。范围["0" - "15"], 不支持小数。 "0" 最低沉， "15" 最尖

    static class BaiduTTSHandler extends Handler {
        WeakReference<ScannerActivity> mTheActivity;

        BaiduTTSHandler(ScannerActivity activity) {
            mTheActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case PRINT:
                    break;
                case UI_CHANGE_INPUT_TEXT_SELECTION:
                    Log.d(TAG, "tts playing progress = " + msg.arg1);
                    break;
                case UI_CHANGE_SYNTHES_TEXT_SELECTION:
                    Log.d(TAG, "tts synthesizing progress = " + msg.arg1);
                    break;
                case UI_CHANGE_TTS_START:
                    mTheActivity.get().mWorkState |= TTS_START_STOP_MASK;
                    mTheActivity.get().updateRecognizeButton();
                    break;
                case UI_CHANGE_TTS_END:
                    mTheActivity.get().mWorkState &= ~TTS_START_STOP_MASK;
                    mTheActivity.get().updateRecognizeButton();
                    break;
            }
        }
    }

    private void initBaiduTTS() {
        // 设置初始化参数
        initBaiduTTSSettings();

        Handler handler = new BaiduTTSHandler(this);
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(handler);

        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig("17181087", "13MaYeYFLEYqZt8K3yMe8CGN", "b3CGEBw4W0np2TWquO08GAh0Q8HXcIMV",
                                    TtsMode.MIX, params, listener);

        mBaiduTTS_Synthesizer = new NonBlockSyntherizer(this, initConfig, handler); // 此处可以改为MySyntherizer 了解调用过程

        mWorkState |= TTS_INIT_EXIT_MASK;
    }

    private void initBaiduTTSSettings() {
        mBaiduTTS_Speaker = mSettingResults.mVoiceEnable_Speaker;
        mBaiduTTS_Volume = mSettingResults.mVoiceEnable_Volume;
        mBaiduTTS_Speed = mSettingResults.mVoiceEnable_Speed;
        mBaiduTTS_Pitch = mSettingResults.mVoiceEnable_Pitch;
    }

    private void releaseBaiduTTS() {
        if (null != mBaiduTTS_Synthesizer) mBaiduTTS_Synthesizer.release();
        mWorkState &= ~TTS_INIT_EXIT_MASK;
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     */
    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(mBaiduTTS_Speaker));
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(mBaiduTTS_Volume));
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, String.valueOf(mBaiduTTS_Speed));
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, String.valueOf(mBaiduTTS_Pitch));

        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource();
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        return params;
    }

    private OfflineResource createOfflineResource() {
        OfflineResource offlineResource = null;
        try {
            String voiceType = OfflineResource.VOICE_FEMALE;
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
        }
        return offlineResource;
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void startTTSSpeak(String ttsText, long delayMS) {
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (!TextUtils.isEmpty(ttsText)) {
            // 合成前可以修改参数：
            Map<String, String> params = getParams();
            mBaiduTTS_Synthesizer.setParams(params);
            mBaiduTTS_Synthesizer.speak(ttsText);
            if (delayMS > 0) SystemClock.sleep(delayMS);
        }
    }

    private boolean stopTTSSpeak() {
        if ((mWorkState & TTS_START_STOP_MASK) > 0) {
            mBaiduTTS_Synthesizer.stop();
            mWorkState &= ~TTS_START_STOP_MASK;
            updateRecognizeButton();
            return true;
        } else {
            return false;
        }
    }
}