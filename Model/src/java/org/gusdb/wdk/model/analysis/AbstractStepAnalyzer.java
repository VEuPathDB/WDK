package org.gusdb.wdk.model.analysis;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.analysis.IllegalAnswerValueException;

public abstract class AbstractStepAnalyzer implements StepAnalyzer {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(AbstractStepAnalyzer.class);
  
  private WdkModel _wdkModel;
  private Map<String, String> _properties = new HashMap<>();
  private Map<String, String[]> _formParams = new HashMap<>();
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
  @Override
  public void validateProperties() throws WdkModelException {
    // no required properties
  }

  protected Map<String,String[]> getFormParams() {
    return _formParams;
  }
  @Override
  public void setFormParams(Map<String, String[]> formParams) {
    _formParams = formParams;
  }
  @Override
  public Map<String, String> validateFormParams(Map<String, String[]> formParams) {
    // no validation
    return null;
  }
  
  @Override
  public Object getFormViewModel() throws WdkModelException {
    return null;
  }
  
  @Override
  public void preApproveAnswer(AnswerValue answerValue)
      throws IllegalAnswerValueException, WdkModelException {
    // do nothing
  }
}
