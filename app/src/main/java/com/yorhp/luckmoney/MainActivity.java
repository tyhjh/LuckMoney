package com.yorhp.luckmoney;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.yorhp.luckmoney.service.LuckMoneyService;
import com.yorhp.luckmoney.util.AccessbilityUtil;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Switch swWx;
    CheckBox ckSingle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ckSingle = findViewById(R.id.ckSingle);
        swWx = findViewById(R.id.swWx);
        swWx.setOnClickListener((v) -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        ckSingle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LuckMoneyService.isSingle = b;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        swWx.setChecked(AccessbilityUtil.isAccessibilitySettingsOn(this, LuckMoneyService.class));
    }
}
