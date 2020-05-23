package com.map.mymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.TileOverlayOptions;
import com.map.mymap.db.SQLdm;
import com.map.mymap.ui.BaseActivity;
import com.map.mymap.ui.StatusBarUtil;

import java.util.Arrays;
import java.util.Calendar;

import static com.map.mymap.MainActivity.ALT_HEATMAP_GRADIENT;

public class HeatDate extends BaseActivity implements View.OnClickListener {

    private MapView mMapView;
    private AMap mMap;
    private LinearLayout timeLayout;
    private RelativeLayout timepickerrl;
    private int count = 0;
    private SQLiteDatabase db;
    private SQLdm s = new SQLdm();
    private TextView timeHour;
    private TextView timeMin;
    private TextView back;
    private TimePicker timePicker;
    private Button btnTimeSure;
    private RecyclerView heatDataRecycler;

    public String heatTime = "201810030000";
    public String heatHour = "00";
    public String heatMinute = "00";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_heat_date);
        init();
        initUI();
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.heatMapView);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (mMap == null) {
            mMap = mMapView.getMap();
        }

        LatLng latLng = new LatLng(41.677287,123.465009);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        initDataAndHeatMap();
    }

    private void initUI() {
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);
    }

    private void init() {
        timeLayout = (LinearLayout)findViewById(R.id.time_Layout);
        timePicker = (TimePicker)findViewById(R.id.timepicker);
        timepickerrl = (RelativeLayout)findViewById(R.id.timepickerll);
        timeHour = (TextView)findViewById(R.id.time_hour);
        timeMin = (TextView)findViewById(R.id.time_min);
        back = (TextView)findViewById(R.id.back_to_map);
        btnTimeSure = (Button)findViewById(R.id.btntimesure);
        heatDataRecycler = (RecyclerView)findViewById(R.id.recycler_heat_data);

        getTime();

        timeLayout.setOnClickListener(this);
        back.setOnClickListener(this);
        btnTimeSure.setOnClickListener(this);

        timeHour.setText(heatHour);
        timeMin.setText(heatMinute);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) { //时间选择器变化
                if (hour < 10) {
                    heatHour = "0" + String.valueOf(hour);
                } else {
                    heatHour = String.valueOf(hour);
                }
                if (minute < 10) {
                    heatMinute = "0" + String.valueOf(minute);
                } else {
                    heatMinute = String.valueOf(minute);
                }

                heatTime = heatHour + heatMinute;
            }
        });
    }

    private void getTime() {
        Calendar calendar = Calendar.getInstance();
        heatHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        heatMinute = String.valueOf(calendar.get(Calendar.MINUTE));
        heatTime = heatHour + heatMinute;
    }

    private void initDataAndHeatMap() {
        if(count == 0) {
            db = s.openDatabase(getApplicationContext());
            Cursor cursor = db.rawQuery("select * from heat where timestamp like ?", new String[]{"%"+heatTime+"%"});
            LatLng[] latlngs = new LatLng[cursor.getCount()];
            Double latlng = 0.0;
            Double longitude = 0.0;
            cursor.moveToFirst();
            for(int i = 0;i < cursor.getCount();i++){
                latlng = cursor.getDouble(cursor.getColumnIndex("latitude"));
                longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                latlngs[i] = new LatLng(latlng,longitude);
                cursor.moveToNext();
            }
            cursor.close();

            // 第二步： 构建热力图 TileProvider
            HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
            builder.data(Arrays.asList(latlngs)) // 设置热力图绘制的数据
                    .gradient(ALT_HEATMAP_GRADIENT); // 设置热力图渐变，有默认值 DEFAULT_GRADIENT，可不设置该接口
            // Gradient 的设置可见参考手册
            // 构造热力图对象
            HeatmapTileProvider heatmapTileProvider = builder.build();

            // 第三步： 构建热力图参数对象
            TileOverlayOptions tileOverlayOptions = new TileOverlayOptions();
            tileOverlayOptions.tileProvider(heatmapTileProvider); // 设置瓦片图层的提供者

            // 第四步： 添加热力图
            mMap.addTileOverlay(tileOverlayOptions);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_to_map:
                finish();
                break;
            case R.id.time_Layout:
                timepickerrl.setVisibility(View.VISIBLE);
                break;
            case R.id.btntimesure:
                mMap.clear();
                initDataAndHeatMap();
                timepickerrl.setVisibility(View.GONE);
                timeHour.setText(heatHour);
                timeMin.setText(heatMinute);
                break;
            default:
                break;
        }

    }
}
