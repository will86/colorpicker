package net.jileniao.colorpicker.ver2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A preference type that allows a user to choose a time
 * 
 * @author Sergey Margaritov
 */
public class CopyOfColorPickerPreference extends Preference implements
        Preference.OnPreferenceClickListener,
        ColorPickerDialog.OnColorChangedListener,
        CompoundButton.OnCheckedChangeListener {
    private View mView;
    private ColorPickerDialog mDialog;
    private boolean mShowCheckbox = false;
    private boolean mPickerEnabled = false;
    private int mDefaultValue = Color.BLACK;
    private int mValue = Color.BLACK;
    private float mDensity = 0;
    private boolean mAlphaSliderEnabled = false;
    private boolean mHexValueEnabled = false;
    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private Context c = null;

    public CopyOfColorPickerPreference(Context context) {
        super(context);
        c = context;
        init(context, null);
    }

    public CopyOfColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        init(context, attrs);
    }

    public CopyOfColorPickerPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        c = context;
        init(context, attrs);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            mPickerEnabled = !mShowCheckbox
                    || getSharedPreferences().getBoolean(getKey() + "_enabled",
                            mPickerEnabled);
        }

        onColorChanged(restoreValue ? getValue() : mDefaultValue);
    }

    private void init(Context context, AttributeSet attrs) {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        setOnPreferenceClickListener(this);
        if (attrs != null) {
            String defaultValue = attrs.getAttributeValue(androidns,
                    "defaultValue");
            if (defaultValue == null || defaultValue.isEmpty()) {

                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(context);
                mDefaultValue = pref.getInt("key_test_color", 0xffFF80FF);
            } else if (defaultValue.startsWith("#")) {
                try {
                    mDefaultValue = ColorUtil.convertToColorInt(defaultValue);
                } catch (NumberFormatException e) {
                    Log.e("ColorPickerPreference", "Wrong color: "
                            + defaultValue);
                    mDefaultValue = ColorUtil.convertToColorInt("#FF000000");
                }
            } else {
                int resourceId = attrs.getAttributeResourceValue(androidns,
                        "defaultValue", 0);
                if (resourceId != 0) {
                    mDefaultValue = context.getResources().getInteger(
                            resourceId);
                }
            }

            mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null,
                    "alphaSlider", false);
            mHexValueEnabled = attrs.getAttributeBooleanValue(null, "hexValue",
                    true);
            mShowCheckbox = attrs.getAttributeBooleanValue(null,
                    "showCheckbox", false);
            mPickerEnabled = !mShowCheckbox
                    || attrs.getAttributeBooleanValue(null, "enabledByDefault",
                            false);
        }
        mValue = mDefaultValue;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mView = view;
        persistBothValues();
        setPreviewColor();
    }

    private void setPreviewColor() {
        if (mView == null) {
            return;
        }

        CheckBox cbPickerEnabled = null;
        if (mShowCheckbox) {
            cbPickerEnabled = new CheckBox(getContext());
            cbPickerEnabled.setFocusable(false);
            cbPickerEnabled.setEnabled(super.isEnabled());
            cbPickerEnabled.setChecked(mPickerEnabled);
            cbPickerEnabled.setOnCheckedChangeListener(this);
        }

        ImageView iView = new ImageView(getContext());
        LinearLayout widgetFrameView = ((LinearLayout) mView
                .findViewById(android.R.id.widget_frame));
        if (widgetFrameView == null) {
            return;
        }
        widgetFrameView.setVisibility(View.VISIBLE);
        widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(), (int) (mDensity * 8),
                widgetFrameView.getPaddingBottom());

        // remove already create preview image
        int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }
        if (mShowCheckbox) {
            widgetFrameView.setOrientation(LinearLayout.HORIZONTAL);
            widgetFrameView.addView(cbPickerEnabled);
        }
        widgetFrameView.addView(iView);
        widgetFrameView.setMinimumWidth(0);
        iView.setBackground(new AlphaPatternDrawable((int) (5 * mDensity)));
        iView.setImageBitmap(getPreviewBitmap());
    }

    private Bitmap getPreviewBitmap() {
        int d = (int) (mDensity * 31); // 30dip
        int color = getValue();
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c = (i <= 1 || j <= 1 || i >= (w - 2) || j >= (h - 2)) ? Color.GRAY
                        : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }

    public int getValue() {
        try {
            if (shouldPersist()) {
                mValue = getPersistedInt(mDefaultValue);
            }
        } catch (ClassCastException e) {
            mValue = mDefaultValue;
        }

        return mValue;
    }

    @Override
    public void onColorChanged(int color) {
        mValue = color;
        persistBothValues();
        setPreviewColor();
        notifyChanged();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPickerEnabled = isChecked;
        persistBothValues();
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
    }

    protected void persistBothValues() {
        if (shouldPersist()) {
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(getKey(), mValue);
            if (mShowCheckbox) {
                editor.putBoolean(getKey() + "_enabled", mPickerEnabled);
            }

            if (shouldCommit()) {
                try {
                    editor.apply();
                } catch (AbstractMethodError unused) {
                    editor.commit();
                }
            }
        } else {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("key_test_color", mValue);
            editor.commit();
        }
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && mPickerEnabled;
    }

    /**
	 *
	 */
    public boolean onPreferenceClick(Preference preference) {
        showDialog(null);
        return false;
    }

    protected void showDialog(Bundle state) {
        Log.v("TestColorPicker", "showDialog");

        mDialog = new ColorPickerDialog(getContext(), getValue());
        mDialog.setOnColorChangedListener(this);

        if (mAlphaSliderEnabled) {
            mDialog.setAlphaSliderVisible(true);
        }

        if (mHexValueEnabled) {
            mDialog.setHexValueEnabled(true);
        }

        if (state != null) {
            mDialog.onRestoreInstanceState(state);
        }

        mDialog.show();
    }
}
