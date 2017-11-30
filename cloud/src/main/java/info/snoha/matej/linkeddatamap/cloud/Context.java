package info.snoha.matej.linkeddatamap.cloud;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import info.snoha.matej.linkeddatamap.Log;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * Context encapsulates request and response objects 
 * and provides convenience methods.<br>
 * (Facade pattern) 
 */
public class Context {
	
	private ServletContext servletContext;
	
	/**
	 * Content types
	 */
	public enum ContentType {

		BINARY("application/octet-stream"),
		HTML("text/html"),
		JSON("application/json"),
		NONE(""),
		TEXT("text/plain"),		
		XHTML("application/xhtml+xml"),
		XML("application/xml");
		
		private final String name;
		
		ContentType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Request
	 */
	private HttpServletRequest request;
	
	/**
	 * Response
	 */
	private HttpServletResponse response;
	
	/**
	 * GSON instance for deserializing JSON data
	 */
	private static final Gson gson = new Gson();

	/**
	 * Constructs a context from request and response objects
	 * 
	 * @param request
	 * @param response
	 */
	public Context(HttpServletRequest request, HttpServletResponse response, 
			ServletContext servletContext) {
		
		this.request = request;
		this.response = response;
		this.servletContext = servletContext;

		// both request and response are UTF-8
		response.setCharacterEncoding("UTF-8");
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}
	
	public ServletContext getServletContext() {
		return servletContext;
	}
	
	/**
	 * @return URI of the request
	 */
	public String getURI() {
		
		return request.getScheme()
			+ "://"
			+ request.getServerName()
			+ ("http".equals(request.getScheme())
					&& request.getServerPort() == 80
					|| "https".equals(request.getScheme())
					&& request.getServerPort() == 443 ? "" : ":"
					+ request.getServerPort())
			+ request.getRequestURI()
			+ (request.getQueryString() != null ? "?"
					+ request.getQueryString() : "");
	}
	
	/**
	 * Returns JSON-deserialized request parameter by its key.
	 * 
	 * @param key of the "application/x-www-form-urlencoded" key-value pair
	 * @return the resulting class of type T or null if it could not be deserialized
	 */
	public <T> T getJsonData(String key) {
		try {

			// get request body data
			String value = getData(key);
			
			// deserialize
			Type type = new TypeToken<T>() {}.getType();
			T result = gson.fromJson(value, type);
			
			return result;
			
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Returns JSON-deserialized request parameter by its key.
	 *
	 * @param key of the "application/x-www-form-urlencoded" key-value pair
	 * @return the resulting class of type T or null if it could not be deserialized
	 */
	public <T> T getJsonData(String key, Class<T> cls) {
		try {

			// get request body data
			String value = getData(key);

			// deserialize
			T result = gson.fromJson(value, cls);

			return result;

		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Returns JSON-deserialized request parameter by its key.
	 *
	 * @param key of the "application/x-www-form-urlencoded" key-value pair
	 * @return the resulting class of type T or null if it could not be deserialized
	 */
	public <T> T getJsonData(String key, TypeToken<T> typeToken) {
		try {

			// get request body data
			String value = getData(key);

			// deserialize
			T result = gson.fromJson(value, typeToken.getType());

			return result;

		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Returns request body data by its key.
	 * 
	 * @param key of the "application/x-www-form-urlencoded" key-value pair
	 * @return the resulting parameter as String
	 */
	public String getData(String key) {
		try {

			return request.getParameter(key);
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Returns all request data
	 * 
	 * @return the resulting data as String
	 */
	public String getAllData() {
		try {

			StringBuilder buffer = new StringBuilder();
		    BufferedReader reader = request.getReader();
		    String line;
		    while ((line = reader.readLine()) != null) {
		        buffer.append(line);
		    }
		    return buffer.toString();
			
		} catch (Exception ex) {
			return null;
		}
	}

	public String getClientVersion() {
		try {
			return request.getHeader("User-Agent").split("\\s+")[0].split("/")[1];
		} catch (Exception ex) {
			return null;
		}
	}

	public void setContentType(ContentType contentType) {
		response.setContentType(contentType.toString());
	}
	
	
	/**
	 * Writes Object as JSON to response
	 * 
	 * @param data
	 */
	public void writeJsonData(Object data) {
		try {
			
			response.getWriter().println(gson.toJson(data));
			response.getWriter().flush();
			
		} catch (Exception ex) {
			Log.error("Could not write Json to response", ex);
		}
	}
	
	/**
	 * Writes String to response
	 */
	public void writeString(String content) {
		try {
			
			response.getWriter().print(content);
			response.getWriter().flush();
			
		} catch (Exception ex) {
			Log.error("Could not write String to response", ex);
		}
	}
	
	/**
	 * Writes stream data to response
	 * 
	 * @param data
	 */
	public void writeStream(InputStream data) {
		try {
			
			IOUtils.copy(data, response.getOutputStream());
			
		} catch (Exception ex) {
			Log.error("Could not write Stream to response", ex);
		}
	}
	
	public void setStatusCode(int statusCode) {
		response.setStatus(statusCode);
	}

	public String getIpAddress() {
		String ip = request.getRemoteAddr();
		if (ip.startsWith("127.")
				|| ip.startsWith("0:")
				|| ip.startsWith("fe80:")) {

			ip = request.getHeader("X-Real-IP"); // added by our nginx conf
		}
		return ip;
	}
}
