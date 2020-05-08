package com.map.mymap.setting;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;

import com.map.mymap.R;
import com.map.mymap.ui.StatusBarUtil;


public class SettingsFragment extends PreferenceActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private CheckBoxPreference check_satellitemap;

    private SwitchPreference switch_heatmap;

    private SwitchPreference switch_roadcondition;

    public Boolean ck_satellite,sw_heatmap,sw_roadcondition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settingspreference);

        StatusBarUtil.setTranslucentStatus(this);
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }
        StatusBarUtil.setRootViewFitsSystemWindows(this,true);

        init();
    }

    private void init() {
        //初始化控件
        check_satellitemap = (CheckBoxPreference)findPreference("checkbox_satellitemap");
        switch_heatmap = (SwitchPreference)findPreference("switch_heatmap");
        switch_roadcondition = (SwitchPreference)findPreference(("switch_mapcondition"));

        //设置监听器
        check_satellitemap.setOnPreferenceClickListener(this);
        check_satellitemap.setOnPreferenceChangeListener(this);
        switch_heatmap.setOnPreferenceChangeListener(this);
        switch_heatmap.setOnPreferenceClickListener(this);
        switch_roadcondition.setOnPreferenceClickListener(this);
        switch_roadcondition.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        return false;
    }

}
