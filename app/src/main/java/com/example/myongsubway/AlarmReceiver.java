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
/*public class AlarmReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
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
        notificationHelper.getManager().notify(1, nb.build());
    }
}*/
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
            NotificationChannel channel=new NotificationChannel(channelId,"Channel human readable title",NotificationManager.IMPORTANCE_MIN);
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
/*public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingI = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");


        //OREO API 26 이상에서는 채널 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


            String channelName ="매일 알람 채널";
            String description = "매일 정해진 시간에 알람합니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH; //소리와 알림메시지를 같이 보여줌

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남


        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())

                .setTicker("{Time to watch some cool stuff!}")
                .setContentTitle("상태바 드래그시 보이는 타이틀")
                .setContentText("상태바 드래그시 보이는 서브타이틀")
                .setContentInfo("INFO")
                .setContentIntent(pendingI);

        if (notificationManager != null) {

            // 노티피케이션 동작시킴
            notificationManager.notify(1234, builder.build());

            Calendar nextNotifyTime = Calendar.getInstance();

            // 내일 같은 시간으로 알람시간 결정
            nextNotifyTime.add(Calendar.DATE, 1);

            //  Preference에 설정한 값 저장
            SharedPreferences.Editor editor = context.getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
            editor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
            editor.apply();

            Date currentDateTime = nextNotifyTime.getTime();
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
            Toast.makeText(context.getApplicationContext(),"다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();
        }
    }
}
*/