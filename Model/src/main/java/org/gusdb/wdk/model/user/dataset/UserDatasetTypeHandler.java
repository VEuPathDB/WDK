package org.gusdb.wdk.model.user.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModelException;

/**
 * A handler for a particular type of dataset.  These are plugged in to the wdk.
 * If a particular type is not plugged in, then datasets of that type are not
 * compatible with this wdk application, for that reason.
 * @author steve
 *
 */
public abstract class UserDatasetTypeHandler {

  private static final Logger logger = Logger.getLogger(UserDatasetTypeHandler.class);
      
  /**
   * Check if a dataset is compatible with this application, based on its data dependencies.
   * @param userDataset
   * @return
   */
  public abstract UserDatasetCompatibility getCompatibility(UserDataset userDataset, DataSource appDbDataSource);
  
  /**
   * The user dataset type this handler handles.
   * @return
   */
  public abstract UserDatasetType getUserDatasetType();
  
  public abstract String[] getInstallInAppDbCommand(UserDataset userDataset, Map<String, Path> fileNameToTempFileMap);
  
  public abstract Set<String> getInstallInAppDbFileNames(UserDataset userDataset);
  
  public abstract String[] getUninstallInAppDbCommand(Integer userDatasetId);

  public void installInAppDb(UserDataset userDataset, Path tmpDir) throws WdkModelException {

    Map<String, Path> nameToTempFileMap = new HashMap<String, Path>();

    for (String userDatasetFileName : getInstallInAppDbFileNames(userDataset)) {
      UserDatasetFile udf = userDataset.getFile(userDatasetFileName);
      if (udf == null) throw new WdkModelException("File name requested by type handler, '" + userDatasetFileName + "' is not found in user dataset " + userDataset.getUserDatasetId() + " of type " + userDataset.getType());
      Path tmpFile = udf.getLocalCopy(tmpDir);
      nameToTempFileMap.put(userDatasetFileName, tmpFile);
    }
    runCommand(getInstallInAppDbCommand(userDataset, nameToTempFileMap));
    try {
      for (Path tmpFile : nameToTempFileMap.values()) Files.delete(tmpFile);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  public void uninstallInAppDb(Integer userDatasetId) throws WdkModelException {
    runCommand(getUninstallInAppDbCommand(userDatasetId));    
  }

  private void runCommand(String[] command) throws WdkModelException {
    StringBuilder builder = new StringBuilder();
    for (String s : command) { builder.append(s + " "); }
    logger.info("Running command: " + builder);
    try {
      Process p = Runtime.getRuntime().exec(command);
      p.waitFor();
    }
    catch (IOException | InterruptedException e) {
      throw new WdkModelException(e);
    }
  }

}
