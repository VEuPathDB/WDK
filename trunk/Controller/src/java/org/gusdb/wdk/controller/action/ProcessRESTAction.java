package org.gusdb.wdk.controller.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.json.JSONException;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ProcessRESTAction extends ShowQuestionAction {

    private static final Logger logger = Logger.getLogger(ProcessRESTAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessQuestionAction..");
		String outputType = null;
        try {
            UserBean wdkUser = ActionUtility.getUser(servlet, request);
            // get question
			String strutsParam = mapping.getParameter();
			String qFullName = strutsParam.split("::")[0];
			outputType = strutsParam.split("::")[1];
			if(outputType == null) outputType = "xml";
			logger.info(outputType);
			
			logger.debug(qFullName);

			WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
            QuestionBean wdkQuestion = null;
            if (qFullName != null)
             	wdkQuestion = getQuestionByFullName(qFullName);
            if (wdkQuestion == null)
                throw new WdkUserException("The question '" + qFullName
                        + "' doesn't exist.");
			Map<String,String> params = new LinkedHashMap<String,String>();
			Map<String,String> outputConfig = new LinkedHashMap<String,String>();
			for(String key : wdkQuestion.getParamsMap().keySet() ){
				String val = null;
				if(wdkQuestion.getParamsMap().get(key) instanceof DatasetParamBean){
	                String data = null;
	                String uploadFile = "";
	                //if (type.equalsIgnoreCase("data")) {
	                    data = request.getParameter(key);
	                /*} else if (type.equalsIgnoreCase("file")) {
	                    FormFile file = (FormFile) qform.getMyPropObject(paramName
	                            + "_file");
	                    uploadFile = file.getFileName();
	                    logger.debug("upload file: " + uploadFile);
	                    data = new String(file.getFileData());
	                }*/

	                logger.debug("dataset data: '" + data + "'");
	                if (data != null && data.trim().length() > 0) {
	                    String[] values = Utilities.toArray(data);
	                    DatasetBean dataset = wdkUser.createDataset(uploadFile, values);
	                    val = Integer.toString(dataset.getUserDatasetId());
	                }
				}else{
					String[] vals = request.getParameterValues(key);
					if(vals == null || vals.length <= 1){
						val = request.getParameter(key);
					}else{
						StringBuffer buf = new StringBuffer();
						buf.append(vals[0]);
						for(int i=0;i<vals.length;i++){
							buf.append("," + vals[i]);
						}
						val = buf.toString();
					}
				}
				params.put(key, val);
			}
			Map reqParams = request.getParameterMap();
			for(Object kobj : reqParams.keySet()){
				String k = (String)kobj;
				String v = null;
				if(k.startsWith("o-")){
					String[] vs = request.getParameterValues(k);
					if(vs == null || vs.length <=1){
						v = request.getParameter(k);
					}else{
						StringBuffer b = new StringBuffer();
						b.append(vs[0]);
						for(int j=0;j<vs.length;j++){
							b.append("," + vs[j]);
						}
						v = b.toString();
					}
				}
				outputConfig.put(k, v);
			}
			outputConfig.put("downloadType", "plain");
			outputConfig.put("hasEmptyTable", "true");
			// FROM SHOWSUMMARY
			
			StepBean step = wdkUser.createStep(wdkQuestion, params, "all_results", true);
	        AnswerValueBean answerValue = step.getAnswerValue();
            // construct the forward to show_summary action
			request.setAttribute("wdkAnswer",answerValue);
            ActionForward forward = mapping.findForward("show_result");
			Reporter reporter = answerValue.createReport(outputType, outputConfig);
			reporter.configure(outputConfig);
			ServletOutputStream out = response.getOutputStream();
            response.setHeader( "Pragma", "Public" );
            response.setContentType( reporter.getHttpContentType() );
            
            String fileName = reporter.getDownloadFileName();
            if ( fileName != null ) {
                response.setHeader( "Content-disposition",
                        "attachment; filename="
                                + reporter.getDownloadFileName() );
            }
            logger.info("ABOUT TO WRITE RESULTS");
            reporter.write( out );
            out.flush();
            out.close();
        } catch (Exception ex) {
			if(ex instanceof WdkModelException && outputType != null){
				logger.info("WdkModelException");
				WdkModelException wdkEx = (WdkModelException)ex;
				if(wdkEx.getParamErrors() != null){
					reportError(response, wdkEx.getParamErrors(), "Input Parameter Error", "010", outputType);
				}else{
					Map<String,String> exMap = new LinkedHashMap();
					exMap.put("1",wdkEx.getMessage());
					reportError(response, exMap, "Output Parameter Error", "011", outputType);
				}
			}else if(ex instanceof WdkUserException && outputType != null){
				logger.info("WdkUserException");
				WdkUserException wdkEx = (WdkUserException)ex;
				Map<String,String> errMap = new LinkedHashMap();
				errMap.put("1",wdkEx.getMessage());
				reportError(response, errMap, "User Error", "020", outputType);
			}else{
				logger.info("OtherException");
            	Map<String,String> exMap = new LinkedHashMap();
				exMap.put("0",ex.getMessage());
				reportError(response,exMap, "Unknown Error", "000", outputType);
			}
        } finally {
			return null;
		}
    }

	private void reportError(HttpServletResponse resp, 
			Map<String,String> msg, String errType, 
			String errCode, String type) throws IOException {
		logger.info("ERROR VALUE = " + errType);
		ServletOutputStream errout = resp.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(errout));
		resp.setHeader( "Pragma", "Public" );
		resp.setContentType("text/plain");
		if(type.equals("xml")) resp.setContentType("text/xml");
		if(type.equals("xml")){
			writer.println("<?xml version='1.0' encoding='UTF-8'?>");
			writer.println("<response>");
			writer.println("<error type='"+errType+"' code='"+errCode+"'>");
			for(String m : msg.keySet())
				writer.println("<msg>"+msg.get(m)+"</msg>");
			writer.println("</error>");
			writer.println("</response>");
		}else{
			writer.print("{\"response\":{\"error\":{\"type\":\""+errType+"\",\"code\":\""+errCode+"\",\"msg\":[");
			int c = 0;
			for(String m : msg.keySet()){
				if(c > 0) writer.print(",");
				writer.print("\""+msg.get(m).substring(1,msg.get(m).length()-1)+"\"");
				c++;
			}
			writer.print("]}}}");
		}	
		writer.flush();
		errout.flush();
		errout.close();
		return;
	}
}
