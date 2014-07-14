package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.actionutil.QuestionRequestParams;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.RequestParams;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetVocabAction extends Action {

  private static final Logger logger = Logger.getLogger(GetVocabAction.class);

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.struts.action.Action#execute(org.apache.struts.action. ActionMapping,
   * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.trace("Entering GetVocabAction...");
    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
    try {
      String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
      wdkModel.validateQuestionFullName(qFullName);
      String paramName = request.getParameter("name");

      // the dependent values are a JSON representation of {name: [values],
      // name: [values],...}
      Map<String, String> dependedValues = new LinkedHashMap<>();
      String values = request.getParameter("dependedValue");
      if (values != null && values.length() > 0) {
        JSONObject jsValues = new JSONObject(values);
        @SuppressWarnings("unchecked")
        Iterator<String> keys = jsValues.keys();
        while (keys.hasNext()) {
          String pName = keys.next();

          JSONArray jsArray = jsValues.getJSONArray(pName);
          StringBuilder buffer = new StringBuilder();
          for (int i = 0; i < jsArray.length(); i++) {
            if (buffer.length() > 0)
              buffer.append(",");
            buffer.append(jsArray.getString(i));
          }
          dependedValues.put(pName, buffer.toString());
        }
      }

      boolean getJson = Boolean.valueOf(request.getParameter("json"));
      boolean getXml = Boolean.valueOf(request.getParameter("xml"));
      QuestionBean wdkQuestion = wdkModel.getQuestion(qFullName);
      EnumParamBean param = (EnumParamBean) wdkQuestion.getParamsMap().get(paramName);

      param.setDependedValues(dependedValues);

      UserBean user = ActionUtility.getUser(servlet, request);
      QuestionForm qForm = (QuestionForm) form;
      RequestParams requestParams = new QuestionRequestParams(request, qForm);
      param.prepareDisplay(user, requestParams, dependedValues);
      request.setAttribute("vocabParam", param);

      if (getJson) {    // output json string directly
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        JSONObject jsValues = param.getJsonValues();
        writer.print(jsValues.toString());
        writer.flush();
        writer.close();
        return null;
      }
      else {    // output xml or html.
        String xmlVocabFile = CConstants.WDK_DEFAULT_VIEW_DIR + File.separator + CConstants.WDK_PAGES_DIR +
            File.separator + "vocabXml.jsp";

        String htmlVocabFile = CConstants.WDK_DEFAULT_VIEW_DIR + File.separator + CConstants.WDK_PAGES_DIR +
            File.separator + "vocabHtml.jsp";

        ActionForward forward = new ActionForward(getXml ? xmlVocabFile : htmlVocabFile);

        logger.trace("Leaving GetVocabAction...");
        return forward;
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }
}
