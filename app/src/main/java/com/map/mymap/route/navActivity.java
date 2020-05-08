package com.map.mymap.route;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRoutePlanResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DrivePlanQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.map.mymap.R;
import com.map.mymap.overlay.DrivingRouteOverlay;
import com.map.mymap.overlay.DrivingRoutePlanOverlay;
import com.map.mymap.overlay.PoiOverlay;
import com.map.mymap.overlay.WalkRouteOverlay;
import com.map.mymap.ui.StatusBarUtil;
import com.map.mymap.util.AMapUtil;
import com.map.mymap.util.ToastUtil;
import com.map.mymap.util.utils;
import com.map.mymap.view.TravelView;

import java.util.ArrayList;
import java.util.List;



/**
 * Route路径规划: 驾车规划、公交规划、步行规划
 */
public class navActivity extends Activity implements OnMapClickListener,
        OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter,
        OnRouteSearchListener, TextWatcher, Inputtips.InputtipsListener, OnClickListener, PoiSearch.OnPoiSearchListener,
        TextView.OnEditorActionListener, RouteSearch.OnRoutePlanSearchListener {
    private AMap aMap;
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;
    private WalkRouteResult mWalkRouteResult;

    private LatLonPoint mStartPoint = null;
    private LatLonPoint mEndPoint = null;//终点，116.481288,39.995576
    private LatLonPoint mStartPoint_bus = null;
    private LatLonPoint mEndPoint_bus = null;//终点，

    private String mCurrentCityName = "沈阳";
    private final int ROUTE_TYPE_BUS = 1;
    private final int ROUTE_TYPE_DRIVE = 2;
    private final int ROUTE_TYPE_WALK = 3;
    private final int ROUTE_TYPE_CROSSTOWN = 4;
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch poiSearch;
    private LatLonPoint lp;
    private Double stalatlng = 0.0;
    private Double staLongtude = 0.0;
    private Double endlatlng = 0.0;
    private Double endLongitude = 0.0;
    private Double test =0.0;
    private PoiResult poiResult; // poi返回的结果
    private PoiSearch.Query query;
    private String keyWord;
    private String endKeyWord;
    private Boolean startorend = false;
    private Boolean ischoose_pre = false;
    private Boolean plandata_pre = false;
    private String citycode;
    private long arriveTime = 0;

    private UiSettings uiSettings;

    private LinearLayout mBusResultLayout;
    private RelativeLayout mBottomLayout;
    private RelativeLayout mHeaderLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private TextView title;
    private ImageView mBus;
    private ImageView mDrive;
    private ImageView mWalk;
    private Button btnprediction;
    private ImageButton btnexchangepoi;
    private AutoCompleteTextView startedit;
    private AutoCompleteTextView endedit;
    private ListView mBusResultList;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private TravelView travelView;
    private DriveRoutePlanResult mDriveRoutePlanResult;
    private int currentpage =  1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        StatusBarUtil.setTranslucentStatus(this);
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);


        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();
