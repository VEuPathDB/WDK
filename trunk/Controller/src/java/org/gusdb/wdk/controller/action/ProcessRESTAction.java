package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.AbstractMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.report.Reporter;

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
        logger.debug("Entering ProcessRESTAction..");
        String outputType = null;
        try {
            UserBean wdkUser = ActionUtility.getUser(servlet, request);
            // get question
            String strutsParam = mapping.getParameter();
            String qFullName = strutsParam.split("::")[0];
            outputType = strutsParam.split("::")[1];
            logger.debug(outputType);

            if (outputType.equals("wadl")) {
                createWADL(request, response, qFullName);
                return null;
            }
            if (outputType == null) outputType = "xml";

            logger.debug(qFullName);

            QuestionBean wdkQuestion = null;
            if (qFullName != null)
                wdkQuestion = getQuestionByFullName(qFullName);
            if (wdkQuestion == null)
                throw new WdkUserException("The question '" + qFullName
                        + "' doesn't exist.");
            Map<String, String> params = new LinkedHashMap<String, String>();
            Map<String, String> outputConfig = new LinkedHashMap<String, String>();
            Map<String, ParamBean> paramMap = wdkQuestion.getParamsMap();
            for (String key : paramMap.keySet()) {
                String val = null;
                ParamBean param = paramMap.get(key);
                if (param instanceof DatasetParamBean) {
                    String data = null;
                    String uploadFile = "";
                    // if (type.equalsIgnoreCase("data")) {
                    data = request.getParameter(key);
                    /*
                     * } else if (type.equalsIgnoreCase("file")) { FormFile file
                     * = (FormFile) qform.getMyPropObject(paramName + "_file");
                     * uploadFile = file.getFileName();
                     * logger.debug("upload file: " + uploadFile); data = new
                     * String(file.getFileData()); }
                     */

                    logger.debug("dataset data: '" + data + "'");
                    if (data != null && data.trim().length() > 0) {
                        DatasetParamBean datasetParam = (DatasetParamBean) param;
                        RecordClassBean recordClass = datasetParam.getRecordClass();
                        DatasetBean dataset = wdkUser.createDataset(
                                recordClass, uploadFile, data);
                        val = Integer.toString(dataset.getUserDatasetId());
                    }
                } else {
                    String[] vals = request.getParameterValues(key);
                    if (vals == null || vals.length <= 1) {
                        val = request.getParameter(key);
                    } else {
                        StringBuffer buf = new StringBuffer();
                        buf.append(vals[0]);
                        for (int i = 0; i < vals.length; i++) {
                            buf.append("," + vals[i]);
                        }
                        val = buf.toString();
                    }
                }
                params.put(key, val);
            }
            Map<?, ?> reqParams = request.getParameterMap();
            for (Object kobj : reqParams.keySet()) {
                String k = (String) kobj;
                String v = null;
                if (k.startsWith("o-")) {
                    String[] vs = request.getParameterValues(k);
                    if (vs == null || vs.length <= 1) {
                        v = request.getParameter(k);
                    } else {
                        StringBuffer b = new StringBuffer();
                        b.append(vs[0]);
                        for (int j = 0; j < vs.length; j++) {
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

            StepBean step = wdkUser.createStep(wdkQuestion, params,
                    "all_results", false, true);
            AnswerValueBean answerValue = step.getAnswerValue();
            // construct the forward to show_summary action
            request.setAttribute("wdkAnswer", answerValue);
            Reporter reporter = answerValue.createReport(outputType,
                    outputConfig);
            reporter.configure(outputConfig);
            ServletOutputStream out = response.getOutputStream();
            response.setHeader("Pragma", "Public");
            response.setContentType(reporter.getHttpContentType());

            String fileName = reporter.getDownloadFileName();
            if (fileName != null) {
                response.setHeader("Content-disposition",
                        "attachment; filename="
                                + reporter.getDownloadFileName());
            }
            logger.info("ABOUT TO WRITE RESULTS");
            reporter.write(out);
            out.flush();
            out.close();
        } catch (Exception ex) {
            if (ex instanceof WdkModelException && outputType != null) {
                logger.info("WdkModelException");
                WdkModelException wdkEx = (WdkModelException) ex;
                if (wdkEx.getParamErrors() != null) {
                    reportError(response, wdkEx.getParamErrors(),
                            "Input Parameter Error", "010", outputType);
                } else {
                    Map<String, String> exMap = new LinkedHashMap<String, String>();
                    exMap.put("1", wdkEx.getMessage());
                    reportError(response, exMap, "Output Parameter Error",
                            "011", outputType);
                }
            } else if (ex instanceof WdkUserException && outputType != null) {
                logger.info("WdkUserException");
                WdkUserException wdkEx = (WdkUserException) ex;
                Map<String, String> errMap = new LinkedHashMap<String, String>();
                errMap.put("1", wdkEx.getMessage());
                reportError(response, errMap, "User Error", "020", outputType);
            } else {
                logger.info("OtherException");
                Map<String, String> exMap = new LinkedHashMap<String, String>();
                exMap.put("0", ex.getMessage());
                reportError(response, exMap, "Unknown Error", "000", outputType);
            }
        }
        return null;
    }

    private void reportError(HttpServletResponse resp, Map<String, String> msg,
            String errType, String errCode, String type) throws IOException {
        logger.info("ERROR VALUE = " + errType);
        ServletOutputStream errout = resp.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(errout));
        resp.setHeader("Pragma", "Public");
        resp.setContentType("text/plain");
        if (type.equals("xml")) resp.setContentType("text/xml");
        if (type.equals("xml")) {
            writer.println("<?xml version='1.0' encoding='UTF-8'?>");
            writer.println("<response>");
            writer.println("<error type='" + errType + "' code='" + errCode
                    + "'>");
            for (String m : msg.keySet())
                writer.println("<msg><![CDATA[" + msg.get(m) + "]]></msg>");
            writer.println("</error>");
            writer.println("</response>");
        } else {
            writer.print("{\"response\":{\"error\":{\"type\":\"" + errType
                    + "\",\"code\":\"" + errCode + "\",\"msg\":[");
            int c = 0;
            for (String m : msg.keySet()) {
                if (c > 0) writer.print(",");
                writer.print("\""
                        + msg.get(m).substring(1, msg.get(m).length() - 1)
                        + "\"");
                c++;
            }
            writer.print("]}}}");
        }
        writer.flush();
        errout.flush();
        errout.close();
        return;
    }

    private void createWADL(HttpServletRequest request,
            HttpServletResponse response, String qFullName) throws Exception {
		ServletOutputStream out = response.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        try{
		String sQName = qFullName.replace('.', ':');
        // get question
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        QuestionBean wdkQuestion = null;
        QuestionSetBean wdkQuestionSet = null;
        response.setHeader("Pragma", "Public");
        response.setContentType("text/xml");

        writer.println("<?xml version='1.0'?>");
        writer.println("<application xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://wadl.dev.java.net/2009/02 wadl.xsd' "
                + "xmlns:xsd='http://www.w3.org/2001/XMLSchema' "
                + "xmlns='http://wadl.dev.java.net/2009/02'>");
        String base = request.getHeader("Host") + "/webservices/";
        writer.println("<resources base='http://" + base + "'>");
        if (sQName.split(":")[1].equals("all")) {
            if (qFullName != null)
                wdkQuestionSet = wdkModel.getQuestionSetsMap().get(
                        sQName.split(":")[0]);
            if (wdkQuestionSet == null)
                throw new WdkUserException("The question set '"
                        + sQName.split(":")[0] + "' doesn't exist.");
            writer.println("<resource path='" + wdkQuestionSet.getName() + "'>");
            for (String ques : wdkQuestionSet.getQuestionsMap().keySet()) {
                writeWADL(wdkQuestionSet.getQuestionsMap().get(ques), writer);
            }
            writer.println("</resource>");
        } else {
            if (qFullName != null)
                wdkQuestion = getQuestionByFullName(qFullName);
            if (wdkQuestion == null)
                throw new WdkUserException("The question '" + qFullName
                        + "' doesn't exist.");
            writer.println("<resource path='" + sQName.split(":")[0] + "'>");
            writeWADL(wdkQuestion, writer);
            writer.println("</resource>");
        }
        writer.println("</resources>");
        writer.println("</application>");
    	}catch(Exception e){
			throw e;
		}finally{
		writer.flush();
        out.flush();
        out.close();
		}
    }

    private void writeWADL(QuestionBean wdkQuestion, PrintWriter writer)
            throws Exception {
        logger.debug(wdkQuestion.getDisplayName());
        writer.println("<resource path='" + wdkQuestion.getName() + ".xml'>");
        writer.println("<method href='#" + wdkQuestion.getName().toLowerCase()
                + "'/>");
        writer.println("</resource>");
        writer.println("<resource path='" + wdkQuestion.getName() + ".json'>");
        writer.println("<method href='#" + wdkQuestion.getName().toLowerCase()
                + "'/>");
        writer.println("</resource>");
        writer.println("<method name='POST' id='"
                + wdkQuestion.getName().toLowerCase() + "'>");
		writer.println("<doc title='display_name'><![CDATA["
                + wdkQuestion.getDisplayName() + "]]></doc>");
        writer.println("<doc title='summary'><![CDATA["
                + wdkQuestion.getSummary() + "]]></doc>");
        writer.println("<doc title='description'><![CDATA["
                + wdkQuestion.getDescription() + "]]></doc>");
        writer.println("<request>");
        for (String key : wdkQuestion.getParamsMap().keySet()) {
            writer.println("<param name='" + key + "' type='xsd:string'>");
            writer.println("<doc title='prompt'><![CDATA["
                    + wdkQuestion.getParamsMap().get(key).getPrompt() + "]]></doc>");
			writer.println("<doc title='help'><![CDATA["
                    + wdkQuestion.getParamsMap().get(key).getHelp() + "]]></doc>");
			if(wdkQuestion.getParamsMap().get(key).getDefault() != null && wdkQuestion.getParamsMap().get(key).getDefault().length() > 0 && wdkQuestion.getParamsMap().get(key).getIsAllowEmpty() && wdkQuestion.getParamsMap().get(key).getEmptyValue() == null){
				writer.println("<doc title='default'><![CDATA["
	                    + wdkQuestion.getParamsMap().get(key).getDefault() + "]]></doc>");
			}
            ParamBean p = wdkQuestion.getParamsMap().get(key);
            if (p instanceof EnumParamBean) {
                EnumParamBean ep = (EnumParamBean) p;
				if(ep.getMultiPick())
					writer.println("<doc title='MultiValued'>Provide one or more values. Use comma as a delimter.</doc>");
				else
					writer.println("<doc title='SingleValued'>Choose at most one value from the options</doc>");
                if(ep.getDependedParam() == null){
					for (String term : ep.getVocabMap().keySet()) {
                    	//writer.println("<option>" + term + "</option>");
						writer.println("<option value='" + term + "'><doc title='description'><![CDATA[" + ep.getDisplayMap().get(term) + "]]></doc></option>");
                	}
				}else{
					HashSet pSet = new HashSet();
					EnumParamBean depep = new EnumParamBean(ep.getDependedParam());
					for(String depterm : depep.getVocabMap().keySet()){
						ep.setDependedValue(depterm);
						try{ 
							pSet.addAll(ep.getDisplayMap().entrySet());
						}catch(WdkModelException e){
							logger.info("expected Empty result set for dependent parameter.");
							continue;
						}catch(Exception ex){
							logger.info(e.toString());
							e.printStackTrace();
						}
					}
					Iterator iter = pSet.iterator();
					while(iter.hasNext()){
						Map.Entry mp = (Map.Entry) iter.next();
                    	//writer.println("<option>" + term + "</option>");
						writer.println("<option value='" + mp.getKey() + "'><doc title='description'><![CDATA[" + mp.getValue() + "]]></doc></option>");
                	}
				}
            }
            writer.println("</param>");
        }
        writer.println("<param name='o-fields' type='xsd:string'>");
        writer.println("<doc title='Prompt'><![CDATA[Output Fields]]></doc>");
        writer.println("<doc title='help'><![CDATA[Single valued attributes of the feature.]]></doc>");
        writer.println("<doc title='default'><![CDATA[none]]></doc>");
        //writer.println("<option>all</option>");
        //writer.println("<option>none</option>");
        writer.println("<option value='all'><doc title='description'>Show all attributes</doc></option>");
        writer.println("<option value='none'><doc title='description'>Show no attributes</doc></option>");
		for (String attr : wdkQuestion.getReportMakerAttributesMap().keySet())
			writer.println("<option value='" + attr + "'><doc title='description'><![CDATA[" + wdkQuestion.getReportMakerAttributesMap().get(attr).getDisplayName() + "]]></doc></option>");
            //writer.println("<option>" + attr + "</option>");
        writer.println("</param>");
        writer.println("<param name='o-tables' type='xsd:string'>");
		writer.println("<doc title='Prompt'><![CDATA[Output Tables]]></doc>");
        writer.println("<doc title='help'><![CDATA[Multi-valued attributes of the feature.]]></doc>");
        writer.println("<doc title='default'><![CDATA[none]]></doc>");
		//writer.println("<option >all</option>");
        //writer.println("<option>none</option>");
        writer.println("<option value='all'><doc title='description'>Show all tables</doc></option>");
        writer.println("<option value='none'><doc title='description'>Show no tables</doc></option>");
        for (String tab : wdkQuestion.getReportMakerTablesMap().keySet())
			writer.println("<option value='" + tab + "'><doc title='description'><![CDATA[" + wdkQuestion.getReportMakerTablesMap().get(tab).getDisplayName() + "]]></doc></option>");
            //writer.println("<option>" + tab + "</option>");
        writer.println("</param>");
        writer.println("</request>");
        writer.println("<response>");
        writer.println("<representation mediaType='text/xml'/>");
        writer.println("<representation mediaType='text/plain'/>");
        writer.println("</response>");
        writer.println("</method>");

        // construct the forward to show_summary action
        return;
    }

    /*
     * <?xml version="1.0"?> <application
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:schemaLocation="http://wadl.dev.java.net/2009/02 wadl.xsd"
     * xmlns:tns="urn:yahoo:yn" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
     * xmlns:yn="urn:yahoo:yn" xmlns:ya="urn:yahoo:api"
     * xmlns="http://wadl.dev.java.net/2009/02"> <grammars> <include
     * href="NewsSearchResponse.xsd"/> <include href="Error.xsd"/> </grammars>
     * 
     * <resources base="http://api.search.yahoo.com/NewsSearchService/V1/">
     * <resource path="newsSearch"> <method name="GET" id="search"> <request>
     * <param name="appid" type="xsd:string" style="query" required="true"/>
     * <param name="query" type="xsd:string" style="query" required="true"/>
     * <param name="type" style="query" default="all"> <option value="all"/>
     * <option value="any"/> <option value="phrase"/> </param> <param
     * name="results" style="query" type="xsd:int" default="10"/> <param
     * name="start" style="query" type="xsd:int" default="1"/> <param
     * name="sort" style="query" default="rank"> <option value="rank"/> <option
     * value="date"/> </param> <param name="language" style="query"
     * type="xsd:string"/> </request> <response status="200"> <representation
     * mediaType="application/xml" element="yn:ResultSet"/> </response>
     * <response status="400"> <representation mediaType="application/xml"
     * element="ya:Error"/> </response> </method> </resource> </resources>
     * 
     * </application>
     */

}
