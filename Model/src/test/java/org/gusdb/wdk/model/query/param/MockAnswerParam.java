package org.gusdb.wdk.model.query.param;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MockAnswerParam extends AnswerParam {
  
  private final Set<String> allowedTypes;

  public MockAnswerParam(Collection<String> allowedTypes) {
    this.allowedTypes = new HashSet<>(allowedTypes);
  }

  @Override
  public boolean allowRecordClass(String recordClassName) {
    return allowedTypes.contains(recordClassName);
  }
}
