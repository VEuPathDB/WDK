package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

/**
 * All record class primary key attributes need to be associated either with an alias query
 * or an alias query so that the primary key may be determined to be valid.  An invalid
 * primary key will result in a 404 to the client.  A valid primary key that otherwise has
 * an issue will result in a 500 to the client.  In some cases, there is no way to access
 * the validity of a primary key.  In those cases, we need a simple plugin that simply
 * returns the primary key given by the client.
 * 
 * @author crisl-adm
 */
public class GenericRecordPrimaryKeyAliasPlugin implements PrimaryKeyAliasPlugin {

  @Override
  public List<Map<String, Object>> getPrimaryKey(User user, Map<String, Object> inputPkValues)
      throws WdkModelException, WdkUserException {

    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    result.add(inputPkValues);
    return result;
  }

}
