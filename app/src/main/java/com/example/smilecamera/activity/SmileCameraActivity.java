package com.example.smilecamera.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.smilecamera.R;
import com.example.smilecamera.camera.LensEnginePreview;
import com.example.smilecamera.overlay.GraphicOverlay;
import com.example.smilecamera.overlay.LocalFaceGraphic;
import com.example.smilecamera.util.ConstantUtil;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.common.MLResultTrailer;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting;
import com.huawei.hms.mlsdk.face.MLFaceEmotion;
import com.huawei.hms.mlsdk.face.MLMaxSizeFaceTransactor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SmileCameraActivity extends AppCompatActivity {
    private static final String TAG = "SmileCameraActivity";
    private final float smilingRate = 0.8f;

    private LensEnginePreview mPreview;

    private GraphicOverlay overlay;

    private boolean isFront = false;
    private int detectMode; //识别方式
    private final float smilingPossibility = 0.95f;
    private final static int TAKE_SMILE_PHOTO = 1;

    private final static int STOP_PREVIEW = 2;

    private final String storePath = "/storage/emulated/0/DCIM/SmileCamera";

    private MLFaceAnalyzer analyzer;

    private boolean safeToTakePicture = false;

    private LensEngine mLensEngine;

    private int lensType = LensEngine.BACK_LENS;

    private ImageView ivSwitch;
    private ImageView ivYes;
    private ImageView ivNo;

    private ImageView ivBack;

    private byte[] restBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smile_camera);
        initView();
        initClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLensEngine();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLensEngine != null) mLensEngine.release();
        if (analyzer != null) {
            try {
                analyzer.stop();
            } catch (IOException e) {
                Log.e(TAG, "Stop failed: " + e.getMessage());
            }
        }
    }

    private void initView() {
        ConstantUtil.setStatusLine(getWindow(), this);

        Intent intent = this.getIntent();
        mPreview = findViewById(R.id.preview);
        detectMode = intent.getIntExtra(ConstantUtil.DETECT_MODE, -1);

        createFaceAnalyzer();
        overlay = findViewById(R.id.face_overlay);
        ivSwitch = findViewById(R.id.facingSwitch);
        ivYes = findViewById(R.id.ic_yes);
        ivNo = findViewById(R.id.ic_no);
        ivBack = findViewById(R.id.back);
        createLensEngine();
    }

    private void initClick() {
        ivBack.setOnClickListener(view -> {
            finish();
        });
        ivSwitch.setOnClickListener(view -> {
            isFront = !isFront;
            if (isFront) {
                lensType = LensEngine.FRONT_LENS;
            } else {
                lensType = LensEngine.BACK_LENS;
            }
            if (mLensEngine != null) mLensEngine.close();
            startPreview();
        });

        ivYes.setOnClickListener(view -> {
            if (restBytes != null) {
                if (restBytes.length != 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(restBytes, 0, restBytes.length);
                    saveBitmapToDisk(bitmap);
                    Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
                }
            }
            ivYes.setVisibility(View.GONE);
            ivNo.setVisibility(View.GONE);
            ivSwitch.setVisibility(View.VISIBLE);
            startPreview();
        });
        ivNo.setOnClickListener(view -> {
            if (restBytes != null) {
                restBytes = null;
            }
            ivYes.setVisibility(View.GONE);
            ivNo.setVisibility(View.GONE);
            ivSwitch.setVisibility(View.VISIBLE);
            startPreview();
        });
    }

    /**
     * 创建视觉引擎
     */
    private void createLensEngine() {
        Context context = getApplicationContext();
        mLensEngine = new LensEngine.Creator(context, analyzer).setLensType(lensType)
                .applyDisplayDimension(640, 480)
                .applyFps(25.0f)
                .enableAutomaticFocus(true)
                .create();
    }

    /**
     * 打开笑脸相机拍照
     */
    private void takeSmilePic() {
        this.mLensEngine.photograph(null, new LensEngine.PhotographListener() {
            @Override
            public void takenPhotograph(byte[] bytes) {
                mHandler.sendEmptyMessage(STOP_PREVIEW);
                restBytes = bytes;
                ivSwitch.setVisibility(View.GONE);
                ivYes.setVisibility(View.VISIBLE);
                ivNo.setVisibility(View.VISIBLE);
            }
        });
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAKE_SMILE_PHOTO:
                    takeSmilePic();
                    break;
                case STOP_PREVIEW:
                    stopPreview();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 创建脸部分析器
     */
    private void createFaceAnalyzer() {
        MLFaceAnalyzerSetting setting =
                new MLFaceAnalyzerSetting.Factory()
                        .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
                        .setKeyPointType(MLFaceAnalyzerSetting.TYPE_UNSUPPORT_KEYPOINTS)
                        .setMinFaceProportion(0.1f)
                        .setTracingAllowed(true)
                        .create();
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting);
        if (this.detectMode == ConstantUtil.NEAREST_PEOPLE) {
            MLMaxSizeFaceTransactor transactor = new MLMaxSizeFaceTransactor.Creator(analyzer, new MLResultTrailer<MLFace>() {
                @Override
                public void objectCreateCallback(int itemId, MLFace obj) {
                    overlay.clear();
                    if (obj == null) {
                        return;
                    }
                    LocalFaceGraphic faceGraphic =
                            new LocalFaceGraphic(overlay, obj, SmileCameraActivity.this);
                    overlay.addGraphic(faceGraphic);
                    MLFaceEmotion emotion = obj.getEmotions();
                    if (emotion.getSmilingProbability() > smilingPossibility) {
                        safeToTakePicture = false;
                        mHandler.sendEmptyMessage(TAKE_SMILE_PHOTO);
                    }
                }

                @Override
                public void objectUpdateCallback(MLAnalyzer.Result<MLFace> var1, MLFace obj) {
                    overlay.clear();
                    if (obj == null) {
                        return;
                    }
                    LocalFaceGraphic faceGraphic =
                            new LocalFaceGraphic(overlay, obj, SmileCameraActivity.this);
                    overlay.addGraphic(faceGraphic);
                    MLFaceEmotion emotion = obj.getEmotions();
                    if (emotion.getSmilingProbability() > smilingPossibility && safeToTakePicture) {
                        safeToTakePicture = false;
                        mHandler.sendEmptyMessage(TAKE_SMILE_PHOTO);
                    }
                }

                @Override
                public void lostCallback(MLAnalyzer.Result<MLFace> result) {
                    overlay.clear();
                }

                @Override
                public void completeCallback() {
                    overlay.clear();

                }
            }).create();
            this.analyzer.setTransactor(transactor);

        } else {
            this.analyzer.setTransactor(new MLAnalyzer.MLTransactor<MLFace>() {
                @Override
                public void destroy() {
                }

                @Override
                public void transactResult(MLAnalyzer.Result<MLFace> result) {
                    SparseArray<MLFace> faceSparseArray = result.getAnalyseList();
                    int flag = 0;
                    for (int i = 0; i < faceSparseArray.size(); i++) {
                        MLFaceEmotion emotion = faceSparseArray.valueAt(i).getEmotions();
                        if (emotion.getSmilingProbability() > smilingPossibility) {
                            flag++;
                        }
                    }
                    if (flag > faceSparseArray.size() * smilingRate && safeToTakePicture) {
                        safeToTakePicture = false;
                        mHandler.sendEmptyMessage(TAKE_SMILE_PHOTO);
                    }
                }
            });
        }
    }

    /**
     * 启动视觉引擎
     */
    private void startLensEngine() {
        if (this.mLensEngine != null) {
            try {
                if (this.detectMode == ConstantUtil.NEAREST_PEOPLE) {
                    this.mPreview.start(this.mLensEngine, this.overlay);
                } else {
                    this.mPreview.start(this.mLensEngine);
                }

                this.safeToTakePicture = true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to start lens engine.", e);
                this.mLensEngine.release();
                this.mLensEngine = null;
            }
        }
    }

    /**
     * 开始获取画面
     */
    public void startPreview() {
        createFaceAnalyzer();
        mPreview.release();
        createLensEngine();
        startLensEngine();
    }

    /**
     * 停止获取画面
     */
    private void stopPreview() {
        if (mLensEngine != null) {
            mLensEngine.release();
            this.safeToTakePicture = false;
        }
        if (analyzer != null) {
            try {
                this.analyzer.stop();
            } catch (IOException e) {
                Log.e(TAG, "Stop failed: " + e.getMessage());
            }
        }
    }

    /**
     * 把拍摄的图片保存在磁盘
     * @param bitmap bitmap文件
     * @return 文件路径
     */
    private String saveBitmapToDisk(Bitmap bitmap) {
        String filePath = "";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            boolean res = appDir.mkdir();
            if (!res) {
                Log.e(TAG, "saveBitmapToDisk failed");
                return "";
            }
        }

        String fileName = "SmileDemo" + System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();

            Uri uri = Uri.fromFile(file);
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        try {
            filePath=file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("smilepic", filePath);
        return filePath;
    }
}