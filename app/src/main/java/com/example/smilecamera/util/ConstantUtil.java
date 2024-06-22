package com.example.smilecamera.util;

import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.example.smilecamera.R;

public class ConstantUtil {

    public static final String DETECT_MODE = "detect_mode";
    public static final String PHOTO_ALBUM_MODE = "photo_album_mode";

    public static final int MOST_PEOPLE = 1002;

    public static final int NEAREST_PEOPLE = 1003;

    public static final int SYSTEM_PHOTO = 1006;

    public static final int SMILE_PHOTO = 1007;

    /**
     *  设置状态栏
     * @param window 当前窗口对象
     * @param context 当前上下文
     */
    public static void setStatusLine(Window window, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.gray));
        }
    }

}
