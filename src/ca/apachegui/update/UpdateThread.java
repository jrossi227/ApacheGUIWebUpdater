package ca.apachegui.update;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class UpdateThread implements Runnable {
	@Override
	public void run() {
		
		try {
			Update.setStatus(Update.StatusType.Downloading);
			
			UpdateInfo info=Update.getDetails();
			File downloadFile=new File(Update.getUpdaterHome(), "files/ApacheGUI.war");
			downloadFile(info.getUrl(), downloadFile);
			 
			//Create File
			new File(System.getProperty("java.io.tmpdir"),"ApacheGUIUpdate").createNewFile();
			
			Update.setStatus(Update.StatusType.Installing);
			renameFile((new File(Update.getUpdaterHome(), "files/ApacheGUI.war")).getAbsolutePath(), (new File((new File(Update.getUpdaterHome())).getParentFile().getAbsolutePath(), "ApacheGUI.war")).getAbsolutePath());
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void downloadFile(String url, File file) throws Exception {
		BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
        	in = new BufferedInputStream(new URL(url).openStream());
            fout = new FileOutputStream(file);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1)
            {
            	fout.write(data, 0, count);
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

}
