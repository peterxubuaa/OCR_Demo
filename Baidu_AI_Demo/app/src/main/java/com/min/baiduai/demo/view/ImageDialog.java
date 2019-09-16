package com.min.baiduai.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.min.baiduai.demo.R;

import androidx.annotation.NonNull;

public class ImageDialog extends Dialog {

    private Bitmap mBmp = null;
    private String mTitle = null;
    private String mFilterTitle = null;

    public ImageDialog(@NonNull Context context) {
        super(context);
    }

    public void addBitmap(Bitmap bmp) {
        mBmp = bmp;
    }

    public void addTitle(String title, String filterTitle) {
        mTitle = title;
        mFilterTitle = filterTitle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_dialog);
    }

    @Override
    public void show() {
        super.show();

        ImageView imageView = findViewById(R.id.image_dialog_imageView);
        TextView textView = findViewById(R.id.image_dialog_textView);
        TextView filterTextView = findViewById(R.id.image_dialog_filtertextView);

        imageView.setImageBitmap(mBmp);

        textView.setText(mTitle);

        filterTextView.setText(mFilterTitle);
    }

    @Override
    public void dismiss() {
//        mBmp.recycle();
//        mBmp = null;
        System.gc();
        super.dismiss();
    }
}