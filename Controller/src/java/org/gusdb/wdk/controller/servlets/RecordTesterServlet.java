package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordInstance;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.ResultFactory;

import org.gusdb.gus.wdk.view.GlobalRepository;
import org.gusdb.gus.wdk.view.RecordInstanceView;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class RecordTesterServlet extends HttpServlet {

    private static final int DESTINATION_PLAIN=0;
    private static final int DESTINATION_JSP=1;
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {

      String recordSetName = req.getParameter("recordSetName");
      String recordName = req.getParameter("recordName");
      String primaryKey = req.getParameter("primaryKey");
      String style = req.getParameter("style");
        
      int destination = DESTINATION_PLAIN;
      if ("jsp".equalsIgnoreCase(style)) {
          destination = DESTINATION_JSP;
      }
      
      RecordInstance recordInstance = null;
      
      try {
	    ResultFactory resultFactory = GlobalRepository.getInstance().getRecordResultFactory();
	    RecordSet recordSet = GlobalRepository.getInstance().getRecordSet(recordSetName);
	    Record record = recordSet.getRecord(recordName);
	    recordInstance = record.makeInstance();
	    recordInstance.setPrimaryKey(primaryKey);


      } catch (Exception e) {
	    e.printStackTrace();
//	    System.exit(1);
      }
    
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
              RequestDispatcher rd = sc.getRequestDispatcher("/views/CDSView.jsp");
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

}
    
