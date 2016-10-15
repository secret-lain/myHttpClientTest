package la.hitomi.hitomila.common;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;

import java.util.Queue;

import cz.msebera.android.httpclient.Header;
import la.hitomi.hitomila.DownloadService;

/**
 * Created by admin on 2016-10-10.
 */

public class hitomiClient extends AsyncHttpClient {
    ExternalViewControlCallback callback;
    private final String TAG = "hitomiClient";
    private String[] allowedContentTypes = new String[]{"image/png", "image/jpeg", "image/gif"};

    public String[] getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public hitomiClient(ExternalViewControlCallback callback) {
        this.callback = callback;
    }

    //이곳의 responseBody는 Reader Response이다.
    public void download(String responseBody, String galleryNumber, Context mContext, final DownloadService.notificationCallback callback, final int notificationID){
        final Queue<String> imageList = hitomiParser.extractImageList(responseBody, galleryNumber);
        final String mangaTitle = hitomiParser.parseTitleFromReader(responseBody);
        final hitomiFileWriter writer = new hitomiFileWriter(mContext, mangaTitle);


        callback.initNotification(mangaTitle, imageList.size(), notificationID);
        //최초에 maxConnection 만큼 get request를 일단 던진다. 그 이후에는 재귀적호출. (계속 max유지)
        for(int i = 0 ; i < getMaxConnections() ; i++){
            get(imageList.poll() , new BinaryHttpResponseHandler(allowedContentTypes) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                    Log.d(TAG+"::download", this.getRequestURI().toString() + " download completed");
                    String fileName = hitomiParser.getImageNameFromRequestURI(this.getRequestURI().toString());
                    if(writer.writeImage(fileName,binaryData))
                        callback.notifyPageDownloaded(notificationID);

                    if(!imageList.isEmpty()){
                        try {
                            Thread.sleep(500);
                            get(imageList.poll(), this);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else callback.notifyDownloadCompleted(notificationID);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                    Log.d(TAG+"::download", this.getRequestURI().toString() + " download FAILED\n" +
                            "statusCode : " + statusCode + ", error : " + error.getMessage());
                }
            });
        }
        //이건 변수 하나 지정해서 notify 하고 해당 notify에서 로그박는걸로.
        //Log.d("hitomiClient::download", "download process DONE.");
    }

    private boolean checkAddrVaild(String galleryAddr) {
        if (galleryAddr == null || galleryAddr.equals("")) {
            dummyPreview();
            return false;
        }

        if (!hitomiParser.checkGallery(galleryAddr)) {
            callback.sendMessage("번호가 뭔가 이상한데", Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    public void preview(String galleryAddr) {
        if (checkAddrVaild(galleryAddr) == false || callback == null)
            return;

        String completeAddress = hitomiParser.getAbsoluteGalleryAddress(galleryAddr);
        callback.processStart();
        get(completeAddress, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //갤러리 정보 불러오기 성공
                String responseString = new String(responseBody);

                //갤러리 Response 불러왔으나 워닝이 뜬 신비한 경우
                if (responseString.contains("유해정보사이트")) {//;;;
                    callback.sendMessage("워닝뜸ㅋㅋㅋ개발자새끼한테 문의좀", Toast.LENGTH_SHORT);
                    callback.processDone();
                }
                //갤러리 정보 제대로 불러왔을 경우 (Warning이 아닌 경우)
                else {
                    Log.d(TAG+"::preview", "onSuccess(reached gallery page) StatusCode:" + statusCode);
                    final galleryObject galleryVO = hitomiParser.parsePreviewObject(responseString);
                    get(galleryVO.getThumbnailAddr(), new BinaryHttpResponseHandler(allowedContentTypes) {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                            //섬네일 데이터 불러오기 성공
                            Log.d(TAG+"::download","onSuccess(reached thumbnailImage download) StatusCode:" + statusCode);
                            galleryVO.setThumbnailBitmap(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
                            if (galleryVO.isCompleted()) {
                                callback.onPreviewDataCompleted(galleryVO);
                                callback.processDone();
                            } else throw new RuntimeException("아 몰랑");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                            //섬네일 데이터 불러오기 실패
                            callback.sendMessage("섬네일 데이터가 불러워지지 않았음", Toast.LENGTH_SHORT);
                            callback.processDone();
                            error.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                switch (statusCode) {
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
        //String addr = "https://ld.hitomi.la/galleries/661006/00.jpg";

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
                callback.sendMessage("쿠지락스센세로 보내드렸습니다^^", Toast.LENGTH_SHORT);
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
