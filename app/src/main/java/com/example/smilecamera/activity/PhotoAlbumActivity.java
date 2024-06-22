package com.example.smilecamera.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.smilecamera.R;
import com.example.smilecamera.adapter.PhotoAdapter;
import com.example.smilecamera.util.ConstantUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotoAlbumActivity extends AppCompatActivity {

    private final List<Bitmap> picList = new ArrayList<>();

    private ImageView emptyDataIv;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        initView();
    }

    private void initView() {
        ConstantUtil.setStatusLine(getWindow(), this);
        Toolbar toolbar = findViewById(R.id.child_toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });
        recyclerView = findViewById(R.id.rv_photo);
        emptyDataIv = findViewById(R.id.iv_empty_data);
        TextView title = findViewById(R.id.toolbar_title);
        Intent intent = this.getIntent();
        int mode = intent.getIntExtra(ConstantUtil.PHOTO_ALBUM_MODE, -1);
        if (mode == ConstantUtil.SYSTEM_PHOTO) {
            title.setText("系统相机相册");
            getSystemFile();
        } else if (mode == ConstantUtil.SMILE_PHOTO) {
            title.setText("笑脸相机相册");
            getSmileFile();
        }


//        StaggeredGridLayoutManager staggeredGridLayoutManager =
//                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4,
                RecyclerView.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        PhotoAdapter photoAdapter = new PhotoAdapter(picList);
        recyclerView.setAdapter(photoAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (picList != null) picList.clear();
    }

    private void getSmileFile(){
        String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/SmileCamera";
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                if (files.length > 0) {
                    for (File file : files) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        picList.add(bitmap);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyDataIv.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void getSystemFile(){
        String directoryPath = "/storage/emulated/0/DCIM/Camera/";
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                if (files.length > 0) {
                    for (File file : files) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        picList.add(bitmap);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyDataIv.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}