package com.example.myongsubway;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.StringTokenizer;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class AlarmReceiver extends BroadcastReceiver {

    private Context context;
    private String channelId="alarm_channel";
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        Intent pathIntent = new Intent(context, ShortestPathActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(pathIntent);
        PendingIntent pathPendingIntent =
                stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.mipmap.ic_launcher).setDefaults(Notification.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setContentTitle("알람")
                .setContentText("도착했습니다")
                .setContentIntent(pathPendingIntent);


        final NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(channelId,"Channel human readable title",NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        int id=(int)System.currentTimeMillis();
        if(pref.getBoolean("sneeze",true)){//진동
            final Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(1000, 100));
        }
        if(pref.getBoolean("ring", true)){//소리 재생
            String ring = pref.getString("ringtone_list","기본");
            MediaPlayer m = null;
            switch (ring){
                case "기본":
                    m = MediaPlayer.create(context, R.raw.clock);
                    m.start();
                    break;
                case "beep":
                    m = MediaPlayer.create(context, R.raw.beep);
                    m.start();
                    break;
                case "카톡":
                    m = MediaPlayer.create(context, R.raw.kakao);
                    m.start();
                    break;
                case "카톡카톡":
                    m = MediaPlayer.create(context, R.raw.kakaokakao);
                    m.start();
                    break;
                case "shortBeep":
                    m = MediaPlayer.create(context, R.raw.short_beep);
                    m.start();
                    break;
                case "blop":
                    m = MediaPlayer.create(context, R.raw.blop);
                    m.start();
                    break;
            }

        }
        notificationManager.notify(id,notificationBuilder.build());

    }
}