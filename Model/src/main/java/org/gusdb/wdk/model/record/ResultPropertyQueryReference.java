package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModelException;

public class ResultPropertyQueryReference extends Reference {

	private String propertyName;
	
	public ResultPropertyQueryReference() {
	}

	public ResultPropertyQueryReference(String twoPartName)
			throws WdkModelException {
		super(twoPartName);
	}
	
	 public void setPropertyName(String propertyName) {
		  this.propertyName = propertyName;
	  }		
	 
	  public String getPropertyName() { return propertyName; }


}
