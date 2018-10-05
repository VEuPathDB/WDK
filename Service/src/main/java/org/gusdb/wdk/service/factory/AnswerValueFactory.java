package org.gusdb.wdk.service.factory;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.ParamValue;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.answer.AnswerDetails;
import org.gusdb.wdk.service.request.answer.AttributeFieldSortSpec;
import org.gusdb.wdk.service.request.exception.DataValidationException;

public class AnswerValueFactory {

  private final User _user;

  public AnswerValueFactory(User user) {
    _user = user;
  }

  public AnswerValue createFromAnswerSpec(AnswerSpec request) throws WdkModelException, DataValidationException {
    try {
      // FIXME: looks like index starts at 1 and end index is inclusive;
      //   would much rather see 0-based start and have end index be exclusive
      //   (i.e. need to fix on AnswerValue)
      AnswerValue answerValue = request.getQuestion().makeAnswerValue(_user,
          request.getQueryInstanceSpec().toMap(), 1, -1,
          null, request.getLegacyFilter(), true, request.getAssignedWeight());
      answerValue.setFilterOptions(request.getFilterOptions());
      answerValue.setViewFilterOptions(request.getViewFilterOptions());
      return answerValue;
    }
    catch (WdkUserException e) {
      throw new DataValidationException(FormatUtil.prettyPrint(e.getParamErrors(), Style.MULTI_LINE), e);
    }
  }

  public AnswerValue getConfiguredAnswer(AnswerValue answerValue, AnswerDetails config) throws WdkModelException {
    int startIndex = config.getOffset() + 1;
    int endIndex = startIndex + config.getNumRecords() - 1;
    AnswerValue configuredAnswer = answerValue.cloneWithNewPaging(startIndex, endIndex);
    Map<String, Boolean>  sorting = AttributeFieldSortSpec.convertSorting(config.getSorting());
    configuredAnswer.setSortingMap(sorting);
    return configuredAnswer;
  }

  public static Map<String, String> convertParams(Map<String, ParamValue> params) {
    Map<String, String> conversion = new HashMap<>();
    for (ParamValue param : params.values()) {
      conversion.put(param.getName(), param.getObjectValue() == null ? null : param.getObjectValue().toString());
    }
    return conversion;
  }
}
