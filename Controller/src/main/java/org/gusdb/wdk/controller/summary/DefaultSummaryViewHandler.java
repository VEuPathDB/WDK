package org.gusdb.wdk.controller.summary;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryViewHandler;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

public class DefaultSummaryViewHandler implements SummaryViewHandler {

  @Override
  public Map<String, Object> process(Step step, Map<String, String[]> parameters, User user, WdkModel model)
      throws WdkModelException, WdkUserException {
    UserBean userBean = new UserBean(user);
    StepBean stepBean = new StepBean(userBean, step);
    AnswerValueBean answer = stepBean.getViewAnswerValue();
    answer.getRecords();
    return ResultTablePaging.processPaging(parameters, stepBean.getQuestion(), userBean, answer);
  }

  @Override
  public String processUpdate(Step step, Map<String, String[]> parameters, User user, WdkModel wdkModel)
      throws WdkModelException, WdkUserException {
    return SummaryTableUpdateProcessor.processUpdates(step, parameters, user, wdkModel, "");
  }

}
