package info.snoha.matej.linkeddatamap.cloud;

import info.snoha.matej.linkeddatamap.Log;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rewrites HEAD requests as GET, discards the body and returns correct content-length header
 */
public class HttpHeadToGetFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {
		Log.debug("HTTP HEAD --> GET filter active");
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		if (httpServletRequest.getMethod().equalsIgnoreCase("HEAD")) {
			Log.debug("HEAD --> GET");
			RequestWrapper requestWrapper = new RequestWrapper(httpServletRequest);
			ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
			chain.doFilter(requestWrapper, responseWrapper);
			responseWrapper.setWrappedContentLength();
		} else {
			chain.doFilter(request, response);
		}
	}

	private class RequestWrapper extends HttpServletRequestWrapper {

		public RequestWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getMethod() {
			return "GET";
		}
	}

	private class ResponseWrapper extends HttpServletResponseWrapper {

		private final NoBodyOutputStream noBodyOutputStream = new NoBodyOutputStream();

		private PrintWriter writer;

		public ResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return noBodyOutputStream;
		}

		@Override
		public PrintWriter getWriter() throws UnsupportedEncodingException {
			if (writer == null) {
				writer = new PrintWriter(new OutputStreamWriter(noBodyOutputStream, getCharacterEncoding()));
			}
			return writer;
		}

		void setWrappedContentLength() {
			super.setContentLength(noBodyOutputStream.getContentLength());
		}
	}

	private class NoBodyOutputStream extends ServletOutputStream {

		private AtomicInteger contentLength = new AtomicInteger(0);

		int getContentLength() {
			try {
				flush();
			} catch (IOException e) {
				Log.debug("Could not flush output stream", e);
			}
			Log.debug("content length " + contentLength.get());
			return contentLength.get();
		}

		@Override
		public void write(int b) {
			contentLength.incrementAndGet();
		}

		@Override
		public void write(byte buf[], int offset, int len) {
			contentLength.addAndGet(len);
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			try {
				writeListener.onWritePossible();
			} catch (Exception e) {
				Log.warn("Could not execute listener onWritePossible()", e);
			}
		}
	}
}