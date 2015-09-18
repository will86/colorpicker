package net.jileniao.colorpicker.ver2;

import java.util.Locale;

import net.jileniao.colorpicker.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColorPickerDialog extends Dialog implements
        ColorPickerView.OnColorChangedListener, View.OnClickListener {

    private static final int COLORPICKERPANEVIEW_LDPI_HEIGHT = 23;

    private ColorPickerView mColorPicker;

    private ColorPickerPanelView mOldColor;
    private ColorPickerPanelView mNewColor;

    private EditText mHexVal;
    private boolean mHexValueEnabled = false;
    private ColorStateList mHexDefaultTextColor;

    private OnColorChangedListener mListener;

    public interface OnColorChangedListener {
        public void onColorChanged(int color);
    }

    public ColorPickerDialog(Context context, int initialColor) {
        super(context);
        setWindowLayoutParams();
        init(initialColor);
    }

    private void setWindowLayoutParams() {
        LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.RIGHT;
        getWindow().setAttributes(params);
    }

    private void init(int color) {
        // To fight color banding.
        getWindow().setFormat(PixelFormat.RGBA_8888);
        setUp(color);
    }

    private void setUp(int color) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.dialog_color_picker, null);

        setContentView(layout);

        setTitle(R.string.title_test_color);

        mColorPicker = (ColorPickerView) layout
                .findViewById(R.id.color_picker_view);
        mOldColor = (ColorPickerPanelView) layout
                .findViewById(R.id.old_color_panel);
        mNewColor = (ColorPickerPanelView) layout
                .findViewById(R.id.new_color_panel);

        mHexVal = (EditText) layout.findViewById(R.id.hex_val);
        mHexVal.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mHexDefaultTextColor = mHexVal.getTextColors();

        mHexVal.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v
                            .getContext().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    String s = mHexVal.getText().toString();
                    if (s.length() > 5 || s.length() < 10) {
                        try {
                            int c = ColorUtil.convertToColorInt(s.toString());
                            mColorPicker.setColor(c, true);
                            mHexVal.setTextColor(mHexDefaultTextColor);
                        } catch (IllegalArgumentException e) {
                            mHexVal.setTextColor(Color.RED);
                        }
                    } else {
                        mHexVal.setTextColor(Color.RED);
                    }
                    return true;
                }
                return false;
            }
        });

        ((LinearLayout) mOldColor.getParent()).setPadding(
                Math.round(mColorPicker.getDrawingOffset()), 0,
                Math.round(mColorPicker.getDrawingOffset()), 0);

        mOldColor.setOnClickListener(this);
        mNewColor.setOnClickListener(this);
        mColorPicker.setOnColorChangedListener(this);
        mOldColor.setColor(color);
        mColorPicker.setColor(color, true);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) {
            LinearLayout textLayout = (LinearLayout) layout
                    .findViewById(R.id.text_hex_wrapper);
            textLayout.setVisibility(LinearLayout.GONE);
            mOldColor.getLayoutParams().height = COLORPICKERPANEVIEW_LDPI_HEIGHT;
            mNewColor.getLayoutParams().height = COLORPICKERPANEVIEW_LDPI_HEIGHT;
        }
    }

    @Override
    public void onColorChanged(int color) {
        mNewColor.setColor(color);

        if (mHexValueEnabled) {
            updateHexValue(color);
        }
    }

    public void setHexValueEnabled(boolean enable) {
        mHexValueEnabled = enable;
        if (enable) {
            mHexVal.setVisibility(View.VISIBLE);
            updateHexLengthFilter();
            updateHexValue(getColor());
        } else
            mHexVal.setVisibility(View.GONE);
    }

    public boolean getHexValueEnabled() {
        return mHexValueEnabled;
    }

    private void updateHexLengthFilter() {
        if (getAlphaSliderVisible())
            mHexVal.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
                    9) });
        else
            mHexVal.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
                    7) });
    }

    private void updateHexValue(int color) {
        if (getAlphaSliderVisible()) {
            mHexVal.setText(ColorUtil.convertToARGB(color)
                    .toUpperCase(Locale.getDefault()));
        } else {
            mHexVal.setText(ColorUtil.convertToRGB(color)
                    .toUpperCase(Locale.getDefault()));
        }
        mHexVal.setTextColor(mHexDefaultTextColor);
    }

    public void setAlphaSliderVisible(boolean visible) {
        mColorPicker.setAlphaSliderVisible(visible);
        if (mHexValueEnabled) {
            updateHexLengthFilter();
            updateHexValue(getColor());
        }
    }

    public boolean getAlphaSliderVisible() {
        return mColorPicker.getAlphaSliderVisible();
    }

    /**
     * Set a OnColorChangedListener to get notified when the color selected by
     * the user has changed.
     * 
     * @param listener
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.new_color_panel) {
            if (mListener != null) {
                mListener.onColorChanged(mNewColor.getColor());
            }
        }
        dismiss();
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt("old_color", mOldColor.getColor());
        state.putInt("new_color", mNewColor.getColor());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mOldColor.setColor(savedInstanceState.getInt("old_color"));
        mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
    }
}
