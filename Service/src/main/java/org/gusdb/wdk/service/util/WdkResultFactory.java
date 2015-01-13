package org.gusdb.wdk.service.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.FilterBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.service.util.WdkResultRequestSpecifics.SortItem;

public class WdkResultFactory {

  private final UserBean _user;

  public WdkResultFactory(UserBean user) {
    _user = user;
  }

  public AnswerValueBean createResult(WdkResultRequest request, WdkResultRequestSpecifics specifics) throws WdkModelException {
    try {
      QuestionBean questionBean = request.getQuestion();
      // FIXME: for now just past name of first filter if one exists
      List<FilterBean> filters = request.getFilterValues();
      String filter = (filters.isEmpty() ? null : filters.iterator().next().getName());
      int startIndex = specifics.getOffset();
      int endIndex = startIndex + specifics.getNumRecords();
      questionBean.makeAnswerValue(_user, convertParams(request.getParamValues()), startIndex, endIndex,
          convertSorting(specifics.getSorting()), filter, true, 0);
      return request.getQuestion().getAnswerValue();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  public AnswerValueBean createResult(WdkResultRequest request) throws WdkModelException {
    try {
      QuestionBean questionBean = request.getQuestion();
      questionBean.makeAnswerValue(_user, convertParams(request.getParamValues()), true, 0);
      return request.getQuestion().getAnswerValue();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  private Map<String, String> convertParams(Map<String, ParamBean<?>> params) {
    Map<String, String> conversion = new HashMap<>();
    for (ParamBean<?> param : params.values()) {
      // FIXME: use getValue().toString() once implemented (or whatever is appropriate
      conversion.put(param.getName(), param.getStableValue());
    }
    return conversion;
  }
  
  private Map<String, Boolean> convertSorting(List<SortItem> sorting) {
    Map<String, Boolean> conversion = new LinkedHashMap<>();
    for (SortItem sort : sorting) {
      conversion.put(sort.getColumn().getName(), sort.getDirection().getBoolValue());
    }
    return conversion;
  }
}
