package org.gusdb.wdk.model.record.alias;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordNotFoundException;
import org.gusdb.wdk.model.user.User;

public interface PrimaryKeyAliasPlugin {

  List<Map<String, Object>> getPrimaryKey(User user, Map<String, Object> inputPkValues)
      throws WdkModelException, RecordNotFoundException;

}
