package com.example.smilecamera.overlay;

import android.graphics.Canvas;

import com.huawei.hms.mlsdk.common.LensEngine;


public abstract class BaseGraphic {
    private GraphicOverlay graphicOverlay;

    public BaseGraphic(GraphicOverlay overlay) {
        this.graphicOverlay = overlay;
    }

    public abstract void draw(Canvas canvas);

    public float scaleX(float x) {
        return x * this.graphicOverlay.getWidthScaleValue();
    }

    public float scaleY(float y) {
        return y * this.graphicOverlay.getHeightScaleValue();
    }

    public float translateX(float x) {
        if (this.graphicOverlay.getCameraFacing() == LensEngine.FRONT_LENS) {
            return this.graphicOverlay.getWidth() - this.scaleX(x);
        } else {
            return this.scaleX(x);
        }
    }

    public float translateY(float y) {
        return this.scaleY(y);
    }
}
