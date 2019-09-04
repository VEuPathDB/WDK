package org.gusdb.wdk.model.user.dataset;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;

/**
 * Construct UserDatasetTypes, enforcing one singleton per (name, version) pair
 *
 * @author steve
 */
public final class UserDatasetTypeFactory {

  static Map<String, UserDatasetType> map = new HashMap<>();

  /**
   * Get a singleton UserDatasetType object that conforms to the input name and
   * version
   */
  public static UserDatasetType getUserDatasetType(String name, String version) {
    // there must be a better way to do this
    if (!map.containsKey(name + "_DELIM_" + version)) {
      map.put(name + "_DELIM_" + version, new UserDatasetType(name, version));
    }
    return map.get(name + "_DELIM_" + version);
  }

  /**
   * Find a type handler for the provided user dataset.
   */
  public static UserDatasetTypeHandler getCompatibleTypeHandler(
      Map<UserDatasetType, UserDatasetTypeHandler> handlers, UserDataset userDataset)
          throws WdkModelException {
    return handlers.get(userDataset.getType());
  }
}
