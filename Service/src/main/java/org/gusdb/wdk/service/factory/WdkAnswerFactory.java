package org.gusdb.wdk.service.factory;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.beans.ParamValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.service.request.answer.AnswerRequest;
import org.gusdb.wdk.service.request.answer.AnswerRequestSpecifics;
import org.gusdb.wdk.service.request.answer.SortItem;

public class WdkAnswerFactory {

  private final UserBean _user;

  public WdkAnswerFactory(UserBean user) {
    _user = user;
  }

  public AnswerValueBean createAnswer(AnswerRequest request, AnswerRequestSpecifics specifics) throws WdkModelException {
    try {
      // FIXME: looks like index starts at 1 and end index is inclusive;
      //   would much rather see 0-based start and have end index be exclusive
      //   (i.e. need to fix on AnswerValue)
      int startIndex = specifics.getOffset() + 1;
      int endIndex = startIndex + specifics.getNumRecords() - 1;
      Map<String, Boolean>  sorting = SortItem.convertSorting(specifics.getSorting());
      AnswerValue answer = request.getQuestion().makeAnswerValue(_user.getUser(),
          convertParams(request.getParamValues()), startIndex, endIndex,
          sorting, request.getLegacyFilter(), true, request.getWeight());
      answer.setFilterOptions(request.getFilterValues());
      answer.setViewFilterOptions(request.getViewFilterValues());
      return new AnswerValueBean(answer);
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
}
