/*
 * Created on Aug 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.gusdb.wdk.controller;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

/**
 * Class supporting access to configuration settings.
 */
public class WdkConfig {

    public static final String NAV_RESULT_PAGE
	= "javax.servlet.jsp.jstl.fmt.locale";

    /**
     * Name of configuration setting for fallback locale
     */
    public static final String FMT_FALLBACK_LOCALE
	= "javax.servlet.jsp.jstl.fmt.fallbackLocale";

    /**
     * Name of configuration setting for i18n localization context
     */
    public static final String FMT_LOCALIZATION_CONTEXT
	= "javax.servlet.jsp.jstl.fmt.localizationContext";

    /**
     * Name of localization setting for time zone
     */
    public static final String FMT_TIME_ZONE
	= "javax.servlet.jsp.jstl.fmt.timeZone";

    /*
     * SQL actions related configuration data
     */

    /**
     * Name of configuration setting for SQL data source
     */
    public static final String SQL_DATA_SOURCE
	= "javax.servlet.jsp.jstl.sql.dataSource";

    /**
     * Name of configuration setting for maximum number of rows to be included
     * in SQL query result
     */
    public static final String SQL_MAX_ROWS
	= "javax.servlet.jsp.jstl.sql.maxRows";
	
    /*
     * Private constants
     */
    private static final String PAGE_SCOPE_SUFFIX = ".page";
    private static final String REQUEST_SCOPE_SUFFIX = ".request";
    private static final String SESSION_SCOPE_SUFFIX = ".session";
    private static final String APPLICATION_SCOPE_SUFFIX = ".application";

    public static Object get(PageContext pc, String name, int scope) {
	switch (scope) {
	case PageContext.PAGE_SCOPE:
	    return pc.getAttribute(name + PAGE_SCOPE_SUFFIX, scope);
	case PageContext.REQUEST_SCOPE:
	    return pc.getAttribute(name + REQUEST_SCOPE_SUFFIX, scope);
	case PageContext.SESSION_SCOPE:
	    return get(pc.getSession(), name);
	case PageContext.APPLICATION_SCOPE:
	    return pc.getAttribute(name + APPLICATION_SCOPE_SUFFIX, scope);
	default:
	    throw new IllegalArgumentException("unknown scope");
	}
    }


    public static Object get(ServletRequest request, String name) {
	return request.getAttribute(name + REQUEST_SCOPE_SUFFIX);
    }


    public static Object get(HttpSession session, String name) {
        Object ret = null;
        if (session != null) {
            try {
                ret = session.getAttribute(name + SESSION_SCOPE_SUFFIX);
            } catch (IllegalStateException ex) {} // when session is invalidated
        }
        return ret;
    }


    public static Object get(ServletContext context, String name) {
	return context.getAttribute(name + APPLICATION_SCOPE_SUFFIX);
    }


    public static void set(PageContext pc, String name, Object value,
			   int scope) {
	switch (scope) {
	case PageContext.PAGE_SCOPE:
	    pc.setAttribute(name + PAGE_SCOPE_SUFFIX, value, scope);
	    break;
	case PageContext.REQUEST_SCOPE:
	    pc.setAttribute(name + REQUEST_SCOPE_SUFFIX, value, scope);
	    break;
	case PageContext.SESSION_SCOPE:
	    pc.setAttribute(name + SESSION_SCOPE_SUFFIX, value, scope);
	    break;
	case PageContext.APPLICATION_SCOPE:
	    pc.setAttribute(name + APPLICATION_SCOPE_SUFFIX, value, scope);
	    break;
	default:
	    throw new IllegalArgumentException("unknown scope");
	}
    }


    public static void set(ServletRequest request, String name, Object value) {
	request.setAttribute(name + REQUEST_SCOPE_SUFFIX, value);
    }


    public static void set(HttpSession session, String name, Object value) {
	session.setAttribute(name + SESSION_SCOPE_SUFFIX, value);
    }


    public static void set(ServletContext context, String name, Object value) {
	context.setAttribute(name + APPLICATION_SCOPE_SUFFIX, value);
    }
 

    public static void remove(PageContext pc, String name, int scope) {
	switch (scope) {
	case PageContext.PAGE_SCOPE:
	    pc.removeAttribute(name + PAGE_SCOPE_SUFFIX, scope);
	    break;
	case PageContext.REQUEST_SCOPE:
	    pc.removeAttribute(name + REQUEST_SCOPE_SUFFIX, scope);
	    break;
	case PageContext.SESSION_SCOPE:
	    pc.removeAttribute(name + SESSION_SCOPE_SUFFIX, scope);
	    break;
	case PageContext.APPLICATION_SCOPE:
	    pc.removeAttribute(name + APPLICATION_SCOPE_SUFFIX, scope);
	    break;
	default:
	    throw new IllegalArgumentException("unknown scope");
	}
    }


    public static void remove(ServletRequest request, String name) {
	request.removeAttribute(name + REQUEST_SCOPE_SUFFIX);
    }


    public static void remove(HttpSession session, String name) {
	session.removeAttribute(name + SESSION_SCOPE_SUFFIX);
    }


    public static void remove(ServletContext context, String name) {
	context.removeAttribute(name + APPLICATION_SCOPE_SUFFIX);
    }
 

    public static Object find(PageContext pc, String name) {
	Object ret = get(pc, name, PageContext.PAGE_SCOPE);
	if (ret == null) {
	    ret = get(pc, name, PageContext.REQUEST_SCOPE);
	    if (ret == null) {
		if (pc.getSession() != null) {
		    // check session only if a session is present
		    ret = get(pc, name, PageContext.SESSION_SCOPE);
		}
		if (ret == null) {
		    ret = get(pc, name, PageContext.APPLICATION_SCOPE);
		    if (ret == null) {
			ret = pc.getServletContext().getInitParameter(name);
		    }
		}
	    }
	}

	return ret;
    }
}