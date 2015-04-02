package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;

public class ResultPropertiesPluginRef {
	String javaClassName;
	List<String> propertyNames = new ArrayList<String>();
	ResultProperties implementationInstance = null;
	
	public void setImplementation(String javaClassName) {
		this.javaClassName = javaClassName;
	}
	
	public String getImplementation() {return javaClassName;}
	
	public void addPropertyName(String propName) {
		propertyNames.add(propName);
	}

	public List<String> getPropertyNames() {
		return Collections.unmodifiableList(propertyNames);
	}
	
	public ResultProperties getImplementationInstance() throws WdkModelException {
		String errmsg = "Can't create java class for ResultProperties from class name '" + javaClassName + "'";
		if (implementationInstance == null) { // resolve the handler class
			try {
				Class<?> classs = Class.forName(javaClassName);

				if (ResultProperties.class.isAssignableFrom(classs)) {
					implementationInstance = (ResultProperties) classs.newInstance();
				} else {
					throw new WdkModelException(
							"Invalid class: class should be child of MyInterface");
				}
			} catch (ClassNotFoundException ex) {
				throw new WdkModelException(errmsg, ex);
			} catch (InstantiationException ex) {
				throw new WdkModelException(errmsg, ex);
			} catch (IllegalAccessException ex) {
				throw new WdkModelException(errmsg, ex);
			}
		}
		return implementationInstance;
	}

}
