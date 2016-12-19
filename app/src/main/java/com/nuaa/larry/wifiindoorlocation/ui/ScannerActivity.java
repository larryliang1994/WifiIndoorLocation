package com.nuaa.larry.wifiindoorlocation.ui;

import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVCloudQueryResult;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CloudQueryCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogInCallback;
import com.nuaa.larry.wifiindoorlocation.adapter.CollectorListAdapter;
import com.nuaa.larry.wifiindoorlocation.R;
import com.nuaa.larry.wifiindoorlocation.common.ApInfo;
import com.nuaa.larry.wifiindoorlocation.common.Config;
import com.nuaa.larry.wifiindoorlocation.common.Constants;
import com.nuaa.larry.wifiindoorlocation.common.WifiScanner;
import com.nuaa.larry.wifiindoorlocation.widge.CircularAnim;
import com.nuaa.larry.wifiindoorlocation.widge.DividerItemDecoration;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.github.hendraanggrian.circularrevealanimator.CircularRevealAnimator;
import me.drakeet.materialdialog.MaterialDialog;

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

    private WifiScanner wifiScanner;

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

        wifiScanner = new WifiScanner(this);
        wifiScanner.initScanner(this, new WifiScanner.WifiScannerCallback() {
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
                Button button = (Button) view;

                if (scanning) {
                    button.setBackground(getResources().getDrawable(R.drawable.radius_button_not_pressed));
                    button.setText(R.string.scan);
                    scanning = false;
                    wifiScanner.stopScan();
                } else {
                    button.setBackground(getResources().getDrawable(R.drawable.radius_button_pressed));
                    button.setText(R.string.scanning);
                    scanning = true;

                    if (mAdapter == null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                wifiScanner.startScan();
                            }
                        }, 350);
                    } else {
                        wifiScanner.startScan();
                    }
                }

                CircularRevealAnimator.of(this).reveal(button, button.getWidth() / 2, button.getHeight() / 2);

                break;
        }
    }

    private void refresh() {
        mWifiNumTextView.setText(wifiScanner.getWifiList().size() + "");

        int apNum = 0;

        for (ScanResult result : wifiScanner.getWifiList()) {
            if (Config.APName.equals(result.SSID)) {
                apNum++;
            }
        }

        mAPNumTextView.setText(apNum + "");

        if (wifiScanner.getWifiList().size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLinearLayout.setVisibility(View.GONE);
        }

        mAdapter = new CollectorListAdapter(this, wifiScanner.getWifiList());
        mRecyclerView.setAdapter(mAdapter);

//        if (mAdapter == null) {
//
//        } else {
//            mAdapter.notifyDataSetChanged();
//        }
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

            case R.id.clear_data:
                cleanData();
                break;

            case R.id.copy_data:
                copyData();
                break;

            case R.id.distance_in_group:
                distanceInGroup();
                break;

            case R.id.distance_same_level:
                distanceSameLevelDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void distanceSameLevelDialog() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        final EditText editText = (EditText) content.findViewById(R.id.edt_input);

        final MaterialDialog dialog = new MaterialDialog(this);
        dialog.setTitle(getString(R.string.enter_the_location_name))
                .setContentView(content)
                .setCanceledOnTouchOutside(true)
                .setNegativeButton(R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        distanceSameLevel(editText.getText().toString());

                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void distanceSameLevel(String startName) {
        final int name = Integer.valueOf(startName);

        AVQuery<AVObject> query1 = new AVQuery<>("fingerprint");
        query1.whereEqualTo("name", name + 0 + "");

        AVQuery<AVObject> query2 = new AVQuery<>("fingerprint");
        query2.whereEqualTo("name", name + 2 + "");

        AVQuery<AVObject> query3 = new AVQuery<>("fingerprint");
        query3.whereEqualTo("name", name + 4 + "");

        AVQuery<AVObject> query4 = new AVQuery<>("fingerprint");
        query4.whereEqualTo("name", name + 6 + "");

        AVQuery<AVObject> query5 = new AVQuery<>("fingerprint");
        query5.whereEqualTo("name", name + 8 + "");

        AVQuery<AVObject> query = AVQuery.or(
                Arrays.asList(query1, query2, query3, query4, query5));
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                List<AVObject> newList = list;

                List<String> distances = new ArrayList<>();

                for (int i = 0; i < newList.size(); i++) {
                    AVObject outerObject = newList.get(i);
                    int outerName = Integer.valueOf(outerObject.getString("name"));

                    for (int j = 0; j < newList.size(); j++) {
                        AVObject innerObject = list.get(j);
                        int innerName = Integer.valueOf(innerObject.getString("name"));

                        if (outerName == innerName - 2) {
                            String distance = getDistance(
                                    outerObject.getString("info"), innerObject.getString("info"));

                            distances.add("     " + outerName + " <==> " + innerName + "   :   " + distance);
                        }
                    }
                }

                ListView listView = new ListView(ScannerActivity.this);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ScannerActivity.this,
                        android.R.layout.simple_list_item_1, distances);
                listView.setAdapter(arrayAdapter);

                AlertDialog dialog = new AlertDialog.Builder(ScannerActivity.this)
                        .setTitle("距离")
                        .setView(listView)
                        .setCancelable(true)
                        .setPositiveButton(R.string.close, null)
                        .create();
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });
    }

    private void distanceInGroup() {
        AVQuery<AVObject> query = new AVQuery<>("fingerprint");
        query.whereStartsWith("name", "7");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                List<String> distances = new ArrayList<>();

                for (int i = 0; i < list.size(); i++) {
                    AVObject outerObject = list.get(i);
                    String outerName = outerObject.getString("name");
                    for (int j = i + 1; j < list.size(); j++) {
                        AVObject innerObject = list.get(j);
                        String innerName = innerObject.getString("name");
                        if (outerName.equals(innerName)) {
                            String distance = getDistance(
                                    outerObject.getString("info"), innerObject.getString("info"));

                            distances.add("     " + outerName + "  <===>  " + distance);

                            break;
                        }
                    }
                }

                ListView listView = new ListView(ScannerActivity.this);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ScannerActivity.this,
                        android.R.layout.simple_list_item_1, distances);
                listView.setAdapter(arrayAdapter);

                AlertDialog dialog = new AlertDialog.Builder(ScannerActivity.this)
                        .setTitle("同一点两组数据的距离")
                        .setView(listView)
                        .setCancelable(true)
                        .setPositiveButton(R.string.close, null)
                        .create();
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });
    }

    private String getDistance(String info1, String info2) {
        info1 = "{\"info\":" + info1 + "}";
        info2 = "{\"info\":" + info2 + "}";

        try {
            JSONObject jsonObject1 = new JSONObject(info1);
            JSONArray jsonArray1 = jsonObject1.getJSONArray("info");

            JSONObject jsonObject2 = new JSONObject(info2);
            JSONArray jsonArray2 = jsonObject2.getJSONArray("info");

            double distance = 0;

            for (int i = 0; i < jsonArray1.length(); i++) {
                JSONObject object1 = jsonArray1.getJSONObject(i);
                String mac1 = object1.getString("mac");
                double rss1 = object1.getDouble("rss");

                for (int j = 0; j < jsonArray2.length(); j++) {
                    JSONObject object2 = jsonArray2.getJSONObject(j);
                    String mac2 = object2.getString("mac");
                    double rss2 = object2.getDouble("rss");

                    if (mac1.equals(mac2)) {
                        distance += Math.pow(rss1 - rss2, 2);
                        break;
                    }
                }
            }

            distance = Math.sqrt(distance);

            return String.format("%.2f", distance);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return "";
    }

    private void cleanData() {
        AVQuery<AVObject> q = new AVQuery<>("fingerprintcopy");
        q.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                List<AVObject> avObjectList = list;

                for (AVObject avObject : avObjectList) {
                    String name = avObject.getString("name");
                    String info = avObject.getString("info");
                    info = "{\"info\":" + info + "}";

                    try {
                        JSONObject jsonObject = new JSONObject(info);
                        JSONArray jsonArray = jsonObject.getJSONArray("info");

                        List<ApInfo> fingerprint = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String mac = object.getString("mac");
                            double rss = object.getDouble("rss");

                            if (rss > -98) {
                                fingerprint.add(new ApInfo(mac, rss));
                            }
                        }

                        String output = "";
                        output += "[";
                        for (ApInfo apInfo : fingerprint) {
                            output += "{\"mac\":\"" + apInfo.getMacAddress() + "\",\"rss\":\"" + apInfo.getRss() + "\"},";
                        }
                        output = output.substring(0, output.length() - 1);
                        output += "]";

                        AVObject object = new AVObject("fingerprint");
                        object.put("name", name);
                        object.put("info", output);
                        object.saveInBackground();

                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void copyData() {
        AVQuery<AVObject> query = new AVQuery<>("fingerprint");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                List<AVObject> priorityEqualsZeroTodos = list;

                for (AVObject avObject : priorityEqualsZeroTodos) {
                    AVObject object = new AVObject("fingerprintcopy");
                    object.put("name", avObject.getString("name"));
                    object.put("info", avObject.getString("info"));
                    object.saveInBackground();// 保存到服务端
                }
            }
        });
    }

    private List<ApInfo> avobjectToApInfoList(AVObject avObject) {
        List<ApInfo> apInfoList = new ArrayList<>();

        String info = avObject.getString("info");
        info = "{\"info\":" + info + "}";

        try {
            JSONObject jsonObject = new JSONObject(info);
            JSONArray jsonArray = jsonObject.getJSONArray("info");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String mac = object.getString("mac");
                double rss = object.getDouble("rss");

                apInfoList.add(new ApInfo(mac, rss));
            }

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return apInfoList;
    }

    // 统一数据维度
    public List<List<ApInfo>> preProcess(List<List<ApInfo>> dataList) {
        List<List<ApInfo>> newDataList = new ArrayList<>();

        List<String> dimensions = getDimension(dataList);

        for (int groupIndex = 0; groupIndex < dataList.size(); groupIndex++) {
            List<ApInfo> newApInfos = new ArrayList<>();
            for (int dimensionIndex = 0; dimensionIndex < dimensions.size(); dimensionIndex++) {
                String macAddress = dimensions.get(dimensionIndex);
                double rss = -99;

                for (ApInfo apInfo : dataList.get(groupIndex)) {
                    if (apInfo.getMacAddress().equals(macAddress)) {
                        rss = apInfo.getRss();
                        break;
                    }
                }

                newApInfos.add(new ApInfo(macAddress, rss));
            }
            newDataList.add(newApInfos);
        }

        return newDataList;
    }

    // 获取当前所有数据包含的维度
    private List<String> getDimension(List<List<ApInfo>> dataList) {
        List<String> dimensions = new ArrayList<>();
        for (List<ApInfo> apInfos : dataList) {
            for (ApInfo apInfo : apInfos) {
                boolean hasThisDimension = false;

                for (String dimension : dimensions) {
                    if (dimension.equals(apInfo.getMacAddress())) {
                        hasThisDimension = true;
                        break;
                    }
                }

                if (!hasThisDimension) {
                    dimensions.add(apInfo.getMacAddress());
                }
            }
        }

        Log.i("info", "共 " + dimensions.size() + " 个维度");

        for (String dimension : dimensions) {
            Log.i("info", dimension);
        }

        Log.i("info", "-----------------------------------------------");

        return dimensions;
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
        wifiScanner.stopScan();

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
