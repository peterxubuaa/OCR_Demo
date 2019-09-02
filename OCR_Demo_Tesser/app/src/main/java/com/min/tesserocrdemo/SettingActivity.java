package com.min.tesserocrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;

public class SettingActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    public static final int OCR_MODE_TESSER = 1;
    public static final int OCR_MODE_BAIDU = 2;
    private static final String PREF_SETTINGS = "pref_settings";
    private static final String PREF_CHESS = "pref_chess";
    private static final String PREF_VOICE = "pref_voice";
    private static final String PREF_DEBUG = "pref_debug";
    private static final String PREF_OCR_MODE = "pref_ocr_mode";
    private boolean mChessEnable = false;
    private boolean mVoiceEnable = false;
    private boolean mDebugEnable = false;
    private int mOcrMode = -1;

    public static boolean isChessEnable(Context ctx) {
        SharedPreferences settingPrefs = ctx.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        return settingPrefs.getBoolean(PREF_CHESS, false);
    }

    public static boolean isVoiceEnable(Context ctx) {
        SharedPreferences settingPrefs = ctx.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        return settingPrefs.getBoolean(PREF_VOICE, false);
    }

    public static boolean isDebugEnable(Context ctx) {
        SharedPreferences settingPrefs = ctx.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        return settingPrefs.getBoolean(PREF_DEBUG, true);
    }

    public static int getOCRMode(Context ctx) {
        SharedPreferences settingPrefs = ctx.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        return settingPrefs.getInt(PREF_OCR_MODE, OCR_MODE_TESSER);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        obtainSettings();

        ((Switch)findViewById(R.id.switch_chess)).setChecked(mChessEnable);
        ((Switch)findViewById(R.id.switch_voice)).setChecked(mVoiceEnable);
        ((Switch)findViewById(R.id.switch_debug)).setChecked(mDebugEnable);
        setOrcModeRadioValue((RadioGroup)findViewById(R.id.ocr_model), mOcrMode);

        findViewById(R.id.setting_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChessEnable = ((Switch)findViewById(R.id.switch_chess)).isChecked();
                mVoiceEnable = ((Switch)findViewById(R.id.switch_voice)).isChecked();
                mDebugEnable = ((Switch)findViewById(R.id.switch_debug)).isChecked();
                saveSettings();
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

        ((RadioGroup)findViewById(R.id.ocr_model)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int id = group.getId();
        if (id == R.id.ocr_model) {
            mOcrMode = getOrcModeRadioValue(checkedId);
        }
    }

    private int getOrcModeRadioValue(int checkedId) {
        int value = -1;
        switch (checkedId) {
            case R.id.ocr_tesser:
                value = OCR_MODE_TESSER;
                break;
            case R.id.ocr_baidu:
                value = OCR_MODE_BAIDU;
                break;
            default:
                break;
        }

        return value;
    }

    private void setOrcModeRadioValue(RadioGroup group, int value) {
        switch (value) {
            case OCR_MODE_TESSER:
                group.check(R.id.ocr_tesser);
                break;
            case OCR_MODE_BAIDU:
                group.check(R.id.ocr_baidu);
                break;
            default:
                break;
        }
    }

    private void saveSettings() {
        SharedPreferences settingPrefs = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = settingPrefs.edit();

        settingEditor.putBoolean(PREF_CHESS, mChessEnable);
        settingEditor.putBoolean(PREF_VOICE, mVoiceEnable);
        settingEditor.putBoolean(PREF_DEBUG, mDebugEnable);
        settingEditor.putInt(PREF_OCR_MODE, mOcrMode);

        settingEditor.apply();
    }

    private void obtainSettings() {
        SharedPreferences settingPrefs = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);

        mChessEnable = settingPrefs.getBoolean(PREF_CHESS, false);
        mVoiceEnable = settingPrefs.getBoolean(PREF_VOICE, false);
        mDebugEnable = settingPrefs.getBoolean(PREF_DEBUG, true);
        mOcrMode = settingPrefs.getInt(PREF_OCR_MODE, OCR_MODE_TESSER);
    }
}
