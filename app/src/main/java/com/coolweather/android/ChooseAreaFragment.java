package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private final static String TAG = "aa";

    //当前选页面的级别
    //省级
    public static final int LEVEL_PROVINCE = 0;
    //市级
    public static final int LEVEL_CITY = 1;
    //县级
    public static final int LEVEL_COUNTY = 2;

    //等待对话框
    private ProgressDialog progressDialog;

    //标题
    private TextView titleText;
    //返回按钮
    private Button backButton;
    //数据列表
    private ListView listView;
    //数据列表 listView 的 adapter
    private ArrayAdapter<String> adapter;
    //数据列表 listView 的 adapter 的集合数据
    private List<String> dataList = new ArrayList<>();

    //从数据库中查询得来的 省 列表
    private List<Province> provinceList;
    //从数据库中查询得来的 市 列表
    private List<City> cityList;
    //从数据库中查询得来的 县 列表
    private List<County> countyList;

    //选中的省
    private Province selectedProvince;
    //选中的市
    private City selectedCity;

    //当前选中的级别
    private int currentLevel;

    /**
     * 当 choose_area view 创建时执行，初始化一些变量
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //获取当前 view 布局（choose_area.xml）
        View view = inflater.inflate(R.layout.choose_area, container, false);
        //获取 标题 布局
        titleText = view.findViewById(R.id.title_text);
        //获取 返回按钮 布局
        backButton = view.findViewById(R.id.back_button);
        //获取 列表 布局
        listView = view.findViewById(R.id.list_view);
        //初始化 listView 的 adapter
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        //给 listView 列表布局设置 adapter
        listView.setAdapter(adapter);
        return view;
    }

    /**
     * 当活动创建时执行
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //给 listView 的每项设置 adapter 的点击监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //如果当前 选中级别 等于 省级
                if (currentLevel == LEVEL_PROVINCE) {
                    //从省数据集合中获取当前省数据
                    selectedProvince = provinceList.get(position);
                    //查询当前省下的市数据
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    //如果当前 选中级别 等于 市级
                    //从市数据集合中获取当前市数据
                    selectedCity = cityList.get(position);
                    //查询当前市下县数据
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        //设置返回按钮的监听
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果当前 选中级别 是 县级
                if (currentLevel == LEVEL_COUNTY) {
                    //查询当前省下的市数据
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    //如果当前 选中级别 是 市级
                    //查询所有省的数据
                    queryProvinces();
                }
            }
        });
        //默认查询所有省的数据
        queryProvinces();
    }

    /**
     * 查询所有省的数据，优先从数据库中查询，如果没有就从服务器中查询
     */
    private void queryProvinces() {
        //设置页面标题
        titleText.setText("中国");
        //因为是省级所以不显示返回按钮
        backButton.setVisibility(View.GONE);
        //从数据库中查询 province 省表中的所有数据
        provinceList = LitePal.findAll(Province.class);
        //如果有数据
        if (provinceList.size() > 0) {
            //清除 数据集合 防止重复
            dataList.clear();
            //遍历每个省数据
            for (Province province : provinceList) {
                //把省名称添加到 dataList 中
                dataList.add(province.getProvinceName());
            }
            //因为 dataList 是 ListView adapter 中使用的数据，所以当 dataList 发生改变后要通知 adapter 数据发生改变了。
            adapter.notifyDataSetChanged();
            //默认设置 listView 中 adapter 默认选中项为 第 1 项
            listView.setSelection(0);
            //设置当前 选中级别 为 省级
            currentLevel = LEVEL_PROVINCE;
        } else {
            //如果数据库中没有 省 数据
            String address = "http://guolin.tech/api/china/";
            //走从服务器查询方法
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询当前 省 下所有的 市 数据
     */
    private void queryCities() {
        //设置 市 页面标题为当前选中省数据的 省 名称
        titleText.setText(selectedProvince.getProvinceName());
        //因为市级是第二级所以要显示返回按钮
        backButton.setVisibility(View.VISIBLE);
        //查询当前省对应的市数据（不能查询全部市，所以用 where 查询，条件是查询数据库中市表里 privinceid 字段等于当前选中省的 id）
        cityList =
                LitePal.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        //如果有数据
        if (cityList.size() > 0) {
            //清除 数据集合 防止重复
            dataList.clear();
            //遍历每个市数据
            for (City city : cityList) {
                //把市名称添加到 dataList 中
                dataList.add(city.getCityName());
            }
            //因为 dataList 是 ListView adapter 中使用的数据，所以当 dataList 发生改变后要通知 adapter 数据发生改变了。
            adapter.notifyDataSetChanged();
            //默认设置 listView 中 adapter 默认选中项为 第 1 项
            listView.setSelection(0);
            //设置当前 选中级别 为 省级
            currentLevel = LEVEL_CITY;
        } else {
            //如果数据库中没有 市 数据
            //获取当前选中省的代号，用于从服务器接口查询
            int provinceCode = selectedProvince.getProvinceCode();
            //组装地址
            String address = "http://guolin.tech/api/china/" + provinceCode;
            //走从服务器查询方法
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList =
                LitePal.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            //走从服务器查询方法
            queryFromServer(address, "county");
        }
    }

    /**
     * 从服务器查询地区数据
     *
     * @param address 接口地址 http://guolin.tech/api/china http://guolin.tech/api/china/16
     *                http://guolin.tech/api/china/16/115
     * @param type    类别 province city county
     */
    private void queryFromServer(String address, final String type) {
        //因为是子线程查询（耗时操作）所以显示一下等待对话框
        showProgressDialog();
        //发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            //当前成功响应结果
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //获取响应数据
                String responseText = response.body().string();
                Log.d(TAG, "onResponse: responseText: " + responseText);
                //声明数据库保存结果
                boolean result = false;
                //如果请求类型是 省
                if ("province".equals(type)) {
                    //把省数据保存到数据库中并返回是否成功（下面以此类推）
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                //如果保存成功
                if (result) {
                    //执行在子线程中修改 UI 界面
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭 progress
                            closeProgressDialog();
                            //如果类型为 省 从数据库查询 省 并更新界面（下面以此类推）
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            //如果请求失败
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //关闭 progress 并给出错误提示
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    //显示 progress 对话框
    private void showProgressDialog() {
        //判断一下防止重复创建
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            //设置显示信息
            progressDialog.setMessage("加载中...");
            //设置点击遮罩不关闭
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭 progreee 对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
