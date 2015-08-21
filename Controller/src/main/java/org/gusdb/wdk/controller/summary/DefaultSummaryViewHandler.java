package org.gusdb.wdk.controller.summary;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryViewHandler;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.user.Step;

public class DefaultSummaryViewHandler implements SummaryViewHandler {

  @Override
  public Map<String, Object> process(Step step, Map<String, String[]> parameters)
      throws WdkModelException, WdkUserException {
    UserBean user = new UserBean(step.getUser());
    StepBean stepBean = new StepBean(user, step);
    AnswerValueBean answer = stepBean.getViewAnswerValue();
    answer.getRecords();
    return ResultTablePaging.processPaging(parameters, stepBean.getQuestion(), user, answer);
  }

}
