package com.dsm.t700.update.assets;

import android.content.Context;

import com.dsm.t700.update.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by T.C on 2016/8/15.
 */
public class AssetsCopyer {

    private static final String TAG = AssetsCopyer.class.getSimpleName();

    public static void copyFolderFromAssets(Context context, String fileDirPath, String filelocalpath) {
        try {
            String[] fileNames = context.getAssets().list(fileDirPath);
            if (fileNames != null) {
                for (String filename : fileNames) {
                    copyFileFromAssets(context, fileDirPath, filename, filelocalpath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileFromAssets(Context context, String fileDirPath, String filename, String filelocalpath) {
        try {
            InputStream inputStream = context.getAssets().open(fileDirPath + "/" + filename);
            File destFile = new File(filelocalpath);
            if(!destFile.exists()){
                if(!destFile.mkdirs()){
                    return;
                }
            }
            OutputStream outputStream = new FileOutputStream(new File(filelocalpath, filename));

            byte[] buffer = new byte[1024];
            int length = inputStream.read(buffer);
            while(length != -1) {
                outputStream.write(buffer, 0, length);
                length = inputStream.read(buffer);
            }

            outputStream.flush();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllFileFromLocal(String filepath) {
        File file = new File(filepath);
        if(!file.exists()){
            LogUtil.d(TAG, "存放固件的目录不存在");
            return null;
        }

        if (file.isDirectory()) {
            String[] filelist = file.list();
            List<String> firewareList = new ArrayList<>();
            for (String filename : filelist) {
                firewareList.add(filename.substring(0,filename.lastIndexOf(".")));
            }
            return firewareList;
        } else {
            LogUtil.d(TAG, filepath + "不是一个目录");
            return null;
        }
    }

}
