package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.model.WdkModel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;


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
public class ModelAdminServlet extends HttpServlet {
    
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        WdkModel model = null; // FIXME
        
		String action = req.getParameter("action");
		String type = req.getParameter("type");
		String name = req.getParameter("name");
//		String formName = req.getParameter("formName");
//		String defaultChoice = req.getParameter("defaultChoice");
//        String initialExpansion = req.getParameter("initialExpansion");
//        
//		if (fromPage == null) {
//			msg("fromPage shouldn't be null. Internal error", res);
//			return;
//		}
//		if (queryRecordGroup == null) {
//			msg("queryRecordGroup shouldn't be null. Internal error", res);
//			return;
//		}
//		if (queryRecordName == null) {
//			msg("Qualified queryRecordName shouldn't be null. Internal error", res);
//			return;
//		}
        
		if ("display".equalsIgnoreCase(action)) {
            displayModel(res, model);
            return;
        }
        
        if ("reload".equalsIgnoreCase(action)) {
            reloadModel(res, model);
            return;
        }       
          
        if ("browse".equalsIgnoreCase(action)) {
            browseModel(res, model, type, name);
            return;
        }       
          
        String toPage = "/admin/modelAdmin.jsp";
        
        redirect(req, res, toPage);
		return;
	}

    /**
     * @param name
     * @param type
     * @param model
     * @param res
     * 
     */
    private void browseModel(HttpServletResponse res, WdkModel model, String type, String name) {
        // TODO Auto-generated method stub
        
    }

    /**
     * 
     */
    private void reloadModel(HttpServletResponse res, WdkModel model) {
        // TODO
    }

    private void displayModel(HttpServletResponse res, WdkModel model) throws IOException {
        Document doc = model.getDocument(); 
        // TODO Is toString enough?
        res.setContentType("text/xml");
        Writer w = res.getWriter();
        w.write(doc.toString());
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