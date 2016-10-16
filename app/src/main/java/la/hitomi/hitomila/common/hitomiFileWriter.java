package la.hitomi.hitomila.common;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by admin on 2016-10-12.
 */

public class hitomiFileWriter {
    private Context parentContext;
    private String filePath;

    public String getFilePath(){
        return filePath;
    }

    public hitomiFileWriter(Context mContext, String directoryName){
        parentContext = mContext;
        filePath = Environment.getExternalStorageDirectory().getPath() + "/hitomi/" + directoryName;
        directoryWrite(directoryName);
    }

    private void directoryWrite(String directoryName){
        File directory = new File(filePath);
        boolean created = false;
        if(!directory.exists()){
            created = directory.mkdirs();
            if(created == false){
                Toast.makeText(parentContext, "파일권한이 없습니다. 앱설정을 해주세요", Toast.LENGTH_SHORT).show();
                throw new RuntimeException("directory Creation Failed - directory.mkdirs()");
            }
        }



    }

    public boolean writeImage(String imageName, byte[] binary){
        imageName = hitomiParser.parseFileNameToTwoDigits(imageName);
        File image = new File(filePath, imageName);
        FileOutputStream oStream = null;

        try {
            oStream = new FileOutputStream(image);
            oStream.write(binary);
            oStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