//		getIntentData();
        //判断起始点输入框是否为空
        if (startedit.getText().toString().length() == 0) {
            stalatlng = getIntent().getDoubleExtra("localatLng", 3.8f);
            staLongtude = getIntent().getDoubleExtra("Longitude", 3.8f);
            LatLng stalat = new LatLng(stalatlng, staLongtude);
            startedit.setText("我的位置");
            endlatlng = getIntent().getDoubleExtra("endlatlng", 3.8f);
            endLongitude = getIntent().getDoubleExtra("endLongitude", 3.8);
            String endPoi = getIntent().getStringExtra("endpoi");
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stalat, 7));
            mStartPoint = new LatLonPoint(stalatlng, staLongtude);
            mStartPoint_bus = new LatLonPoint(stalatlng, staLongtude);

            if (endLongitude != 0 && endlatlng != 0) {
                endedit.setText(endPoi);
                mEndPoint = new LatLonPoint(endlatlng, endLongitude);
                mEndPoint_bus = new LatLonPoint(endlatlng, endLongitude);
            }
            if ("".equals(endedit.getText().toString())){

            }else {
                searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
                mDrive.setImageResource(R.drawable.route_drive_select);
                btnprediction.setVisibility(View.VISIBLE);
                currentpage = 1;
            }

        }

        setfromandtoMarker();

    }

    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)));
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mHeaderLayout = (RelativeLayout) findViewById(R.id.routemap_header);
        mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);
        mRouteSearch.setOnRoutePlanSearchListener(this);
        mDrive = (ImageView) findViewById(R.id.route_drive);
        mBus = (ImageView) findViewById(R.id.route_bus);
        mWalk = (ImageView) findViewById(R.id.route_walk);
        mBusResultList = (ListView) findViewById(R.id.bus_result_list);
        startedit = (AutoCompleteTextView) findViewById(R.id.start_poi);
        endedit = (AutoCompleteTextView) findViewById(R.id.end_poi);
        btnprediction = (Button) findViewById(R.id.btnprediction);
        btnexchangepoi = (ImageButton) findViewById(R.id.ibtexchange);
        travelView = (TravelView) findViewById(R.id.travel_view);

        uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        aMap.setTrafficEnabled(true);

        registerListener();

    }

    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(navActivity.this);
        aMap.setOnMarkerClickListener(navActivity.this);
        aMap.setOnInfoWindowClickListener(navActivity.this);
        aMap.setInfoWindowAdapter(navActivity.this);

        startedit.addTextChangedListener(this);
        startedit.setOnEditorActionListener(this);
        startedit.setOnClickListener(this);
        endedit.addTextChangedListener(this);
        endedit.setOnEditorActionListener(this);
        endedit.setOnClickListener(this);
        btnexchangepoi.setOnClickListener(this);
        btnprediction.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibtexchange:
                exchangepoi();
                break;

            case R.id.btnprediction:

              if(!ischoose_pre) {
                  btnprediction.setBackground(getResources().getDrawable(R.drawable.bluechange2));
                  btnprediction.setTextColor(getResources().getColor(R.color.colorWhite));
                  request();
                  mBottomLayout.setVisibility(View.GONE);
                  ischoose_pre = true;
              }else {
                  travelView.setVisibility(View.GONE);
                  btnprediction.setBackground(getResources().getDrawable(R.drawable.un_prediction));
                  btnprediction.setTextColor(getResources().getColor(R.color.colorAccent));
                  ischoose_pre = false;
              }
                break;

            case R.id.start_poi:
                startorend = false;
                startedit.setFocusable(true);
                startedit.setFocusableInTouchMode(true);
                startedit.requestFocus();
                startedit.requestFocusFromTouch();
                plandata_pre = false;

                /*InputMethodManager  inputManager =
                        (InputMethodManager)startedit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(startedit, 0);

                 */
                break;

            case R.id.end_poi:
                startorend = true;
                endedit.setFocusable(true);
                endedit.setFocusableInTouchMode(true);
                endedit.requestFocus();
                endedit.requestFocusFromTouch();

                plandata_pre = false;

                /*InputMethodManager  endinput =
                        (InputMethodManager)endedit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                endinput.showSoftInput(endedit, 0);

                 */
                break;

            default:
                break;
        }
    }

    private void exchangepoi() {

        if (!"".equals(startedit) && !"".equals(endedit)) {
            aMap.clear();
            String exchange = null;
            exchange = endedit.getText().toString();
            endedit.setText(startedit.getText().toString());
            startedit.setText(exchange);
            LatLonPoint exchangepoi;
            exchangepoi = mEndPoint;
            mEndPoint = mStartPoint;
            mStartPoint = exchangepoi;

            travelView.setVisibility(View.GONE);
            plandata_pre = false;
            ischoose_pre = false;
            btnprediction.setBackground(getResources().getDrawable(R.drawable.un_prediction));
            btnprediction.setTextColor(getResources().getColor(R.color.colorAccent));
            setfromandtoMarker();

            switch (currentpage){
                case 1 :
                    searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
                    break;
                case 3:
                    searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
                    break;
                default:
                    searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
                    break;

            }

        }

    }

    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_end,
                null);
        title = (TextView) view.findViewById(R.id.end_title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.end_snippet);
        snippet.setText(marker.getSnippet());
        Button button = (Button) view
                .findViewById(R.id.end_amap_app);
        // 调起高德地图app
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAMapNavi(marker);
                if ((mStartPoint != null) && (mEndPoint != new LatLonPoint (test,test))){
                    ToastUtil.show(getApplicationContext(),"选择一种交通方式.");
                    mHeaderLayout.setClickable(true);
                }
            }
        });
        return view;
    }

    private void startAMapNavi(Marker marker) {
        if (!startorend){
            startedit.setText(marker.getTitle());
            stalatlng = marker.getPosition().latitude;
            staLongtude = marker.getPosition().longitude;
            mStartPoint = new LatLonPoint(stalatlng,staLongtude);
            mStartPoint_bus = mStartPoint;
        }else if(startorend){
            endedit.setText(marker.getTitle());
            endlatlng = marker.getPosition().latitude;
            endLongitude = marker.getPosition().longitude;
            mEndPoint = new LatLonPoint(endlatlng,endLongitude);
            mEndPoint_bus = mEndPoint;
        }

    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }

    /**
     * 公交路线搜索
     */
    public void onBusClick(View view) {
        currentpage = 1;
        searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BusDefault);
        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_select);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        mapView.setVisibility(View.GONE);
        btnprediction.setVisibility(View.GONE);
        btnprediction.setBackground(getResources().getDrawable(R.drawable.un_prediction));
        travelView.setVisibility(View.GONE);
        mBusResultLayout.setVisibility(View.VISIBLE);
        btnprediction.setVisibility(View.GONE);
        btnexchangepoi.setVisibility(View.GONE);
    }

    /**
     * 驾车路线搜索
     */
    public void onDriveClick(View view) {
        currentpage = 2;
        searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
        mDrive.setImageResource(R.drawable.route_drive_select);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        btnprediction.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
        travelView.setVisibility(View.GONE);
        btnprediction.setVisibility(View.VISIBLE);
        btnexchangepoi.setVisibility(View.VISIBLE);
    }

    /**
     * 步行路线搜索
     */
    public void onWalkClick(View view) {
        currentpage = 3;
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_select);
        btnprediction.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
        travelView.setVisibility(View.GONE);
        btnprediction.setVisibility(View.GONE);
        btnexchangepoi.setVisibility(View.VISIBLE);
    }

    /**
     * 跨城公交路线搜索
     */
    public void onCrosstownBusClick(View view) {
        searchRouteResult(ROUTE_TYPE_CROSSTOWN, RouteSearch.BusDefault);
        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        btnprediction.setVisibility(View.GONE);
        mapView.setVisibility(View.GONE);
        mBusResultLayout.setVisibility(View.VISIBLE);
        travelView.setVisibility(View.GONE);
        btnprediction.setVisibility(View.GONE);
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
            BusRouteQuery query = new BusRouteQuery(fromAndTo, mode,
                    mCurrentCityName, 1);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        } else if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            DriveRouteQuery query = new DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        } else if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            WalkRouteQuery query = new WalkRouteQuery(fromAndTo);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        } else if (routeType == ROUTE_TYPE_CROSSTOWN) {
            RouteSearch.FromAndTo fromAndTo_bus = new RouteSearch.FromAndTo(
                    mStartPoint_bus, mEndPoint_bus);
            BusRouteQuery query = new BusRouteQuery(fromAndTo_bus, mode,
                    citycode, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            query.setCityd("农安县");
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        }
    }


    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult_plan(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            int  time = (int)(System.currentTimeMillis() /1000 );
            DrivePlanQuery query = new DrivePlanQuery(fromAndTo, time + utils.queryFirstInterval,  utils.queryInterval* 60, 48);

    // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDrivePlanAsyn(query);// 异步路径规划驾车模式查询
        }
        plandata_pre = true;
    }

    /**
     * 公交路线搜索结果方法回调
     */
    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        mBottomLayout.setVisibility(View.GONE);
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    /**
     * 驾车路线搜索结果方法回调
     */
    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mDriveRouteResult = result;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    if (drivePath == null) {
                        return;
                    }
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            mContext, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    mRotueTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.VISIBLE);
                    int taxiCost = (int) mDriveRouteResult.getTaxiCost();
                    mRouteDetailDes.setText("打车约" + taxiCost + "元");
                    mBottomLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    DriveRouteDetailActivity.class);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result",
                                    mDriveRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }


    }

    /**
     * 步行路线搜索结果方法回调
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    if (walkPath == null) {
                        return;
                    }
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    mRotueTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.GONE);
                    mBottomLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    WalkRouteDetailActivity.class);
                            intent.putExtra("walk_path", walkPath);
                            intent.putExtra("walk_result",
                                    mWalkRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                    mBottomLayout.setVisibility(View.GONE);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
                mBottomLayout.setVisibility(View.GONE);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
            mBottomLayout.setVisibility(View.GONE);
        }
    }


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
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

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * &#x65b9;&#x6cd5;&#x5fc5;&#x987b;&#x91cd;&#x5199;
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 骑行路线搜索结果方法回调
     */
    @Override
    public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
        String newText = s.toString().trim();
        citycode = getIntent().getStringExtra("locacitycode");
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, citycode);
            Inputtips inputTips = new Inputtips(navActivity.this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }

    }

    @Override
    public void afterTextChanged(Editable editable) {

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
                startedit.setAdapter(aAdapter);
                endedit.setAdapter(aAdapter);
                aAdapter.notifyDataSetChanged();
            } else {
                ToastUtil.showerror(this, rCode);
        }



    }

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
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(navActivity.this,
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(navActivity.this,
                        R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }


    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(navActivity.this, infomation);
    }



    private void request() {
        searchRouteResult_plan(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
        travelView.setVisibility(View.VISIBLE);

    }


    @Override
    public void onBackPressed(){
        if (travelView != null && travelView.getVisibility() == View.VISIBLE) {
            travelView.setVisibility(View.GONE);
        } else {
            finish();
        }
    }


    @Override
    public void onDriveRoutePlanSearched(final DriveRoutePlanResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                travelView.setVisibility(View.VISIBLE);
                travelView.init(result, new TravelView.IndexListener() {
                    @Override
                    public void onClicked(int index) {
                        Log.d("qyd","onDriveRoutePlanSearched onClicked index:" + index);
                        if (result.getPaths().size() > 0) {
                            mDriveRoutePlanResult = result;

                            DrivingRoutePlanOverlay drivingRouteOverlay = new DrivingRoutePlanOverlay(
                                    mContext, aMap, mDriveRoutePlanResult, index);
                            drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                            drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                            drivingRouteOverlay.removeFromMap();
                            drivingRouteOverlay.addToMap();
                            drivingRouteOverlay.zoomToSpan();

                        } else if (result != null && result.getPaths() == null) {
                            ToastUtil.show(mContext, R.string.no_result);
                        }
                    }
                }, arriveTime );

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        if(!startorend){
            lp = new LatLonPoint(stalatlng, staLongtude);
            query = new PoiSearch.Query(keyWord, "", citycode);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        }else if(startorend){
            lp = new LatLonPoint(endlatlng,endLongitude);
            query = new PoiSearch.Query(endKeyWord, "", citycode);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
            InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                        0);
            }
        }
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();

        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_normal);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        keyWord = AMapUtil.checkEditText(startedit);
        endKeyWord = AMapUtil.checkEditText(endedit);

        if(!startorend) {
            if ("".equals(keyWord)) {
                ToastUtil.show(navActivity.this, "请输入搜索关键字");
            } else {
                doSearchQuery();
            }
        }else if (startorend) {
            if (("".equals(endKeyWord))) {
                ToastUtil.show(navActivity.this, "请输入搜索关键字。");
            } else {
                doSearchQuery();
            }
        }

        //收起软键盘
        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                    0);
        }
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

