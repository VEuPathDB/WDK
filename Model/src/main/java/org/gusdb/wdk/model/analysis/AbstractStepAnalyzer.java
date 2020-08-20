package org.gusdb.wdk.model.analysis;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.json.JSONException;

public abstract class AbstractStepAnalyzer implements StepAnalyzer {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(AbstractStepAnalyzer.class);
  
  private WdkModel _wdkModel;
  private AnswerValue _answerValue;
  private Map<String, String> _properties = new HashMap<>();
  private Map<String, String> _formParams = new HashMap<>();
  private Path _storageDirectory;
  private String _charData;
  private byte[] _binaryData;
  
  protected WdkModel getWdkModel() {
    return _wdkModel;
  }
  @Override
  public void setWdkModel(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }
  
  protected AnswerValue getAnswerValue() {
    return _answerValue;
  }

  @Override
  public void setAnswerValue(AnswerValue answerValue) {
    _answerValue = answerValue;
  }
  
  protected Path getStorageDirectory() {
    return _storageDirectory;
  }

  @Override
  public void setStorageDirectory(Path storageDirectory) {
    _storageDirectory = storageDirectory;
  }
  
  @Override
  public String getPersistentCharData() {
    return _charData;
  }

  @Override
  public void setPersistentCharData(String data) {
    _charData = data;
  }
  
  @Override
  public byte[] getPersistentBinaryData() {
    return _binaryData;
  }

  @Override
  public void setPersistentBinaryData(byte[] data) {
    _binaryData = data;
  }

  protected Object getPersistentObject() throws WdkModelException {
    try {
      if (_binaryData == null) return null;
      return IoUtil.deserialize(_binaryData);
    }
    catch (ClassNotFoundException | IOException e) {
      throw new WdkModelException("Unable to deserialize object.", e);
    }
  }

  protected void setPersistentObject(Serializable obj) throws WdkModelException {
    try {
      _binaryData = IoUtil.serialize(obj);
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to serialize object.", e);
    }
  }

  protected String getProperty(String key) {
    return _properties.get(key);
  }

  @Override
  public void setProperty(String key, String value) {
    _properties.put(key, value);
  }

  protected Map<String,String> getFormParams() {
    return _formParams;
  }

  @Override
  public void setFormParamValues(Map<String,String> formParams) {
    _formParams = formParams;
  }

  /*%%%%%%%%%%%%%% Helper functions for Property validation %%%%%%%%%%%%%%*/

  protected void checkPropertyExistence(String propName) throws WdkModelException {
    if (getProperty(propName) == null) {
      throw new WdkModelException(String.format(
        "Missing required property '%s' in instance of Step Analysis Plugin '%s'",
        propName,
        getClass().getName()
      ));
    }
  }

  protected void checkAtLeastOneExists(String... propNames) throws WdkModelException {
    for (String propName : propNames) {
      if (getProperty(propName) != null) {
        return; // found one of the properties
      }
    }
    throw new WdkModelException("Missing required property.  " +
        "Must have one or more of " + FormatUtil.arrayToString(propNames));
  }

  protected void checkPositiveIntegerIfPresent(String propName) throws WdkModelException {
    String prop = getProperty(propName);
    if (prop != null && !prop.isEmpty() &&
        !FormatUtil.isInteger(prop) && Integer.parseInt(prop) <= 0) {
      throw new WdkModelException("Optional property '" + propName +
          "' in instance of Step Analysis Plugin '" +
          getClass().getName() + "' must be a positive integer.");
    }
  }

  protected void checkBooleanIfPresent(String propName) throws WdkModelException {
    String prop = getProperty(propName);
    if (prop != null && !prop.isEmpty() &&
        !prop.equalsIgnoreCase("true") && !prop.equalsIgnoreCase("false")) {
      throw new WdkModelException("Optional property '" + propName +
          "' in instance of Step Analysis Plugin '" +
          getClass().getName() + "' must have value 'true' or 'false'.");
    }
  }

  /*%%%%%%%%%%%%%% Helper functions for Parameter validation %%%%%%%%%%%%%%*/

  protected static String getSingleValue(Map<String, String> params, String enumParamName) throws WdkUserException {
    String errPrefix = "Enum Parameter '" + enumParamName + "': ";
    String enumParamStableValue = params.get(enumParamName);
    try {
      if (enumParamStableValue == null) {
        throw new WdkUserException(errPrefix + "Value is required.");
      }
      List<String> terms = AbstractEnumParam.convertToTerms(enumParamStableValue);
      if (terms.size() != 1) {
        throw new WdkUserException(errPrefix + "Expected exactly one value but received " + enumParamStableValue);
      }
      return terms.get(0);
    }
    catch (JSONException e) {
      throw new WdkUserException(errPrefix + "Stable value '" + enumParamStableValue + "' is not JSON.");
    }
  }
}
