package org.gusdb.wdk.controller.servlets;

import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.Record;
import org.gusdb.wdk.model.Summary;
import org.gusdb.wdk.model.SummaryInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.RIVList;
import org.gusdb.wdk.model.WdkLogManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * InteractiveRecordListServlet
 *
 * This servlet interacts with the query custom tags to present and validate a form
 * for the user
 *
 * Created: May 9, 2004
 *
 * @author Adrian Tivey
 * @version $Revision$ $Date$ $Author$
 */
public class InteractiveRecordListServlet extends HttpServlet {

    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int MIN_PAGE_SIZE = 5;
    private static final int MAX_PAGE_SIZE = 50;
    
    private static final String SUBVIEW_PREFIX = "/WEB-INF/indirectPages/subviews/";
    
    private static final String RESULT_SUMMARY_PAGE = "/WEB-INF/indirectPages/top/resultSummary.jsp";
    
    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.controller.servlets.InteractiveRecordListServlet");
    
    private final static boolean autoRedirect = true;
    
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
	    int start = 1;
	    int pageSize = DEFAULT_PAGE_SIZE;
	    
		String fromPage = req.getParameter("fromPage");
		String questionSetName = req.getParameter("questionSetName");
		String questionName = req.getParameter("questionName");
		String formName = req.getParameter("formName");
		String defaultChoice = req.getParameter("defaultChoice");
        String initialExpansion = req.getParameter("initialExpansion");
        String startString = req.getParameter("pager.offset");
        String pageSizeString = req.getParameter("pageSize");
        
        
		if (fromPage == null) {
			msg("fromPage shouldn't be null. Internal error", res);
			return;
		}
		if (questionSetName == null) {
			msg("questionSetName shouldn't be null. Internal error", res);
			return;
		}
		if (questionName == null) {
			msg("Qualified questionName shouldn't be null. Internal error", res);
			return;
		}
		if (formName == null) {
			msg("formName shouldn't be null. Internal error", res);
			return;
		}
		if (defaultChoice == null) {
			msg("defaultChoice shouldn't be null. Internal error", res);
			return;
		}
		
        if (questionName.equals(defaultChoice)) {
            req.setAttribute(formName+".error.query.noQuery", "Please choose a query");
            redirect(req, res, fromPage);
            return;
        }
        
        if (questionName.indexOf('.')==-1) {
            msg("question name isn't qualified: "+questionName, res);
            return;
		}
		
        try {
            if ( startString != null) {
                start = Integer.parseInt(startString);
                start++;
            }
        } catch (NumberFormatException exp) {
            // Deliberately ignore - leave as default value
        }
        try {
            if (pageSizeString != null) {
                pageSize = Integer.parseInt(pageSizeString);
            }
        } catch (NumberFormatException exp) {
            // Deliberately ignore - leave as default value
        }
            
        if (start < 1) {
            start = 1;
        }
        if ( pageSize < MIN_PAGE_SIZE) {
            pageSize = MIN_PAGE_SIZE;
        }
        if ( pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        int end = start+pageSize-1;
        
		// We have a queryRecord name
        WdkModel wm = (WdkModel) getServletContext().getAttribute("wdk.wdkModel");
        
        Summary question = wm.getSummary(questionName);
        Query sq = question.getQuery();

        if (sq == null) {
            msg("sq is null for "+questionName, res);
            return;
        }
		QueryInstance sqii = sq.makeInstance();
		Map paramValues = new HashMap();
        SummaryInstance si = null;
		req.setAttribute(formName+".question", question);
        String formQueryPrefix = formName+"."+questionName+".";
        System.err.println("formQueryPrefix is called: "+formQueryPrefix);
        boolean problem = false;
        if ("true".equals(initialExpansion)) {
            problem = true;
        } else {
            // Now check state of params
            Enumeration names = req.getParameterNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                System.err.println("Got an element called: "+name);
                if (name.startsWith(formQueryPrefix)) {
                    // TODO Cope with multiple values
                    String shortName = name.substring(formQueryPrefix.length());
                    paramValues.put(shortName, req.getParameter(name));
                }
            }

            try {
                logger.finest("About to try and set param values");
                si = question.makeSummaryInstance(paramValues, start, end);
            }
            catch (WdkUserException exp) {
                Map errors = exp.getBooBoos();
                problem = true;
                for (Iterator it = errors.keySet().iterator(); it.hasNext();) {
                    Param param = (Param) it.next();
                    String name = param.getName();
                    // FIXME Magic number - struct?
                    String errorMsg = ((String[]) errors.get(param))[1];
                    req.setAttribute(formName+".error."+questionName+"."+name, errorMsg);
                    // TODO Cope with correct values
                }
            }           
            catch (WdkModelException e) {
                // TODO What does this mean?
                e.printStackTrace();
            }

        }

		
		if (problem) {
			// If fail, redirect to page
			redirect(req, res, fromPage);
			return;
		}

          
        String toPage = RESULT_SUMMARY_PAGE;
        
