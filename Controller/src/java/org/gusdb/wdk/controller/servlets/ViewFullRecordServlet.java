package org.gusdb.wdk.controller.servlets;

import org.gusdb.wdk.model.WdkLogManager;
import org.gusdb.wdk.model.Record;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModel;

import org.gusdb.wdk.model.RecordInstanceView;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Servlet to generate a RecordInstance for forwarding to a view
 */
public class ViewFullRecordServlet extends HttpServlet {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.controller.servlets.ViewFullRecordServlet");
    
    private static final int DESTINATION_PLAIN=0;
    private static final int DESTINATION_JSP=1;
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {

      String recordReference = req.getParameter("recordReference");
      String primaryKey = req.getParameter("primaryKey");
      String style = req.getParameter("style");

      
      int destination = DESTINATION_JSP;
      if ("plain".equalsIgnoreCase(style)) {
          destination = DESTINATION_PLAIN;
      }
      
      RecordInstance recordInstance = (RecordInstance) req.getAttribute("ri");
      
      String recordName = (String) req.getAttribute("recordClassName");
      if (recordName != null) {
          recordReference = recordName;
          primaryKey = (String) req.getAttribute("primaryKey");
      }
//      if (recordInstance != null) {
//          recordReference = recordInstance.getRecord().getFullName();
//          
//      } else {

          try {
              //	    ResultFactory resultFactory = 
              //            (ResultFactory) getServletContext().getAttribute("wdk.recordResultFactory");
              WdkModel wm = (WdkModel) getServletContext().getAttribute("wdk.wdkModel");
              Record record = wm.getRecord(recordReference);
              recordInstance = record.makeRecordInstance();
              recordInstance.setPrimaryKey(primaryKey);
              
              
          } catch (Exception e) {
              e.printStackTrace();
              //	    System.exit(1);
          }
//      }
    
      switch (destination) {
      case DESTINATION_PLAIN:
          res.setContentType("text/plain");
          try {
              PrintWriter out = res.getWriter();
              out.println("recordInstance is "+recordInstance.toString());
              out.println("===========================================");
              out.println( recordInstance.print() );
          } catch (IOException exp) {
              exp.printStackTrace();
          }
          catch (Exception exp) {
              exp.printStackTrace();
          }
          break;
      case DESTINATION_JSP:
          try {
              req.setAttribute( "ri" , new RecordInstanceView(recordInstance));
              ServletContext sc = getServletContext();
              String page = getRendererForRecordRef(recordReference);
              RequestDispatcher rd = sc.getRequestDispatcher(page);
              rd.forward(req, res);
          } catch (IOException exp) {
              exp.printStackTrace();
          }
          catch (Exception exp) {
              exp.printStackTrace();
          }
          break;      
      }
      
    }

    private String getRendererForRecordRef(String recordFullName) {
        // TODO Set default for where no renderer found
        String renderer = recordFullName;
        //logger.severe("renderer is "+renderer);

        String path = getServletContext().getRealPath("/WEB-INF/indirectPages/views/"+renderer+".jsp");
        File f = new File(path);
        if (f.exists()) {
            //logger.severe("Found file for "+path);
            return "/WEB-INF/indirectPages/views/"+renderer+".jsp";
        }
        //logger.severe("Returning default view, couldn't find "+path);
        return "/WEB-INF/indirectPages/views/defaultView.jsp";
        
    }
    
}
    
