package la.hitomi.hitomila;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import la.hitomi.hitomila.common.ExternalViewControlCallback;
import la.hitomi.hitomila.common.hitomiClient;

public class MainActivity extends AppCompatActivity {
    private ImageView previewImage;
    private TextView addrTextView;
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
                if(previewImageLoading.getVisibility() != View.GONE)
                    previewImageLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void receiveBitmap(Bitmap bitmap) {
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
                //TODO 다운로드 서비스 구현
            }
        });

    }

    private void extractPageList(String galleryAddr){
        client.get("https://httpbin.org/get", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                error.printStackTrace(System.out);
            }
        });
    }
}
