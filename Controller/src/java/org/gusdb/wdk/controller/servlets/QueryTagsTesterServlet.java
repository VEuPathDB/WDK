package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.model.SimpleQueryI;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;
import org.gusdb.gus.wdk.model.SimpleQuerySet;

import org.gusdb.gus.wdk.view.GlobalRepository;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class QueryTagsTesterServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

      String fromPage = req.getParameter("fromPage");
      String querySet = req.getParameter("querySet");
      String queryName = req.getParameter("queryName");
      String formName = req.getParameter("formName");
      String defaultChoice = req.getParameter("defaultChoice");
      
      if (fromPage == null) {
          msg("fromPage shouldn't be null. Internal error", res);
          return;
      }
      if (querySet == null) {
          msg("querySet shouldn't be null. Internal error", res);
          return;
      }
      if (queryName == null) {
          msg("queryName shouldn't be null. Internal error", res);
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
      
      if (queryName.equals(defaultChoice)) {
          req.setAttribute(formName+".error.query.noQuery", "Please choose a query");
          redirect(req, res, fromPage);
          return;
      }
      
      // We have a query name
      SimpleQuerySet sqs = GlobalRepository.getInstance().getSimpleQuerySet(querySet);
      SimpleQueryI sq = sqs.getQuery(queryName);
      SimpleQueryInstanceI sqii = sq.makeInstance();
      
      
//      try {
//	    ResultFactory resultFactory = GlobalRepository.getInstance().getRecordResultFactory();
//	    RecordSet recordSet = GlobalRepository.getInstance().getRecordSet(recordSetName);
//	    Record record = recordSet.getRecord(recordName);
//	    RecordInstance recordInstance = record.makeInstance();
//	    recordInstance.setPrimaryKey(primaryKey);
//        res.setContentType("text/plain");
//        PrintWriter out = res.getWriter();
//	    out.println( recordInstance.print() );
//
//	} catch (Exception e) {
//	    e.printStackTrace();
////	    System.exit(1);
//        } 
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