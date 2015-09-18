package net.jileniao.colorpicker;

import net.jileniao.colorpicker.ver1.ColorPickerDialog;
import net.jileniao.colorpicker.ver1.ColorPickerDialog.OnColorChangedListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button mColorPickerBtnVer1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mColorPickerBtnVer1 = (Button) findViewById(R.id.color_btn1);
    }

    public void showColorPicker1(View v) {
        OnColorChangedListener listener = new OnColorChangedListener() {

            @Override
            public void colorChanged(int color) {
                Toast.makeText(getApplicationContext(), "color0:" + color,
                        Toast.LENGTH_LONG).show();
                mColorPickerBtnVer1.setBackgroundColor(color);
            }
        };
        ColorPickerDialog dialog = new ColorPickerDialog(this, listener, 0);
        dialog.show();
    }

    public void showColorPicker2(View v) {
        Intent intent = new Intent(this, ColorPickerV2Activity.class);
        startActivity(intent);
    }
}
