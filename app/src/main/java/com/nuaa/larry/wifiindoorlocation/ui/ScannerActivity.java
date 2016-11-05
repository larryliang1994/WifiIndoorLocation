package com.nuaa.larry.wifiindoorlocation.ui;

import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nuaa.larry.wifiindoorlocation.adapter.CollectorListAdapter;
import com.nuaa.larry.wifiindoorlocation.R;
import com.nuaa.larry.wifiindoorlocation.common.Config;
import com.nuaa.larry.wifiindoorlocation.common.WifiScanner;
import com.nuaa.larry.wifiindoorlocation.widge.CircularAnim;
import com.nuaa.larry.wifiindoorlocation.widge.DividerItemDecoration;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.github.hendraanggrian.circularrevealanimator.CircularRevealAnimator;

/**
 * Created by liang on 22/10/2016.
 */

public class ScannerActivity extends AppCompatActivity {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.tv_wifi_num)
    TextView mWifiNumTextView;

    @Bind(R.id.tv_ap_num)
    TextView mAPNumTextView;

    @Bind(R.id.tv_ap_name)
    TextView mAPNameTextView;

    @Bind(R.id.ll_empty)
    LinearLayout mEmptyLinearLayout;

    private CollectorListAdapter mAdapter;

    private boolean scanning = false;
    private boolean useFilter = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);

        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        initToolbar();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAPNameTextView.setText(Config.APName);

        Config.Scanner.initScanner(this, new WifiScanner.WifiScannerCallback() {
            @Override
            public void success() {
                refresh();
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick({R.id.btn_scan})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                Button button = (Button)view;

                if (scanning) {
                    button.setBackground(getResources().getDrawable(R.drawable.radius_button_not_pressed));
                    button.setText("扫描");
                    scanning = false;
                    Config.Scanner.stopScan();
                } else {
                    button.setBackground(getResources().getDrawable(R.drawable.radius_button_pressed));
                    button.setText("扫描中");
                    scanning = true;

                    if (mAdapter == null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Config.Scanner.startScan();
                            }
                        }, 350);
                    } else {
                        Config.Scanner.startScan();
                    }
                }

                CircularRevealAnimator.of(this).reveal(button, button.getWidth() / 2, button.getHeight() / 2);

                break;
        }
    }

    private void refresh() {
        mWifiNumTextView.setText(Config.Scanner.getWifiList().size() + "");

        int apNum = 0;

        for (ScanResult result: Config.Scanner.getWifiList()) {
            if (Config.APName.equals(result.SSID)) {
                apNum++;
            }
        }

        mAPNumTextView.setText(apNum + "");

        if (Config.Scanner.getWifiList().size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLinearLayout.setVisibility(View.GONE);
        }

        if (mAdapter == null) {
            mAdapter = new CollectorListAdapter(this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scanner, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.show_all:
                break;

            case R.id.show_ap:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finish();
            overridePendingTransition(R.anim.scale_stay, R.anim.scale_stay);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Config.Scanner.stopScan();

        super.onDestroy();
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
