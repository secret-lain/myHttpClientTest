package la.hitomi.hitomila.common;

import android.content.Context;
import android.os.Environment;

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

    public hitomiFileWriter(Context mContext){
        parentContext = mContext;
        filePath = Environment.getExternalStorageDirectory().getPath() + "/hitomi";
        directoryWrite();
    }

    private void directoryWrite(){
        File directory = new File(filePath);
        boolean created = false;
        if(!directory.exists())
            created = directory.mkdirs();
        if(created == false)
            throw new RuntimeException("directory Creation Failed");
    }

    public void writeImage(String imageName, byte[] binary){
        File image = new File(filePath, imageName);
        FileOutputStream oStream = null;

        try {
            oStream = new FileOutputStream(image);
            oStream.write(binary);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
