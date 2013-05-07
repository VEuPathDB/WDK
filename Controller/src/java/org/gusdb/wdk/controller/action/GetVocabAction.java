package org.gusdb.wdk.controller.action;

import java.io.File;
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
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.WdkEmptyEnumListException;
import org.json.JSONObject;
import org.json.JSONArray;

public class GetVocabAction extends Action {

  private static final Logger logger = Logger.getLogger(GetVocabAction.class);

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.struts.action.Action#execute(org.apache.struts.action.
   * ActionMapping, org.apache.struts.action.ActionForm,
   * javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    logger.trace("Entering GetVocabAction...");
    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
    try {
      String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
      String paramName = request.getParameter("name");

      // the dependent values are a JSON representation of {name: [values], name: [values],...}
      Map<String, String> dependedValues = new LinkedHashMap<>();
      String values = request.getParameter("dependedValue");
      if (values != null && values.length() > 0) {
        JSONObject jsValues = new JSONObject(values);
        Iterator<String> keys = jsValues.keys();
        while (keys.hasNext()) {
          String pName = keys.next();
          
          JSONArray jsArray = jsValues.getJSONArray(pName);
          StringBuilder buffer = new StringBuilder();
          for (int i = 0; i < jsArray.length(); i++) {
            if (buffer.length() > 0) buffer.append(",");
            buffer.append(jsArray.getString(i));
          }
          dependedValues.put(pName, buffer.toString());
        }
      }

      boolean getXml = Boolean.valueOf(request.getParameter("xml"));
      QuestionBean wdkQuestion = wdkModel.getQuestion(qFullName);
      EnumParamBean param = (EnumParamBean) wdkQuestion.getParamsMap().get(
          paramName);

      param.setDependedValues(dependedValues);

      // try the dependent value, and ignore empty list exception, since
      // it may be caused by the choices on the depended param.
      try {
        param.getDisplayMap();
      } catch (WdkEmptyEnumListException ex) {
        // do nothing.
        logger.debug("the choice of the depended param cause this: " + ex);
      }

      request.setAttribute("vocabParam", param);

      String xmlVocabFile = CConstants.WDK_DEFAULT_VIEW_DIR + File.separator
          + CConstants.WDK_PAGES_DIR + File.separator + "vocabXml.jsp";

      String htmlVocabFile = CConstants.WDK_DEFAULT_VIEW_DIR + File.separator
          + CConstants.WDK_PAGES_DIR + File.separator + "vocabHtml.jsp";

      ActionForward forward;

      if (getXml) {
        forward = new ActionForward(xmlVocabFile);
      } else {
        ShowQuestionAction.prepareQuestionForm(wdkQuestion, getServlet(),
            request, (QuestionForm) form);
        forward = new ActionForward(htmlVocabFile);
      }

      logger.trace("Leaving GetVocabAction...");
      return forward;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }
}
