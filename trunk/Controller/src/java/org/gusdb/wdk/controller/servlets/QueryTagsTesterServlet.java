package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordInstance;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.ResultFactory;

import org.gusdb.gus.wdk.view.GlobalRepository;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class QueryTagsTesterServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res) {

      String recordSetName = req.getParameter("recordSetName");
      String recordName = req.getParameter("recordName");
      String primaryKey = req.getParameter("primaryKey");
        
      try {
	    ResultFactory resultFactory = GlobalRepository.getInstance().getRecordResultFactory();
	    RecordSet recordSet = GlobalRepository.getInstance().getRecordSet(recordSetName);
	    Record record = recordSet.getRecord(recordName);
	    RecordInstance recordInstance = record.makeInstance();
	    recordInstance.setPrimaryKey(primaryKey);
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
	    out.println( recordInstance.print() );

	} catch (Exception e) {
	    e.printStackTrace();
//	    System.exit(1);
        } 
    }

}
    
