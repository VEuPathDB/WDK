package org.gusdb.wdk.model.analysis;

import java.nio.file.Path;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
import org.gusdb.wdk.model.user.analysis.IllegalAnswerValueException;
import org.gusdb.wdk.model.user.analysis.StatusLogger;
import org.json.JSONObject;

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
  void setWdkModel(WdkModel wdkModel);
  
  /**
   * Get JSON containing the information needed to render a result of this StepAnalyzer
   * @return
   * @throws WdkModelException
   */
  JSONObject getResultViewModelJson() throws WdkModelException;
  
  /**
   * Get JSON containing the information needed to render a parameter input form for this StepAnalyzer
   * @return
   * @throws WdkModelException
   */
  JSONObject getFormViewModelJson() throws WdkModelException;


  /**
   * Sets a property as passed in from the WDK Model
   * 
   * @param key property name
   * @param value property value
   */
  void setProperty(String key, String value);

  /**
   * Validate that property names and values are valid.
   * 
   * @throws WdkModelException if properties set are invalid
   */
  default void validateProperties() throws WdkModelException {}

  /**
   * Validate the XML definition of this analyzer's params against the
   * analyzer's internal expectations.
   *
   * @param params params from XML analyzer definition.
   * @throws WdkModelException if unable to validate params
   */
  default void validateParams(Map <String, Param> params)
      throws WdkModelException {}

  /**
   * Validate that this analysis plugin has been assigned to a valid Question.
   * 
   * @param question question to validate
   * @throws WdkModelException if question is inappropriate to this plugin
   */
  default void validateQuestion(Question question) throws WdkModelException {
    // any question is fine by default; to be overridden by subclass
  }

  /**
   * Sets form params retrieved from the submission of this plugin's form
   * 
   * @param formParams name->values map of submitted form parameters
   */
  void setFormParamValues(Map<String,String[]> formParams);

  /**
   * Validates that form parameters are valid.  Note this method will be called
   * before params are passed in via <code>setFormParamValues()</code>.
   *
   * @param formParams form parameter values to be validated
   * @return an object encapsulating the errors, or null if no errors occurred
   * @throws WdkModelException if unable to validate values
   */
  default ValidationErrors validateFormParamValues(Map<String,String[]> formParams)
      throws WdkModelException {
    return null;
  }

  /**
   * Sets answer value retrieved from the step being analyzed
   * 
   * @param answerValue answer value from the step being analyzed
   */
  void setAnswerValue(AnswerValue answerValue);
  
  /**
   * Checks that this is a valid answer for this analyzer, and throws an
   * exception if is not.
   * 
   * @param answerValue answer that might be analyzed by this analyzer
   * @throws IllegalAnswerValueException if answer cannot be analyzed by this analyzer
   * @throws WdkModelException if error occurs while determining validity of answer
   * @throws WdkUserException 
   */
  default void validateAnswerValue(AnswerValue answerValue)
      throws IllegalAnswerValueException, WdkModelException, WdkUserException {}

  /**
   * Runs an analysis of the passed AnswerValue.  In-process logging can be
   * recorded using the passed StatusLogger.
   * 
   * @param answerValue answer value of step this plugin is analyzing
   * @param log logger mechanism; this will be for diagnostic purposes or may be exposed to the user
   * @return completion status of this run.  Typically this value is COMPLETE, INTERRUPTED, or ERROR
   * @throws WdkModelException if an unexpected error occurs.  This is a slightly messier equivalent of returning ERROR
   * @throws WdkUserException 
   */
  ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log) throws WdkModelException, WdkUserException;

  /**
   * Sets the storage directory for this analyzer.  Usage of this directory is
   * optional and can be used instead of, or in combination with the CLOB
   * storage in the database.
   * 
   * @param storageDirectory
   */
  void setStorageDirectory(Path storageDirectory);
  
  /**
   * Store any persistent character data as part of this execution.  It will be
   * retrievable later when a result view object is requested.  The value will
   * be stored in a CLOB field of the application database.
   * 
   * @return character data to be persistently stored
   */
  String getPersistentCharData();
  
  /**
   * This method will be called with your persistent character data from a
   * previous execution.  It is up to the implementation to locally store or
   * translate this object and use it to produce a result view model.  If no
   * data string was stored, this method will still be called but passed a null
   * value.
   * 
   * @param data persistently stored character data
   */
  void setPersistentCharData(String data);

  /**
   * Store any persistent binary data as part of this execution.  It will be
   * retrievable later when a result view object is requested.  The value will
   * be stored in a BLOB field of the application database.
   * 
   * @return binary data to be persistently stored
   */
  byte[] getPersistentBinaryData();
  
  /**
   * This method will be called with your persistent binary data from a previous
   * execution.  It is up to the implementation to locally store or translate
   * this object and use it to produce a result view model.  If no data was
   * stored, this method will still be called but passed a null value.
   * 
   * @param data persistently stored binary data
   */
  void setPersistentBinaryData(byte[] data);
}
