package com.map.mymap.ui;

import android.app.AppComponentFactory;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.map.mymap.HeatDate;
import com.map.mymap.MainActivity;
import com.map.mymap.R;

public class BottomLayout extends LinearLayout implements View.OnClickListener {

    TextView textMap;
    TextView textHeat;
    public BottomLayout(Context context, AttributeSet attrs) {
        super(context,attrs);
        LayoutInflater.from(context).inflate(R.layout.bottom_selector,this);
        textMap = (TextView)findViewById(R.id.text_map);
        textHeat = (TextView)findViewById(R.id.text_heat);
        BaseActivity baseActivity = new BaseActivity();
        String activityName = baseActivity.getActivityName();
        if (activityName == "MainActivity"){
            textMap.setBackground(getResources().getDrawable(R.drawable.shape));
            textMap.setTextColor(getResources().getColor(R.color.white));
        }else if(activityName == "HeatDate"){
            textHeat.setBackground(getResources().getDrawable(R.drawable.shape));
            textHeat.setTextColor(getResources().getColor(R.color.white));
        }else {
            Toast.makeText(getContext(),"错误",Toast.LENGTH_LONG).show();
        }
        textMap.setOnClickListener(this);
        textHeat.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.text_map:
                textMap.setBackground(getResources().getDrawable(R.drawable.shape));
                textMap.setTextColor(getResources().getColor(R.color.white));
                textHeat.setTextColor(getResources().getColor(R.color.black));
                textHeat.setBackground(null);
                Intent intent_to_map = new Intent(getContext(), MainActivity.class);
                intent_to_map.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getContext().startActivity(intent_to_map);
                break;
            case R.id.text_heat:
                textHeat.setBackground(getResources().getDrawable(R.drawable.shape));
                textHeat.setTextColor(getResources().getColor(R.color.white));
                textMap.setTextColor(getResources().getColor(R.color.black));
                textMap.setBackground(null);
                Intent intent_to_heat = new Intent(getContext(), HeatDate.class);
                intent_to_heat.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getContext().startActivity(intent_to_heat);
                break;
            default:
                break;
        }

    }
}
