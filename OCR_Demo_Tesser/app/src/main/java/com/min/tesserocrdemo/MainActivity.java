package com.min.tesserocrdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    TessBaseAPI mTess;
    Button button;
    Bitmap bitmap;
    String result;
    ImageView imgView;
    TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.btnShot);
        imgView = (ImageView)this.findViewById(R.id.imageView);
        txtView = (TextView)this.findViewById(R.id.idCard_textView);

        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.textimage_chi_3);//识别图片源
        imgView.setImageBitmap(bitmap);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTess.setImage(bitmap);
                result = mTess.getUTF8Text();
                txtView.setText("结果为:" + result);
            }
        });
        initTessBaseData();
    }

    private void initTessBaseData() {
        mTess = new TessBaseAPI();
        String datapath = "/storage/emulated/0/tesseract/"; //语言包目录
        String language = "chi_sim";//"eng";
        File dir = new File(datapath + "tessdata/");
        if (!dir.exists()){
            Log.e("tag","文件不存在");
        }
        mTess.init(datapath, language);
    }
}