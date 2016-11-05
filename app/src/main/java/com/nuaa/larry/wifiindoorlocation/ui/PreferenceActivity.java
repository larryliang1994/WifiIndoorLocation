package com.nuaa.larry.wifiindoorlocation.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.nuaa.larry.wifiindoorlocation.App;
import com.nuaa.larry.wifiindoorlocation.R;
import com.nuaa.larry.wifiindoorlocation.common.Config;
import com.nuaa.larry.wifiindoorlocation.common.Constants;
import com.nuaa.larry.wifiindoorlocation.common.UtilBox;
import com.nuaa.larry.wifiindoorlocation.widge.RippleView;
import com.umeng.analytics.MobclickAgent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by liang on 23/10/2016.
 */

public class PreferenceActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.sp_group_count)
    Spinner mGroupCountSpinner;

    @Bind(R.id.sp_collect_interval)
    Spinner mCollectIntervalSpinner;

    @Bind(R.id.sp_dependable_rate)
    Spinner mDependableRateSpinner;

    @Bind(R.id.tv_ap_name)
    TextView mApNameTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preference);

        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        initToolbar();

        mApNameTextView.setText(Config.APName);

        mGroupCountSpinner.setSelection(Config.GroupCountIndex);
        mCollectIntervalSpinner.setSelection(Config.CollectIntervalIndex);
        mDependableRateSpinner.setSelection(Config.DependableProbabilityIndex);

        mGroupCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Config.GroupCountIndex = i;

                SharedPreferences.Editor editor = App.sp.edit();
                editor.putInt(Constants.SP_KEY_GROUP_COUNT, i);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

       mCollectIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Config.CollectIntervalIndex = i;

                SharedPreferences.Editor editor = App.sp.edit();
                editor.putInt(Constants.SP_KEY_COLLECT_INTERVAL, i);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mDependableRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Config.DependableProbabilityIndex = i;

                SharedPreferences.Editor editor = App.sp.edit();
                editor.putInt(Constants.SP_KEY_DEPENDABLE_RATE, i);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.rv_count, R.id.rv_collect_interval, R.id.rv_dependable_rate})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rv_count:
                mGroupCountSpinner.performClick();
                break;

            case R.id.rv_collect_interval:
                mCollectIntervalSpinner.performClick();
                break;

            case R.id.rv_dependable_rate:
                mDependableRateSpinner.performClick();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
