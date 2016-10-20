package la.hitomi.hitomila.common;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016-10-10.
 */

public class hitomiParser {
    public static final String domain = "https://hitomi.la/";
    public static final String galleryDomain = "https://hitomi.la/galleries/";
    public static final String readerDomain = "https://hitomi.la/reader/";

    //response 전체에서 해당 정보 추출하기
    public static galleryObject parsePreviewObject(String responseBody){
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
    public static void parsePreviewObject(byte[] responseBody){
        parsePreviewObject(new String(responseBody));
    }

    //고정값에 대해서는 https://hitomi.la/reader.js
    public static Queue<String> extractImageList(String responseBody, String galleryNumber){
        int numberOfFrontEnds = 6; // 알수없는 고정값. 나중에 변경을 대비해 따로 받아오는게 좋을듯
        String titleSearchRegex = "(?:<div class=\"img-url\">)(.*)(?:</div>)";
        Pattern pattern = Pattern.compile(titleSearchRegex);
        Matcher match = pattern.matcher(responseBody);

        Queue<String> result = new LinkedList<>();

        char a = (char) (97 + (Integer.parseInt(galleryNumber)) % numberOfFrontEnds);
        //String prefix = "l" + String.valueOf(a); // l은 미국, 한국 데이터의 접두사.
        String prefix = "ba";
        while(match.find()){
            String data = match.group(1);
            data = Pattern.compile("//.\\.hitomi\\.la/")
                    .matcher(data)
                    //.reset(data)
                    .replaceFirst("https://" + prefix + ".hitomi.la/");


            result.add(data);
        }

        return result;
    }


    public static boolean checkGallery(String galleryNumber){
        String regex = "[0-9]*";
        if(galleryNumber.matches(regex))
            return true;
        else return false;
    }

    public static String getAbsoluteGalleryAddress(String galleryNumber){
        if(checkGallery(galleryNumber))
            return galleryDomain + galleryNumber + ".html";
        return galleryNumber;
    }

    public static String getAbsoulteReaderAddress(String galleryNumber){
        if(checkGallery(galleryNumber))
            return readerDomain + galleryNumber + ".html";
        return galleryNumber;
    }

    public static String getImageNameFromRequestURI(String requestURI){
        String extractImageNameRegex = "(?:galleries\\/\\d+\\/)(.*)";
        Pattern pattern = Pattern.compile(extractImageNameRegex);
        Matcher match = pattern.matcher(requestURI);

        if(match.find()){
            return match.group(1);
        }
        return null;
    }

    public static String parseTitleFromReader(String responseBody) {
        String extractImageNameRegex = "(?:<title>)([^|]*)";
        Pattern pattern = Pattern.compile(extractImageNameRegex);
        Matcher match = pattern.matcher(responseBody);

        if(match.find()){
            return match.group(1);
        }
        return null;
    }

    public static String parseFileNameToTwoDigits(String filename){
        if(isOneDigit(filename))
                return "0" + filename;
        return filename;
    }
    private static boolean isOneDigit(String filename){
        String extractImageNameRegex = "[^\\.]*";
        Pattern pattern = Pattern.compile(extractImageNameRegex);
        Matcher match = pattern.matcher(filename);

        if(match.find())
            if(match.group(0).length() == 1) return true;
        return false;
    }
}
