package org.gusdb.wdk.model.answer.filter;

import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.answer.AnswerValue;

public interface Filter {

  String getSql(AnswerValue answer);
  
  void addProperty(WdkModelText property);
}
