package com.example.smilecamera.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceShape;

import java.util.List;

public class LocalFaceGraphic extends BaseGraphic {

    private final GraphicOverlay overlay;

    private final Paint facePaint;

    private volatile MLFace face;
    private Context mContext;

    public LocalFaceGraphic(GraphicOverlay overlay, MLFace face, Context context) {
        super(overlay);

        this.mContext = context;
        this.face = face;
        this.overlay = overlay;

        float line_width = dp2px(this.mContext, 1);
        this.facePaint = new Paint();
        this.facePaint.setColor(Color.parseColor("#ffcc66"));
        this.facePaint.setStyle(Paint.Style.STROKE);
        this.facePaint.setStrokeWidth(line_width);
    }

    public float dp2px(Context context, float dipValue) {
        return dipValue * context.getResources().getDisplayMetrics().density + 0.5f;
    }

    @Override
    public void draw(Canvas canvas) {
        if (this.face == null) {
            return;
        }
        MLFaceShape faceShape = this.face.getFaceShape(MLFaceShape.TYPE_FACE);
        List<MLPosition> points = faceShape.getPoints();
        float verticalMin = Float.MAX_VALUE;
        float verticalMax = 0f;
        float horizontalMin = Float.MAX_VALUE;
        float horizontalMax = 0f;
        for (int i = 0; i < points.size(); i++) {
            MLPosition point = points.get(i);
            if (point == null) {
                continue;
            }
            if (point.getX() != null && point.getY() != null) {
                if (point.getX() > horizontalMax) horizontalMax = point.getX();
                if (point.getX() < horizontalMin) horizontalMin = point.getX();
                if (point.getY() > verticalMax) verticalMax = point.getY();
                if (point.getY() < verticalMin) verticalMin = point.getY();

            }
        }
        Rect rect = new Rect((int) this.translateX(horizontalMin), (int) this.translateY(verticalMin), (int) this.translateX(horizontalMax), (int) this.translateY(verticalMax));
        canvas.drawRect(rect, this.facePaint);
    }
}
