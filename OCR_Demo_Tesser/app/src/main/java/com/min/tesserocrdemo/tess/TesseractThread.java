package com.min.tesserocrdemo.tess;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * 解析拍照文字线程
 */
public class TesseractThread implements Runnable {
    private Context mContext;
    private Bitmap mBitmap;
    private TesseractCallback mCallback;


    public TesseractThread(Context context, Bitmap bitmap, TesseractCallback callback) {
        mContext = context;
        mBitmap = bitmap;
        mCallback = callback;
    }

    public void release() {
        TessEngine.releaseInstance();
    }

    @Override
    public void run() {
        if (null == mCallback) return;

        if (mBitmap == null) {
            mCallback.fail();
        } else {
            mCallback.succeed(TessEngine.getInstance(mContext).detectText(mBitmap));
        }
    }
}
