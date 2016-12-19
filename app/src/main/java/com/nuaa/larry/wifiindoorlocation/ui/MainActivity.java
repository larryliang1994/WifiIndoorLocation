package com.nuaa.larry.wifiindoorlocation.ui;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.text.LoginFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.FunctionCallback;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nuaa.larry.wifiindoorlocation.R;
import com.nuaa.larry.wifiindoorlocation.common.ApInfo;
import com.nuaa.larry.wifiindoorlocation.common.Calculator;
import com.nuaa.larry.wifiindoorlocation.common.Config;
import com.nuaa.larry.wifiindoorlocation.common.Constants;
import com.nuaa.larry.wifiindoorlocation.common.WifiScanner;
import com.nuaa.larry.wifiindoorlocation.widge.CircularAnim;
import com.nuaa.larry.wifiindoorlocation.widge.fabProgressCircle.completefab.CompleteFABListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.appBar)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.iv_cover)
    ImageView mCoverImageView;

    @Bind(R.id.floating_actions)
    FloatingActionsMenu mFloatingActionsMenu;

    @Bind(R.id.tv_location)
    TextView mLocationTextView;

    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        initToolbar();

        mFloatingActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mCoverImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                mCoverImageView.setVisibility(View.GONE);
            }
        });

        mCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCoverImageView.setVisibility(View.GONE);

                mFloatingActionsMenu.collapse();
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
    }

    @OnClick({R.id.fab_scan, R.id.fab_collect, R.id.btn_location})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_scan:
                if (checkWifi()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CircularAnim.fullActivity(MainActivity.this, view)
                                .colorOrImageRes(R.color.colorPrimary)
                                .go(new CircularAnim.OnAnimationEndListener() {
                                    @Override
                                    public void onAnimationEnd() {
                                        startActivity(new Intent(MainActivity.this, ScannerActivity.class));
                                        overridePendingTransition(R.anim.scale_stay, R.anim.scale_stay);
                                    }
                                });
                    } else {
                        startActivity(new Intent(MainActivity.this, ScannerActivity.class));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mFloatingActionsMenu.collapse();
                            }
                        }, 500);
                    }
                }

                break;

            case R.id.fab_collect:
                if (checkWifi()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CircularAnim.fullActivity(MainActivity.this, view)
                                .colorOrImageRes(R.color.colorPrimary)
                                .go(new CircularAnim.OnAnimationEndListener() {
                                    @Override
                                    public void onAnimationEnd() {
                                        startActivity(new Intent(MainActivity.this, CollectorActivity.class));
                                        overridePendingTransition(R.anim.scale_stay, R.anim.scale_stay);
                                    }
                                });
                    } else {
                        startActivity(new Intent(MainActivity.this, CollectorActivity.class));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mFloatingActionsMenu.collapse();
                            }
                        }, 500);
                    }
                }
                break;

            case R.id.btn_location:
                if (checkWifi()) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    final int groupCountIndex = Config.GroupCountIndex;
                    final int collectIntervalIndex = Config.CollectIntervalIndex;
                    final int dependableProbabilityIndex = Config.DependableProbabilityIndex;

                    Config.GroupCountIndex = 0;
                    Config.DependableProbabilityIndex = 0;
                    Config.CollectIntervalIndex = 0;

                    final Calculator calculator = new Calculator(this);

                    calculator.collect(new Calculator.CalculatorCollectCallback() {
                        @Override
                        public void collectProcess(int process) {
                        }

                        @Override
                        public void collectFail() {
                            Toast.makeText(MainActivity.this, "收不到 "+ Config.APName +" 的信号", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void doneCollect() {
                            List<List<ApInfo>> dataList = calculator.preProcess();
                            List<ApInfo> fingerprint = new ArrayList<>();

                            for (int dimension = 0; dimension < calculator.getDimensions().size(); dimension++) {
                                double rss = 0;
                                for (int groupIndex = 0; groupIndex < Constants.GROUP_COUNT[Config.GroupCountIndex]; groupIndex++) {
                                    rss += dataList.get(groupIndex).get(dimension).getRss();
                                }
                                fingerprint.add(new ApInfo(calculator.getDimensions().get(dimension), rss / Constants.GROUP_COUNT[Config.GroupCountIndex]));
                            }

                            Config.GroupCountIndex = groupCountIndex;
                            Config.CollectIntervalIndex = collectIntervalIndex;
                            Config.DependableProbabilityIndex = dependableProbabilityIndex;

                            int apNum = 0;

                            String output = "";
                            output += "{";

                            for (ApInfo apInfo : fingerprint) {
                                if(apInfo.getRss() > -98) {
                                    apNum++;
                                    output += "\'mac" + apNum + "\':\'" + apInfo.getMacAddress() + "\',\'rss" + apNum + "\':\'" + apInfo.getRss() + "\',";
                                }
                            }
                            output += "\'count\':\'" + apNum + "\'";
                            output += "}";

                            Log.i("info", output);

                            Toast.makeText(MainActivity.this, output, Toast.LENGTH_LONG).show();

                            Map<String, String> parameters = new HashMap<>();
                            parameters.put("info", output);

                            AVCloud.callFunctionInBackground("get_location", parameters, new FunctionCallback() {
                                public void done(Object object, AVException e) {
                                    if (e == null) {
                                        if (object instanceof String) {
                                            String result = (String) object;
                                            if (!"".equals(result)) {
                                                mLocationTextView.setText(result);
                                            }
                                        }

                                        Log.i("info", object.toString());

                                        mProgressBar.setVisibility(View.INVISIBLE);
                                    } else {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }

                break;
        }
    }

    private boolean checkWifi() {
        if (!Config.Scanner.getWifiStatus()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.turning_on_wifi)
                    .setPositiveButton(R.string.turn_on, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Config.Scanner.openWifi();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(true);

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (mFloatingActionsMenu.isExpanded()) {
            mFloatingActionsMenu.collapse();
        } else {
            super.onBackPressed();
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
