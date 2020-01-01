package com.yorhp.luckmoney;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.yorhp.luckmoney.service.LuckMoneyService;
import com.yorhp.luckmoney.util.AccessbilityUtil;
import com.yorhp.luckmoney.util.ScreenUtil;
import com.yorhp.luckmoney.util.SharedPreferencesUtil;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author yorhp
 */
public class MainActivity extends AppCompatActivity {

    Switch swWx;
    CheckBox ckSingle,ckPause;
    TextView tvTime,tvOpenTime,tvDevice;

    /**
     * 等待红包弹出窗时间
     */
    private static final int MAX_WAIT_WINDOW_TIME=2000;

    /**
     * 保存状态字段
     */
    public static final String NEED_SET_TIME="need_set_time";
    public static final String WAIT_WINDOW_TIME="waitWindowTime";
    public static final String WAIT_GET_MONEY_TIME="waitGetMoneyTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesUtil.init(getApplication());
        setContentView(R.layout.activity_main);
        ScreenUtil.getScreenSize(this);
        ckSingle = findViewById(R.id.ckSingle);
        ckPause = findViewById(R.id.ckPause);
        swWx = findViewById(R.id.swWx);
        tvDevice=findViewById(R.id.tv_device);
        tvTime=findViewById(R.id.tv_wait_time);
        LuckMoneyService.waitWindowTime=SharedPreferencesUtil.getInt(WAIT_WINDOW_TIME,150);
        tvTime.setText(LuckMoneyService.waitWindowTime+"ms");
        tvOpenTime=findViewById(R.id.tv_wait_open_time);
        LuckMoneyService.waitGetMoneyTime=SharedPreferencesUtil.getInt(WAIT_GET_MONEY_TIME,700);
        tvOpenTime.setText(LuckMoneyService.waitGetMoneyTime+"ms");
        swWx.setOnClickListener((v) -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        ckSingle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LuckMoneyService.isSingle = b;
            }
        });

        ckPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LuckMoneyService.isPause = b;
            }
        });

        findViewById(R.id.ll_wait_time).setOnClickListener(v->{
            if(LuckMoneyService.needSetTime==0){
                Toast.makeText(MainActivity.this,"当前不可修改",Toast.LENGTH_SHORT).show();
                return;
            }
            if(LuckMoneyService.waitWindowTime<MAX_WAIT_WINDOW_TIME/4){
                LuckMoneyService.waitWindowTime=LuckMoneyService.waitWindowTime+30;
            }else {
                LuckMoneyService.waitWindowTime=0;
            }
            tvTime.setText(LuckMoneyService.waitWindowTime+"ms");
        });

        findViewById(R.id.ll_wait_open_time).setOnClickListener(v->{
            if(LuckMoneyService.needSetTime==0){
                Toast.makeText(MainActivity.this,"当前不可修改",Toast.LENGTH_SHORT).show();
                return;
            }
            if(LuckMoneyService.waitGetMoneyTime<MAX_WAIT_WINDOW_TIME){
                LuckMoneyService.waitGetMoneyTime=LuckMoneyService.waitGetMoneyTime+100;
            }else {
                LuckMoneyService.waitGetMoneyTime=0;
            }
            tvOpenTime.setText(LuckMoneyService.waitGetMoneyTime+"ms");
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(LuckMoneyService.needSetTime==-1){
            LuckMoneyService.needSetTime=SharedPreferencesUtil.getInt(NEED_SET_TIME,-1);
        }
        swWx.setChecked(AccessbilityUtil.isAccessibilitySettingsOn(this, LuckMoneyService.class));
        if(LuckMoneyService.needSetTime==1){
            tvDevice.setText("当前设备需要进行下面两项时间设置以达到最佳状态，值的大小不会影响抢红包的速度，值越大越能确保抢到红包，但是值太大返回流程可能会出问题，无法继续抢下一个");
        }else if(LuckMoneyService.needSetTime==0){
            tvDevice.setText("当前设备不需要关心下面两项设置");
        }
        SharedPreferencesUtil.save(NEED_SET_TIME,LuckMoneyService.needSetTime);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferencesUtil.save(WAIT_WINDOW_TIME,LuckMoneyService.waitWindowTime);
        SharedPreferencesUtil.save(WAIT_GET_MONEY_TIME,LuckMoneyService.waitGetMoneyTime);
    }
}
