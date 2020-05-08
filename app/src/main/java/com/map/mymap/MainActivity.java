package com.map.mymap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Gradient;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.map.mymap.db.SQLdm;
import com.map.mymap.overlay.PoiOverlay;
import com.map.mymap.route.navActivity;
import com.map.mymap.ui.StatusBarUtil;
import com.map.mymap.util.AMapUtil;
import com.map.mymap.util.ToastUtil;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends Activity implements View.OnClickListener, AMapLocationListener,
        AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, TextWatcher,
        PoiSearch.OnPoiSearchListener, InputtipsListener, TextView.OnEditorActionListener {

    private AMap mMap;

    private UiSettings uiSettings;

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),
            Color.argb(255 / 3 * 2, 0, 255, 0),
            Color.rgb(125, 191, 0),
            Color.rgb(185, 71, 0),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = { 0.0f,
            0.10f, 0.20f, 0.60f, 1.0f };
    private int count = 0;

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(
            ALT_HEATMAP_GRADIENT_COLORS, ALT_HEATMAP_GRADIENT_START_POINTS);


    private ImageButton ib_now;
    private ImageButton ib_layer;
    private ImageButton ib_nav;
    private ImageButton ibt_busmode;
    private ImageButton btheat,btroadc;

    private Button searchbtn;
    private Button timesurebtn;

    private TextView timeText;

    private RelativeLayout timePickerRl;


    private Marker locationMarker;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;

    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;


    private MyLocationStyle myLocationStyle;

    public SQLiteDatabase database;

    public Double localatLng;
    public Double Longitude;
    public Double endLatlng;
    public Double endLongitude;
    public String citycode;
    public String keyWord;
    public String heatHour = "00";
    public String heatMinute = "00";
    public String heatTime = "201810030000";
    private LatLonPoint lp;
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch poiSearch;
    private List<PoiItem> poiItems;// poi数据
    private PoiSearch.Query query;
    private AutoCompleteTextView searchedit;// 输入搜索关键字
    private ProgressDialog progDialog = null;// 搜索时进度条
    private static final int NOT_NOTICE = 2;//如果勾选了不再询问
    private AlertDialog alertDialog;
    private AlertDialog mDialog;
    private TextView title;
    private TimePicker timePicker;


    private Boolean btsate_use = false;
    private Boolean btheat_use = false;
    private Boolean btroadc_use = false;
    private Boolean busmode_used = false;
    private int maptype;
    public Context mContext;
    private SQLdm s = new SQLdm();
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        myRequetPermission();
        setUpMapIfNeeded();
        setupMap();
        getTime();

    }

    private void myRequetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else {
            //Toast.makeText(this,"您已经申请了权限!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {//选择了“始终允许”
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])){//用户选择了禁止不再询问

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("请授权应用相关权限")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (mDialog != null && mDialog.isShowing()) {
                                            mDialog.dismiss();
                                        }
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);//注意就是"package",不用改成自己的包名
                                        intent.setData(uri);
                                        startActivityForResult(intent, NOT_NOTICE);
                                    }
                                });
                        mDialog = builder.create();
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.show();



                    }else {//选择禁止
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用我们的app哦")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (alertDialog != null && alertDialog.isShowing()) {
                                            alertDialog.dismiss();
                                        }
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                      }
                                });
                        alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }

                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==NOT_NOTICE){
            myRequetPermission();//由于不知道是否选择了允许所以需要再次判断
        }
    }


    private void getTime() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR) < 10){
            heatHour = "0" + String.valueOf(calendar.get(Calendar.HOUR));
        }else {
            heatHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }
        if (calendar.get(Calendar.MINUTE) < 10){
            heatMinute = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        }else {
            heatMinute = String.valueOf(calendar.get(Calendar.MINUTE));
        }

        heatTime = heatHour + heatMinute;
    }


    private void initDataAndHeatMap() {
        if(count == 0) {
            db =s.openDatabase(getApplicationContext());
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


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbnav:
                Intent intentnav = new Intent(MainActivity.this, navActivity.class);
                intentnav.putExtra("localatLng",localatLng);
                intentnav.putExtra("Longitude",Longitude);
                intentnav.putExtra("locacitycode",citycode);
                ib_layer.setImageResource(R.drawable.layer);
                startActivity(intentnav);
                break;

            case R.id.imbnow:
                mMap.clear();
                initLocation();
                timePickerRl.setVisibility(View.GONE);
                timeText.setVisibility(View.GONE);
                if (mLocationClient != null) {
                    mLocationClient.startLocation();
                }
                ib_layer.setImageResource(R.drawable.layer);
                btheat_use = false;
                btheat.setImageResource(R.drawable.offheat);

                break;

            case R.id.btn_search:
                timePickerRl.setVisibility(View.GONE);
                keyWord = AMapUtil.checkEditText(searchedit);
                if ("".equals(keyWord)) {
                    ToastUtil.show(MainActivity.this, "请输入搜索关键字");
                    return;
                } else {
                    doSearchQuery();
                }
                //收起软键盘
                InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                            0);
                }

                break;

            case R.id.bt_heatmap:
                if(!btheat_use){
                    LatLng latLng = new LatLng(41.677287,123.465009);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    initDataAndHeatMap();
                    btheat.setImageResource(R.drawable.onheat);
                    btheat_use = true;
                    timePickerRl.setVisibility(View.VISIBLE);
                    timeText.setVisibility(View.VISIBLE);
                    timeText.setText(" " +heatHour + ":" + heatMinute +"  |  ⚙");
                }else {
                    mMap.clear();
                    btheat.setImageResource(R.drawable.offheat);
                    btheat_use = false;
                    timePickerRl.setVisibility(View.GONE);
                    timeText.setVisibility(View.GONE);
                }
                break;

            case R.id.imblay:
                if(!btsate_use){
                    mMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                    ib_layer.setBackground(getResources().getDrawable(R.drawable.on_satemap));
                    timePickerRl.setVisibility(View.GONE);
                    btsate_use = true;
                }else{
                    mMap.setMapType(AMap.MAP_TYPE_NORMAL);
                    ib_layer.setBackground(getResources().getDrawable(R.drawable.layer));
                    btsate_use = false;
                }
                break;

            case R.id.btnroadc_new:
                if(!btroadc_use){
                    mMap.setTrafficEnabled(true);
                    btroadc.setImageResource(R.drawable.roadc_choosed);
                    timePickerRl.setVisibility(View.GONE);
                    //timeText.setVisibility(View.GONE);
                    btroadc_use = true;
                }else {
                    mMap.setTrafficEnabled(false);
                    btroadc.setImageResource(R.drawable.roadc_unchoose);
                    timePickerRl.setVisibility(View.GONE);
                    btroadc_use = false;
                }
                break;

            case R.id.ibtbusmode:
                if(!busmode_used){
                    mMap.setMapType(AMap.MAP_TYPE_BUS);
                    ibt_busmode.setBackground(getResources().getDrawable(R.drawable.onbusmode));
                    timePickerRl.setVisibility(View.GONE);
                    busmode_used = true;
                }else{
                    mMap.setMapType(maptype);
                    ibt_busmode.setBackground(getResources().getDrawable(R.drawable.offbusmode));
                    timePickerRl.setVisibility(View.GONE);
                    busmode_used = false;
                }
                break;
            case R.id.btntimesure:
                mMap.clear();
                initDataAndHeatMap();
                timePickerRl.setVisibility(View.GONE);
                timeText.setText(" "+heatHour + ":" + heatMinute +"  |  ⚙" );
                break;
            default:
                break;
        }
    }


    private void init() {

        ib_layer = (ImageButton) findViewById(R.id.imblay);
        ib_nav = (ImageButton) findViewById(R.id.imbnav);
        ib_now = (ImageButton) findViewById(R.id.imbnow);
        btheat = (ImageButton)findViewById(R.id.bt_heatmap);
        btroadc = (ImageButton)findViewById(R.id.btnroadc_new);
        ibt_busmode = (ImageButton)findViewById(R.id.ibtbusmode);
        searchedit = (AutoCompleteTextView) findViewById(R.id.input_edittext);
        searchbtn = (Button)findViewById(R.id.btn_search);
        timesurebtn = (Button)findViewById(R.id.btntimesure);
        timePicker = (TimePicker)findViewById(R.id.timepicker);
        timePickerRl = (RelativeLayout)findViewById(R.id.timepickerll);
        timeText = (TextView)findViewById(R.id.time_text);

        ib_now.setOnClickListener(this);
        ib_nav.setOnClickListener(this);
        ib_layer.setOnClickListener(this);
        ibt_busmode.setOnClickListener(this);
        btheat.setOnClickListener(this);
        btroadc.setOnClickListener(this);
        timesurebtn.setOnClickListener(this);
        searchedit.setOnClickListener(this);
        searchbtn.setOnClickListener(this);
        searchedit.addTextChangedListener(this);
        searchedit.setOnEditorActionListener(this);
        mMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        mMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) { //时间选择器变化
                if (hour < 10){
                    heatHour = "0" + String.valueOf(hour);
                }else {
                    heatHour = String.valueOf(hour);
                }
                if (minute < 10){
                    heatMinute ="0" + String.valueOf(minute);
                }else {
                    heatMinute = String.valueOf(minute);
                }

                heatTime = heatHour + heatMinute;
            }
        });
        //timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        mMap.showIndoorMap(true);
        maptype = mMap.getMapType();

        //首次执行导入.db文件


    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        lp = new LatLonPoint(localatLng,Longitude);
        query = new PoiSearch.Query(keyWord, "",citycode);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + keyWord);
        progDialog.show();
    }
    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
        if (database.isOpen()){
            database.close();

        }
        db.close();
    }

    /**
     * 获取Amap 对象
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((TextureMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            setupMap();
        }

    }

    private void setupMap() {
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);

        init();
        initLocation();
        if (mLocationClient != null) {
            mLocationClient.startLocation();
        }

    }



    private void initLocation(){

        //设置定位模式
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(true);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //取精度3s内最高的一次
        mLocationOption.setOnceLocationLatest(true);

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                //取出经纬度
                LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                localatLng = amapLocation.getLatitude();
                Longitude = amapLocation.getLongitude();
                citycode = amapLocation.getCityCode();

                //添加Marker显示定位位置
                if (locationMarker == null) {
                    //如果是空的添加一个新的,icon方法就是设置定位图标，可以自定义
                    locationMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point)));
                } else {
                    locationMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point)));
                    //已经添加过了，修改位置即可
                    //locationMarker.setPosition(latLng);
                }

                //然后可以移动到定位点,使用animateCamera就有动画效果
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

            }
        }

    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, citycode);
            Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
                null);
        title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        Button button = (Button) view
                .findViewById(R.id.start_amap_app);
        // 调起高德地图app
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAMapNavi(marker);
            }
        });
        return view;
    }

    /**
     * 调起高德地图导航功能，如果没安装高德地图，会进入异常，可以在异常中处理，调起高德地图app的下载页面
     */
    public void startAMapNavi(Marker marker) {
        // 构造导航参数
        /*NaviPara naviPara = new NaviPara();
        // 设置终点位置
        naviPara.setTargetPoint(marker.getPosition());
        // 设置导航策略，这里是避免拥堵
        naviPara.setNaviStyle(NaviPara.DRIVING_AVOID_CONGESTION);*/

        endLatlng = marker.getPosition().latitude;
        endLongitude = marker.getPosition().longitude;
        String endpoi = marker.getTitle();
        Intent intent = new Intent(MainActivity.this,navActivity.class);
        intent.putExtra("localatLng",localatLng);
        intent.putExtra("Longitude",Longitude);
        intent.putExtra("endlatlng",endLatlng);
        intent.putExtra("citycode",citycode);
        intent.putExtra("endLongitude",endLongitude);
        intent.putExtra("endpoi",endpoi);
        startActivity(intent);

        // 调起高德地图导航
        /*try {
            AMapUtils.openAMapNavi(naviPara, getApplicationContext());
        } catch (com.amap.api.maps.AMapException e) {

            // 如果没安装会进入异常，调起下载页面
            AMapUtils.getLatestAMapApp(getApplicationContext());

        }*/

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.route_inputs, listString);
            searchedit.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        mMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(mMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(MainActivity.this,
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(MainActivity.this,
                        R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }

    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(MainActivity.this, infomation);
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        keyWord = AMapUtil.checkEditText(searchedit);
        if ("".equals(keyWord)) {
            ToastUtil.show(MainActivity.this, "请输入搜索关键字");
        } else {
            doSearchQuery();
        }

        //收起软键盘
        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                    0);
        }
            return false;
    }


    public void timeSet(View view) {
        timePickerRl.setVisibility(View.VISIBLE);
    }
}
