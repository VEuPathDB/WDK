package org.gusdb.wdk.controller.actionutil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;

/**
 * Grouping of a set of validated parameters and their definitions.
 * 
 * @author rdoherty
 */
public class ParamGroup {
	
  private static final Logger LOG = Logger.getLogger(ParamGroup.class.getName());
  
  private static final Set<String> TRUE_VALUES =
      new HashSet<>(Arrays.asList(new String[]{ "true", "1", "yes" }));

  private Map<String, ParamDef> _defs;
  private Map<String, String[]> _values;
	private Map<String, DiskFileItem> _uploads;
	
	public ParamGroup(Map<String,ParamDef> definitions, Map<String, String[]> values, Map<String, DiskFileItem> uploads) {
		_defs = definitions;
		_values = values;
		_uploads = uploads;
	}
	
	/**
	 * @return a set of the names of all parameters in this group
	 */
	public Set<String> getKeys() {
		return _defs.keySet();
	}
	
	public ParamDef getParamDef(String key) {
		checkValidKey(key);
		return _defs.get(key);
	}
	
	public String getValue(String key) {
		checkValidKey(key);
		if (_defs.get(key).isMultiple()) {
			throw new IllegalArgumentException("The key [ " + key + " ] refers to a multi-value param.  Please call getValues().");
		}
		return (_values.get(key).length == 0 ? null : _values.get(key)[0]);
	}
	
	/**
	 * Looks for the key in the parameter set.  If there is no value present,
	 * returns an empty string.
	 * 
	 * @param key parameter name
	 * @return the value of the parameter or an empty string if no value exists
	 * @throws IllegalArgumentException if multiple values exist for this parameter
	 */
	public String getValueOrEmpty(String key) {
	  String[] values = _values.get(key);
	  if (values != null && values.length > 1) {
	    throw new IllegalArgumentException("The key [ " + key + " ] contains multiple values which cannot be retrieved with this method.");
	  }
	  return (values == null || values.length == 0 ? "" : values[0]);
	}
	
	public String[] getValues(String key) {
		if (_defs.containsKey(key) && !_defs.get(key).isMultiple()) {
			LOG.warn("The key [ " + key + " ] refers to a single-value param but is being accessed via getValues().");
		}
		return (_values.get(key) == null ? new String[0] : _values.get(key));
	}
	
	private void checkValidKey(String key) {
		if (!_defs.containsKey(key)) {
			throw new IllegalArgumentException("This param group does not contain param with name [ " + key + " ].");
		}
	}
	
	public boolean getSingleCheckboxValue(String key) {
		checkValidKey(key);
		String value = getValue(key);
		return (value != null && value.equals("on"));
	}
	
	public Integer getIntValue(String key) {
		checkValidKey(key);
		if (!_defs.get(key).getDataType().equals(ParamDef.DataType.INTEGER)) {
			throw new IllegalArgumentException("The key [ " + key + " ] does not refer to an integer param.");
		}
		String str = getValue(key);
		return (str == null ? null : Integer.parseInt(str));
	}
  
	public boolean getBooleanValue(String key) {
    checkValidKey(key);
    if (!_defs.get(key).getDataType().equals(ParamDef.DataType.BOOLEAN)) {
      throw new IllegalArgumentException("The key [ " + key + " ] does not refer to a boolean param.");
    }
    return TRUE_VALUES.contains(getValue(key));
  }
	
	public Map<String, String[]> getParamMap() {
		return _values;
	}

  public DiskFileItem getUpload(String key) {
    checkValidKey(key);
    return _uploads.get(key);
  }
}
