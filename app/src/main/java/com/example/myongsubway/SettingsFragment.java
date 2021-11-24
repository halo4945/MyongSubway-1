package com.example.myongsubway;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.preference.PreferenceCategory;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;


public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences prefs;//설정 저장
    Preference ringtonePreference;//벨소리


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {

        setPreferencesFromResource(R.xml.fragment_settings, s);
        ringtonePreference = (ListPreference)findPreference("ringtone_list");//벨소리 설정
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());//sharedPreference
        //prefs = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        if(!prefs.getString("ringtone_list", "").equals("")) {//불러옴
            ringtonePreference.setSummary(prefs.getString("ringtone_list", "기본"));
        }

        prefs.registerOnSharedPreferenceChangeListener(prefListener);//설정 변경 리스너
    }//OnCreatePreferences

    SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {//설정 변경 리스너
            if(s.equals("ringtone_list")) {
                ringtonePreference.setSummary(prefs.getString("ringtone_list", "기본"));//키에서 찾아서 리턴
                Toast myToast = Toast.makeText(getActivity(),prefs.getString("ringtone_list", "기본"), Toast.LENGTH_SHORT);
                myToast.show();
            }else if(s.equals("ring")){
                boolean bool = prefs.getBoolean(s, true);
                Toast myToast;
                if(bool) myToast = Toast.makeText(getActivity(),"알림음 ON" , Toast.LENGTH_SHORT);
                else myToast = Toast.makeText(getActivity(),"알림음 OFF" , Toast.LENGTH_SHORT);
                myToast.show();
            }else if(s.equals("sneeze")){
                boolean bool = prefs.getBoolean(s, true);
                Toast myToast;
                if(bool) myToast = Toast.makeText(getActivity(),"진동ON", Toast.LENGTH_SHORT);
                else myToast = Toast.makeText(getActivity(),"진동OFF", Toast.LENGTH_SHORT);
                myToast.show();
            }
        }
    };
}