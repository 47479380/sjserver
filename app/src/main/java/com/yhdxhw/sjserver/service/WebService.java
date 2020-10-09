package com.yhdxhw.sjserver.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.yhdxhw.sjserver.MainActivity;
import com.yhdxhw.sjserver.R;
import com.yhdxhw.sjserver.server.Directory;
import com.yhdxhw.sjserver.server.HttpServer;
import com.yhdxhw.sjserver.server.ServerException;
import com.yhdxhw.sjserver.ui.Web.WebServerFragment;

import java.io.IOException;

public class WebService extends Service {
    private HttpServer httpServer;
    private IBinder serverBinder = new ServerBinder();

    private Context mContext = this;
    private static final String TAG = "WebService";

    public class ServerBinder extends Binder {
        /**
         * 判断server是否正在运行
         * @return 正在运行返回true 否则返回false
         */
        public boolean isAlive() {
            return httpServer.isAlive();
        }

        public void startServer() throws ServerException {


            try {
                httpServer.start();
            } catch (Error | Exception e) {
                throw new ServerException(e);
            }
           startForeground();
        }
        private void startForeground(){
            String CHANNEL_ONE_ID = "CHANNEL_ONE_ID";
            String CHANNEL_ONE_NAME= "CHANNEL_ONE_ID";
            NotificationChannel notificationChannel= null;
            //进行8.0的判断
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationChannel= new NotificationChannel(CHANNEL_ONE_ID,
                        CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(false);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setShowBadge(false);
                notificationChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(notificationChannel);
            }

            PendingIntent pendingIntent=PendingIntent.getActivity(WebService.this,0,new Intent(WebService.this, MainActivity.class),0);
            Notification notification = new NotificationCompat.Builder(WebService.this, CHANNEL_ONE_ID)
                    .setContentTitle("手机服务器已经启动")
                    .setContentText("访问http://"+ WebServerFragment.serverIp+":"+httpServer.getListeningPort())
                    .setWhen(System.currentTimeMillis())
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .build();
            WebService.this.startForeground(1,notification);
        }

        public void stopServer() {


            httpServer.stop();

                stopForeground(true);

        }

        /**
         * 获取server监听的端口
         * @return 端口号
         */
        public int getListeningPort(){
            return httpServer.getListeningPort();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return serverBinder;
    }

    @Override
    public void onCreate() {


        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int port = Integer.parseInt(defaultSharedPreferences.getString("port", "8090"));
        String dir_root = defaultSharedPreferences.getString("root_dir", Environment.getExternalStorageDirectory().getAbsolutePath());
        Directory.setRoot(dir_root);
        httpServer = new HttpServer(port);




        Log.i(TAG, "服务器创建完成");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        httpServer.stop();
        Log.i(TAG, "WebService onCreate");
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }
}
