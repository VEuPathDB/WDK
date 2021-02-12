package org.gusdb.wdk.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.service.request.exception.DataValidationException;

/**
 * Simple validator to check key and value lengths for user preferences and profile properties
 * 
 * @author crisl
 */
public class UserPreferenceValidator {

  private static final int PREFERENCE_NAME_MAX_LENGTH = 4000;
  private static final int PREFERENCE_VALUE_MAX_LENGTH = 4000;

  private static final String PROPERTIES_TOO_LONG = "The following property names and/or values exceed their maximum allowed lengths of ";

  /**
   * Validates whether all preference names and values have string lengths at or below the corresponding maximum limits 
   * @param propertiesMap - map of preferences as name/value pairs
   * @throws DataValidationException - thrown if one or more user preference exceeds the maximum allowed legnth for either its name or value.
   */
  public static void validatePreferenceSizes(Map<String, String> propertiesMap) throws DataValidationException {
    List<String> lengthyData = new ArrayList<>();
    for (String property : propertiesMap.keySet()) {
      int keyLength = FormatUtil.getUtf8EncodedBytes(property).length;
      int valueLength = FormatUtil.getUtf8EncodedBytes(propertiesMap.get(property)).length;
      if(keyLength > PREFERENCE_NAME_MAX_LENGTH || valueLength > PREFERENCE_VALUE_MAX_LENGTH) {
        lengthyData.add(property + " : " + propertiesMap.get(property));
      }
    }
    if(!lengthyData.isEmpty()) {
      String lengthy = FormatUtil.join(lengthyData.toArray(), ",");
      throw new DataValidationException(PROPERTIES_TOO_LONG + PREFERENCE_NAME_MAX_LENGTH + " / " + PREFERENCE_VALUE_MAX_LENGTH + ": " + lengthy);
    }
  }
}
