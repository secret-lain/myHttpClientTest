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
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import la.hitomi.hitomila.common.hitomiClient;
import la.hitomi.hitomila.common.hitomiParser;
import la.hitomi.hitomila.common.mangaInformationData;

/**
 * Created by admin on 2016-10-11.
 */

public class DownloadService extends Service{
    private hitomiClient client;
    private static int initialNotificationNumber = 1203;
    private int currentServiceNotificationNumber;
    private int threadCount;
    private String galleryAddr;
    private HashMap<Integer, mangaInformationData> dataSet;
    Notification.Builder mBuilder;
    NotificationManager mNotificationManager;

//    String mangaTitle = "";
//    int currPageDownloaded = 0;
//    int maxPages = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        dataSet = new HashMap<>();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle item = intent.getExtras();
        threadCount = item.getInt("threadCount");
        galleryAddr = item.getString("galleryAddress");

        currentServiceNotificationNumber = initialNotificationNumber;
        initialNotificationNumber = initialNotificationNumber + 1;

        client = new hitomiClient(null);
        client.setMaxConnections(threadCount);

        Toast.makeText(this, "다운로드 시작", Toast.LENGTH_SHORT).show();

        //reader page loading
        client.get(hitomiParser.getAbsoulteReaderAddress(galleryAddr), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                client.download(new String(responseBody), galleryAddr, DownloadService.this.getApplicationContext(), notificationCallback, currentServiceNotificationNumber);
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

    private void _initNotification(mangaInformationData item){
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
                .setContentTitle(item.mangaTitle)
                .setContentText(item.currDownloadedPages + " / " + item.maxPages);

        mNotificationManager.notify(item.notificationID, mBuilder.build());
    }


    public interface notificationCallback{
        void initNotification(String title, int maxPages, int notificationID);
        void notifyPageDownloaded(int notificationID);
        void notifyDownloadCompleted(int notificationID);
    }
    notificationCallback notificationCallback = new notificationCallback() {
        @Override
        public void initNotification(String _title, int _maxPages, int notificationID) {
            dataSet.put(notificationID, new mangaInformationData(_title, 0, _maxPages, notificationID));
            _initNotification(dataSet.get(notificationID));
        }

        @Override
        public void notifyPageDownloaded(int notificationID) {
            mangaInformationData item = dataSet.get(notificationID);
            item.currDownloadedPages += 1;
            mBuilder
                    .setContentTitle(item.mangaTitle)
                    .setContentText(item.currDownloadedPages + " / " + item.maxPages);

            mNotificationManager.notify(item.notificationID, mBuilder.build());
        }

        @Override
        public void notifyDownloadCompleted(int notificationID) {
            mangaInformationData item = dataSet.get(notificationID);

            mBuilder
                    .setAutoCancel(false)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(false)
                    .setContentTitle(item.mangaTitle)
                    .setContentText("Download done - " + item.currDownloadedPages+ " / " + item.maxPages);

            mNotificationManager.notify(item.notificationID, mBuilder.build());
        }
    };

}
