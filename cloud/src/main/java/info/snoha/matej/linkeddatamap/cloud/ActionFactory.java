package info.snoha.matej.linkeddatamap.cloud;

import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.cloud.layers.DataLayerListAction;
import info.snoha.matej.linkeddatamap.cloud.layers.LayerGetAction;
import info.snoha.matej.linkeddatamap.cloud.layers.MapLayerListAction;
import info.snoha.matej.linkeddatamap.cloud.status.StatusAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 *  Returns the action to execute based on the request.
 */
public class ActionFactory {

	private static Map<String, Action> actions = new LinkedHashMap<>();
	
	static {

		/////////////////////////////////////////
		//     CURRENT API 1 for LDM >= 1.1    //
		/////////////////////////////////////////
		
		// order from longest prefix to shortest prefix 
		
		// status
		
		actions.put(
				"GET/api/1/status",
				new StatusAction());

		// layers

		actions.put(
				"GET/api/1/layers/\\?type=map",
				new MapLayerListAction());

		actions.put(
				"GET/api/1/layers/\\?type=data",
				new DataLayerListAction());

		actions.put(
				"GET/api/1/layers/.*",
				new LayerGetAction());
		
		// static web
		
		actions.put(
				"GET/api/favicon.ico",
				new RedirectAction("/static/favicon.ico"));
		
		actions.put(
				"GET/", // last, this will match everything
				new RedirectAction("/static/index.html"));
		
		///////////////////////////////////////////////
		
	}

	public static String getRoute(HttpServletRequest request) {

		return request.getMethod()
				+ request.getRequestURI()
				+ (request.getQueryString() != null ? "?"
				+ request.getQueryString() : "");
	}
	
	/**
	 * Returns instance of an action to execute based on the request
	 * 
	 * @param request
	 * @return the action to execute 
	 */
	public static Action getAction(HttpServletRequest request) {
		
		// look up action
		String requestRoute = getRoute(request);
		Action result = actions.get(requestRoute);
		if (result == null) {

			// try as regex
			for (Entry<String, Action> entry : actions.entrySet()) {
				if (Pattern.compile(entry.getKey()).matcher(requestRoute).find()) { // TODO cache compiled
					
					// found as regex
					return entry.getValue();
				}
			}
			
			// nothing found
			
			// Jetty 404
			return getStatusCodeAction(HttpServletResponse.SC_NOT_FOUND, null);
			
			// redirect to web
			// return new RedirectAction("https://ldm.matej.snoha.info");
			
			// meta refresh redirect, so broken API clients don't get the whole website
			// return new RedirectAction("/static/index.html");
			
			// Error handler
			// return null;
			
		} else {
			
			// found as full match
			return result;
		}
	}
	
	/**
	 * Returns instance of an action which only sends HTTP status
	 * 
	 * @param statusCode the HTTP status code to sent when executed
	 * @param message optional reason message, can be null
	 */
	public static Action getStatusCodeAction(int statusCode, String message) {
		if (statusCode < HttpServletResponse.SC_BAD_REQUEST) {
			return new StatusCodeAction(statusCode, message);	
		} else {
			return new StatusCodeErrorAction(statusCode, message);
		}
	}
	
	/**
	 * Action which only sets the status code. 
	 */
	private static class StatusCodeAction implements Action {

		protected int statusCode;
		protected String message;

		/**
		 * 
		 * @param statusCode the HTTP status code to sent when executed
		 * @param message optional reason message, can be null
		 */
		public StatusCodeAction(int statusCode, String message) {
			this.statusCode = statusCode;
			this.message = message;
			
		}
		
		protected void log(Context context) {
			String data = context.getAllData(); 
			Log.info("HTTP " + statusCode
					+ (message != null ? " " + message : "")
					+ (data != null && data.length() > 0 ? " DATA:\n" + data : ""));
		}

		@Override
		public String execute(Context context) throws Exception {
			
			log(context);
			context.getResponse().setStatus(statusCode);
			if (message != null) {
				context.getResponse().getWriter().println(message);
			}
			return null;
		}
	}

	/**
	 * Action which returns the specified error. 
	 */
	private static class StatusCodeErrorAction extends StatusCodeAction {

		/**
		 * @param statusCode the HTTP status code to sent when executed
		 * @param message optional reason message, can be null
		 */
		public StatusCodeErrorAction(int statusCode, String message) {
			super(statusCode, message);
		}

		@Override
		public String execute(Context context) throws Exception {
			
			log(context);
			context.getResponse().sendError(statusCode, message);
			return null;
		}
	}
	
	/**
	 * Action which sends a redirect
	 */
	private static class RedirectAction implements Action {

		final String target;
		
		public RedirectAction(String target) {
			this.target = target;
		}
		
		@Override
		public String execute(Context context) throws Exception {
			return target;
		}		
	}
}