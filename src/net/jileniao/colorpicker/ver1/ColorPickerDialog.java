package net.jileniao.colorpicker.ver1;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class ColorPickerDialog extends Dialog {
    private OnColorChangedListener mListener;
    private int mInitialColor;

    private int mDeviceH;

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public ColorPickerDialog(Context context, OnColorChangedListener listener,
            int initialColor) {
        super(context);
        mListener = listener;
        mInitialColor = initialColor;
        initScreenSize();
    }

    private void initScreenSize() {
        DisplayMetrics metrics = getContext().getResources()
                .getDisplayMetrics();
        mDeviceH = metrics.heightPixels;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                mListener.colorChanged(color);
                dismiss();
            }
        };

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setContentView(new ColorPickerView(getContext(), l, mInitialColor), lp);
        setTitle("- Color -");
    }

    private class ColorPickerView extends View {
        private final int[] mColors;

        private Paint mPaint;
        private Paint mPaintC;
        private Paint mOKPaint;
        private int[] mChroma;
        private OnColorChangedListener mListener;
        private Shader sg, lg;
        private int selectColor;
        private float selectHue = 0;

        private boolean mTrackingOK;
        private boolean mHighlightOK;

        private final int CENTER_X = (int) (mDeviceH * 0.25f);
        private final int CENTER_Y = (int) (mDeviceH * 0.25f);
        private final int CENTER_RADIUS = 24;
        private final float OK_X0 = -CENTER_X / 2;
        private final float OK_X1 = CENTER_X / 2;
        private final float OK_Y0 = (float) (CENTER_X * 1.2);
        private final float OK_Y1 = (float) (CENTER_X * 1.5);

        private RectF roundRect = null;
        private Paint textPaint = null;

        private RectF mOvalRect;
        private RectF mRoundRect;

        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            mListener = l;
            selectColor = color;
            selectHue = getHue(selectColor);
            mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
                    0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

            mChroma = new int[] { 0xFF000000, 0xFF888888, 0xFFFFFFFF };

            sg = new SweepGradient(0, 0, mColors, null);
            lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null,
                    Shader.TileMode.CLAMP);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setShader(sg);
            mPaint.setStrokeWidth(CENTER_RADIUS);

            mPaintC = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintC.setStyle(Paint.Style.FILL);
            mPaintC.setShader(lg);
            mPaintC.setStrokeWidth(2);

            mOKPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOKPaint.setStyle(Paint.Style.FILL);
            mOKPaint.setColor(selectColor);
            mOKPaint.setStrokeWidth(5);

            roundRect = new RectF(OK_X0, OK_Y0, OK_X1, OK_Y1);
            textPaint = new Paint();
            mOvalRect = new RectF();
            mRoundRect = new RectF();
            setBackgroundColor(Color.WHITE);
        }

        private void drawSVRegion(Canvas canvas) {
            final float RESOLUTION = (float) 0.005;

            for (float y = 0; y < 1; y += RESOLUTION) {
                mChroma = new int[10];

                int i = 0;
                for (float x = 0; i < 10; x += 0.1, i += 1) {
                    mChroma[i] = setHSVColor(selectHue, x, y);
                }
                lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null,
                        Shader.TileMode.CLAMP);
                mPaintC.setShader(lg);

                canvas.drawLine(OK_X0, OK_X0 + (CENTER_X * y), OK_X1, OK_X0
                        + (float) (CENTER_X * (y)), mPaintC);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;

            canvas.translate(CENTER_X, CENTER_X);
            mOvalRect.set(-r, -r, r, r);
            canvas.drawOval(mOvalRect, mPaint);

            drawSVRegion(canvas);

            canvas.drawRoundRect(roundRect, 5, 5, mOKPaint);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(20);
            textPaint.setAntiAlias(true);
            canvas.drawText("OK", 0 - 14, (float) (CENTER_X * 1.4) + 2,
                    textPaint);

            if (mTrackingOK) {
                int c = mOKPaint.getColor();
                mOKPaint.setStyle(Paint.Style.STROKE);

                if (mHighlightOK) {
                    mOKPaint.setAlpha(0xFF);
                } else {
                    mOKPaint.setAlpha(0x80);
                }

                float padding = 5;
                mRoundRect.set(OK_X0 - padding, OK_Y0 - padding, OK_X1
                        + padding, OK_Y1 + padding);
                canvas.drawRoundRect(mRoundRect, 5, 5, mOKPaint);
                mOKPaint.setStyle(Paint.Style.FILL);
                mOKPaint.setColor(c);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X * 2, (int) (CENTER_Y * 2.8));
        }

        private int floatToByte(float x) {
            int n = java.lang.Math.round(x);
            return n;
        }

        private int pinToByte(int n) {
            if (n < 0) {
                n = 0;
            } else if (n > 255) {
                n = 255;
            }
            return n;
        }

        private float getHue(int color) {
            float hsv[] = new float[3];
            Color.colorToHSV(color, hsv);
            return hsv[0];
        }

        private int ave(int s, int d, float p) {
            return s + Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }

            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int) p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i + 1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        @SuppressWarnings("unused")
        private int rotateColor(int color, float rad) {
            float deg = rad * 180 / PI;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            ColorMatrix cm = new ColorMatrix();
            ColorMatrix tmp = new ColorMatrix();

            cm.setRGB2YUV();
            tmp.setRotate(0, deg);
            cm.postConcat(tmp);
            tmp.setYUV2RGB();
            cm.postConcat(tmp);

            final float[] a = cm.getArray();

            int ir = floatToByte(a[0] * r + a[1] * g + a[2] * b);
            int ig = floatToByte(a[5] * r + a[6] * g + a[7] * b);
            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);

            return Color.argb(Color.alpha(color), pinToByte(ir), pinToByte(ig),
                    pinToByte(ib));
        }

        private int setHSVColor(float hue, float saturation, float value) {
            float[] hsv = new float[3];

            if (hue >= 360) {
                hue = 359;
            } else if (hue < 0) {
                hue = 0;
            }

            if (saturation > 1) {
                saturation = 1;
            } else if (saturation < 0) {
                saturation = 0;
            }

            if (value > 1) {
                value = 1;
            } else if (value < 0) {
                value = 0;
            }

            hsv[0] = hue;
            hsv[1] = saturation;
            hsv[2] = value;

            return Color.HSVToColor(hsv);
        }

        private static final float PI = 3.1415927f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
            float r = (float) (java.lang.Math.sqrt(x * x + y * y));
            boolean inOK = false;
            boolean inOval = false;
            boolean inRect = false;

            if (r <= CENTER_X) {
                if (r > CENTER_X - CENTER_RADIUS) {
                    inOval = true;
                } else if (x >= OK_X0 && x < OK_X1 && y >= OK_X0 && y < OK_X1) {
                    inRect = true;
                }
            } else if (x >= OK_X0 && x < OK_X1 && y >= OK_Y0 && y < OK_Y1) {
                inOK = true;
            }

            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingOK = inOK;
                if (inOK) {
                    mHighlightOK = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingOK) {
                    if (mHighlightOK != inOK) {
                        mHighlightOK = inOK;
                        invalidate();
                    }
                } else if (inOval) {
                    float angle = (float) java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle / (2 * PI);
                    if (unit < 0) {
                        unit += 1;
                    }

                    selectColor = interpColor(mColors, unit);
                    mOKPaint.setColor(selectColor);
                    selectHue = getHue(selectColor);

                    invalidate();
                } else if (inRect) {
                    int selectColor2 = setHSVColor(selectHue, (x - OK_X0)
                            / CENTER_X, (y - OK_X0) / CENTER_Y);
                    selectColor = selectColor2;
                    mOKPaint.setColor(selectColor);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingOK) {
                    if (inOK) {
                        mListener.colorChanged(mOKPaint.getColor());
                    }
                    mTrackingOK = false;
                    invalidate();
                }
                break;
            }
            return true;
        }
    }
}
