package org.stellar.authentication.app;

import org.stellar.authentication.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("ClickableViewAccessibility")
public class LittleIcon extends View {
    private float viewX;
    private float viewY;
    private Paint mPaint;
    private Bitmap androidIcon;

    public LittleIcon(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        androidIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    }

    @Override
    public void onDraw(Canvas cvs) {
        cvs.drawBitmap(androidIcon, viewX - androidIcon.getWidth() / 2, viewY - androidIcon.getHeight()
                / 2, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchedX = Math.abs(viewX - event.getX()) > androidIcon.getWidth();
        boolean touchedY = Math.abs(viewY - event.getY()) > androidIcon.getHeight();
        boolean isValidTouch = !touchedX && !touchedY;
        if (isValidTouch) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_UP) {
                viewX = event.getX();
                viewY = event.getY();
            }
            invalidate();
            return true;

        }
        else
            return false;
    }
}