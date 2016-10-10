package la.hitomi.hitomila.common;

import android.graphics.Bitmap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016-10-10.
 */

public class hitomiParser {
    public static final String domain = "https://hitomi.la/";
    public static final String galleryDomain = "https://hitomi.la/galleries/";

    //response 전체에서 해당 정보 추출하기
    public static galleryObject parse(String responseBody){
        String mangatitle = null;
        String thumbnailAddr = null;

        String titleSearchRegex = "(?:<title>)(.*)(?: - Read)";
        Pattern pattern = Pattern.compile(titleSearchRegex);
        Matcher match = pattern.matcher(responseBody);
        if(match.find()){
            mangatitle = match.group(1);
        }

        String thumbnailSearchRegex = "(?:<div class=\"cover\">.*src=\")([^\"]*)";
        pattern = Pattern.compile(thumbnailSearchRegex);
        match = pattern.matcher(responseBody);
        if(match.find()){
            thumbnailAddr = match.group(1);
        }
        thumbnailAddr = "https:" + thumbnailAddr;

        return new galleryObject(thumbnailAddr,mangatitle);
    }
    public static void parse(byte[] responseBody){
        parse(new String(responseBody));
    }

    public static boolean checkGallery(String galleryNumber){
        String regex = "[0-9]*";
        if(galleryNumber.matches(regex))
            return true;
        else return false;
    }

    public static String getAbsoluteGalleryAddress(String galleryName){
        if(checkGallery(galleryName))
            return galleryDomain + galleryName + ".html";
        return galleryName;
    }
}
