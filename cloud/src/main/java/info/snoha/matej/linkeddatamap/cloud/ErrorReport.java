package info.snoha.matej.linkeddatamap.cloud;

import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.UIDUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.http.HttpServletResponse;

public class ErrorReport {
	
	public String execute(Context context, Exception ex) {

		String id = UIDUtils.getErrorID();
		
    	String publicReport =
			"Error ID: " + id + "\n"
    		+ "Server: " + CloudUtils.getHostname() + "\n"
    		+ "Time: " + System.currentTimeMillis() + "\n";
	
    	String privateReport = 
    			"Request: " + context.getURI() + "\n";
    	
    	if (ex != null) {
    		
    		privateReport += "Ex: " + ex + "\n"
        		+ "Ex details: " + ExceptionUtils.getStackTrace(ex) + "\n"; 
    		
    		if (ex.getCause() != null) {
    			
    			privateReport += "Cause: " + ex.getCause() + "\n"
    	        		+ "Cause details: " + ExceptionUtils.getStackTrace(ex.getCause()) + "\n";
    		}
    	}
    	
        Log.info("Sending error report: " + publicReport + privateReport);

        context.setContentType(Context.ContentType.HTML);
        context.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
        context.writeString("<html>\n"
        		+ "<body>\n"
        		+ "<h1>Error</h1>\n"
        		+ "<h2>Linked Data Map Cloud was unable to process your request</h2>\n"
        		+ "If you need help with this issue, please contact\n"
        		+ "<a href=\"mailto:matej@snoha.info\">matej@snoha.info</a>\n"
        		+ "and include the following report:<br><br>\n"
        		+ "<pre>\n"
        		+ "======= Linked Data Map Cloud Error Report =======\n"
        		+ publicReport
        		+ "===========================================\n"
        		+ "</pre>\n"
        		+ "</body>\n"
        		+ "</html>\n");

        return null;
    }
}
