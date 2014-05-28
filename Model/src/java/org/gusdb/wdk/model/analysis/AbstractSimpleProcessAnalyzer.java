package org.gusdb.wdk.model.analysis;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
import org.gusdb.wdk.model.user.analysis.StatusLogger;
import org.gusdb.fgputil.FormatUtil;

public abstract class AbstractSimpleProcessAnalyzer extends AbstractStepAnalyzer {
  
  private static final Logger LOG = Logger.getLogger(AbstractSimpleProcessAnalyzer.class);
  
  private static final String STDOUT_FILE_NAME = "stdout.txt";
  private static final String STDERR_FILE_NAME = "stderr.txt";
  
  protected abstract String[] getCommand(AnswerValue answerValue) throws WdkModelException;
  
  /**
   * Allows subclasses to configure the environment of the process they want to
   * run.  The passed environmen will be a copy of the current Java process's
   * environment.  The default implementation adds the GUS_HOME environment
   * variable, and adds GUS_HOME/bin to PATH.  An overriding method should call
   * super.configureEnvironment() to receive those modifications.
   * 
   * @param environment writable map of environment for subprocess to run in
   */
  protected void configureEnvironment(Map<String,String> environment) {
    String gusHome = GusHome.getGusHome();
    environment.put("GUS_HOME", gusHome);
    environment.put("PATH", Paths.get(gusHome, "bin") + ":" + environment.get("PATH"));
  }
  
  @Override
  public ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log)
      throws WdkModelException {
    ProcessBuilder builder = new ProcessBuilder(getCommand(answerValue));
    configureEnvironment(builder.environment());
    builder.directory(getStorageDirectory().toFile());
    builder.redirectOutput(getStdoutFilePath().toFile());
    builder.redirectError(getStderrFilePath().toFile());

    InputStream providedInput = null;
    Process process = null;
    try {
      process = builder.start();
      OutputStream stdin = process.getOutputStream();
      if ((providedInput = getProvidedInput()) != null) {
        try {
          IoUtil.transferStream(stdin, providedInput);
        }
        finally {
          IoUtil.closeQuietly(stdin);
        }
      }
      int exitValue = process.waitFor();
      LOG.info("Received exit code from spawned process: " + exitValue);
      if (exitValue != 0) informUserOfError(String.valueOf(exitValue));
      return (exitValue == 0 ? ExecutionStatus.COMPLETE : ExecutionStatus.ERROR);
    }
    catch (InterruptedException ie) {
      LOG.warn("Thread for step analysis was interrupted before completion.", ie);
      LOG.warn("Check files in " + getStorageDirectory() + " for level of completion before interruption.");
      return ExecutionStatus.INTERRUPTED;
    }
    catch (Exception e) {
      LOG.error("Thread for step analysis threw an exception before completion.", e);
      informUserOfError("unknown");
      return ExecutionStatus.ERROR;
    }
    finally {
      IoUtil.closeQuietly(providedInput);
      if (process != null) process.destroy();
    }
  }

  private void informUserOfError(String exitValue) {
    LOG.error("Error occurred in spawned process (exitCode=" + exitValue + "). For details, check " +
        getStdoutFileName() + " and " + getStderrFileName() + " in dir: " + getStorageDirectory());
  }

  protected String getStdoutFileName() { return STDOUT_FILE_NAME; }
  protected String getStderrFileName() { return STDERR_FILE_NAME; }
  
  protected Path getStdoutFilePath() {
    return Paths.get(getStorageDirectory().toString(), getStdoutFileName());
  }
  
  protected Path getStderrFilePath() {
    return Paths.get(getStorageDirectory().toString(), getStderrFileName());
  }
  
  protected InputStream getProvidedInput() {
    return null;
  }

  /**
   * Utility method to return multiple param values for the given key as an SQL compatible list
   * string (i.e. to be placed in an 'in' clause).  Values are assumed to be
   * Strings, and so are single-quoted.
   * 
   * @param paramKey name of parameter
   * @param formParams form params passed to this plugin
   * @param errors validation errors object to append additional errors to; note
   * this value may be null; if so, no errors will be appended
   * @return SQL compatible list string
   */
  public static String getArrayParamValueAsString(String paramKey,
      Map<String, String[]> formParams, ValidationErrors errors) {
    String[] values = formParams.get(paramKey);
    if ((values == null || values.length == 0) && errors != null) {
      errors.addParamMessage(paramKey, "Missing required parameter.");
    }
    return "'" + FormatUtil.join(values, "','") + "'";
  }

}
