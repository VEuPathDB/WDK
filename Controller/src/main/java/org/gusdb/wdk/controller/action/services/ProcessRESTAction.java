package org.gusdb.wdk.controller.action.services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.web.HttpRequestData;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.MDCUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamTermNode;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterFactory;
import org.gusdb.wdk.model.report.StandardConfig;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It 1) reads param values from
 * input form bean, 2) runs the query and saves the answer 3) forwards control to a jsp page that displays a
 * summary
 */

public class ProcessRESTAction extends Action {

  private static final Logger LOG = Logger.getLogger(ProcessRESTAction.class);

  private static final ConcurrentSkipListSet<String> RIDS = new ConcurrentSkipListSet<>();

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    LOG.debug("Entering " + ProcessRESTAction.class.getName());

    // Prevent this method from being called more than once during the same request
    String rid = MDCUtil.getRequestId();
    if (rid != null) {
      if (RIDS.contains(rid)) {
        LOG.warn("Multiple visits to ProcessRESTAction:execute() by the same request (rid=" + rid + ").");
        return null;
      }
      RIDS.add(rid);
    }

    String outputType = null;
    try {
      WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
      UserBean wdkUser = ActionUtility.getUser(request);

      // get question from URL
      String strutsParam = mapping.getParameter();
      LOG.info("Parameter received from struts: " + strutsParam);
      String[] paramSplit = strutsParam.split("::");
      if (paramSplit.length != 2)
        throw new WdkModelException("Parameter split on '::' resulted in " + paramSplit.length + " tokens");
      String qFullName = paramSplit[0];
      outputType = paramSplit[1];
      String[] questionSplit = qFullName.split("\\.");
      if (questionSplit.length != 2)
        throw new WdkModelException("Question split on '.' resulted in " + questionSplit.length + " tokens");
      String questionSetName = questionSplit[0];
      String questionName = questionSplit[1];

      LOG.info("WebServices request for " + questionSetName + "/" + questionName + " (" + outputType + ")");

      QuestionSetBean questionSet = wdkModel.getQuestionSetsMap().get(questionSetName);
      if (questionSet == null)
        throw new WdkUserException("The question set '" + questionSetName + "' doesn't exist.");

      boolean allQuestions = false;
      QuestionBean question = null;
      if (questionName.equals("all")) {
        allQuestions = true;
      }
      else {
        question = questionSet.getQuestionsMap().get(questionName);
        if (question == null)
          throw new WdkUserException("The question '" + questionName + "' is not a member of question set '" + questionSetName + "'.");
      }

      if (outputType.equals("wadl")) {
        createWADL(request, response, questionSet, question, allQuestions);
        return null;
      }
      else if (questionName.equals("all")) {
        throw new WdkUserException("Only WADLs can be supplied for all questions in a question set.  Select a single question.");
      }
      else if (!outputType.equals("json") && !outputType.equals("xml")) {
        throw new WdkUserException("Invalid format '" + outputType + "'.  Only 'json' and 'xml' formats are supported.");
      }

      LOG.info("Verified question in request: " + question.getFullName());
      LOG.info("Incoming params: " + FormatUtil.prettyPrint(new HttpRequestData(request).getTypedParamMap(),
          Style.MULTI_LINE, obj -> FormatUtil.arrayToString(obj)));

      Map<String, String> outputConfig = new LinkedHashMap<String, String>();

      // prepare and get the param values
      Map<String, ParamBean<?>> params = question.getParamsMap();
      LegacyRestServiceRequestParams requestParams = new LegacyRestServiceRequestParams(request);
      Map<String, String> stableValues = new LinkedHashMap<>();
      for (String paramName : params.keySet()) {
        ParamBean<?> param = params.get(paramName);
        String stableValue = param.getStableValue(wdkUser, requestParams);
        stableValues.put(paramName, stableValue);
      }

      // get other fields and validate
      for (String key : requestParams.paramNames()) {
        if (key.startsWith("o-")) {
          String[] values = requestParams.getArray(key);
          if (values != null && values.length > 0) {
            // build comma-delimited list from array
            outputConfig.put(key, FormatUtil.join(values, ","));
          }
        }
      }
      // hard coded values for web services
      outputConfig.put(StandardConfig.ATTACHMENT_TYPE, "plain");
      outputConfig.put(StandardConfig.INCLUDE_EMPTY_TABLES, "true");

      StepBean step = wdkUser.createStep(null, question, stableValues, null, false, Utilities.DEFAULT_WEIGHT);
      AnswerValueBean answerValue = step.getAnswerValue();

      Reporter reporter = ReporterFactory.getReporter(answerValue.getAnswerValue(), outputType, outputConfig);

      response.setContentType(reporter.getHttpContentType());
      switch(reporter.getContentDisposition()) {
        case INLINE:
          response.setHeader("Pragma", "Public");
          break;
        case ATTACHMENT:
          response.setHeader("Content-disposition", "attachment; filename=" + reporter.getDownloadFileName());
          break;
        default:
          throw new WdkModelException("Unsupported content disposition: " + reporter.getContentDisposition());
      }

      LOG.info("Writing webservice results");
      OutputStream out = response.getOutputStream();
      reporter.report(out);
      out.flush();
    }
    catch (Exception ex) {
      LOG.error("Error while processing (old) webservice result (outputType=" + outputType + ")", ex);
      if (ex instanceof WdkUserException && outputType != null) {
        WdkUserException uex = (WdkUserException)ex;
        Map<String,String> paramErrors = uex.getParamErrors();
        if (paramErrors != null && !paramErrors.isEmpty()) {
          reportError(response, paramErrors, "Input Parameter Error", "010", outputType);
        }
        else {
          Map<String, String> exMap = new MapBuilder<String, String>("1", ex.getMessage()).toMap();
          reportError(response, exMap, "User Error", "020", outputType);
        }
      }
      else if (ex instanceof WdkModelException && outputType != null) {
        Map<String, String> exMap = new MapBuilder<String, String>("1", ex.getMessage()).toMap();
        reportError(response, exMap, "Output Parameter Error", "011", outputType);
      }
      else {
        Map<String, String> exMap = new MapBuilder<String, String>("0", ex.getMessage()).toMap();
        reportError(response, exMap, "Unknown Error", "000", outputType);
      }
    }
    return null;
  }

  private void reportError(HttpServletResponse response, final Map<String, String> msg, String errType,
      String errCode, String type) {
    OutputStream out = null;
    try {
      // try to get stream and write to it; if it has already been closed or opened in a way we can no
      //   longer write, just write generic error message.  Exception is logged already.
      out = response.getOutputStream();
      out.write(' ');
    }
    catch(Exception e) {
      // could not get writer or could not write to it
      LOG.error("Unable to handle webservices error; Could not " + (out == null ?
          "get handle on servlet output stream." : "write to servlet output stream."));
      return;
    }
    try {
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
      // this may not work if header is already out the door
      response.setHeader("Pragma", "Public");
      // choose error format to match requested data format
      if (type.equals("xml")) {
        response.setContentType("text/xml");
        writer.println("<?xml version='1.0' encoding='UTF-8'?>");
        writer.println("<response>");
        writer.println("<error type='" + errType + "' code='" + errCode + "'>");
        for (String m : msg.keySet()) {
          writer.println("<msg><![CDATA[" + m + ": " + msg.get(m) + "]]></msg>");
        }
        writer.println("</error>");
        writer.println("</response>");
      }
      else {
        response.setContentType("text/plain");
        String messages = FormatUtil.join(Functions.mapToList(msg.keySet(), new Function<String,String>() {
          @Override public String apply(String key) { return "\"" + key + ": " + msg.get(key) + "\""; }
        }).toArray(), ",");
        writer.print("{\"response\":{\"error\":{\"type\":\"" + errType + "\",\"code\":\"" + errCode +
            "\",\"msg\":[" + messages + "]}}}");
      }
      writer.flush();
    }
    catch(Exception e) {
      LOG.error("Error writing webservices error response.  No further output will be sent.", e);
    }
  }

  private void createWADL(HttpServletRequest request, HttpServletResponse response,
      QuestionSetBean questionSet, QuestionBean question, boolean isAllQuestions) throws IOException, WdkModelException {
    response.setHeader("Pragma", "Public");
    response.setContentType("text/xml");
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()));
    try {
      writer.println("<?xml version='1.0'?>");
      writer.println("<application xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
          + "xsi:schemaLocation='http://wadl.dev.java.net/2009/02 wadl.xsd' "
          + "xmlns:xsd='http://www.w3.org/2001/XMLSchema' " + "xmlns='http://wadl.dev.java.net/2009/02'>");
      String base = request.getHeader("Host") + "/webservices/";
      writer.println("<resources base='http://" + base + "'>");
      writer.println("<resource path='" + questionSet.getName() + "'>");
      if (isAllQuestions) {
        for (QuestionBean ques : questionSet.getQuestions()) {
          writeWADL(ques, writer);
        }
      }
      else {
        writeWADL(question, writer);
      }
      writer.println("</resource>");
      writer.println("</resources>");
      writer.println("</application>");
    }
    finally {
      writer.flush();
    }
  }

  private void writeWADL(QuestionBean wdkQuestion, PrintWriter writer) throws WdkModelException {
    LOG.debug(wdkQuestion.getDisplayName());
    String def_value = "";
    String repeating = "";
    writer.println("<resource path='" + wdkQuestion.getName() + ".xml'>");
    writer.println("<method href='#" + wdkQuestion.getName().toLowerCase() + "'/>");
    writer.println("</resource>");
    writer.println("<resource path='" + wdkQuestion.getName() + ".json'>");
    writer.println("<method href='#" + wdkQuestion.getName().toLowerCase() + "'/>");
    writer.println("</resource>");
    writer.println("<method name='POST' id='" + wdkQuestion.getName().toLowerCase() + "'>");
    writer.println("<doc title='display_name'><![CDATA[" + wdkQuestion.getDisplayName() + "]]></doc>");
    writer.println("<doc title='summary'><![CDATA[" + wdkQuestion.getSummary() + "]]></doc>");
    writer.println("<doc title='description'><![CDATA[" + wdkQuestion.getDescription() + "]]></doc>");
    writer.println("<request>");
    Map<String, ParamBean<?>> params = wdkQuestion.getParamsMap();
    for (String key : params.keySet()) {
      ParamBean<?> param = params.get(key);
      def_value = param.getDefault();
      repeating = "";
      if (param instanceof EnumParamBean) {
        EnumParamBean ep = (EnumParamBean)param;
        def_value = filterDefaultValue(ep, def_value);
        if (ep.getMultiPick())
          repeating = "repeating='true'";
        else
          repeating = "repeating='false'";
      }
      if (def_value != null && def_value.length() > 0) {
        def_value = htmlEncode(def_value);
      }
      else if (def_value == null)
        def_value = "";

      // dataset param will display the data sub-param instead
      if (param instanceof DatasetParamBean) {
        key = ((DatasetParamBean) param).getDataSubParam();
      }

      writer.println("<param name='" + key + "' type='xsd:string' required='" + !param.getIsAllowEmpty() +
          "' default='" + def_value + "' " + repeating + ">");
      writer.println("<doc title='prompt'><![CDATA[" + param.getPrompt() + "]]></doc>");
      writer.println("<doc title='help'><![CDATA[" + param.getHelp() + "]]></doc>");
      writer.println("<doc title='default'><![CDATA[" + param.getDefault() + "]]></doc>");

      if (param instanceof EnumParamBean) {
        EnumParamBean ep = (EnumParamBean) param;
        if (ep.getMultiPick())
          writer.println("<doc title='MultiValued'>" + "Provide one or more values. "
              + "Use comma as a delimter.</doc>");
        else
          writer.println("<doc title='SingleValued'>Choose " + "at most one value from the options</doc>");

        Set<String> values = ep.getAllValues();
        for (String term : values) {
          // writer.println("<option>" + term + "</option>");
          writer.println("<option value='" + htmlEncode(term) + "'><doc title='description'><![CDATA[" +
              term + "]]></doc></option>");
        }
      }
      writer.println("</param>");

      // for datasetParam, will also show additional sub-params
      if (param instanceof DatasetParamBean) {
        DatasetParamBean datasetParam = (DatasetParamBean) param;
        String parserParam = datasetParam.getParserSubParam();
        String defaultParser = ListDatasetParser.NAME;
        writer.println("<param name='" + parserParam + "' type='xsd:string' required='false' default='" +
            defaultParser + "' " + repeating + ">");
        writer.println("  <doc title='prompt'><![CDATA[Input format]]></doc>");
        writer.println("  <doc title='help'><![CDATA[The format of the input data for param " +
            datasetParam.getName() + "]]></doc>");
        writer.println("  <doc title='default'><![CDATA[" + defaultParser + "]]></doc>");
        for (DatasetParser parser : datasetParam.getParsers()) {
          writer.println("  <option value='" + htmlEncode(parser.getName()) + "'>");
          writer.println("    <doc title='display'><![CDATA[" + parser.getDisplay() + "]]></doc>");
          writer.println("    <doc title='description'><![CDATA[" + parser.getDescription() + "]]></doc>");
          writer.println("  </option>");
        }
        writer.println("</param>");
      }
    }
    writer.println("<param name='o-fields' type='xsd:string' required='false' default='none' repeating='true'>");
    writer.println("<doc title='prompt'><![CDATA[Output Fields]]></doc>");
    writer.println("<doc title='help'><![CDATA[Single valued attributes of the feature.]]></doc>");
    writer.println("<doc title='default'><![CDATA[none]]></doc>");
    writer.println("<doc title='MultiValued'>Provide one or more values. Use comma as a delimter.</doc>");
    // writer.println("<option>all</option>");
    // writer.println("<option>none</option>");
		//   writer.println("<option value='all'><doc title='description'>Show all attributes</doc></option>");
    writer.println("<option value='none'><doc title='description'>Show no attributes</doc></option>");
    for (String attr : wdkQuestion.getReportMakerAttributesMap().keySet())
      writer.println("<option value='" + attr.replaceAll("'", "&apos;") +
          "'><doc title='description'><![CDATA[" +
          wdkQuestion.getReportMakerAttributesMap().get(attr).getDisplayName() + "]]></doc></option>");
    // writer.println("<option>" + attr + "</option>");
    writer.println("</param>");
    writer.println("<param name='o-tables' type='xsd:string' required='false' default='none' repeating='true'>");
    writer.println("<doc title='prompt'><![CDATA[Output Tables]]></doc>");
    writer.println("<doc title='help'><![CDATA[Multi-valued attributes of the feature.]]></doc>");
    writer.println("<doc title='default'><![CDATA[none]]></doc>");
    writer.println("<doc title='MultiValued'>Provide one or more values. Use comma as a delimter.</doc>");
    // writer.println("<option >all</option>");
    // writer.println("<option>none</option>");
    //writer.println("<option value='all'><doc title='description'>Show all tables</doc></option>");
		 writer.println("<option value='none'><doc title='description'>Show no tables</doc></option>");
    for (String tab : wdkQuestion.getReportMakerTablesMap().keySet())
      writer.println("<option value='" + tab + "'><doc title='description'><![CDATA[" +
          wdkQuestion.getReportMakerTablesMap().get(tab).getDisplayName() + "]]></doc></option>");
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

  private String filterDefaultValue(EnumParamBean param, String defaultValue) {
    if (defaultValue == null || defaultValue.length() == 0)
      return defaultValue;

    String[] terms = defaultValue.split(",");
    Map<String, String> displayMap = getDisplayMap(param);
    StringBuilder values = new StringBuilder();
    for (String term : terms) {
      if (!displayMap.containsKey(term))
        continue;
      if (values.length() > 0)
        values.append(",");
      values.append(term);
    }
    return values.toString();
  }

  private Map<String, String> getDisplayMap(EnumParamBean param) {
    String displayType = param.getDisplayType();
    boolean isTreeBox = (displayType != null && displayType.equals(AbstractEnumParam.DISPLAY_TREEBOX));
    LOG.debug(param.getFullName() + " as tree: " + isTreeBox);
    if (!isTreeBox)
      return param.getDisplayMap();

    Stack<EnumParamTermNode> stack = new Stack<EnumParamTermNode>();
    for (EnumParamTermNode root : param.getVocabTreeRoots())
      stack.push(root);

    Map<String, String> displayMap = new LinkedHashMap<String, String>();
    while (!stack.isEmpty()) {
      EnumParamTermNode node = stack.pop();
      EnumParamTermNode[] children = node.getChildren();
      if (children.length == 0) { // find a leaf, output its term/display
        displayMap.put(node.getTerm(), node.getDisplay());
      }
      else { // internal node, skip and process its children
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
}
