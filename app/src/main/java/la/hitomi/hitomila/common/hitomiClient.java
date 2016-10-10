package la.hitomi.hitomila.common;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

import static la.hitomi.hitomila.common.hitomiParser.parse;

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
        if(galleryAddr == null || galleryAddr.equals(""))
            dummyPreview();
        if(!hitomiParser.checkGallery(galleryAddr)){
            callback.sendMessage("번호가 뭔가 이상한데", Toast.LENGTH_SHORT);
            return;
        }

        String completeAddress = hitomiParser.getAbsoluteGalleryAddress(galleryAddr);
        get(completeAddress, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String responseString = new String(responseBody);

                if(responseString.contains("유해정보사이트")){//;;;
                    callback.sendMessage("워닝뜸ㅋㅋㅋ개발자새끼한테 문의좀", Toast.LENGTH_SHORT);
                } else{
                    galleryObject galleryVO = hitomiParser.parse(responseString);
                    //TODO galleryVO에서 이미지주소 받아서 이미지 불러오기
                }
                Log.d("hitomiClient","onSuccess(reached gallery page) StatusCode:" + statusCode);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                switch(statusCode){
                    case 404:
                        callback.sendMessage("페이지를 찾을 수 없대", Toast.LENGTH_SHORT);
                        break;
                    default:
                        //아래로 내려감
                        break;
                }
                error.printStackTrace();
            }
        });

    }

    private void dummyPreview() {
        //입력 없으면 강제로 쿠지락스센세의 가이거카운터를 보여준다.
        String addr = "https://d.hitomi.la/galleries/661006/31.jpg";

        get(addr, new BinaryHttpResponseHandler(allowedContentTypes) {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                if(bytesWritten == totalSize/2)
                    callback.sendMessage("절반은 받아졌음",  Toast.LENGTH_SHORT);
            }

            @Override
            public void onStart() {
                //Loading Progressbar view
                callback.processStart();
                callback.sendMessage("딱히 정한 주소가 없지만 기다려봐", Toast.LENGTH_SHORT);
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                //Loading Progressbar dismiss
                callback.processDone();
                callback.sendMessage("쿠지락스센세로 보내드렸습니다^^",Toast.LENGTH_SHORT);
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
