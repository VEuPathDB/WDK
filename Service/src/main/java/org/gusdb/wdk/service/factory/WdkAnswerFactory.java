package org.gusdb.wdk.service.factory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.beans.ParamValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.service.request.WdkAnswerRequest;
import org.gusdb.wdk.service.request.WdkAnswerRequestSpecifics;
import org.gusdb.wdk.service.request.WdkAnswerRequestSpecifics.SortItem;

public class WdkAnswerFactory {

  private final UserBean _user;

  public WdkAnswerFactory(UserBean user) {
    _user = user;
  }

  public AnswerValueBean createResult(WdkAnswerRequest request, WdkAnswerRequestSpecifics specifics) throws WdkModelException {
    if (specifics == null)
      return createResult(request);
    try {
      Question question = request.getQuestion();
      // FIXME: looks like index starts at 1 and end index is inclusive;
      //   would much rather see 0-based start and have end index be exclusive
      //   (i.e. need to fix on AnswerValue)
      int startIndex = specifics.getOffset() + 1;
      int endIndex = startIndex + specifics.getNumRecords() - 1;
      AnswerValue answer = question.makeAnswerValue(_user.getUser(),
          convertParams(request.getParamValues()), startIndex, endIndex,
          convertSorting(specifics.getSorting()), request.getLegacyFilter(), true, 0);
      answer.setFilterOptions(request.getFilterValues());
      answer.setViewFilterOptions(request.getViewFilterValues());
      // if view filters are present, then apply them; if user
      //   doesn't want them, they will omit in request
      answer.setApplyViewFilters(true);
      return new AnswerValueBean(answer);
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  public AnswerValueBean createResult(WdkAnswerRequest request) throws WdkModelException {
    try {
      Question question = request.getQuestion();
      return new QuestionBean(question).makeAnswerValue(_user, convertParams(request.getParamValues()), true, 0);
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  private Map<String, String> convertParams(Map<String, ParamValue> params) {
    Map<String, String> conversion = new HashMap<>();
    for (ParamValue param : params.values()) {
      conversion.put(param.getName(), param.getObjectValue().toString());
    }
    return conversion;
  }

  private Map<String, Boolean> convertSorting(List<SortItem> sorting) {
    Map<String, Boolean> conversion = new LinkedHashMap<>();
    for (SortItem sort : sorting) {
      conversion.put(sort.getAttributeField().getName(), sort.getDirection().getBoolValue());
    }
    return conversion;
  }
}
