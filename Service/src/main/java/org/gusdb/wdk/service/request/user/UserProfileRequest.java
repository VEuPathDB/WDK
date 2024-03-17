package org.gusdb.wdk.service.request.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.accountdb.UserProfile;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the JSON object returned by either a PUT or PATCH REST request for
 * a User profile.
 * 
 * @author crisl-adm
 */
public class UserProfileRequest {

  private static Logger LOG = Logger.getLogger(UserProfileRequest.class);

  private static final String PROFILE_VALUES_TOO_LONG = "The following profile values exceed the maximum " +
      "allowed length (" + UserProfile.MAX_PROPERTY_VALUE_SIZE + "): ";
  private static final String REQUIRED_VALUES = "The following profile values cannot be empty: ";
  private static final String ALL_PROPS_REQUIRED = "All profile properties are required in this request. " +
      "The following profile values are missing: ";

  private String _email;
  private Map<String,String> _profileMap;

  public String getEmail() {
    return _email;
  }

  public void setEmail(String email) {
    _email = email;
  }

  public Map<String,String> getProfileMap() {
    return _profileMap;
  }

  public void setProfileMap(Map<String,String> profileMap) {
    _profileMap = profileMap;
  }

  /**
   * Input Format:
   * {
   *  String key: String,
   *  String key: String,
   *  ...
   * }
   * 
   * @param json request body for a profile request
   * @param configuredProps user profile properties configured in the model
   * @param requireRequiredProperties whether to error if not all required properties present
   * @return validated request data
   * @throws RequestMisformatException if JSON is misformatted or has incorrect data types
   * @throws DataValidationException if data contained in request is invalid for another reason
   */
  public static UserProfileRequest createFromJson(JSONObject json,
      Collection<UserProperty> configuredProps, boolean requireRequiredProperties)
      throws RequestMisformatException, DataValidationException {
    try {
      UserProfileRequest request = new UserProfileRequest();
      request.setEmail(validateEmail(json, requireRequiredProperties));
      request.setProfileMap(parseProperties(json, getPropMap(configuredProps), requireRequiredProperties));
      return request;
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

  private static String validateEmail(JSONObject json, boolean isRequired)
      throws JSONException, DataValidationException {
    String emailProp = "Profile property 'email' ";
    if (isRequired && !json.has(JsonKeys.EMAIL)) {
      throw new DataValidationException(emailProp + "is required in this request.");
    }
    String email = json.getString(JsonKeys.EMAIL).trim();
    if (email.isEmpty()) {
      throw new DataValidationException(emailProp + "cannot be empty.");
    }
    if (FormatUtil.getUtf8EncodedBytes(email).length > UserProfile.MAX_EMAIL_LENGTH) {
      throw new DataValidationException(emailProp + "cannot be longer than " + UserProfile.MAX_EMAIL_LENGTH + " characters.");
    }
    return email;
  }

  private static Map<String, UserProperty> getPropMap(Collection<UserProperty> configuredProps) {
    return Functions.getMapFromValues(configuredProps, UserProperty::getName);
  }

  /**
   * Provides a map of the profile related JSON objects.  Only retrieves those
   * key/value pairs that belong in a user profile.
   * @param json properties object
   * @param configuredProps property name configuration from the model
   * @param requireRequiredProps whether to throw exception if not all required props are present
   * @return - map of user profile property enum | value pairs
   * @throws JSONException among other reasons, thrown in the event of a non-string value detail message
   * @throws DataValidationException if JSON format and types are ok, but further data validation fails
   */
  private static Map<String,String> parseProperties(JSONObject json,
      Map<String, UserProperty> configuredProps, boolean requireRequiredProperties) throws JSONException, DataValidationException {
    if (!json.has(JsonKeys.PROPERTIES)) {
      if (requireRequiredProperties) {
        throw new JSONException("'properties' property is required.");
      }
      else {
        return Collections.EMPTY_MAP;
      }
    }
    JSONObject propsJson = json.getJSONObject(JsonKeys.PROPERTIES);
    List<String> unrecognizedProps = new ArrayList<>();
    List<String> oversizedProps = new ArrayList<>();
    Map<String, String> parsedProps = new HashMap<>();
    for (String key : JsonUtil.getKeys(propsJson)) {
      if (configuredProps.containsKey(key)) {
        String value = propsJson.getString(key).trim();
        if (FormatUtil.getUtf8EncodedBytes(value).length > UserProfile.MAX_PROPERTY_VALUE_SIZE) {
          oversizedProps.add(key);
        }
        else {
          parsedProps.put(key, value);
        }
      }
      else {
        unrecognizedProps.add(key);
      }
    }
    validateUnrecognized(unrecognizedProps);
    validatePropertyLengths(oversizedProps);
    validateNonEmptyRequirement(parsedProps, configuredProps);
    if (requireRequiredProperties) {
      validateRequiredPropsPresent(parsedProps, configuredProps);
    }
    return parsedProps;
  }

  private static void validateRequiredPropsPresent(Map<String, String> parsedProps,
      Map<String, UserProperty> configuredProps) throws DataValidationException {
    List<String> missingProps = new ArrayList<>();
    for (String propKey : configuredProps.keySet()) {
      if (!parsedProps.containsKey(propKey) && configuredProps.get(propKey).isRequired()) {
        missingProps.add(propKey);
      }
    }
    if (!missingProps.isEmpty()) {
      throw new DataValidationException(ALL_PROPS_REQUIRED + FormatUtil.join(missingProps.toArray(), ", "));
    }
  }

  private static void validateNonEmptyRequirement(Map<String, String> parsedProps,
      Map<String, UserProperty> configuredProps) throws DataValidationException {
    // make sure any required (i.e. value must be non-empty) properties are not being modified to be empty
    List<String> emptyRequiredValues = new ArrayList<String>();
    for (String propKey : configuredProps.keySet()) {
      if (configuredProps.get(propKey).isRequired()) {
        String value = parsedProps.get(propKey);
        if (value != null && value.isEmpty()) {
          emptyRequiredValues.add(propKey);
        }
      }
    }
    if (!emptyRequiredValues.isEmpty()) {
      throw new DataValidationException(REQUIRED_VALUES + FormatUtil.join(emptyRequiredValues.toArray(), ", "));
    }
  }

  private static void validatePropertyLengths(List<String> oversizedProperties) throws DataValidationException {
    // error if properties may not be over a certain length
    if (!oversizedProperties.isEmpty()) {
      String lengthy = FormatUtil.join(oversizedProperties.toArray(), ",");
      throw new DataValidationException(PROFILE_VALUES_TOO_LONG + lengthy);
    }
  }

  private static void validateUnrecognized(List<String> unrecognizedProperties) {
    // ignore unrecognized properties, but log warning- this is a potential error on the client
    // NOTE: converting this to an exception will break ClinEpi, which appends additional props onto user JSON
    if (!unrecognizedProperties.isEmpty()) {
      String unrecognized = FormatUtil.join(unrecognizedProperties.toArray(), ",");
      LOG.warn("This user service request contains the following unrecognized profile property names: " + unrecognized);
    }
  }
}
