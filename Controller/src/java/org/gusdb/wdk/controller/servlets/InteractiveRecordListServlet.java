package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.RecordList;
import org.gusdb.gus.wdk.model.RecordListInstance;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.model.WdkUserException;
import org.gusdb.gus.wdk.view.QueryRecordGroupMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    private final static boolean autoRedirect = true;
    
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
		String fromPage = req.getParameter("fromPage");
		String queryRecordGroup = req.getParameter("recordGroup");
		String queryRecordName = req.getParameter("queryRecordName");
		String formName = req.getParameter("formName");
		String defaultChoice = req.getParameter("defaultChoice");
        String initialExpansion = req.getParameter("initialExpansion");
        
		if (fromPage == null) {
			msg("fromPage shouldn't be null. Internal error", res);
			return;
		}
		if (queryRecordGroup == null) {
			msg("queryRecordGroup shouldn't be null. Internal error", res);
			return;
		}
		if (queryRecordName == null) {
			msg("queryRecordName shouldn't be null. Internal error", res);
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
		
        if (queryRecordName.equals(defaultChoice)) {
            req.setAttribute(formName+".error.query.noQuery", "Please choose a query");
            redirect(req, res, fromPage);
            return;
        }
        
        if (queryRecordName.indexOf('.')==-1) {
            msg("queryRecord name isn't qualified: "+queryRecordName, res);
            return;
		}
		
		// We have a queryRecord name
        WdkModel wm = (WdkModel) getServletContext().getAttribute("wdk.wdkModel");
        
        RecordList recordList = QueryRecordGroupMgr.getRecordList(wm, queryRecordName);
        Query sq = recordList.getQuery();

        if (sq == null) {
            msg("sq is null for "+queryRecordName, res);
            return;
        }
		QueryInstance sqii = sq.makeInstance();
		Map paramValues = new HashMap();
        RecordListInstance rli = null;
		req.setAttribute(formName+".sqii", sqii);
        String formQueryPrefix = formName+"."+queryRecordName+".";
        boolean problem = false;
        if ("true".equals(initialExpansion)) {
            problem = true;
        } else {
            // Now check state of params
            Enumeration names = req.getParameterNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                if (name.startsWith(formQueryPrefix)) {
                    // TODO Cope with multiple values
                    paramValues.put(name, req.getParameter(name));
                }
            }
            rli = recordList.makeRecordListInstance();
            try {
                rli.setValues(paramValues, 1, 20);
            }
            catch (WdkUserException exp) {
                Map errors = exp.getBooBoos();
                problem = true;
                for (Iterator it = errors.keySet().iterator(); it.hasNext();) {
                    Param param = (Param) it.next();
                    String name = param.getName();
                    // FIXME Magic number - struct?
                    String errorMsg = ((String[]) errors.get(param))[1];
                    req.setAttribute(formName+".error."+queryRecordName+"."+name, errorMsg);
                    // TODO Cope with correct values
                }
            }           
            catch (WdkModelException e) {
                // TODO What does this mean?
                //e.printStackTrace();
            }
                
                
//                try {
//                    //            System.err.println("Printing Record Instances on page " + pageCount);
////            try {
//                    rli.print();
//                } catch (WdkModelException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
        }

		
		if (problem) {
			// If fail, redirect to page
			redirect(req, res, fromPage);
			return;
		}

          
        String toPage = "";
        
        // TODO Work out size
        int size = 3;
        
        if (size==1 && autoRedirect) {
        
        } else {           
            req.setAttribute("rli", rli);
            req.setAttribute("recordListName", queryRecordGroup + "." + queryRecordName);
        }
        
        redirect(req, res, toPage);
		return;
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