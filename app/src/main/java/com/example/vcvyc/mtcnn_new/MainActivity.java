package com.example.vcvyc.mtcnn_new;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    ImageView imageView;

    MTCNN mtcnn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initMTCNN();

        processImage(getBitmapFromAssetFile("trump2.jpg"));
    }

    private void initView() {
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 0x1);
            }
        });
    }

    private void initMTCNN() {
        mtcnn = new MTCNN(getAssets());
    }

    private Bitmap getBitmapFromAssetFile(String filename) {
        Bitmap bitmap;
        InputStream is = null;
        try {
            is = getAssets().open(filename);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e("MainActivity", "[*]failed to open " + filename);
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception ignore) {
            }
        }
        return Utils.copyBitmap(bitmap); //返回mutable的image
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            processImage(bitmap);
        } catch (Exception e) {
            Log.d("MainActivity", "[*]" + e);
        }
    }

    public void processImage(Bitmap bitmap) {
        Bitmap bm = Utils.copyBitmap(bitmap);
        try {
            Vector<Box> boxes = mtcnn.detectFaces(bm, 40);
            for (int i = 0; i < boxes.size(); i++) {
                Utils.drawRect(bm, boxes.get(i).transform2Rect());
                Utils.drawPoints(bm, boxes.get(i).landmark);
            }
            imageView.setImageBitmap(bm);
        } catch (Exception e) {
            Log.e(TAG, "[*]detect false:" + e);
        }
    }
}
