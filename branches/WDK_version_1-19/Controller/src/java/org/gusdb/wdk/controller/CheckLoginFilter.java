package org.gusdb.wdk.controller;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.UserFactory;

public class CheckLoginFilter implements Filter {
    private FilterConfig config = null;
    private ServletContext context = null;
    private static Logger logger = Logger.getLogger(CheckLoginFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
        this.context = config.getServletContext();
        this.context.log("Filter CheckLoginFilter initialized.");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) context.getAttribute(CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) req.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
    
        Cookie cookies[] = req.getCookies();
        Cookie loginCookie = null;
	
        if (cookies != null) {
            for (int i = 0; i < cookies.length; ++i) {
                if (cookies[i].getName().equals(CConstants.WDK_LOGIN_COOKIE_KEY)) {
                    loginCookie = cookies[i];
                    break;
                }
            }
        }
	
        if (loginCookie != null) {
            if (wdkUser == null || !loginCookie.getValue().contains(wdkUser.getSignature())) {
                try {
                    UserFactoryBean factory = wdkModel.getUserFactory();
		    
                    // Check if cookie has been modified since it was set.
                    String secretValue = wdkModel.getSecretKey();

                    secretValue = loginCookie.getValue().substring(0,
                            loginCookie.getValue().lastIndexOf("-"))
                            + secretValue;
                    String cookieHash = loginCookie.getValue().substring(
                            loginCookie.getValue().lastIndexOf("-") + 1);
		    
                    secretValue = factory.encrypt(secretValue);

                    if (!secretValue.equals(cookieHash)) {
                        logger.debug("Secret Value: " + secretValue);
                        logger.debug("Cookie Hash: " + cookieHash);
                        throw new Exception("Login cookie is invalid and must be deleted.");
                    }
		    
                    String signature;
                    String[] cookieParts = loginCookie.getValue().split("-");
		    
                    signature = cookieParts[0];

                    UserBean user = factory.getUser(signature);
                    if (loginCookie.getValue().contains("remember")) {
                        loginCookie.setMaxAge(java.lang.Integer.MAX_VALUE / 256);
                        loginCookie.setPath("/");
                        res.addCookie(loginCookie);
                    }

                    req.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
                    req.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY, "");
                } catch (Exception ex) {
                    logger.error("Caught exception while checking login "
                            + "cookie: " + ex);
                    // tell browser to delete cookie if we had a problem
                    loginCookie.setMaxAge(0);
                    loginCookie.setPath("/");
                    res.addCookie(loginCookie);
                }
            }
        }
        
        // set session id
        HttpSession session = req.getSession();
        session.setAttribute("sessionId", session.getId());

        chain.doFilter(request, response);
    }

    public void destroy() {
        this.context = null;
        this.config = null;
    }
}
