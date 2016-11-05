package com.nuaa.larry.wifiindoorlocation.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.nuaa.larry.wifiindoorlocation.R;
import com.nuaa.larry.wifiindoorlocation.common.ApInfo;
import com.nuaa.larry.wifiindoorlocation.common.Calculator;
import com.nuaa.larry.wifiindoorlocation.common.Config;
import com.nuaa.larry.wifiindoorlocation.common.Constants;
import com.nuaa.larry.wifiindoorlocation.common.UtilBox;
import com.nuaa.larry.wifiindoorlocation.widge.WaterWaveView;
import com.nuaa.larry.wifiindoorlocation.widge.fabProgressCircle.FABProgressCircle;
import com.nuaa.larry.wifiindoorlocation.widge.fabProgressCircle.listeners.FABProgressListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.hendraanggrian.circularrevealanimator.CircularRevealAnimator;
import io.github.hendraanggrian.circularrevealanimator.PathPoint;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by liang on 28/10/2016.
 */

public class CollectorActivity extends AppCompatActivity {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tv_collected_num)
    TextView mCollectedNumTextView;

    @Bind(R.id.fabProgressCircle)
    FABProgressCircle mFabProgressCircle;

    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    @Bind(R.id.appBar)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.ll_extra_options)
    LinearLayout mExtraOptionsLinearLayout;

    @Bind(R.id.ll_options)
    LinearLayout mOptionsLinearLayout;

    @Bind(R.id.tv_dimension)
    TextView mDimensionTextView;

    @Bind(R.id.tv_available)
    TextView mAvailableTextView;

    @Bind(R.id.ibtn_copy)
    ImageButton mCopyImageButton;

    @Bind(R.id.ibtn_watch)
    ImageButton mWatchImageButton;

    @Bind(R.id.ibtn_more)
    ImageButton mMoreImageButton;

    @Bind(R.id.water_wave_view)
    WaterWaveView mWaterWaveView;

    private Calculator mCalculator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_collector);

        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        initToolbar();

        mCalculator = new Calculator();

        mWaterWaveView.setmWaterLevel(0);

        mCollectedNumTextView.setText("点击右侧开始采集");

        mFabProgressCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabProgressCircle.setClickable(false);

                mFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.stop));

                mFabProgressCircle.show();

                mCalculator.clear();

                mCalculator.collect(new Calculator.CalculatorCollectCallback() {
                    @Override
                    public void collectProcess(int process) {
                        mCollectedNumTextView.setText("正在采集第 " + process + " 组数据");

                        mWaterWaveView.setmWaterLevel((int) (process * 1.0 / Constants.GROUP_COUNT[Config.GroupCountIndex] * 100));
                    }

                    @Override
                    public void doneCollect() {
                        mFabProgressCircle.beginFinalAnimation();
                    }

                    @Override
                    public void collectFail() {
                        mFabProgressCircle.hide();

                        new AlertDialog.Builder(CollectorActivity.this)
                                .setTitle("没有采集到任何wifi信息")
                                .setCancelable(true)
                                .setPositiveButton("关闭", null)
                                .show();

                        mCollectedNumTextView.setText("点击右侧开始采集");

                        mFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.play));

                        mFabProgressCircle.setClickable(true);

                        mCalculator.clear();

                        mWaterWaveView.setmWaterLevel(0);
                    }
                });
            }
        });

        mFabProgressCircle.attachListener(new FABProgressListener() {
            @Override
            public void onFABProgressAnimationEnd() {
                mCollectedNumTextView.setText("采集完成");

                mCalculator.process();

                double validRate = mCalculator.validCount * 1.0
                        / (Constants.GROUP_COUNT[Config.GroupCountIndex] * mCalculator.getDimensions().size()) * 100;

                mDimensionTextView.setText(mCalculator.getDimensions().size() + "");
                mAvailableTextView.setText((int) validRate + "");

                showCompleteView(true);
            }
        });

        mAppBarLayout.setEnabled(false);

        // 开始执行
        mWaterWaveView.startWave();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void showCompleteView(boolean show) {
        float waterWaveAlphaFrom = show ? 1.0f : 0.0f;
        float waterWaveAlphaTo = show ? 0.0f : 1.0f;
        float waterWaveScaleXFrom = show ? 1.0f : 0.8f;
        float waterWaveScaleXTo = show ? 0.8f : 1.0f;
        float waterWaveScaleYFrom = show ? 1.0f : 0.8f;
        float waterWaveScaleYTo = show ? 0.8f : 1.0f;

        float optionsLinearLayoutAlphaFrom = show ? 0.0f : 1.0f;
        float optionsLinearLayoutAlphaTo = show ? 1.0f : 0.0f;
        float optionsLinearLayoutScaleXFrom = show ? 0.8f : 1.0f;
        float optionsLinearLayoutScaleXTo = show ? 1.0f : 0.8f;
        float optionsLinearLayoutScaleYFrom = show ? 0.8f : 1.0f;
        float optionsLinearLayoutScaleYTo = show ? 1.0f : 0.8f;

        float extraOptionsLinearLayoutAlphaFrom = show ? 0.0f : 1.0f;
        float extraOptionsLinearLayoutAlphaTo = show ? 1.0f : 0.0f;
        float extraOptionsLinearLayoutTranslationYFrom =
                show ? UtilBox.dip2px(CollectorActivity.this, mExtraOptionsLinearLayout.getHeight()) : 0;
        float extraOptionsLinearLayoutTranslationYTo =
                show ? 0 : UtilBox.dip2px(CollectorActivity.this, mExtraOptionsLinearLayout.getHeight());

        int waterWaveAnimatorSetDelay = show ? 0 : 300;
        int optionsLinearLayoutAnimatorSetDelay = show ? 300 : 0;

        ObjectAnimator waterWaveAlphaAnimator =
                ObjectAnimator.ofFloat(mWaterWaveView, "alpha",
                        waterWaveAlphaFrom, waterWaveAlphaTo).setDuration(500);
        ObjectAnimator waterWaveScaleXAnimator =
                ObjectAnimator.ofFloat(mWaterWaveView, "scaleX",
                        waterWaveScaleXFrom, waterWaveScaleXTo).setDuration(500);
        ObjectAnimator waterWaveScaleYAnimator =
                ObjectAnimator.ofFloat(mWaterWaveView, "scaleY",
                        waterWaveScaleYFrom, waterWaveScaleYTo).setDuration(500);

        ObjectAnimator optionsLinearLayoutAlphaAnimator =
                ObjectAnimator.ofFloat(mOptionsLinearLayout, "alpha",
                        optionsLinearLayoutAlphaFrom, optionsLinearLayoutAlphaTo).setDuration(500);
        ObjectAnimator optionsLinearLayoutScaleXAnimator =
                ObjectAnimator.ofFloat(mOptionsLinearLayout, "scaleX",
                        optionsLinearLayoutScaleXFrom, optionsLinearLayoutScaleXTo).setDuration(500);
        ObjectAnimator optionsLinearLayoutScaleYAnimator =
                ObjectAnimator.ofFloat(mOptionsLinearLayout, "scaleY",
                        optionsLinearLayoutScaleYFrom, optionsLinearLayoutScaleYTo).setDuration(500);

        ObjectAnimator extraOptionsLinearLayoutAlphaAnimator =
                ObjectAnimator.ofFloat(mExtraOptionsLinearLayout, "alpha",
                        extraOptionsLinearLayoutAlphaFrom, extraOptionsLinearLayoutAlphaTo).setDuration(500);
        ObjectAnimator extraOptionsLinearLayoutTranslationYAnimator =
                ObjectAnimator.ofFloat(mExtraOptionsLinearLayout, "translationY",
                        extraOptionsLinearLayoutTranslationYFrom,
                        extraOptionsLinearLayoutTranslationYTo).setDuration(500);

        AnimatorSet waterWaveAnimatorSet = new AnimatorSet();
        waterWaveAnimatorSet.setDuration(500);
        waterWaveAnimatorSet.setStartDelay(waterWaveAnimatorSetDelay);
        waterWaveAnimatorSet.playTogether(waterWaveAlphaAnimator,
                waterWaveScaleXAnimator, waterWaveScaleYAnimator);

        AnimatorSet optionsLinearLayoutAnimatorSet = new AnimatorSet();
        optionsLinearLayoutAnimatorSet.setDuration(500);
        optionsLinearLayoutAnimatorSet.setStartDelay(optionsLinearLayoutAnimatorSetDelay);
        optionsLinearLayoutAnimatorSet.playTogether(optionsLinearLayoutAlphaAnimator,
                optionsLinearLayoutScaleXAnimator, optionsLinearLayoutScaleYAnimator);

        AnimatorSet extraOptionsLinearLayoutAnimatorSet = new AnimatorSet();
        extraOptionsLinearLayoutAnimatorSet.setDuration(500);
        extraOptionsLinearLayoutAnimatorSet.playTogether(
                extraOptionsLinearLayoutAlphaAnimator, extraOptionsLinearLayoutTranslationYAnimator);

        waterWaveAnimatorSet.start();
        optionsLinearLayoutAnimatorSet.start();
        extraOptionsLinearLayoutAnimatorSet.start();
    }

    @OnClick({R.id.btn_upload, R.id.btn_recollect, R.id.ibtn_copy, R.id.ibtn_watch, R.id.ibtn_more})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibtn_copy:
                String[] items = {"复制所有可信数据", "复制已处理的指纹"};
                new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doCopy(i);
                    }
                }).setCancelable(true).show();
                break;

            case R.id.btn_recollect:
                doRecollect();
                break;

            case R.id.ibtn_watch:
                doWatch();
                break;

            case R.id.btn_upload:
                View content = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
                final EditText editText = (EditText) content.findViewById(R.id.edt_input);
                final TextView textView = (TextView) content.findViewById(R.id.tv_msg);

                final MaterialDialog uploadDialog = new MaterialDialog(CollectorActivity.this);
                uploadDialog.setTitle("输入该点名称")
                        .setContentView(content)
                        .setCanceledOnTouchOutside(true)
                        .setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                uploadDialog.dismiss();
                            }
                        })
                        .setPositiveButton("上传", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                textView.setVisibility(View.VISIBLE);
                                if (editText.getText() == null || "".equals(editText.getText().toString())) {
                                    textView.setText("名称不能为空");
                                } else {
                                    textView.setText("上传中...");
                                    doUpload(uploadDialog, textView, editText.getText().toString());
                                }
                            }
                        })
                        .show();

                break;

            case R.id.ibtn_more:
                Toast.makeText(CollectorActivity.this, "我也不知道这里要放什么\n反正就先占个坑", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void doUpload(final MaterialDialog dialog, final TextView textView, String name) {
        String output = "";
        output += "[";
        for (ApInfo apInfo : mCalculator.getFingerprint()) {
            output += "{\"mac\":\"" + apInfo.getMacAddress() + "\",\"rss\":\"" + apInfo.getRss() + "\"},";
        }
        output = output.substring(0, output.length() - 1);
        output += "]";

        AVObject avObject = new AVObject("fingerprint");
        avObject.put("name", name);
        avObject.put("info", output);
        avObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(final AVException e) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (e == null) {
                            Toast.makeText(CollectorActivity.this, "上传完成", Toast.LENGTH_SHORT).show();
                        } else {
                            textView.setText("上传出错，请重试");
                            e.printStackTrace();
                        }
                    }
                }, 500);
            }
        });
    }

    private void doCopy(int which) {
        String output = "";

        output += "[";

        if (which == 0) {
            for (List<ApInfo> apInfos : mCalculator.getDependableProbabilityValues()) {
                output += "[";
                for (ApInfo apInfo : apInfos) {
                    output += "{\"mac\":\"" + apInfo.getMacAddress() + "\",\"rss\":\"" + apInfo.getRss() + "\"},";
                }
                output = output.substring(0, output.length() - 1);
                output += "],";
            }
        } else {
            for (ApInfo apInfo : mCalculator.getFingerprint()) {
                output += "{\"mac\":\"" + apInfo.getMacAddress() + "\",\"rss\":\"" + apInfo.getRss() + "\"},";
            }
        }
        output = output.substring(0, output.length() - 1);
        output += "]";

        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(output);

        Toast.makeText(CollectorActivity.this,
                "已将" + (which == 0 ? "所有可信" : "指纹") + "数据复制到剪贴板", Toast.LENGTH_LONG).show();
    }

    private void doRecollect() {
        showCompleteView(false);

        mWaterWaveView.setmWaterLevel(0);

        mCalculator.clear();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCollectedNumTextView.setText("点击右侧开始采集");

                mFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.play));

                mFabProgressCircle.setClickable(true);
            }
        }, 800);
    }

    private void doWatch() {
        List<String> output = new ArrayList<>();
        for(ApInfo apInfo: mCalculator.getFingerprint()) {
            output.add(apInfo.getMacAddress() + "  <===>  " + String.format("%.2f", apInfo.getRss()));
        }

        ListView listView = new ListView(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, output);
        listView.setAdapter(arrayAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("该点Wifi指纹")
                .setView(listView)
                .setCancelable(true)
                .setPositiveButton("关闭", null)
                .create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collector, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.preference:
                startActivity(new Intent(this, PreferenceActivity.class));
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
