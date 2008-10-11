package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.Exception;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * <p>Filter protects resources such that only specified usernames, as 
 * authenticated with CAS, can access.</p>
 * @created: Nov. 15, 2007
 * @author Junmin Liu
 */

public class BetaCellLoginFilter implements Filter {

    //*********************************************************************
    // Constance
    private static final String BCBC_COOKIE_NAME = "genomics_cookie";
    private static final String SECRET_KEY = "01234567890abcde";
    private static final String IV_PARAMETER = "fedcba9876543210";
    private static final Log log = LogFactory.getLog(BetaCellLoginFilter.class);
    

    //*********************************************************************
    // Configuration state
    private FilterConfig config;

    //*********************************************************************
    // Initialization 

    public void init(FilterConfig config) throws ServletException {
	log.trace("entering init()");
	this.config = config;

    }

    //*********************************************************************
    // Filter processing

    public void doFilter(
			 ServletRequest request,
			 ServletResponse response,
			 FilterChain fc)
	throws ServletException, IOException {
	//	throws ServletException, IOException {
	
	if (log.isTraceEnabled()){
	    log.trace("entering doFilter(" + request + ", " + response + ", " + fc + ")");
	}

	// make sure we've got an HTTP request
	if (!(request instanceof HttpServletRequest)
	    || !(response instanceof HttpServletResponse)) {
	    log.error("doFilter() called on instance of HttpServletRequest or HttpServletResponse.");
	    throw new ServletException(
				       BetaCellLoginFilter.class.getName() + ": protects only HTTP resources");
	}

	HttpServletRequest hsRequest = (HttpServletRequest) request;
	HttpServletResponse hsResponse = (HttpServletResponse) response;
	String path = hsRequest.getServletPath();

	if(path.endsWith(".do") || path.endsWith(".jsp")){
	    log.info("entering doFilter(" + request + ", " + response + ", " + fc + ")");

	
	    Cookie[] cookies = hsRequest.getCookies();
	    String genomicsCookie = getCookieValue(cookies, BCBC_COOKIE_NAME, "");

	    genomicsCookie = (new URLDecoder()).decode(genomicsCookie);
	    log.info("genomicsCookie: "+genomicsCookie);
	
	    if(!genomicsCookie.equals("") && genomicsCookie.length() > 0){
		try{
		    creatWDKUser(genomicsCookie, hsRequest, hsResponse);
		}catch(Exception ex){
		    log.error(ex.getMessage());
		}
	    }
	    else{
		try{
		    logOutUser(hsRequest, hsResponse);
		}catch(Exception ex){
		    log.error(ex.getMessage());
		}
	    }
	}

	fc.doFilter(request, response);
//	log.info("returning from doFilter()");
    }

    //*********************************************************************
    // Destruction

    public void destroy() {
	this.config = null;
    }



    private void logOutUser (HttpServletRequest hsRequest, HttpServletResponse hsResponse) throws Exception{
	WdkModelBean wdkModel = (WdkModelBean) this.config.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);	
	UserFactoryBean factory = wdkModel.getUserFactory();

	// get the current user
	UserBean guest = (UserBean) hsRequest.getSession().getAttribute(CConstants.WDK_USER_KEY);
	// if guest is null, means the session is timed out, create the guest
	// again
	if (guest == null || !guest.getGuest()) {
	    guest = factory.getGuestUser();
	    hsRequest.getSession().setAttribute(CConstants.WDK_USER_KEY, guest);
	}
	hsRequest.getSession().setAttribute("privacy", "public");
	hsRequest.getSession().setAttribute("bcbcSessionId", "");	   
    }


