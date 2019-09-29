package com.min.baiduai.demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;

import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.min.baiduai.demo.view.MultiLineRadioGroup;

public class SettingActivity extends Activity {

    static class SettingResults implements Cloneable {
        final static String PREF_SETTINGS = "pref_settings";
        final static String PREF_FULL_SCREEN = "pref_full_screen";
        final static String PREF_MAX_SCAN_RECT = "pref_max_scan_rect";

        final static String PREF_OCR = "pref_ocr";
        final static String PREF_OCR_DEBUG = "pref_ocr_debug";
        final static String PREF_OCR_ENHANCE = "pref_ocr_enhance";
        final static String PREF_OCR_NOTIFICATION = "pref_ocr_notification";
        final static String PREF_OCR_PROGRESSBAR = "pref_ocr_progressbar";
        final static String PREF_OCR_FLASH = "pref_ocr_flash";
        final static String PREF_OCR_DETECT_DIRECTION = "pref_ocr_detect_direction";
        final static String PREF_OCR_LANGUAGE_TYPE = "pref_ocr_language_TYPE";
        final static String PREF_OCR_ROTATION = "pref_ocr_rotation";

        final static String PREF_SPEECH = "pref_speech";
        final static String PREF_SPEECH_DEBUG = "pref_speech_debug";
        final static String PREF_SPEECH_CONTROL = "pref_speech_control";
        final static String PREF_SPEECH_VAD_TIMEOUT = "pref_speech_vad_timeout";
        final static String PREF_SPEECH_LANGUAGE_ID = "pref_speech_language_id";

        final static String PREF_VOICE = "pref_voice";
        final static String PREF_VOICE_DEBUG = "pref_voice_debug";
        final static String PREF_VOICE_SPEAKER = "pref_voice_speaker";
        final static String PREF_VOICE_VOLUME = "pref_voice_volume";
        final static String PREF_VOICE_SPEED = "pref_voice_speed";
        final static String PREF_VOICE_PITCH = "pref_voice_pitch";

        final static String PREF_CHESS = "pref_chess";

        boolean mFullScreenEnable;
        boolean mMaxScanRect;

        boolean mOCREnable;
        boolean mOCREnable_Debug;
        boolean mOCREnable_Enhance;
        boolean mOCREnable_Notification;
        boolean mOCREnable_ProgressBar;
        boolean mOCREnable_Flash;
        boolean mOCREnable_DetectDirection;
        String mOCREnable_LanguageType;
        int mOCREnable_Rotation;

        boolean mSpeechEnable;
        boolean mSpeechEnable_Debug;
        boolean mSpeechEnable_Control;
        int mSpeechEnable_VADTimeout;
        int mSpeechEnable_LanguageID;

        boolean mVoiceEnable;
        boolean mVoiceEnable_Debug;
        int mVoiceEnable_Speaker;
        int mVoiceEnable_Volume;
        int mVoiceEnable_Speed;
        int mVoiceEnable_Pitch;

        boolean mChessEnable;

        SettingResults() {
            mFullScreenEnable = true;
            mMaxScanRect = true;

            mOCREnable = true;
            mOCREnable_Debug = false;
            mOCREnable_Enhance = false;
            mOCREnable_Notification = true;
            mOCREnable_ProgressBar = false;
            mOCREnable_Flash = false;
            mOCREnable_DetectDirection = true;
            mOCREnable_LanguageType = GeneralBasicParams.CHINESE_ENGLISH;
            mOCREnable_Rotation = 0;

            mSpeechEnable = false;
            mSpeechEnable_Debug = false;
            mSpeechEnable_Control = true;
            mSpeechEnable_VADTimeout = 0;
            mSpeechEnable_LanguageID = 1536;

            mVoiceEnable = true;
            mVoiceEnable_Debug = false;
            mVoiceEnable_Speaker = 5;
            mVoiceEnable_Volume = 10;
            mVoiceEnable_Speed = 4;
            mVoiceEnable_Pitch = 5;

            mChessEnable = false;
        }

        @Override
        public Object clone() {
            SettingResults settingResults = null;
            try{
                settingResults = (SettingResults)super.clone();
            }catch(CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return settingResults;
        }
    }

