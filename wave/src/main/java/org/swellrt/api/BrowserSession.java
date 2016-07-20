package org.swellrt.api;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Cookies;

/**
 * Shared logic to manage session data in the browser side.
 * 
 * See {@link SessionManagerImpl} and {@link HttpWindowSessionFilter} classes for
 * server side related logic. 
 * 
 * @author pablojan@gmail.com (Pablo Ojanguren)
 *
 */
public class BrowserSession {
	
	/** A property in localStorage to count each new tab/window */
	private final static String PROP_BROWSER_WINDOW_COUNTER = "wc";
	
	/** A window's property storing the current window id */ 
	private final static String PROP_SESSION_WINDOW_ID = "wid";
	
	/** A HTTP header to send window id */
	public final static String HTTP_HEADER_WINDOW_ID = "X-window-id";
	
	/** Name of session cookie */
	private final static String SESSION_COOKIE_NAME = "WSESSIONID";
	
	/** URL parameter name for session id */
	private final static String SESSION_PATH_PARAM = "sid";
	
	/** URL parameter for window id */
	private final static String WINDOWID_PATH_PARAM = "wid";
	
	
	public static native void init() /*-{
	
		var PROP_BROWSER_WINDOW_COUNTER = @org.swellrt.api.BrowserSession::PROP_BROWSER_WINDOW_COUNTER;
		var PROP_SESSION_WINDOW_ID = @org.swellrt.api.BrowserSession::PROP_SESSION_WINDOW_ID;
		
	
	    try {
	
	      if (!$wnd.localStorage) return;
	
	      // Generate a browser window/tab id
	      if (!$wnd[PROP_SESSION_WINDOW_ID]) {
	
	        if (!$wnd.localStorage.getItem(PROP_BROWSER_WINDOW_COUNTER)) {
	          $wnd.localStorage.setItem(PROP_BROWSER_WINDOW_COUNTER, 0);
	        }
	
	        var windowCount = $wnd.localStorage.getItem(PROP_BROWSER_WINDOW_COUNTER);
	        $wnd[PROP_SESSION_WINDOW_ID] = windowCount++;
	        $wnd.localStorage.setItem(PROP_BROWSER_WINDOW_COUNTER, windowCount);
	      }
	
	    } catch (e) {
	      console.log("Error generating session window id: "+e);
	    }
		
	
	}-*/;
	
	
	public static native String getToken() /*-{
	
		var PROP_BROWSER_WINDOW_COUNTER = @org.swellrt.api.BrowserSession::PROP_BROWSER_WINDOW_COUNTER;
		var PROP_SESSION_WINDOW_ID = @org.swellrt.api.BrowserSession::PROP_SESSION_WINDOW_ID;
		
		var token = $wnd.__session['sessionid'];
	    	    
	    try {
	      if ($wnd[PROP_SESSION_WINDOW_ID] != null) {
	        token+=":"+$wnd[PROP_SESSION_WINDOW_ID];
	       }
	     } catch(e) {
			console.log("Error getting session token: "+e);
	     }
	    return token;
	
	}-*/;
	
	
	public static native String getSessionId() /*-{
		return $wnd.__session['sessionid'];
	}-*/;
	
	
	public static native String getWindowId() /*-{
		var PROP_SESSION_WINDOW_ID = @org.swellrt.api.BrowserSession::PROP_SESSION_WINDOW_ID;
		return $wnd[PROP_SESSION_WINDOW_ID];
	}-*/;
	
	
	/**
	 * Define session data in a window object.
	 * 
	 * This is the old way from Wave to share session data
	 * in the client code.
	 *
	 * @param localDomain
	 * @param userAddress
	 * @param sessionId
	 */
	public static native JavaScriptObject setUserData(String localDomain, String userAddress, String seed,
			String sessionId) /*-{
		$wnd.__session = new Object();
		$wnd.__session['domain'] = localDomain;
		$wnd.__session['address'] = userAddress;
		$wnd.__session['id'] = seed; // 'id' is used in Session.java/ClientIdGenerator to get the seed
		$wnd.__session['sessionid'] = sessionId; //
		return $wnd.__session;
		
	}-*/;


	protected static native void clearSessionObject() /*-{
		$wnd.__session = null;
	}-*/;
	
	public static void clearUserData() {		
		Cookies.removeCookie(SESSION_COOKIE_NAME);
		clearSessionObject();
	}
	
	
	public static String getSessionURLparameter() {
	    if (Cookies.getCookie(SESSION_COOKIE_NAME) == null) {
	      return ";" + SESSION_PATH_PARAM + "=" + getSessionId();
	    }
	    return "";
	}
	
	public static String getWindowURLparameter() {
		return (getWindowId() != null ? "&"+WINDOWID_PATH_PARAM+"=" + getWindowId() : "");
	}

	/**
	 * Add an extra path token for session id (;sid=) at the end of the URL path
	 * if session cookie is not available. This is specific for the Jetty
	 * server.
	 *
	 * @param url
	 *            The url where to add the session id
	 * @return
	 */
	public static String addSessionToUrl(String url) {
		if (Cookies.getCookie(SESSION_COOKIE_NAME) == null &&  getSessionId() != null) {
			url += ";" + SESSION_PATH_PARAM + "=" +  getSessionId();
		}
		return url;
	}

}
