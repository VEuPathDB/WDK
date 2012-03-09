package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.Utilities;
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
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamTermNode;
import org.gusdb.wdk.model.report.Reporter;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ProcessRESTAction extends Action {

    private static final Logger logger = Logger.getLogger(ProcessRESTAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessRESTAction..");
        String outputType = null;
        try {
            WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
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
            // if (outputType == null) outputType = "xml";

            logger.debug(qFullName);

            QuestionBean wdkQuestion = null;
            if (qFullName != null)
                wdkQuestion = wdkModel.getQuestion(qFullName);
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

            StepBean step = wdkUser.createStep(wdkQuestion, params, null,
                    false, true, Utilities.DEFAULT_WEIGHT);
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
                response.setHeader(
                        "Content-disposition",
                        "attachment; filename="
                                + reporter.getDownloadFileName());
            }
            logger.info("ABOUT TO WRITE RESULTS");
            reporter.report(out);
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
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
                logger.info("OtherException\n" + ex.toString());
                ex.printStackTrace();
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
        try {
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
                writer.println("<resource path='" + wdkQuestionSet.getName()
                        + "'>");
                for (String ques : wdkQuestionSet.getQuestionsMap().keySet()) {
                    writeWADL(wdkQuestionSet.getQuestionsMap().get(ques),
                            writer);
                }
                writer.println("</resource>");
            } else {
                if (qFullName != null)
                    wdkQuestion = wdkModel.getQuestion(qFullName);
                if (wdkQuestion == null)
                    throw new WdkUserException("The question '" + qFullName
                            + "' doesn't exist.");
                writer.println("<resource path='" + sQName.split(":")[0] + "'>");
                writeWADL(wdkQuestion, writer);
                writer.println("</resource>");
            }
            writer.println("</resources>");
            writer.println("</application>");
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            writer.flush();
            out.flush();
            out.close();
        }
    }

    private void writeWADL(QuestionBean wdkQuestion, PrintWriter writer)
            throws Exception {
        logger.debug(wdkQuestion.getDisplayName());
        // String def_attr = null;
        String def_value = "";
        String repeating = "";
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
        Map<String, ParamBean> params = wdkQuestion.getParamsMap();
        for (String key : params.keySet()) {
            ParamBean param = params.get(key);
            def_value = param.getDefault();
            repeating = "";
            if (param instanceof EnumParamBean) {
                EnumParamBean ep = (EnumParamBean) param;
                def_value = filterDefaultValue(ep, def_value);
                if (ep.getMultiPick()) repeating = "repeating='true'";
                else repeating = "repeating='false'";
            }
            if (def_value != null && def_value.length() > 0) {
                def_value = htmlEncode(def_value);
            } else if (def_value == null) def_value = "";

            writer.println("<param name='" + key
                    + "' type='xsd:string' required='"
                    + !param.getIsAllowEmpty() + "' default='" + def_value
                    + "' " + repeating + ">");
            writer.println("<doc title='prompt'><![CDATA[" + param.getPrompt()
                    + "]]></doc>");
            writer.println("<doc title='help'><![CDATA[" + param.getHelp()
                    + "]]></doc>");
            writer.println("<doc title='default'><![CDATA["
                    + param.getDefault() + "]]></doc>");

            if (param instanceof EnumParamBean) {
                EnumParamBean ep = (EnumParamBean) param;
                if (ep.getMultiPick()) writer.println("<doc title='MultiValued'>"
                        + "Provide one or more values. "
                        + "Use comma as a delimter.</doc>");
                else writer.println("<doc title='SingleValued'>Choose "
                        + "at most one value from the options</doc>");
                Map<String, String> displayMap;
                if (ep.getDependedParam() == null) {
                    displayMap = getDisplayMap(ep);
                } else {
                    displayMap = new HashMap<String, String>();
                    ParamBean dependedParam = ep.getDependedParam();
                    Set<String> dependedValues;
                    if (dependedParam instanceof EnumParamBean) {
                        EnumParamBean enumParam = (EnumParamBean) dependedParam;
                        dependedValues = enumParam.getVocabMap().keySet();
                    } else {
                        dependedValues = new LinkedHashSet<String>();
                        String value = dependedParam.getDefault();
                        if (value != null) dependedValues.add(value);
                    }
                    for (String depterm : dependedValues) {
                        ep.setDependedValue(depterm);
                        try {
                            displayMap.putAll(getDisplayMap(ep));
                        }
                        catch (Exception e) {
                            if (e instanceof WdkModelException) {
                                logger.info("expected Empty result set for dependent parameter.");
                                continue;
                            } else {
                                logger.info(e.toString());
                                e.printStackTrace();
                            }
                        }
                    }
                }
                for (String term : displayMap.keySet()) {
                    String display = displayMap.get(term);
                    // writer.println("<option>" + term + "</option>");
                    writer.println("<option value='" + htmlEncode(term)
                            + "'><doc title='description'><![CDATA[" + display
                            + "]]></doc></option>");
                }
            }
            writer.println("</param>");
        }
        writer.println("<param name='o-fields' type='xsd:string' required='false' default='none' repeating='true'>");
        writer.println("<doc title='prompt'><![CDATA[Output Fields]]></doc>");
        writer.println("<doc title='help'><![CDATA[Single valued attributes of the feature.]]></doc>");
        writer.println("<doc title='default'><![CDATA[none]]></doc>");
        writer.println("<doc title='MultiValued'>Provide one or more values. Use comma as a delimter.</doc>");
        // writer.println("<option>all</option>");
        // writer.println("<option>none</option>");
        writer.println("<option value='all'><doc title='description'>Show all attributes</doc></option>");
        writer.println("<option value='none'><doc title='description'>Show no attributes</doc></option>");
        for (String attr : wdkQuestion.getReportMakerAttributesMap().keySet())
            writer.println("<option value='"
                    + attr.replaceAll("'", "&apos;")
                    + "'><doc title='description'><![CDATA["
                    + wdkQuestion.getReportMakerAttributesMap().get(attr).getDisplayName()
                    + "]]></doc></option>");
        // writer.println("<option>" + attr + "</option>");
        writer.println("</param>");
        writer.println("<param name='o-tables' type='xsd:string' required='false' default='none' repeating='true'>");
        writer.println("<doc title='prompt'><![CDATA[Output Tables]]></doc>");
        writer.println("<doc title='help'><![CDATA[Multi-valued attributes of the feature.]]></doc>");
        writer.println("<doc title='default'><![CDATA[none]]></doc>");
        writer.println("<doc title='MultiValued'>Provide one or more values. Use comma as a delimter.</doc>");
        // writer.println("<option >all</option>");
        // writer.println("<option>none</option>");
        writer.println("<option value='all'><doc title='description'>Show all tables</doc></option>");
        writer.println("<option value='none'><doc title='description'>Show no tables</doc></option>");
        for (String tab : wdkQuestion.getReportMakerTablesMap().keySet())
            writer.println("<option value='"
                    + tab
                    + "'><doc title='description'><![CDATA["
                    + wdkQuestion.getReportMakerTablesMap().get(tab).getDisplayName()
                    + "]]></doc></option>");
        // writer.println("<option>" + tab + "</option>");
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

    private String filterDefaultValue(EnumParamBean param, String defaultValue)
            throws Exception {
        if (defaultValue == null || defaultValue.length() == 0)
            return defaultValue;

        String[] terms = defaultValue.split(",");
        Map<String, String> displayMap = getDisplayMap(param);
        StringBuilder values = new StringBuilder();
        for (String term : terms) {
            if (!displayMap.containsKey(term)) continue;
            if (values.length() > 0) values.append(",");
            values.append(term);
        }
        return values.toString();
    }

    private Map<String, String> getDisplayMap(EnumParamBean param)
            throws Exception {
        String displayType = param.getDisplayType();
        boolean isTreeBox = (displayType != null && displayType.equals(AbstractEnumParam.DISPLAY_TREE_BOX));
        logger.debug(param.getFullName() + " as tree: " + isTreeBox);
        if (!isTreeBox) return param.getDisplayMap();

        Stack<EnumParamTermNode> stack = new Stack<EnumParamTermNode>();
        for (EnumParamTermNode root : param.getVocabTreeRoots())
            stack.push(root);

        Map<String, String> displayMap = new LinkedHashMap<String, String>();
        while (!stack.isEmpty()) {
            EnumParamTermNode node = stack.pop();
            EnumParamTermNode[] children = node.getChildren();
            if (children.length == 0) { // find a leaf, output its term/display
                displayMap.put(node.getTerm(), node.getDisplay());
            } else { // internal node, skip and process its children
                for (EnumParamTermNode child : children)
                    stack.push(child);
            }
        }
        return displayMap;
    }

    private String htmlEncode(String x) {
        Map<String, String> codeValues = new LinkedHashMap<String, String>();
        codeValues.put("\"", "&quot;");
        codeValues.put("'", "&apos;");
        // codeValues.put("", "&;");
        for (String k : codeValues.keySet()) {
            x = x.replaceAll(k, codeValues.get(k));
        }
        return x;
    }

    // private String join(String[] a, String d) {
    // String c = "";
    // boolean e = true;
    // for (String b : a) {
    // if (!e) {
    // c = c + d;
    // e = false;
    // }
    // c = c + b;
    // }
    // return c;
    // }
}