    public static SettingResults getSettingResults(Context ctx) {
        SettingResults settingResults = new SettingResults();

        SharedPreferences settingPrefs = ctx.getSharedPreferences(SettingResults.PREF_SETTINGS, Context.MODE_PRIVATE);

        settingResults.mFullScreenEnable = settingPrefs.getBoolean(SettingResults.PREF_FULL_SCREEN, settingResults.mFullScreenEnable);
        settingResults.mMaxScanRect = settingPrefs.getBoolean(SettingResults.PREF_MAX_SCAN_RECT, settingResults.mMaxScanRect);

        settingResults.mOCREnable = settingPrefs.getBoolean(SettingResults.PREF_OCR, settingResults.mOCREnable);
        settingResults.mOCREnable_Debug = settingPrefs.getBoolean(SettingResults.PREF_OCR_DEBUG, settingResults.mOCREnable_Debug);
        settingResults.mOCREnable_Enhance = settingPrefs.getBoolean(SettingResults.PREF_OCR_ENHANCE, settingResults.mOCREnable_Enhance);
        settingResults.mOCREnable_Notification = settingPrefs.getBoolean(SettingResults.PREF_OCR_NOTIFICATION, settingResults.mOCREnable_Notification);
        settingResults.mOCREnable_ProgressBar = settingPrefs.getBoolean(SettingResults.PREF_OCR_PROGRESSBAR, settingResults.mOCREnable_ProgressBar);
        settingResults.mOCREnable_Flash = settingPrefs.getBoolean(SettingResults.PREF_OCR_FLASH, settingResults.mOCREnable_Flash);
        settingResults.mOCREnable_DetectDirection = settingPrefs.getBoolean(SettingResults.PREF_OCR_DETECT_DIRECTION, settingResults.mOCREnable_DetectDirection);
        settingResults.mOCREnable_LanguageType = settingPrefs.getString(SettingResults.PREF_OCR_LANGUAGE_TYPE, settingResults.mOCREnable_LanguageType);
        settingResults.mOCREnable_Rotation = settingPrefs.getInt(SettingResults.PREF_OCR_ROTATION, settingResults.mOCREnable_Rotation);

        settingResults.mSpeechEnable = settingPrefs.getBoolean(SettingResults.PREF_SPEECH, settingResults.mSpeechEnable);
        settingResults.mSpeechEnable_Debug = settingPrefs.getBoolean(SettingResults.PREF_SPEECH_DEBUG, settingResults.mSpeechEnable_Debug);
        settingResults.mSpeechEnable_Control = settingPrefs.getBoolean(SettingResults.PREF_SPEECH_CONTROL, settingResults.mSpeechEnable_Control);
        settingResults.mSpeechEnable_VADTimeout = settingPrefs.getInt(SettingResults.PREF_SPEECH_VAD_TIMEOUT, settingResults.mSpeechEnable_VADTimeout);
        settingResults.mSpeechEnable_LanguageID = settingPrefs.getInt(SettingResults.PREF_SPEECH_LANGUAGE_ID, settingResults.mSpeechEnable_LanguageID);

        settingResults.mVoiceEnable = settingPrefs.getBoolean(SettingResults.PREF_VOICE, settingResults.mVoiceEnable);
        settingResults.mVoiceEnable_Debug = settingPrefs.getBoolean(SettingResults.PREF_VOICE_DEBUG, settingResults.mVoiceEnable_Debug);
        settingResults.mVoiceEnable_Speaker = settingPrefs.getInt(SettingResults.PREF_VOICE_SPEAKER, settingResults.mVoiceEnable_Speaker);
        settingResults.mVoiceEnable_Volume = settingPrefs.getInt(SettingResults.PREF_VOICE_VOLUME, settingResults.mVoiceEnable_Volume);
        settingResults.mVoiceEnable_Speed = settingPrefs.getInt(SettingResults.PREF_VOICE_SPEED, settingResults.mVoiceEnable_Speed);
        settingResults.mVoiceEnable_Pitch = settingPrefs.getInt(SettingResults.PREF_VOICE_PITCH, settingResults.mVoiceEnable_Pitch);

        settingResults.mChessEnable = settingPrefs.getBoolean(SettingResults.PREF_CHESS, settingResults.mChessEnable);

        return settingResults;
    }

