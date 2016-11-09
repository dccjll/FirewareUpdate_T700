package com.dsm.t700.update;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class CommonDownLoadUtil {
	//下载图片
	public static void downloadPicture(final String downUrl, final CommonDownloadCallBack commonDownloadCallBack){
		//创建并启动一个新线程用于从网络上下载图片
        new Thread(){
            @Override
            public void run() {
            	Looper.prepare();
                try {
                    //创建一个url对象
                    URL url=new URL(downUrl);
                    //打开URL对应的资源输入流
                    InputStream is= url.openStream();
                    //从InputStream流中解析出图片
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    if(bitmap == null){
                    	commonDownloadCallBack.downloadPictureFailure("bitmap = null");
                    	return;
                    }
                    commonDownloadCallBack.downloadPictureSuccess(bitmap);
                    //关闭输入流
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    commonDownloadCallBack.downloadPictureFailure(e.getMessage());
                }
                Looper.loop();
            }          
        }.start();
	}
	
	/**
	 * 创建并启动一个新线程用于从网络上下载文件
	 * @param fileserverpath	文件在服务器上的路径 不包括文件名 路径最后一个字符不包括/
	 * @param filename			文件在服务器上的文件名
	 * @param filelocalpath		下载的文件在本地的保存路径 不包括文件名 路径最后一个字符不包括/
	 * @param downloadCallBack	下载状态监听器
	 */
	public static void downloadFile(final String fileserverpath, final String filename, final String filelocalpath, final DownloadCallBack downloadCallBack){
        new Thread(){
            @Override
            public void run() {
            	Looper.prepare();
                try {
                    //创建一个url对象
                    URL url=new URL(fileserverpath + "/" + filename);
                    //打开URL对应的资源输入流
                    InputStream is= url.openStream();
                    if(is == null){
                    	downloadCallBack.downloadFailure("网络错误");
                    	return;
                    }
                    File path = new File(filelocalpath);
                    if(!path.exists()){
                        path.mkdir();
                    }
                    File destFile = new File(filelocalpath + "/" + filename);
                    FileOutputStream os = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while((length = is.read(buffer)) != -1){
                    	os.write(buffer, 0, length);
                    }
                    //关闭流
                    is.close();
                    os.close();
                    downloadCallBack.downloadSuccess(filelocalpath + "/" + filename);
                } catch (Exception e) {
                    e.printStackTrace();
                    downloadCallBack.downloadFailure(e.getMessage());
                }
                Looper.loop();
            }          
        }.start();
	}
	
	public interface DownloadCallBack{
		public void downloadSuccess(String filepathWithName);
		public void downloadFailure(String error);
	}
	
	public interface CommonDownloadCallBack{
		public void downloadPictureSuccess(Bitmap bitmap);
		public void downloadPictureFailure(String error);
	}
}
