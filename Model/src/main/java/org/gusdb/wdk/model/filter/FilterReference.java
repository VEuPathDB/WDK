package org.gusdb.wdk.model.filter;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterReference extends WdkModelBase {

	private static final Logger LOG = Logger.getLogger(FilterReference.class);
  private String _name;
  private String _defaultValueString = null;
  private JSONObject _defaultValueObject = null;


  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }
  public String getDefaultValue() {
	  return _defaultValueString;
  }
  
  public void setDefaultValue(String defaultValueString) {
	  this._defaultValueString = defaultValueString;
  }

  protected JSONObject getDefaultValueObject() {
	  return _defaultValueObject;
  }
  
  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
	    super.resolveReferences(wdkModel);

	    try {
	      if (_defaultValueString != null) _defaultValueObject = new JSONObject(_defaultValueString);
				LOG.debug("FilterReference: " + getDefaultValueObject().toString());

	    }
	    catch (JSONException ex) {
	      throw new WdkModelException(ex);
	    }
	  }

}
