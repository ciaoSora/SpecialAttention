package com.yang.spat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ConfigureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        final EditText edit = (EditText)findViewById(R.id.edittext);
        SharedPreferences pref = getSharedPreferences("user_settings", MODE_PRIVATE);
        edit.setText(pref.getString("rule", ""), TextView.BufferType.EDITABLE);

        Button buttonApply = (Button)findViewById(R.id.button_apply);
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRule(edit.getText().toString());
                Toast.makeText(ConfigureActivity.this, "New rule applied. Reboot to take effect", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigureActivity.this);
            builder.setTitle("WARNING");
            builder.setMessage("All contents will be discarded. Are you sure?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ConfigureActivity.this.finish();
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateRule(String rule) {
        rule = rule.trim();
        SharedPreferences pref = getSharedPreferences("user_settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("rule", rule);
        editor.commit();
        File file = new File("/data/data/com.yang.spat/shared_prefs/user_settings.xml");
        file.setReadable(true, false);
    }
}
