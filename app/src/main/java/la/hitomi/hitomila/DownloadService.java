package la.hitomi.hitomila;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import la.hitomi.hitomila.common.hitomiClient;
import la.hitomi.hitomila.common.hitomiParser;

/**
 * Created by admin on 2016-10-11.
 */

public class DownloadService extends Service{
    public interface notificationCallback{
        void notificationUpdate(String title, String content);
    }
    notificationCallback notificationCallback = new notificationCallback() {
        @Override
        public void notificationUpdate(String title, String content) {
            notificationUp(title, content);
        }
    };

    private hitomiClient client;
    private static int notificationNumber = 1203;
    private int currentServiceNotificationNumber;
    private int threadCount;
    private String galleryAddr;
    Notification.Builder mBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        currentServiceNotificationNumber = notificationNumber++;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle item = intent.getExtras();
        threadCount = item.getInt("threadCount");
        galleryAddr = item.getString("galleryAddress");

        initNotification();
        client = new hitomiClient(null);
        client.setMaxConnections(threadCount);

        client.get(hitomiParser.getAbsoulteReaderAddress(galleryAddr), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                client.download(new String(responseBody), galleryAddr, DownloadService.this.getApplicationContext());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //리더 페이지 접근 실패
                error.printStackTrace();
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initNotification(){
        //notification 클릭시 해당 인텐트가 실행되게 만드는것 같다.
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.hitomi_32x32)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setStyle(new Notification.BigTextStyle().bigText("BigText"))
                .setContentIntent(resultPendingIntent)
                .setContentTitle("Notify")
                .setContentText("textText");
    }

    public void notificationUp(String title, String content){
        mBuilder
                .setContentTitle(title)
                .setContentText(content);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(currentServiceNotificationNumber, mBuilder.build());
    }

}
