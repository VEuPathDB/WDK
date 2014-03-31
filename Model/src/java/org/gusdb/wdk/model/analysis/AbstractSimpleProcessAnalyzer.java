package org.gusdb.wdk.model.analysis;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
import org.gusdb.wdk.model.user.analysis.StatusLogger;

public abstract class AbstractSimpleProcessAnalyzer extends AbstractStepAnalyzer {
  
  private static final Logger LOG = Logger.getLogger(AbstractSimpleProcessAnalyzer.class);
  
  private static final String STDOUT_FILE_NAME = "stdout.txt";
  private static final String STDERR_FILE_NAME = "stderr.txt";
  
  protected abstract String[] getCommand(AnswerValue answerValue);
  
  @Override
  public ExecutionStatus runAnalysis(AnswerValue answerValue, StatusLogger log)
      throws WdkModelException {
    ProcessBuilder builder = new ProcessBuilder(getCommand(answerValue));
    builder.directory(getStorageDirectory().toFile());
    builder.redirectOutput(getStdoutFilePath().toFile());
    builder.redirectError(getStdoutFilePath().toFile());

    InputStream providedInput = null;
    Process process = null;
    try {
      process = builder.start();
      OutputStream stdin = process.getOutputStream();
      if ((providedInput = getProvidedInput()) != null) {
        IoUtil.transferStream(stdin, providedInput);
      }
      int exitValue = process.waitFor();
      return (exitValue == 0 ? ExecutionStatus.COMPLETE : ExecutionStatus.ERROR);
    }
    catch (InterruptedException ie) {
      LOG.warn("Thread for step analysis was interrupted before completion.", ie);
      return ExecutionStatus.INTERRUPTED;
    }
    catch (Exception e) {
      LOG.error("Thread for step analysis threw an exception before completion.", e);
      return ExecutionStatus.ERROR;
    }
    finally {
      IoUtil.closeQuietly(providedInput);
      if (process != null) process.destroy();
    }
  }

  protected Path getStdoutFilePath() {
    return Paths.get(getStorageDirectory().toString(), STDOUT_FILE_NAME);
  }
  
  protected Path getStderrFilePath() {
    return Paths.get(getStorageDirectory().toString(), STDERR_FILE_NAME);
  }
  
  protected InputStream getProvidedInput() {
    return null;
  }

}
