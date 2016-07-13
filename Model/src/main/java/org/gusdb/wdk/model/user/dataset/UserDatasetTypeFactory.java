package org.gusdb.wdk.model.user.dataset;

import java.util.HashMap;
import java.util.Map;

/**
 * Construct UserDatasetTypes, enforcing one singleton per (name, version) pair
 * @author steve
 *
 */
public final class UserDatasetTypeFactory {
  
  static Map<String, UserDatasetType> map = new HashMap<String, UserDatasetType>();
    
  public static UserDatasetType getUserDatasetType(String name, String version) {
    // there must be a better way to do this
    if (!map.containsKey(name + "_DELIM_" + version)) {
      map.put(name + "_DELIM_" + version, new UserDatasetType(name, version));
    }
    return map.get(name + "_DELIM_" + version);
  }

}
