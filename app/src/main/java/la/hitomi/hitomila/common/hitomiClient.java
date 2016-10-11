package la.hitomi.hitomila.common;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

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
        if(galleryAddr == null || galleryAddr.equals("")){
            dummyPreview();
            return;
        }

        if(!hitomiParser.checkGallery(galleryAddr)){
            callback.sendMessage("번호가 뭔가 이상한데", Toast.LENGTH_SHORT);
            return;
        }

        String completeAddress = hitomiParser.getAbsoluteGalleryAddress(galleryAddr);
        callback.processStart();
        get(completeAddress, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //갤러리 정보 불러오기 성공
                String responseString = new String(responseBody);

                //갤러리 Response 불러왔으나 워닝이 뜬 신비한 경우
                if(responseString.contains("유해정보사이트")){//;;;
                    callback.sendMessage("워닝뜸ㅋㅋㅋ개발자새끼한테 문의좀", Toast.LENGTH_SHORT);
                    callback.processDone();
                }
                //갤러리 정보 제대로 불러왔을 경우 (Warning이 아닌 경우)
                else{
                    Log.d("hitomiClient","onSuccess(reached gallery page) StatusCode:" + statusCode);
                    final galleryObject galleryVO = hitomiParser.parse(responseString);
                    get(galleryVO.getThumbnailAddr(), new BinaryHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                            //섬네일 데이터 불러오기 성공
                            Log.d("hitomiClient","onSuccess(reached thumbnailImage download) StatusCode:" + statusCode);
                            galleryVO.setThumbnailBitmap(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
                            if(galleryVO.isCompleted()){
                                callback.onPreviewDataCompleted(galleryVO);
                                callback.processDone();
                            }
                            else throw new RuntimeException("아 몰랑");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                            //섬네일 데이터 불러오기 실패
                            callback.sendMessage("섬네일 데이터가 불러워지지 않았음",Toast.LENGTH_SHORT);
                            callback.processDone();
                            error.printStackTrace();
                        }
                    });
                }
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
                callback.processDone();
                error.printStackTrace();
            }
        });

    }

    private void dummyPreview() {
        //입력 없으면 강제로 쿠지락스센세의 가이거카운터를 보여준다.
        String addr = "https://d.hitomi.la/galleries/661006/31.jpg";

        get(addr, new BinaryHttpResponseHandler(allowedContentTypes) {
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
                callback.onlyReceiveBitmapForDummy(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
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
