package la.hitomi.hitomila.common;

import android.graphics.Bitmap;

/**
 * Created by admin on 2016-10-10.
 */

public interface ExternalViewControlCallback {
    void sendMessage(String message,int ToastLength);
    void processDone();
    void processStart();
    void receiveBitmap(Bitmap bitmap);
}