        // TODO Work out size
        int totalSize = 0;
        
        try {
            totalSize = si.getTotalLength();
            logger.severe("The size of the results is "+totalSize);
        }
        catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
        
        if ( end > totalSize ) {
            end = totalSize;
        }
        
        RIVList rivl = null;
        RecordInstance ri = null;
        
        if (totalSize==1 && autoRedirect) {
            toPage="/ViewFullRecord";
            try {
                ri = si.getNextRecordInstance(); 
            }
            catch (WdkModelException exp) {
                throw new RuntimeException(exp);
            }
            //req.setAttribute("ri", ri);
            req.setAttribute("recordName", ri.getRecord().getFullName());
            req.setAttribute("primaryKey", ri.getPrimaryKey());
            
        } else {

            String uriString = req.getRequestURI();
            
            List editedParamNames = new ArrayList();
            for (Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                if (!"pageSize".equals(key) && !"start".equals(key) &&!"pager.offset".equals(key)) {
                    editedParamNames.add(key);
                }
            }
//              String[] values = req.getParameterValues(key);
//              for (int i = 0; i < values.length; i++) {
//                  String val = values[i];
//
//                  if (first) {
//                      uri.append('?');
//                      first = false;
//                  } else {
//                      uri.append('&');
//                  }
//                  uri.append(URLEncoder.encode(key, "UTF8"));
//                  uri.append("=");
//                  uri.append(URLEncoder.encode(val, "UTF8"));
//              }
//          }
//      }
            //Map paramMap = req.getParameterMap();
//            logger.severe("uriString before is "+uriString);
//            uriString = removeArg(uriString, "start");
//            uriString = removeArg(uriString, "pager.offset");
//            uriString = removeArg(uriString, "pageSize");
//            logger.severe("uriString after is "+uriString);
//            StringBuffer uri = new StringBuffer(uriString);
//            boolean first = true;
//            for (Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
//                String key = (String) en.nextElement();
//                if (!"pageSize".equals(key) && !"start".equals(key) &&!"pager.offset".equals(key)) {
//                    String[] values = req.getParameterValues(key);
//                    for (int i = 0; i < values.length; i++) {
//                        String val = values[i];
//
//                        if (first) {
//                            uri.append('?');
//                            first = false;
//                        } else {
//                            uri.append('&');
//                        }
//                        uri.append(URLEncoder.encode(key, "UTF8"));
//                        uri.append("=");
//                        uri.append(URLEncoder.encode(val, "UTF8"));
//                    }
//                }
//            }
//            uri.append("pager.offset=");
//            uri.append(start);
//            logger.severe("uri after is "+uri.toString());
            
            String renderer = getRendererForRecord(question.getRecord());
            
            rivl = new RIVList(si);
            req.setAttribute("rivl", rivl);
            //req.setAttribute("recordListName", queryRecordGroup + "." + queryRecordName);
            req.setAttribute("wdk_paging_total", new Integer(totalSize));
            req.setAttribute("wdk_paging_pageSize", new Integer(pageSize));
            req.setAttribute("wdk_paging_start", new Integer(start));
            req.setAttribute("wdk_paging_end", new Integer(end));
            req.setAttribute("wdk_paging_url", uriString);
            req.setAttribute("wdk_record_url", req.getContextPath()+"/ViewFullRecord?recordReference="+question.getRecord().getFullName());
            req.setAttribute("wdk_paging_params", editedParamNames);
            req.setAttribute("renderer", renderer);
        }
        
        redirect(req, res, toPage);
        
//        try {
//            if (rivl != null) {
//                rivl.close();
//            }
//            if (ri != null) {
//                ri.close();
//            }
//        }
//        catch (WdkModelException exp) {
//            logger.severe(exp.getMessage());
//        }
        
		return;
	}

    private String removeArg(String in, String arg) {
        int start = in.indexOf("?"+arg+"=");
        
        if ( start == -1) {
            start = in.indexOf("&"+arg+"=");
        } else {
            start++;
        }
        if (start != -1) {
            int end = in.indexOf("&", start);
            if (end == -1) {
                end = in.length();
            }
            in = in.substring(0,start)+in.substring(end);
        }
        
        return in;
    }
	
    private String getRendererForRecord(Record record) {
        // TODO Set default for where no renderer found
        String renderer = record.getFullName();
        String path = getServletContext().getRealPath(SUBVIEW_PREFIX+renderer+".jsp");
        File f = new File(path);
        if (f.exists()) {
            return renderer;
        }
        return "default";
        
    }
    
    
    private void msg(String msg, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<html><body bkground=\"white\">"+msg+"</body></html>" );
    }
    
    private void redirect(HttpServletRequest req, HttpServletResponse res, String page) throws ServletException, IOException {
        ServletContext sc = getServletContext();
        RequestDispatcher rd = sc.getRequestDispatcher(page);
        rd.forward(req, res);
    }
    
}
