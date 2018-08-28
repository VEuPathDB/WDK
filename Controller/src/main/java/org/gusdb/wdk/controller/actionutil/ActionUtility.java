package org.gusdb.wdk.controller.actionutil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gusdb.wdk.controller.WdkInitializer;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * Heritage methods to access the current user,  model, and request params
 * 
 * @author xingao
 */
public class ActionUtility {

    public static UserBean getUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            UserBean wdkUser = (UserBean) session.getAttribute(Utilities.WDK_USER_KEY);
            if (wdkUser == null) {
              throw new IllegalStateException("No user present on session. This should never happen.");
            }
            return wdkUser;
        }
        catch (Exception ex) {
            throw new WdkRuntimeException(ex);
        }
    }

    public static WdkModelBean getWdkModel(HttpServlet servlet) {
        return new WdkModelBean(WdkInitializer.getWdkModel(servlet.getServletContext()));
    }

    public static Map<String, String> getParams(ServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        Enumeration<?> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String[] values = request.getParameterValues(name);
            String value = Utilities.fromArray(values, ",");
            params.put(name, value);
        }
        return params;
    }

    public static void applyModel(HttpServletRequest request, Map<String, Object> model) {
      for (Entry<String, Object> modelValue : model.entrySet()) {
        request.setAttribute(modelValue.getKey(), modelValue.getValue());
      }
    }


    public static AnswerValueBean makeAnswerValue(UserBean user, QuestionBean question, Map<String, String> params)
        throws WdkModelException, WdkUserException {
      return new AnswerValueBean(
        AnswerValueFactory.makeAnswer(user.getUser(),
          AnswerSpec.builder(question.getQuestion().getWdkModel())
          .setQuestionName(question.getFullName())
          .setQueryInstanceSpec(QueryInstanceSpec.builder().putAll(params))
          .buildRunnable()));
    }
}