    public static void setSettingResults(Context ctx, SettingResults settingResults) {
        SharedPreferences settingPrefs = ctx.getSharedPreferences(SettingResults.PREF_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = settingPrefs.edit();

        settingEditor.putBoolean(SettingResults.PREF_FULL_SCREEN, settingResults.mFullScreenEnable);
        settingEditor.putBoolean(SettingResults.PREF_MAX_SCAN_RECT, settingResults.mMaxScanRect);

        settingEditor.putBoolean(SettingResults.PREF_OCR, settingResults.mOCREnable);
        settingEditor.putBoolean(SettingResults.PREF_OCR_DEBUG, settingResults.mOCREnable_Debug);
        settingEditor.putBoolean(SettingResults.PREF_OCR_ENHANCE, settingResults.mOCREnable_Enhance);
        settingEditor.putBoolean(SettingResults.PREF_OCR_NOTIFICATION, settingResults.mOCREnable_Notification);
        settingEditor.putBoolean(SettingResults.PREF_OCR_PROGRESSBAR, settingResults.mOCREnable_ProgressBar);
        settingEditor.putBoolean(SettingResults.PREF_OCR_FLASH, settingResults.mOCREnable_Flash);
        settingEditor.putBoolean(SettingResults.PREF_OCR_DETECT_DIRECTION, settingResults.mOCREnable_DetectDirection);
        settingEditor.putString(SettingResults.PREF_OCR_LANGUAGE_TYPE, settingResults.mOCREnable_LanguageType);
        settingEditor.putInt(SettingResults.PREF_OCR_ROTATION, settingResults.mOCREnable_Rotation);

        settingEditor.putBoolean(SettingResults.PREF_SPEECH, settingResults.mSpeechEnable);
        settingEditor.putBoolean(SettingResults.PREF_SPEECH_DEBUG, settingResults.mSpeechEnable_Debug);
        settingEditor.putBoolean(SettingResults.PREF_SPEECH_CONTROL, settingResults.mSpeechEnable_Control);
        settingEditor.putInt(SettingResults.PREF_SPEECH_VAD_TIMEOUT, settingResults.mSpeechEnable_VADTimeout);
        settingEditor.putInt(SettingResults.PREF_SPEECH_LANGUAGE_ID, settingResults.mSpeechEnable_LanguageID);

        settingEditor.putBoolean(SettingResults.PREF_VOICE, settingResults.mVoiceEnable);
        settingEditor.putBoolean(SettingResults.PREF_VOICE_DEBUG, settingResults.mVoiceEnable_Debug);
        settingEditor.putInt(SettingResults.PREF_VOICE_SPEAKER, settingResults.mVoiceEnable_Speaker);
        settingEditor.putInt(SettingResults.PREF_VOICE_VOLUME, settingResults.mVoiceEnable_Volume);
        settingEditor.putInt(SettingResults.PREF_VOICE_SPEED, settingResults.mVoiceEnable_Speed);
        settingEditor.putInt(SettingResults.PREF_VOICE_PITCH, settingResults.mVoiceEnable_Pitch);

        settingEditor.putBoolean(SettingResults.PREF_CHESS, settingResults.mChessEnable);

        settingEditor.apply();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        SettingResults settingResults = getSettingResults(this);

        ((Switch)findViewById(R.id.fullscreen_switch)).setChecked(settingResults.mFullScreenEnable);
        ((Switch)findViewById(R.id.maxscanrect_switch)).setChecked(settingResults.mMaxScanRect);

        setOcrEnable(settingResults.mOCREnable);
        ((Switch)findViewById(R.id.ocr_switch)).setChecked(settingResults.mOCREnable);
        ((CheckBox)findViewById(R.id.ocr_checkbox_debug)).setChecked(settingResults.mOCREnable_Debug);
        ((CheckBox)findViewById(R.id.ocr_checkbox_enhance)).setChecked(settingResults.mOCREnable_Enhance);
        ((CheckBox)findViewById(R.id.ocr_checkbox_notification)).setChecked(settingResults.mOCREnable_Notification);
        ((CheckBox)findViewById(R.id.ocr_checkbox_progressbar)).setChecked(settingResults.mOCREnable_ProgressBar);
        ((CheckBox)findViewById(R.id.ocr_checkbox_flash)).setChecked(settingResults.mOCREnable_Flash);
        ((CheckBox)findViewById(R.id.ocr_checkbox_detect_direction)).setChecked(settingResults.mOCREnable_DetectDirection);
        setBaiduOCR_LanguageType(settingResults.mOCREnable_LanguageType);
        setBaiduOCR_Rotation(settingResults.mOCREnable_Rotation);

        setSpeechEnable(settingResults.mSpeechEnable);
        ((Switch)findViewById(R.id.speech_switch)).setChecked(settingResults.mSpeechEnable);
        ((CheckBox)findViewById(R.id.speech_checkbox_debug)).setChecked(settingResults.mSpeechEnable_Debug);
        ((CheckBox)findViewById(R.id.speech_checkbox_control)).setChecked(settingResults.mSpeechEnable_Control);
        setBaiduSpeech_VadTimeout(settingResults.mSpeechEnable_VADTimeout);
        setBaiduSpeech_LanguageID(settingResults.mSpeechEnable_LanguageID);

        setTTSEnable(settingResults.mVoiceEnable);
        ((Switch)findViewById(R.id.tts_switch)).setChecked(settingResults.mVoiceEnable);
        ((CheckBox)findViewById(R.id.tts_checkbox_debug)).setChecked(settingResults.mVoiceEnable_Debug);
        setBaiduTTS_Speaker(settingResults.mVoiceEnable_Speaker);
        setBaiduTTS_Volume(settingResults.mVoiceEnable_Volume);
        setBaiduTTS_Speed(settingResults.mVoiceEnable_Speed);
        setBaiduTTS_Pitch(settingResults.mVoiceEnable_Pitch);

        ((Switch)findViewById(R.id.chess_switch)).setChecked(settingResults.mChessEnable);

        switchGroup();

        findViewById(R.id.setting_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingResults settingResults = new SettingResults();
                settingResults.mFullScreenEnable= ((Switch)findViewById(R.id.fullscreen_switch)).isChecked();
                settingResults.mMaxScanRect = ((Switch)findViewById(R.id.maxscanrect_switch)).isChecked();

                settingResults.mOCREnable = ((Switch)findViewById(R.id.ocr_switch)).isChecked();
                settingResults.mOCREnable_Debug = ((CheckBox)findViewById(R.id.ocr_checkbox_debug)).isChecked();
                settingResults.mOCREnable_Enhance = ((CheckBox)findViewById(R.id.ocr_checkbox_enhance)).isChecked();
                settingResults.mOCREnable_Notification = ((CheckBox)findViewById(R.id.ocr_checkbox_notification)).isChecked();
                settingResults.mOCREnable_ProgressBar = ((CheckBox)findViewById(R.id.ocr_checkbox_progressbar)).isChecked();
                settingResults.mOCREnable_Flash = ((CheckBox)findViewById(R.id.ocr_checkbox_flash)).isChecked();
                settingResults.mOCREnable_DetectDirection = ((CheckBox)findViewById(R.id.ocr_checkbox_detect_direction)).isChecked();
                settingResults.mOCREnable_LanguageType = getBaiduOCR_LanguageType();
                settingResults.mOCREnable_Rotation = getBaiduOCR_Rotation();

                settingResults.mSpeechEnable = ((Switch)findViewById(R.id.speech_switch)).isChecked();
                settingResults.mSpeechEnable_Debug = ((CheckBox)findViewById(R.id.speech_checkbox_debug)).isChecked();
                settingResults.mSpeechEnable_Control = ((CheckBox)findViewById(R.id.speech_checkbox_control)).isChecked();
                settingResults.mSpeechEnable_VADTimeout = getBaiduSpeech_VadTimeout();
                settingResults.mSpeechEnable_LanguageID = getBaiduSpeech_LanguageID();

                settingResults.mVoiceEnable = ((Switch)findViewById(R.id.tts_switch)).isChecked();
                settingResults.mVoiceEnable_Debug = ((CheckBox)findViewById(R.id.tts_checkbox_debug)).isChecked();
                settingResults.mVoiceEnable_Speaker = getBaiduTTS_Speaker();
                settingResults.mVoiceEnable_Volume = getBaiduTTS_Volume();
                settingResults.mVoiceEnable_Speed = getBaiduTTS_Speed();
                settingResults.mVoiceEnable_Pitch = getBaiduTTS_Pitch();

                settingResults.mChessEnable = ((Switch)findViewById(R.id.chess_switch)).isChecked();

                setSettingResults(SettingActivity.this, settingResults);

                setResult(Activity.RESULT_OK, null);
                finish();
            }
        });

