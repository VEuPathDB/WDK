package org.gusdb.wdk.model.analysis;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
import org.gusdb.wdk.model.user.analysis.StatusLogger;

/**
 * Interface to be implemented by Step Analysis Plugins.
 * 
 * @author rdoherty
 */
public interface StepAnalyzer {

  /**
   * Allows the framework to pass a reference to the WdkModel
   * 
   * @param wdkModel WDK Model object
   */
  public void setWdkModel(WdkModel wdkModel);
  
  /**
   * A MVC model object to be made available to the JSP rendering the form for
   * this plugin.  It we be available to your JSP page as requestScope.viewModel.
   * 
   * @return model object for form rendering
   */
  public Object getFormViewModel();
  
  /**
   * A MVC model object to be made available to the JSP rendering the results for
   * this plugin.  It we be available to your JSP page as requestScope.viewModel.
   * 
   * @return model object for results rendering
   */
  public Object getResultViewModel();
  
  /**
   * Sets a property as passed in from the WDK Model
   * 
   * @param key property name
   * @param value property value
   */
  public void setProperty(String key, String value);
  
  /**
   * Validate that property names and values are valid.
   * 
   * @throws WdkModelException if properties set are invalid
   */
  public void validateProperties() throws WdkModelException;
  
  /**
   * Sets form params retrieved from the submission of this plugin's form
   * 
   * @param formParams name->values map of submitted form parameters
   */
  public void setFormParams(Map<String, String[]> formParams);
  
  /**
   * Validates that form parameters are valid.  Note this method will be called
   * before params are passed in via <code>setFormParams()</code>.
   * 
   * @param formParams form parameter values to be validated
   * @return a list of errors associated with these values.  If validation
   * passed, an empty list or null is an acceptable return value.
   */
  public List<String> validateFormParams(Map<String, String[]> formParams);
  
  /**
   * Runs an analysis of the passed AnswerValue.  In-process logging can be
   * recorded using the passed StatusLogger.
   * 
   * @param answerValue answer value of step this plugin is analyzing
   * @param log logger mechanism; this will be for diagnostic purposes or may be exposed to the user
   * @return completion status of this run.  Typically this value is COMPLETE, INTERRUPTED, or ERROR
   * @throws WdkModelException if an unexpected error occurs.  This is a slightly messier equivalent of returning ERROR
   */
  public ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log) throws WdkModelException;

  /**
   * Serialize your result into a String.  Your internal data structure and
   * serialization mechanism/encoding is up to you.  The value will be stored in
   * a CLOB field of the application database.
   * 
   * @return string representation of your result
   */
  public String serializeResult();
  
  /**
   * Deserialize your result.  Your internal data structure should be recreated
   * using this string so that you can expose the result in an orderly fashion
   * via the Object returned by <code>getResultViewModel()</code>.
   * 
   * @param serializedResult stored string representation of your result
   */
  public void deserializeResult(String serializedResult);

}
