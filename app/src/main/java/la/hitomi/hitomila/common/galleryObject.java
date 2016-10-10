package la.hitomi.hitomila.common;

import android.graphics.Bitmap;

/**
 * Created by admin on 2016-10-10.
 */

public class galleryObject {
    private boolean isCompleted;
    private String thumbnailAddr;
    private Bitmap thumbnailBitmap;
    private String mangatitle;

    public galleryObject(String thumbnailAddr, String mangaTitle){
        isCompleted = false;
        this.thumbnailAddr = thumbnailAddr;
        this.mangatitle = mangaTitle;
    }

    public void setThumbnailBitmap(Bitmap image){
        thumbnailBitmap = image;
        isCompleted = true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getThumbnailAddr() {
        return thumbnailAddr;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public String getMangatitle() {
        return mangatitle;
    }

}
