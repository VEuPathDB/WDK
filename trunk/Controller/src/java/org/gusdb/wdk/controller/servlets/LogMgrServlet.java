package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.controller.WdkLogManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * LogMgrServlet
 *
 *
 * Created: May 9, 2004
 *
 * @author Adrian Tivey
 * @version $Revision$ $Date$ $Author$
 */
public class LogMgrServlet extends HttpServlet {

    private final static boolean autoRedirect = true;
    private final static String cachePage = "/admin/private/cache.jsp";
    
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
		String action = req.getParameter("action");
        
        boolean actionDone = false;
		if (action == null) {
		    action = "view";
		}
		
        String errorMsg = null;
        
        if ("view".equalsIgnoreCase(action)) {
            viewLog(res);
            return;
        }
        
        if ("tail".equalsIgnoreCase(action)) {
            viewLog(res);
            return;
        }
        
//        if ("changeLevel".equalsIgnoreCase(action)) {
//            actionDone = true;
//            errorMsg = changeLevel(logger, level);
//        }

//        if ("changeLevel".equalsIgnoreCase(action)) {
//            actionDone = true;
//            errorMsg = changeLevel(logger, cacheSize);
//        }        
        
        if (!actionDone) {
            errorMsg = "Don't recognize an action of "+action;
        }
        
        if (errorMsg != null) {
            msg(errorMsg, res);
            return;
        }
        
        req.setAttribute("wdk.pageMessage", "Action competed succesfully");
        res.sendRedirect("/admin/private/logs.jsp");
		return;
	}

    
    private void viewLog(HttpServletResponse res) throws IOException {
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        String fname = WdkLogManager.INSTANCE.getLogFilename();
        try {
            BufferedReader r = new BufferedReader(new FileReader(fname));
            String in = null;
            while ((in = r.readLine()) != null) {
                out.println(in);
            }
        } catch (FileNotFoundException e) {
            out.println("FileNotFound: "+fname);
        } catch (IOException e) {
            out.println("IOException trying to read: "+fname);
        }
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