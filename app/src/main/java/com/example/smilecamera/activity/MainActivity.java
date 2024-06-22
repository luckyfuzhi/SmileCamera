package com.example.smilecamera.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smilecamera.R;
import com.example.smilecamera.util.ConstantUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private CardView systemCamara;

    private CardView smileCamara;

    private CardView photoAlbum;

    private BottomSheetDialog bottomSheetDialog;

    private TextView systemPhotoAlbum;

    private TextView smilePhotoAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initClick();
    }



    /**
     * 初始化视图
     */
    private void initView() {
        ConstantUtil.setStatusLine(getWindow(),this);

        systemCamara = findViewById(R.id.cv_system_camara);
        smileCamara = findViewById(R.id.cv_smile_camara);
        photoAlbum = findViewById(R.id.cv_photo_album);

        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomView = LayoutInflater.from(this).inflate(R.layout.layout_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomView);

        systemPhotoAlbum = bottomView.findViewById(R.id.system_photo_tv);
        smilePhotoAlbum = bottomView.findViewById(R.id.smile_photo_tv);
    }

    /**
     * 初始化点击事件
     */
    private void initClick() {
        systemCamara.setOnClickListener(view -> {
            takePic(this);
        });
        smileCamara.setOnClickListener(view -> {
            Intent intent = new Intent(this, BridgeActivity.class);
            startActivity(intent);
        });
        photoAlbum.setOnClickListener(view -> {
            bottomSheetDialog.show();
        });
        systemPhotoAlbum.setOnClickListener(view -> {
            Intent intent = new Intent(this, PhotoAlbumActivity.class);
            intent.putExtra(ConstantUtil.PHOTO_ALBUM_MODE, ConstantUtil.SYSTEM_PHOTO);
            startActivity(intent);
            bottomSheetDialog.dismiss();
        });
        smilePhotoAlbum.setOnClickListener(view -> {
            Intent intent = new Intent(this, PhotoAlbumActivity.class);
            intent.putExtra(ConstantUtil.PHOTO_ALBUM_MODE, ConstantUtil.SMILE_PHOTO);
            startActivity(intent);
            bottomSheetDialog.dismiss();
        });
    }


    /**
     *  打开系统相机拍照
     * @param context 上下文
     */
    private void takePic(Context context) {
        PictureSelector.create(context)
                .openCamera(SelectMimeType.ofImage())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                        Log.d("pic", "onResult SandBox: " + result.get(0).getSandboxPath());
                        Log.d("pic", "onResult Path: " + result.get(0).getPath());
                        Log.d("pic", "onResult RealPath: " + result.get(0).getRealPath());
                        Log.d("pic", "onResult availablePath: " + result.get(0).getAvailablePath());
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }


}