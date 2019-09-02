package com.min.tesserocrdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.min.tesserocrdemo.camera.CameraManager;
import com.min.tesserocrdemo.decode.CaptureActivityHandler;
import com.min.tesserocrdemo.tess.TesseractCallback;
import com.min.tesserocrdemo.tess.TesseractThread;
import com.min.tesserocrdemo.utils.ScreenUtils;
import com.min.tesserocrdemo.utils.Tools;
import com.min.tesserocrdemo.view.ImageDialog;
import com.min.tesserocrdemo.view.ScannerFinderView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class ScannerActivity extends Activity implements Callback, Camera.PictureCallback, Camera.ShutterCallback{
    private static final int RECOGNIZE_SUCCESS = 0;
    private static final int RECOGNIZE_FAIL = 1;
    private final int SETTING_REQUEST_CODE = 0;
    private final int REQUEST_MULTIPLE_PERMISSION = 100;
    private CaptureActivityHandler mCaptureActivityHandler;
    private boolean mHasSurface;
    private ScannerFinderView mFinderView;
    private SurfaceView mSurfaceView;
    private ViewStub mSurfaceViewStub;
    private Button mRecognizeButton;

    private ProgressDialog progressDialog;
    private Bitmap mBmp;
    private Handler mHandler;
    private int mOrientation = 0;
    private boolean mChessEnable = false;
    private boolean mVoiceEnable = false;
    private boolean mDebugEnable = true;
    private int mOcrMode = -1;
    private boolean mAllApkPermission = false;

    //baidu OCR
    private boolean mHasGotToken = false;
    private AlertDialog.Builder mAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        applyApkPermissions();

        //获取当前Activity的屏幕方向
        int orientation = getRequestedOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ) {
            mOrientation = 90;
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mOrientation = 0;
        }

        mChessEnable = SettingActivity.isChessEnable(this);
        mVoiceEnable = SettingActivity.isVoiceEnable(this);
        mDebugEnable = SettingActivity.isDebugEnable(this);
        mOcrMode = SettingActivity.getOCRMode(this);

        mAlertDialog = new AlertDialog.Builder(this);
        if (SettingActivity.OCR_MODE_BAIDU == mOcrMode) {
            initAccessTokenWithAkSk();
        }

        mHandler = new MyHandler(this);
        initView();
    }

    private void applyApkPermissions() {
        final String[] requiredPermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
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
//                    Toast.makeText(this, "Must allow all permissions", Toast.LENGTH_LONG).show();
//                    finish();
                    return;
                }
            }
            mAllApkPermission = true;
            initView();
            CameraManager.init();
            initCamera();
        }
    }

    private void initView() {
        mFinderView = findViewById(R.id.view_finder);
        mFinderView.setOrientation(mOrientation);
        mSurfaceViewStub = findViewById(R.id.view_stub);
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
                mRecognizeButton.setEnabled(false);
                if (mChessEnable) {
                    playRing("compare_start.m4a");
                    SystemClock.sleep(1000);
                }
                buildProgressDialog();
                CameraManager.get().takeShot(ScannerActivity.this,
                        ScannerActivity.this, ScannerActivity.this);
            }
        });
    }

    private void resetRecognizeBTSize(Button bt) {
        Point scrRes = ScreenUtils.getScreenResolution(this);
        float testSize = scrRes.x / 6;
        bt.setTextSize(testSize);

        ViewGroup.LayoutParams params = bt.getLayoutParams();
        params.width = scrRes.x;
        params.height = scrRes.y * 2 / 5;
        bt.setLayoutParams(params);
    }

    public Rect getCropRect() {
        return mFinderView.getRect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAllApkPermission) {
            CameraManager.init();
            initCamera();
        }
    }

    private void initCamera() {
        if (null == mSurfaceView) {
            mSurfaceViewStub.setLayoutResource(R.layout.layout_surface_view);
            mSurfaceView = (SurfaceView) mSurfaceViewStub.inflate();
        }
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
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
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(SETTING_REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            mChessEnable = SettingActivity.isChessEnable(this);
            mVoiceEnable = SettingActivity.isVoiceEnable(this);
            mDebugEnable = SettingActivity.isDebugEnable(this);
            mOcrMode = SettingActivity.getOCRMode(this);
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
            mCaptureActivityHandler = new CaptureActivityHandler(this);
        }
    }

    public void restartPreview() {
        if (null != mCaptureActivityHandler) {
            try {
                mCaptureActivityHandler.restartPreviewAndDecode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (data == null) {
            return;
        }

        mCaptureActivityHandler.onPause();
        mBmp = null;
        mBmp = Tools.getFocusedBitmap(this, camera, data, getCropRect(), mOrientation);

        if (SettingActivity.OCR_MODE_TESSER == mOcrMode){
            tesserOCRText(mBmp);
        } else if (SettingActivity.OCR_MODE_BAIDU == mOcrMode) {
            baiduOCRText(mBmp);
        }
    }

    @Override
    public void onShutter() {}

    private void phoneSucceed(String result, Bitmap bitmap){
        String filterStr = "";
        if (!TextUtils.isEmpty(result)) {
            filterStr = filterChineseCharacter(result);
            if (mChessEnable) {
                filterStr = CompareRank.filterChessCharacter(filterStr);
                compareRank(filterStr);
            }
        }

        if (mDebugEnable) {
            ImageDialog dialog = new ImageDialog(this);
            dialog.addBitmap(bitmap);
            if (TextUtils.isEmpty(result)) {
                dialog.addTitle(getResources().getString(R.string.recognize_fail), "");
            } else {
                dialog.addTitle(result, filterStr);
            }
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    restartPreview();
                }
            });

            dialog.show();
        } else {
            SystemClock.sleep(3000);
            restartPreview();
        }
    }

    private void compareRank(String content) {
        String[] ranks = splitStr(content, 2);

        String rankRed = null, rankBlack = null;
        for (String rank : ranks) {
            if (CompareRank.isValid(rank)) {
                if (TextUtils.isEmpty(rankRed)) {
                    rankRed = rank;
                } else {
                    rankBlack = rank;
                    break;
                }
            }
        }

        if (!TextUtils.isEmpty(rankRed) && !TextUtils.isEmpty(rankBlack)) {
            int result = CompareRank.compare(rankRed, rankBlack);
            switch (result) {
                case CompareRank.WIN:
                    playRing("left_win.m4a");
                    break;
                case CompareRank.DRAW:
                    playRing("both_dead.m4a");
                    break;
                case CompareRank.LOSE:
                    playRing("right_win.m4a");
                    break;
            }

            if (CompareRank.isGameOver(rankRed, rankBlack)) {
                SystemClock.sleep(2000);
                playRing("game_over.m4a");
            }
        } else {
            playRing("compare_fail.m4a");
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ScannerActivity> mTheActivity;

        MyHandler(ScannerActivity activity) {
            mTheActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ScannerActivity theActivity = mTheActivity.get();
            theActivity.mRecognizeButton.setEnabled(true);
            theActivity.cancelProgressDialog();
            switch (msg.what){
                case ScannerActivity.RECOGNIZE_SUCCESS:
                    theActivity.phoneSucceed((String) msg.obj, theActivity.mBmp);
                    break;
                case ScannerActivity.RECOGNIZE_FAIL:
                    Toast.makeText(theActivity, theActivity.getResources().getString(R.string.recognize_fail),
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    public void buildProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(getResources().getString(R.string.recognizing));
        progressDialog.setCancelable(true);
        progressDialog.show();

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartPreview();
            }
        });
    }

    public void cancelProgressDialog() {
        if (progressDialog != null){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private synchronized void playRing(String name) {
        if (!mVoiceEnable) return;

        try {
            MediaPlayer player = new MediaPlayer();
            AssetFileDescriptor fileDescriptor = getAssets().openFd(name);
            player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getStartOffset());
            player.prepare();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String filterChineseCharacter(String str) {
        StringBuilder chineseCharacter = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            String bb = str.substring(i, i + 1);
            // 生成一个Pattern,同时编译一个正则表达式,其中的u4E00("一"的unicode编码)-\u9FA5("龥"的unicode编码)
            if (java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", bb)) {
                chineseCharacter.append(bb);
            }
        }
        return chineseCharacter.toString();
    }

    private String[] splitStr(String str, int splitLen) {
        int count = str.length() / splitLen + (str.length() % splitLen == 0 ? 0 : 1);
        String[] strs = new String[count];
        for (int i = 0; i < count; i++) {
            if (str.length() <= splitLen) {
                strs[i] = str;
            } else {
                strs[i] = str.substring(0, splitLen);
                str = str.substring(splitLen);
            }
        }
        return strs;
    }

    private void tesserOCRText(Bitmap bmp) {
        TesseractThread mTesseractThread = new TesseractThread(this, bmp, new TesseractCallback() {
            @Override
            public void succeed(String result) {
                Message message = Message.obtain();
                message.what = ScannerActivity.RECOGNIZE_SUCCESS;
                message.obj = result;
                mHandler.sendMessage(message);
            }

            @Override
            public void fail() {
                Message message = Message.obtain();
                message.what = ScannerActivity.RECOGNIZE_FAIL;
                mHandler.sendMessage(message);
            }
        });
        new Thread(mTesseractThread).start();
    }

    //baidu OCR
    /**
     * 用明文ak，sk初始化
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
//                String token = result.getAccessToken();
                mHasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(),  "GSwaMOU1ZgA2UcLFv3A7Pyrm", "WGYP4m3YcnqvLFA9SaOK94CZc7vPApBz");
    }

    private boolean checkTokenStatus() {
        if (!mHasGotToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return mHasGotToken;
    }

    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAlertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }

    private void infoPopText(final String result) {
//        alertText("", result);
        Message message = Message.obtain();
        message.what = ScannerActivity.RECOGNIZE_SUCCESS;
        message.obj = result;
        mHandler.sendMessage(message);
    }

    private void baiduOCRText(Bitmap bmp) {
        if (!checkTokenStatus()) return;

        try {
            File outputFile = new File(getFilesDir(), "pic.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
//            bmp.recycle();
            fileOutputStream.close();

            RecognizeService.recGeneralBasic(this, outputFile.getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText(result);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}