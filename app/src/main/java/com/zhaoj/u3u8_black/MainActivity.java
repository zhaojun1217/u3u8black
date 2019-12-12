package com.zhaoj.u3u8_black;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cw.serialportsdk.cw;

import com.cw.idcardsdk.AsyncParseSFZ;
import com.cw.idcardsdk.ParseSFZAPI;

public class MainActivity extends AppCompatActivity {

    private AsyncParseSFZ asyncParseSFZ;
    private TextView tvBtn, tvShow, tvCloseBtn;
    private CheckBox cbOpen;
    Handler mHandler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            asyncParseSFZ.readSFZ(ParseSFZAPI.THIRD_GENERATION_CARD);
        }
    };
    /**
     * 是否是连续读取
     */
    private boolean isSequentialRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBtn = findViewById(R.id.tvBtn);
        tvCloseBtn = findViewById(R.id.tvCloseBtn);
        tvShow = findViewById(R.id.tvShow);
        cbOpen = findViewById(R.id.cbOpen);

        tvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncParseSFZ.readSFZ(ParseSFZAPI.THIRD_GENERATION_CARD);
            }
        });
        cbOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSequentialRead = true;
                } else {
                    isSequentialRead = false;
                }
            }
        });
        tvCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacksAndMessages(null);
            }
        });

        //1.第一步：实例化
        asyncParseSFZ = new AsyncParseSFZ(getMainLooper(), MainActivity.this);
        //2.第二步：设置读卡监听回调接口,
        //该读卡回调接口需要在合适的地方调用        asyncParseSFZ.readSFZ();
        asyncParseSFZ.setOnReadSFZListener(new AsyncParseSFZ.OnReadSFZListener() {
            @Override
            public void onReadSuccess(ParseSFZAPI.People people) {
                //读卡成功
                tvShow.setText(people.getPeopleIDCode() + people.getPeopleName());
                refresh(isSequentialRead);
            }

            @Override
            public void onReadFail(int confirmationCode) {
                //读卡失败
                tvShow.setText("读卡失败!");
                refresh(isSequentialRead);
            }
        });
    }


    private void refresh(boolean isSequentialRead) {
        if (!isSequentialRead) {
            return;
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(task, 200);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        //2.打开身份证串口
        if (asyncParseSFZ != null) {
            asyncParseSFZ.openIDCardSerialPort(cw.getDeviceModel());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onPause() {
        super.onPause();
        //最后一步：关闭身份证串口
        if (asyncParseSFZ != null) {
            asyncParseSFZ.closeIDCardSerialPort(cw.getDeviceModel());
        }
    }
}
