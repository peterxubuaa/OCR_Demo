package com.min.tesserocrdemo.tess;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

class TessEngine {
    static final String TAG = "DBG_" + TessEngine.class.getName();
    static TessEngine sTessEngine = null;
    TessBaseAPI mTessBaseAPI = null;

    private TessEngine(Context ctx){
        Log.d(TAG, "Initialization of TessBaseApi");
        TessDataManager.initTessTrainedData(ctx);
        mTessBaseAPI = new TessBaseAPI();
        String path = TessDataManager.getTesseractFolder();
        Log.d(TAG, "Tess folder: " + path);
        mTessBaseAPI.init(path, "chi_sim");
    }

    static TessEngine getInstance(Context ctx) {
        if (null == sTessEngine) {
            sTessEngine = new TessEngine(ctx);
        }
        return sTessEngine;
    }

    static void releaseInstance() {
        if (null != sTessEngine) {
            if (null != sTessEngine.mTessBaseAPI) {
                sTessEngine.mTessBaseAPI.end();
                sTessEngine.mTessBaseAPI = null;
                System.gc();
            }
            sTessEngine = null;
        }
    }

    String detectText(Bitmap bitmap) {
        mTessBaseAPI.setImage(bitmap);
        String inspection = mTessBaseAPI.getUTF8Text();
        Log.d(TAG, "text: " + inspection);
        Log.d(TAG, "Confidence values: " + mTessBaseAPI.meanConfidence());
        return inspection;
    }
}
