package org.gusdb.wdk.controller.actionutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.WdkValidationException;

/**
 * Given a map of parameter definitions, validates a map of key/value pairs for
 * presence, number, and type.  Also assigns default values to parameters as
 * requested.  An optional secondary validation can be performed by passing an
 * implementation of SecondaryValidator.
 * 
 * @author rdoherty
 */
public class ParameterValidator {

	private static final Logger LOG = Logger.getLogger(ParameterValidator.class.getName());

	public interface SecondaryValidator {
	  public void performAdditionalValidation(ParamGroup params) throws WdkValidationException;
	}
	
	private List<String> _errors = new ArrayList<String>();
	
	/**
	 * Uses expected parameter definitions to validate parameters passed in.  Any expected but
	 * optional parameters not found will be added to the parameter map as empty String[].
	 * 
	 * @param expectedParams definitions of expected parameters
	 * @param parameters writable map of parameter values
	 * @return param group containing validated parameter definitions and values
	 * @throws WdkValidationException if parameters do not pass validation
	 */
	public ParamGroup validateParameters(
			Map<String, ParamDef> expectedParams,
			Map<String, String[]> parameters) throws WdkValidationException {

		ParamGroup group = new ParamGroup(expectedParams, parameters);
		// validate each param, and assign default value if warranted
		for (String name : expectedParams.keySet()) {
			// insulate code by replacing nulls with empty arrays
			if (!parameters.containsKey(name)) {
				parameters.put(name, new String[]{ });
			}
			String[] newValue = checkParam(name, expectedParams.get(name), parameters.get(name), _errors);
			if (newValue != null) {
				parameters.put(name, newValue);
			}
		}
		// check for extra params (ensures documentation compliance of child classes)
		for (String name : parameters.keySet()) {
			if (!expectedParams.containsKey(name)) {
				_errors.add("Param [ " + name + " ] is not valid for this action.");
			}
		}
		if (!_errors.isEmpty()) {
			String message = "Request parameter validation failed with ";
			LOG.error(message + "the following errors:");
			for (String error : _errors) {
				LOG.error(error);
			}
			throw new WdkValidationException(message +
					(_errors.size() == 1 ? "the following error: " + _errors.get(0) : "multiple errors."), this);
		}
		return group;
	}
	
	/**
	 * Uses expected parameter definitions to validate parameters passed in.  Any expected but
   * optional parameters not found will be added to the parameter map as empty String[]. If
   * an implementation of SecondaryValidator is passed, its performAdditionalValidation()
   * method will be called after primary validation
	 * 
   * @param expectedParams definitions of expected parameters
   * @param parameters writable map of parameter values
   * @param validator secondary validator
   * @return param group containing validated parameter definitions and values
	 * @throws WdkValidationException if parameters do not pass validation
	 */
	public ParamGroup validateParameters(
	    Map<String, ParamDef> expectedParams,
	    Map<String, String[]> parameters,
	    SecondaryValidator validator) throws WdkValidationException {
	  ParamGroup params = validateParameters(expectedParams, parameters);
    try {
      validator.performAdditionalValidation(params);
      return params;
    }
    catch (WdkValidationException e) {
      _errors.add(e.getMessage());
      throw e;
    }
	}
	
	/**
	 * List of problems found with the parameters
	 * TODO: convert to a map
	 * 
	 * @return list of errors
	 */
	public List<String> getErrorList() {
	  return _errors;
	}
	
	private static String[] checkParam(String name, ParamDef paramDef, String[] values, List<String> errors) {
		if (paramDef.isRequired() && values.length == 0) {
			errors.add("Param [ " + name + " ] is required.");
		}
		if (!paramDef.isMultiple() && values.length > 1) {
			errors.add("Param [ " + name + " ] must contain only a single value.");
		}
		
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// TODO: validate data types here!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		if (!paramDef.isRequired() && values.length == 0 && paramDef.getDefaultValue() != null) {
			return paramDef.getDefaultValue();
		}
		return null;
	}
}