/**
@bcbc cookie: Email | fname | lname | status | facultyid | sessionid
where 
email= BETACELL_PROFILE.EMAIL
fname=BETACELL_PROFILE.FIRST_NAME
lname=BETACELL_PROFILE.LAST_NAME
status='Private' or 'Public', based on 'member_of_any_group' or 'not'
facultyid=$_SESSION['id2'] (this is set when someone is logged in,
regardless of membership status)
sessionid=sessionid()
*/
    private void creatWDKUser (String genomicsCookie, HttpServletRequest hsRequest, HttpServletResponse hsResponse) throws Exception{
	String[] cookieValues = decrypt(genomicsCookie).split("\\|");
	//String[] cookieValues = genomicsCookie.split("\\|");
	WdkModelBean wdkModel = (WdkModelBean) this.config.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);	

	UserFactoryBean factory = wdkModel.getUserFactory();

	String email = cookieValues[0];
	String firstName = cookieValues[1];
	String lastName = cookieValues[2];

	String privacy = (cookieValues.length>3)?cookieValues[3]:"public";
	String bcbcSessionId = (cookieValues.length>5)?cookieValues[5]:"";

	String middleName = null, title = null, organization = null;
	String department = null, address = null, city = null, state = null;
	String zipCode = null, phoneNumber = null, country = null;

	// get the current user
	UserBean guest = (UserBean) hsRequest.getSession().getAttribute(CConstants.WDK_USER_KEY);
	// if guest is null, means the session is timed out, create the guest
	// again
	if (guest == null) {
	    guest = factory.getGuestUser();
	    hsRequest.getSession().setAttribute(CConstants.WDK_USER_KEY, guest);
	}

	if (!guest.getGuest()) { 
	    // user has been logged in, redirect back, do nothing
	    log.info("user already logged in, do nothing");
	    hsRequest.getSession().setAttribute("privacy", privacy);
	    hsRequest.getSession().setAttribute("bcbcSessionId", bcbcSessionId);
	    return;
	    //	    hsResponse.sendRedirect(hsResponse.encodeRedirectURL(hsRequest.getRequestURL().toString()));
	}
	
	Map<String, String> globalPreferences = new LinkedHashMap<String, String>();
	Map<String, String> projectPreferences = new LinkedHashMap<String, String>();

	try {
	    UserBean user = factory.loadUser(email);
	    hsRequest.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
	    hsRequest.getSession().setAttribute("privacy", privacy);
	    hsRequest.getSession().setAttribute("bcbcSessionId", bcbcSessionId);
            hsRequest.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY, "");

	} catch (WdkUserException ex) {
	    log.error(ex.getMessage());
	    try {

		UserBean user = factory.createUserNoPass(email, lastName, firstName);
		  /*					 middleName, title, organization, 
							 department, address, city,
							 state, zipCode, phoneNumber, country, 
							 globalPreferences,
							 projectPreferences);*/
		hsRequest.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
		hsRequest.getSession().setAttribute("privacy", privacy);
		hsRequest.getSession().setAttribute("bcbcSessionId", bcbcSessionId);
		hsRequest.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY, "");
	    }
	    catch (Exception ex2){
		log.error(ex2.getMessage());
		// user authentication failed, set the error message
		hsRequest.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY,
						  ex2.getMessage());
	    }
        }



    }

    //*********************************************************************
    // Utility method

    public String getCookieValue(Cookie[] cookies, String cookieName, String defaultValue){
	if(cookies != null){
	    for(int i=0; i<cookies.length; i++){
		Cookie cookie = cookies[i];
		if(cookieName.equals(cookie.getName())){
		    return (cookie.getValue());
		}
	    }
	}
	
	return defaultValue;
    }
    
    private String decrypt  (String cookieStr) throws Exception {
	log.info("genomicsCookie before descrypt: "+cookieStr);
    	Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
    	SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
    	IvParameterSpec ivSpec = new IvParameterSpec(IV_PARAMETER.getBytes());
    	cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
    	byte[] outText = cipher.doFinal(hexToBytes(cookieStr));

	log.info("genomicsCookie after descrypt: "+new String(outText).trim());
    	return new String(outText).trim();	
    }
    
    private byte[] hexToBytes(String str) {
    	if (str==null) {
	    return null;
    	} else if (str.length() < 2) {
	    return null;
    	} else {
	    int len = str.length() / 2;
	    byte[] buffer = new byte[len];
	    for (int i=0; i<len; i++) {
		buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
	    }
	    return buffer;
    	}
    }
}
