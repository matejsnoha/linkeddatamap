package info.snoha.matej.linkeddatamap.cloud;

import info.snoha.matej.linkeddatamap.Log;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Front Controller - centralized entry point of all requests.<br>
 * (FC/Mediator pattern)
 */
public class FrontController extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static List<Runnable> destroyListeners = new ArrayList<>();

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Log.info("Server started");
	}

	public void destroy() {
		super.destroy();
		synchronized (destroyListeners) {
			for (Runnable listener : destroyListeners) {
				try {
					listener.run();
				} catch (Exception e) {
					Log.warn("Servlet destroy listener exception", e);
				}
			}
		}
		Log.info("Server stopped");
	}

	public static void addDestroyListener(Runnable listener) {
		synchronized (destroyListeners) {
			destroyListeners.add(listener);
		}
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) {

		Context context = null;
		
		try {
			
			// construct Context
			context = new Context(request, response, getServletContext());

			// access log
			Log.info(context.getIpAddress() + " " + request.getMethod() + " " + context.getURI());
			
			// construct the action and execute it
	        Action action = ActionFactory.getAction(request);
	        if (action == null) {
				throw new Exception("404 Action Not Found");
			}
	        
	        String view = action.execute(context);

	        // if view is null, we finish processing
	        if (view != null) {
	        	
	        	if (!view.startsWith("http://") && !view.startsWith("https://")) {

	        		// include view if set 
	        		Log.info("Including view " + view);
		            request.getRequestDispatcher(view).forward(request, response);
		            
		        } else {

		        	// redirect if relative address
		        	Log.info("Redirecting to " + view);
		            response.sendRedirect(view); 
		        }
	        }
	        
	    } catch (Exception ex) {
	    	
	        // send HTTP 500
	        try {	
	        	if (context != null) {
	        		new ErrorReport().execute(context, ex);
				} else {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} catch (IOException e) {
				Log.error("Could not send HTTP 500: " + e);
			}	        
	    }
	}
}
