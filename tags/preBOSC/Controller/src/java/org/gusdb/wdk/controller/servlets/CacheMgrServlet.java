package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.WdkModelException;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * CacheMgrServlet
 *
 * This servlet interacts with the query custom tags to present and validate a form
 * for the user
 *
 * Created: May 9, 2004
 *
 * @author Adrian Tivey
 * @version $Revision$ $Date$ $Author$
 */
public class CacheMgrServlet extends HttpServlet {

    private final static boolean autoRedirect = true;
    private final static String cachePage = "/admin/private/cache.jsp";
    
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
		String action = req.getParameter("action");
        
        
        ResultFactory rf = (ResultFactory) getServletContext().getAttribute("wdk.resultFactory");
        int cacheSize = 20; // FIXME
        
        boolean actionDone = false;
		if (action == null) {
		    action = "view";
		}
		
        String errorMsg = null;
        
        if ("view".equalsIgnoreCase(action)) {
            actionDone = true;
            req.setAttribute("dbname", "GUSrw.QueryInstance");
            redirect(req, res, "/admin/tableBrowse.jsp?dbname=GUSrw.QueryInstance");
            return;
        }
        
        if ("recreate".equalsIgnoreCase(action)) {
		    actionDone = true;
            errorMsg = dropCache(rf);
            if (errorMsg != null) {
                errorMsg = createCache(rf, cacheSize);
            }
        }
        
        if ("new".equalsIgnoreCase(action)) {
            actionDone = true;
            errorMsg = createCache(rf, cacheSize);
        }

        if ("drop".equalsIgnoreCase(action)) {
            actionDone = true;
            errorMsg = dropCache(rf);
        }
        
        if ("reset".equalsIgnoreCase(action)) {
            actionDone = true;
            errorMsg = resetCache(rf);
        }
        
        if (!actionDone) {
            errorMsg = "Don't recognize an action of "+action;
        }
        
        if (errorMsg != null) {
            msg(errorMsg, res);
            return;
        }
        
        req.setAttribute("wdk.pageMessage", "Action competed succesfully");
        res.sendRedirect("/admin/private/cache.jsp");
		return;
	}

    private String dropCache(ResultFactory rf) {
        String msg = null;
        try {
            rf.dropCache();
        } catch (WdkModelException e) {
            msg = "Cache not dropped: "+e.getMessage();
        }
        return msg;
    }

    private String resetCache(ResultFactory rf) {
        String msg = null;
        try {
            rf.resetCache();
        } catch (WdkModelException e) {
            msg = "Cache not reset: "+e.getMessage();
        }
        return msg;
    }    

    private String createCache(ResultFactory rf, int cacheSize) {
        String msg = null;
        try {
            rf.createCache(cacheSize);
        } catch (WdkModelException e) {
            msg = "Cache not created: "+e.getMessage();
        }
        return msg;
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