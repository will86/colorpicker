package net.jileniao.colorpicker;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;

public class ColorPickerV2Activity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setWindowLayoutParams();
        this.addPreferencesFromResource(R.xml.colorpicker);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setWindowLayoutParams() {
        LayoutParams params = getWindow().getAttributes();
        // params.gravity = Gravity.RIGHT;
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        params.alpha = 1.0f;
        params.dimAmount = 0.0f;
        // params.width = OkaoCameraView.LAYOUT_DISPLAYW * 45 / 100;
        // params.height = OkaoCameraView.LAYOUT_DISPLAYH * 60 / 100;

        params.height = LayoutParams.WRAP_CONTENT;
        params.width = LayoutParams.MATCH_PARENT;
        // params.height = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
    }

}
