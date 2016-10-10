package la.hitomi.hitomila.common;

import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import la.hitomi.hitomila.MainActivity;

/**
 * Created by admin on 2016-10-10.
 */

public class hitomiClient extends AsyncHttpClient {
    ExternalViewControlCallback callback;
    private String[] allowedContentTypes = new String[]{"image/png", "image/jpeg", "image/gif"};
    public String[] getAllowedContentTypes(){ return allowedContentTypes; }

    public hitomiClient(ExternalViewControlCallback callback){
        this.callback = callback;
    }

    public void preview(String galleryAddr){
        //if(galleryAddr != null || galleryAddr.equals(""))
            dummyPreview();
    }

    private void dummyPreview() {
        //입력 없으면 강제로 쿠지락스센세의 가이거카운터를 보여준다. 킄킄크
        String addr = "https://d.hitomi.la/galleries/661006/31.jpg";


        get(addr, new BinaryHttpResponseHandler(allowedContentTypes) {
            @Override
            public void onStart() {
                //Loading Progressbar view
                callback.processStart();
                callback.sendMessage("좀 기다려", Toast.LENGTH_SHORT);
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                //Loading Progressbar dismiss
                callback.processDone();
                callback.receiveBitmap(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                //ToastMessage
                callback.processDone();
                callback.sendMessage("[" + statusCode + "] connection failed", Toast.LENGTH_LONG);
                error.printStackTrace();
            }
        });
    }
}
