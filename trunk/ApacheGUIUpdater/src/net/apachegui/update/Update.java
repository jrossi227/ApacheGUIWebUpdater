package net.apachegui.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Servlet implementation class Update
 */
public class Update extends HttpServlet {
	Logger log = Logger.getLogger(Update.class);
	
	private static final long serialVersionUID = 1L;
    private static String updateInfoURL;
    private static String updaterHome;
	private String platform; 
	private ExecutorService updateThread;

	public enum StatusType {
		Idle, Downloading, Installing, Finished, Error
	}
	
	private static StatusType status=StatusType.Idle;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Update() {
        super();
        // TODO Auto-generated constructor stub
    }

    public synchronized static StatusType getStatus() {
		return Update.status;
	}

	public synchronized static void setStatus(StatusType status) {
		Update.status = status;
	}
    
    @Override
    public void init() throws ServletException {
    	platform = getServletConfig().getInitParameter("platform");
    	updateInfoURL="http://sourceforge.net/projects/jrossi227.u/files/" + platform + "/updateInfo.txt/download";
    	updateThread=Executors.newSingleThreadExecutor();
    	setUpdaterHome((new File(getServletContext().getRealPath("/"))).getAbsolutePath());
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String option=request.getParameter("option");
		PrintWriter out=response.getWriter();
		if(option.equals("getUpdateInfo"))
		{	
			try
			{
			    UpdateInfo update=getDetails();
			    JSONObject details = new JSONObject();
			    details.put("version", update.getVersion());
			    details.put("size", update.getSize());
			    details.put("details", update.getDetails());
			    details.put("compatibility", update.getCompatibility());
			    details.put("compatibilitys", update.getCompatibilitys());
			    
			    out.print(details.toString());

			}
			catch(Exception e)
			{
				response.setContentLength(0);
				response.setStatus(400);
				log.error(e.getMessage(), e); 
			}
		}
		
		if(option.equals("startUpdate"))
		{	
			try
			{
			   if( getStatus() == StatusType.Downloading || getStatus() == StatusType.Installing) {
				   //update is started
			   } else {
				   updateThread.submit(new UpdateThread());
			   }
			   
			}
			catch(Exception e)
			{
				response.setContentLength(0);
				response.setStatus(400);
				log.error(e.getMessage(), e); 
			}
		}
		
		if(option.equals("getUpdateStatus"))
		{	
			try
			{
				
				JSONObject status = new JSONObject();
				
				switch (getStatus()) {
	                
					case Idle:
		            	status.put("status","Starting");
		                break;
					
		            case Downloading:
		            	status.put("status","Downloading");
		            	status.put("progress",UpdateThread.getDownloadPercent());
		                break;
		                         
		            case Installing:
		            	status.put("status","Installing");
		            	try{
		            		if(!new File(System.getProperty("java.io.tmpdir"),"ApacheGUIUpdate").exists()) {
		            			Update.setStatus(Update.StatusType.Finished);
		            		}
		    			}catch(Exception e){}
		            	
		                break;
		                
		            case Finished:
		            	status.put("status","Finished");
		                break; 
		                
		            case Error:
		            	status.put("status","Error");
		            	Update.setStatus(Update.StatusType.Idle);
		                break;     
				}
				
				out.print(status.toString());

			}
			catch(Exception e)
			{
				response.setContentLength(0);
				response.setStatus(400);
				log.error(e.getMessage(), e); 
			}
		}
		
		out.flush();
	}
	
	public static UpdateInfo getDetails() throws IOException, ParserConfigurationException, SAXException {
		URL update = new URL(updateInfoURL);
        HttpURLConnection con = (HttpURLConnection)update.openConnection();
        HttpURLConnection.setFollowRedirects(true);
        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
        
        String inputLine;

        StringBuffer xml=new StringBuffer();
        while ((inputLine = in.readLine()) != null) { 
            xml.append(inputLine);
        }    
        in.close();

        String version="", size="", details="", url="", compatibility="", compatibilitys="";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml.toString()));
        Document doc=builder.parse(is);
        
        Element root = doc.getDocumentElement();
        version=root.getElementsByTagName("file").item(0).getAttributes().getNamedItem("version").getTextContent();
        size=root.getElementsByTagName("file").item(0).getAttributes().getNamedItem("size").getTextContent();
        details=root.getElementsByTagName("details").item(0).getTextContent();
        url=root.getElementsByTagName("file").item(0).getAttributes().getNamedItem("url").getTextContent();
        compatibility=root.getElementsByTagName("file").item(0).getAttributes().getNamedItem("compatibility").getTextContent();
        compatibilitys=root.getElementsByTagName("file").item(0).getAttributes().getNamedItem("compatibilitys").getTextContent();

        return new UpdateInfo(version, size, details, url, compatibility, compatibilitys);
	}

	public static String getUpdaterHome() {
		return updaterHome;
	}

	public static void setUpdaterHome(String updaterHome) {
		Update.updaterHome = updaterHome;
	}
	
	public static String getTomcatDirectory() {
		File current = (new File(updaterHome));
		  
		while(!isTomcatDirectory(current)) {
			  current = current.getParentFile();
		}
		  
		return current.getAbsolutePath();
	}
	
	private static boolean isTomcatDirectory(File file) {
		  
	  if(file.getName().equals("tomcat")) {
		  return true;
	  }
	  
	  return false;
  }
	  

}