        findViewById(R.id.setting_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED, null);
                finish();
            }
        });
    }

    private void setOcrEnable(boolean enable) {
        findViewById(R.id.ocr_checkbox_debug).setEnabled(enable);
        findViewById(R.id.ocr_checkbox_notification).setEnabled(enable);
        findViewById(R.id.ocr_checkbox_progressbar).setEnabled(enable);
        findViewById(R.id.ocr_checkbox_flash).setEnabled(enable);
        findViewById(R.id.ocr_checkbox_detect_direction).setEnabled(enable);
        findViewById(R.id.ocr_detect_language_title).setEnabled(enable);
        findViewById(R.id.ocr_detect_language_CHN_ENG).setEnabled(enable);
        findViewById(R.id.ocr_detect_language_CHN).setEnabled(enable);
        findViewById(R.id.ocr_detect_language_ENG).setEnabled(enable);
        findViewById(R.id.ocr_rotation_title).setEnabled(enable);
        findViewById(R.id.ocr_rotation_0).setEnabled(enable);
        findViewById(R.id.ocr_rotation_90).setEnabled(enable);
        findViewById(R.id.ocr_rotation_180).setEnabled(enable);
        findViewById(R.id.ocr_rotation_270).setEnabled(enable);
    }

    private void setSpeechEnable(boolean enable) {
        findViewById(R.id.speech_checkbox_debug).setEnabled(enable);
        findViewById(R.id.speech_vad_timeout_title).setEnabled(enable);
        findViewById(R.id.speech_vad_timeout_0).setEnabled(enable);
        findViewById(R.id.speech_vad_timeout_800).setEnabled(enable);
        findViewById(R.id.speech_vad_timeout_2000).setEnabled(enable);
        findViewById(R.id.speech_language_type_title).setEnabled(enable);
        findViewById(R.id.speech_language_type_mandarin).setEnabled(enable);
        findViewById(R.id.speech_language_type_mandarin_punctuation).setEnabled(enable);
        findViewById(R.id.speech_language_type_mandarin_far).setEnabled(enable);
        findViewById(R.id.speech_language_type_sichuanese).setEnabled(enable);
        findViewById(R.id.speech_language_type_english).setEnabled(enable);
    }

    private void setTTSEnable(boolean enable) {
        findViewById(R.id.tts_checkbox_debug).setEnabled(enable);
        findViewById(R.id.tts_speaker_type_title).setEnabled(enable);
        findViewById(R.id.tts_speaker_type_female).setEnabled(enable);
        findViewById(R.id.tts_speaker_type_male).setEnabled(enable);
        findViewById(R.id.tts_speaker_type_female_feeling).setEnabled(enable);
        findViewById(R.id.tts_speaker_type_male_feeling).setEnabled(enable);
        findViewById(R.id.tts_speaker_type_girl_feeling).setEnabled(enable);
        findViewById(R.id.tts_speaker_type_boy_feeling).setEnabled(enable);
        findViewById(R.id.tts_speaker_volume_seekbar_title).setEnabled(enable);
        findViewById(R.id.tts_speaker_volume_seekbar).setEnabled(enable);
        findViewById(R.id.tts_speaker_speed_seekbar_title).setEnabled(enable);
        findViewById(R.id.tts_speaker_speed_seekbar).setEnabled(enable);
        findViewById(R.id.tts_speaker_pitch_seekbar_title).setEnabled(enable);
        findViewById(R.id.tts_speaker_pitch_seekbar).setEnabled(enable);
    }

    private void switchGroup() {
        ((Switch)findViewById(R.id.ocr_switch)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setOcrEnable(isChecked);
                    }
            });
        ((CheckBox)findViewById(R.id.ocr_checkbox_debug)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            findViewById(R.id.ocr_checkbox_enhance).setEnabled(true);
                        } else {
                            findViewById(R.id.ocr_checkbox_enhance).setEnabled(false);
                            ((CheckBox) findViewById(R.id.ocr_checkbox_enhance)).setChecked(false);
                        }
                    }
                });

        ((Switch)findViewById(R.id.speech_switch)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setSpeechEnable(isChecked);
                    }
                });
        ((CheckBox)findViewById(R.id.speech_checkbox_debug)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            findViewById(R.id.speech_checkbox_control).setEnabled(true);
                        } else {
                            findViewById(R.id.speech_checkbox_control).setEnabled(false);
                            ((CheckBox) findViewById(R.id.speech_checkbox_control)).setChecked(true);
                        }
                    }
                });

        ((Switch)findViewById(R.id.tts_switch)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setTTSEnable(isChecked);
                    }
                });
    }

    private String getBaiduOCR_LanguageType() {
        RadioGroup radioGroup = findViewById(R.id.ocr_detect_language);
        int selID = radioGroup.getCheckedRadioButtonId();
        String languageType = "";
        switch (selID) {
            case R.id.ocr_detect_language_CHN_ENG:
                languageType = GeneralBasicParams.CHINESE_ENGLISH;
                break;
            case R.id.ocr_detect_language_CHN:
                languageType = "CHN";
                break;
            case R.id.ocr_detect_language_ENG:
                languageType = GeneralBasicParams.ENGLISH;
                break;
        }
        return languageType;
    }

    private void setBaiduOCR_LanguageType(String languageType) {
        RadioGroup radioGroup = findViewById(R.id.ocr_detect_language);
        if (GeneralBasicParams.CHINESE_ENGLISH.equals(languageType)) {
            radioGroup.check(R.id.ocr_detect_language_CHN_ENG);
        } else if (GeneralBasicParams.ENGLISH.equals(languageType)) {
            radioGroup.check(R.id.ocr_detect_language_ENG);
        } else if ("CHN".equals(languageType)) {
            radioGroup.check(R.id.ocr_detect_language_CHN);
        }
    }

    private int getBaiduOCR_Rotation() {
        RadioGroup radioGroup = findViewById(R.id.ocr_rotation);
        int selID = radioGroup.getCheckedRadioButtonId();
        int rotation = 0;
        switch (selID) {
            case R.id.ocr_rotation_0:
                rotation = 0;
                break;
            case R.id.ocr_rotation_90:
                rotation = 90;
                break;
            case R.id.ocr_rotation_180:
                rotation = 180;
                break;
            case R.id.ocr_rotation_270:
                rotation = 270;
                break;
        }
        return rotation;
    }

    private void setBaiduOCR_Rotation(int rotation) {
        RadioGroup radioGroup = findViewById(R.id.ocr_rotation);
        switch (rotation){
            case 0:
                radioGroup.check(R.id.ocr_rotation_0);
                break;
            case 90:
                radioGroup.check(R.id.ocr_rotation_90);
                break;
            case 180:
                radioGroup.check(R.id.ocr_rotation_180);
                break;
            case 270:
                radioGroup.check(R.id.ocr_rotation_270);
                break;
        }
    }

    private int getBaiduSpeech_VadTimeout() {
        RadioGroup radioGroup = findViewById(R.id.speech_vad_timeout);
        int selID = radioGroup.getCheckedRadioButtonId();
        int vadTimeout = 0;
        switch (selID) {
            case R.id.speech_vad_timeout_0:
                vadTimeout = 0;
                break;
            case R.id.speech_vad_timeout_800:
                vadTimeout = 800;
                break;
            case R.id.speech_vad_timeout_2000:
                vadTimeout = 2000;
                break;
        }
        return vadTimeout;
    }

    private void setBaiduSpeech_VadTimeout(int vadTimeout) {
        RadioGroup radioGroup = findViewById(R.id.speech_vad_timeout);
        switch (vadTimeout){
            case 0:
                radioGroup.check(R.id.speech_vad_timeout_0);
                break;
            case 800:
                radioGroup.check(R.id.speech_vad_timeout_800);
                break;
            case 2000:
                radioGroup.check(R.id.speech_vad_timeout_2000);
                break;
        }
    }

    private int getBaiduSpeech_LanguageID() {
        MultiLineRadioGroup radioGroup = findViewById(R.id.speech_language_type);
        int selID = radioGroup.getCheckedRadioButtonId();
        int languageID = 0;
        switch (selID) {
            case R.id.speech_language_type_mandarin:
                languageID = 1536;
                break;
            case R.id.speech_language_type_mandarin_punctuation:
                languageID = 1537;
                break;
            case R.id.speech_language_type_mandarin_far:
                languageID = 1936;
                break;
            case R.id.speech_language_type_sichuanese:
                languageID = 1837;
                break;
            case R.id.speech_language_type_english:
                languageID = 1737;
                break;
        }
        return languageID;
    }

    private void setBaiduSpeech_LanguageID(int languageID) {
        MultiLineRadioGroup radioGroup = findViewById(R.id.speech_language_type);
        switch (languageID){
            case 1536:
                radioGroup.check(R.id.speech_language_type_mandarin);
                break;
            case 1537:
                radioGroup.check(R.id.speech_language_type_mandarin_punctuation);
                break;
            case 1936:
                radioGroup.check(R.id.speech_language_type_mandarin_far);
                break;
            case 1837:
                radioGroup.check(R.id.speech_language_type_sichuanese);
                break;
            case 1737:
                radioGroup.check(R.id.speech_language_type_english);
                break;
        }
    }

    private int getBaiduTTS_Speaker() {
        MultiLineRadioGroup radioGroup = findViewById(R.id.tts_speaker_type);
        int selID = radioGroup.getCheckedRadioButtonId();
        int speakerType = -1;
        switch (selID) {
            case R.id.tts_speaker_type_female:
                speakerType = 0;
                break;
            case R.id.tts_speaker_type_male:
                speakerType = 1;
                break;
            case R.id.tts_speaker_type_female_feeling:
                speakerType = 5;
                break;
            case R.id.tts_speaker_type_male_feeling:
                speakerType = 3;
                break;
            case R.id.tts_speaker_type_girl_feeling:
                speakerType = 4;
                break;
            case R.id.tts_speaker_type_boy_feeling:
                speakerType = 111;
                break;
        }
        return speakerType;
    }

    private void setBaiduTTS_Speaker(int speakerType) {
        MultiLineRadioGroup radioGroup = findViewById(R.id.tts_speaker_type);
        switch (speakerType){
            case 0:
                radioGroup.check(R.id.tts_speaker_type_female);
                break;
            case 1:
                radioGroup.check(R.id.tts_speaker_type_male);
                break;
            case 5:
                radioGroup.check(R.id.tts_speaker_type_female_feeling);
                break;
            case 3:
                radioGroup.check(R.id.tts_speaker_type_male_feeling);
                break;
            case 4:
                radioGroup.check(R.id.tts_speaker_type_girl_feeling);
                break;
            case 111:
                radioGroup.check(R.id.tts_speaker_type_boy_feeling);
                break;
        }
    }

    private int getBaiduTTS_Volume() {
        SeekBar seekBar = findViewById(R.id.tts_speaker_volume_seekbar);
        return seekBar.getProgress();
    }

    private void setBaiduTTS_Volume(int volume) {
        SeekBar seekBar = findViewById(R.id.tts_speaker_volume_seekbar);
        seekBar.setProgress(volume);
    }

    private int getBaiduTTS_Speed() {
        SeekBar seekBar = findViewById(R.id.tts_speaker_speed_seekbar);
        return seekBar.getProgress();
    }

    private void setBaiduTTS_Speed(int speed) {
        SeekBar seekBar = findViewById(R.id.tts_speaker_speed_seekbar);
        seekBar.setProgress(speed);
    }

    private int getBaiduTTS_Pitch() {
        SeekBar seekBar = findViewById(R.id.tts_speaker_pitch_seekbar);
        return seekBar.getProgress();
    }

    private void setBaiduTTS_Pitch(int pitch) {
        SeekBar seekBar = findViewById(R.id.tts_speaker_pitch_seekbar);
        seekBar.setProgress(pitch);
    }
}
