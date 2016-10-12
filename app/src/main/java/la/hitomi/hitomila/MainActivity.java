package la.hitomi.hitomila;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import la.hitomi.hitomila.common.ExternalViewControlCallback;
import la.hitomi.hitomila.common.galleryObject;
import la.hitomi.hitomila.common.hitomiClient;

public class MainActivity extends AppCompatActivity {
    private ImageView previewImage;
    private TextView addrTextView;
    private TextView mangaTitleTextView;
    private ProgressBar previewImageLoading;

    private hitomiClient client;

    private Button previewButton;
    private Button downloadStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewImage = (ImageView) findViewById(R.id.imageView);
        addrTextView = (TextView) findViewById(R.id.addrEditText);
        previewButton = (Button) findViewById(R.id.previewButton);
        downloadStartButton = (Button) findViewById(R.id.downloadButton);
        previewImageLoading = (ProgressBar) findViewById(R.id.progressBar);
        mangaTitleTextView = (TextView) findViewById(R.id.mangatitleText);


        client = new hitomiClient(new ExternalViewControlCallback() {
            @Override
            public void sendMessage(String message, int ToastLength) {
                Toast.makeText(MainActivity.this , message, ToastLength).show();
            }

            @Override
            public void processDone() {
                previewImageLoading.setVisibility(View.GONE);
            }

            @Override
            public void processStart() {
                previewImage.setImageBitmap(null);
                mangaTitleTextView.setText("");
                previewImageLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPreviewDataCompleted(galleryObject data) {
                previewImage.setImageBitmap(data.getThumbnailBitmap());
                mangaTitleTextView.setText(data.getMangatitle());
            }

            @Override
            public void onlyReceiveBitmapForDummy(Bitmap bitmap) {
                mangaTitleTextView.setText("쿠지락스센세");
                previewImage.setImageBitmap(bitmap);
            }
        });

        previewImageLoading.setVisibility(View.INVISIBLE);
        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.preview(addrTextView.getText().toString());
            }
        });
        downloadStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startService = new Intent(MainActivity.this, DownloadService.class);
                startService.putExtra("threadCount", 5);
                startService.putExtra("galleryAddress", addrTextView.getText().toString());

                startService(startService);
                //TODO 다운로드 서비스 구현
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        stopService(new Intent(MainActivity.this, DownloadService.class));
        mNotificationManager.cancel(1203);
    }
}
