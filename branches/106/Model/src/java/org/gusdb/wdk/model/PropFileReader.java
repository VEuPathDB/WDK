package org.gusdb.wdk.model;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public abstract class PropFileReader {

  private static final Logger LOG = Logger.getLogger(PropFileReader.class.getName());

  public static final String FS = System.getProperty("file.separator");
  
  private Properties _props;
  
  protected abstract String getPropertyFile();
  protected abstract String[] getRequiredProps();
  
  protected void loadProperties() {
    _props = readProperties();
  }
  
  private Properties readProperties() {
    try {
      Properties props = new Properties();
      props.load(new FileReader(getPropertyFile()));
      for (String key : getRequiredProps()) {
        if (!props.containsKey(key)) {
          throw new IOException("Proteome config file missing property [ " + key + "].");
        }
      }
      doExtraValidation(_props);
      return props;
    }
    catch (IOException ioe) {
      throw new RuntimeException("Cannot load config file [ " + getPropertyFile() + " ]", ioe);
    }
  }

  protected void doExtraValidation(Properties props) { /* nothing by default */ }
  
  protected String getStringValue(String propertyName) {
    return _props.getProperty(propertyName);
  }
  
  protected int getIntValue(String propertyName, int defaultValue) {
    String value = _props.getProperty(propertyName);
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      LOG.warn("Configuration value [ " + value + " ] for property [ " + propertyName + " ] " +
      		"is not an integer.  Using default value " + defaultValue);
      return defaultValue;
    }
  }
  
  protected boolean getBoolValue(String propertyName) {
    String value = _props.getProperty(propertyName);
    return // possible 'true' values
        ("true").equalsIgnoreCase(value) ||
        ("yes").equalsIgnoreCase(value) ||
        ("1").equals(value);
  }
  
}
