package org.gusdb.wdk.controller.actionutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.WdkValidationException;
import org.gusdb.wdk.controller.actionutil.ParamDef.DataType;

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
   * @param uploads map of uploaded files
   * @return param group containing validated parameter definitions and values
   * @throws WdkValidationException if parameters do not pass validation
   */
  public ParamGroup validateParameters(
      Map<String, ParamDef> expectedParams,
      Map<String, String[]> parameters,
      Map<String, DiskFileItem> uploads) throws WdkValidationException {
    return validateParameters(expectedParams, parameters, uploads,
        new SecondaryValidator() {
            @Override public void performAdditionalValidation(ParamGroup params)
                throws WdkValidationException { return; } } );
  }
    
  /**
   * Uses expected parameter definitions to validate parameters passed in.  Any expected but
   * optional parameters not found will be added to the parameter map as empty String[]. If
   * an implementation of SecondaryValidator is passed, its performAdditionalValidation()
   * method will be called after primary validation
   * 
   * @param expectedParams definitions of expected parameters
   * @param parameters writable map of parameter values
   * @param uploads map of uploaded files
   * @param validator secondary validator
   * @return param group containing validated parameter definitions and values
   * @throws WdkValidationException if parameters do not pass validation
   */
  public ParamGroup validateParameters(
      Map<String, ParamDef> expectedParams,
      Map<String, String[]> parameters,
      Map<String, DiskFileItem> uploads,
      SecondaryValidator validator) throws WdkValidationException {

    ParamGroup group = new ParamGroup(expectedParams, parameters, uploads);
    // validate each param, and assign default value if warranted
    for (String name : expectedParams.keySet()) {
      ParamDef paramDef = expectedParams.get(name);
      if (paramDef.getDataType().equals(DataType.FILE)) {
        checkFileParam(name, paramDef, uploads.get(name), _errors);
      }
      else {
        // insulate code by replacing nulls with empty arrays
        if (!parameters.containsKey(name)) {
          parameters.put(name, new String[]{ });
        }
        String[] newValue = checkParam(name, expectedParams.get(name), parameters.get(name), _errors);
        if (newValue != null) {
          parameters.put(name, newValue);
        }
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
    try {
      validator.performAdditionalValidation(group);
    }
    catch (WdkValidationException e) {
      _errors.add(e.getMessage());
      LOG.error(e);
      throw e;
    }
    return group;
  }
    
  private static void checkFileParam(String name, ParamDef paramDef, DiskFileItem uploadedFile, List<String> errors) {
    if (paramDef.isRequired() && uploadedFile == null) {
      errors.add("Param [ " + name + " ] is required.");
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // TODO: check file size and type (but how to specify?) !!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
    
  public boolean getErrorsPresent() {
    return !_errors.isEmpty();
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
