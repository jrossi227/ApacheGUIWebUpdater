package ca.apachegui.update;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateThread implements Runnable {
	private static long progress=0;
	private static long size=0;
	
	@Override
	public void run() {
		
		try {
			Update.setStatus(Update.StatusType.Downloading);
			
			UpdateInfo info=Update.getDetails();
			File downloadFile=new File(Update.getUpdaterHome(), "files/ApacheGUI.war");
			
			setProgress(0);
			setSize(0);
			downloadFile(info.getUrl(), downloadFile);
			 
			//Create File
			new File(System.getProperty("java.io.tmpdir"),"ApacheGUIUpdate").createNewFile();
			
			Update.setStatus(Update.StatusType.Installing);
			renameFile((new File(Update.getUpdaterHome(), "files/ApacheGUI.war")).getAbsolutePath(), (new File(Update.getTomcatDirectory(), "webapps/ApacheGUI.war")).getAbsolutePath());
		} 
		catch (Exception e) {
			Update.setStatus(Update.StatusType.Error);
			e.printStackTrace();
		}
	}
	
	private void downloadFile(String url, File file) throws Exception {
		BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
        	HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        	conn.setRequestMethod("GET");
        	conn.setDoInput(true);
        	HttpURLConnection.setFollowRedirects(true);
        	conn.connect();
        	conn.setReadTimeout(60000);
        	setSize(conn.getContentLength());
        	InputStream is = conn.getInputStream();
        	
        	in = new BufferedInputStream(is);
            fout = new FileOutputStream(file);
            
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1)
            {
            	fout.write(data, 0, count);
            	setProgress(getProgress() + count);
            }
        }
        finally
        {
        	if (in != null)
        		in.close();
        	if (fout != null)
        		fout.close();
        }
	}
	
	private void renameFile(String oldName, String newName) throws IOException {
	    File srcFile = new File(oldName);
	    boolean bSucceeded = false;
	    try {
	        File destFile = new File(newName);
	        if (destFile.exists()) {
	            if (!destFile.delete()) {
	                throw new IOException(oldName + " was not successfully renamed to " + newName); 
	            }
	        }
	        if (!srcFile.renameTo(destFile))        {
	            throw new IOException(oldName + " was not successfully renamed to " + newName);
	        } else {
	                bSucceeded = true;
	        }
	    } finally {
	          if (bSucceeded) {
	                srcFile.delete();
	          }
	    }
	}

	public static long getProgress() {
		return progress;
	}

	public synchronized static void setProgress(long progress) {
		UpdateThread.progress = progress;
	}

	public static long getSize() {
		return size;
	}

	public synchronized static void setSize(long size) {
		UpdateThread.size = size;
	}
	
	public static long getDownloadPercent() {
		if(size==0 || progress==0) {
			return 0;
		}
		
		double percent=(progress * 100.0d)/size;
		
		return (long) percent;
	}

}
