package com.min.tesserocrdemo.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.min.tesserocrdemo.R;

import androidx.annotation.NonNull;

public class ImageDialog extends Dialog {

    private Bitmap mBmp = null;
    private String mTitle = null;
    private String mFilterTitle = null;

    public ImageDialog(@NonNull Context context) {
        super(context);
    }

    public ImageDialog addBitmap(Bitmap bmp) {
        if (bmp != null){
            mBmp = bmp;
        }
        return this;
    }

    public ImageDialog addTitle(String title, String filterTitle) {
        if (!TextUtils.isEmpty(title)) {
            mTitle = title;
        }
        if (!TextUtils.isEmpty(filterTitle)) {
            mFilterTitle = filterTitle;
        }
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_dialog);

        ImageView imageView = findViewById(R.id.image_dialog_imageView);
        TextView textView = findViewById(R.id.image_dialog_textView);
        TextView filterTextView = findViewById(R.id.image_dialog_filtertextView);

        if (mBmp != null){
            imageView.setImageBitmap(mBmp);
        }

        if(!TextUtils.isEmpty(mTitle)) {
            textView.setText(mTitle);
        }

        if (!TextUtils.isEmpty(mFilterTitle)) {
            filterTextView.setText(mFilterTitle);
        }
    }

    @Override
    public void dismiss() {
        mBmp.recycle();
        mBmp = null;
        System.gc();
        super.dismiss();
    }
}