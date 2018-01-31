package org.gusdb.wdk.model.user.dataset;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A handler for a particular type of dataset.  These are plugged in to the wdk.
 * If a particular type is not plugged in, then datasets of that type are not
 * compatible with this wdk application, for that reason.
 * @author steve
 *
 */
public abstract class UserDatasetTypeHandler {

  private static final Logger LOG = Logger.getLogger(UserDatasetTypeHandler.class);

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

  public abstract String[] getInstallInAppDbCommand(UserDataset userDataset, Map<String, Path> fileNameToTempFileMap, String project);

  public abstract Set<String> getInstallInAppDbFileNames(UserDataset userDataset);

  public abstract String[] getUninstallInAppDbCommand(Long userDatasetId, String projectName);

  public abstract String[] getRelevantQuestionNames();
  
  public abstract String getAncillaryData(WdkModel wdkModel, UserDataset userDataset) throws WdkModelException;

  public void installInAppDb(UserDatasetSession dsSession, UserDataset userDataset, Path tmpDir, String projectId) throws WdkModelException {

    Map<String, Path> nameToTempFileMap = new HashMap<String, Path>();

    Path workingDir = createWorkingDir(tmpDir, userDataset.getUserDatasetId());

    for (String userDatasetFileName : getInstallInAppDbFileNames(userDataset)) {
      UserDatasetFile udf = userDataset.getFile(dsSession, userDatasetFileName);
      if (udf == null) throw new WdkModelException("File name requested by type handler, '" + userDatasetFileName + "' is not found in user dataset " + userDataset.getUserDatasetId() + " of type " + userDataset.getType());
      Path tmpFile = udf.getLocalCopy(dsSession, workingDir);
      nameToTempFileMap.put(userDatasetFileName, tmpFile);
    }
    String[] command = getInstallInAppDbCommand(userDataset, nameToTempFileMap, projectId);
    // For the case where no user dataset file data is installed into the DB
    if(command.length > 0) {
      runCommand(command, workingDir);
    }  
    deleteWorkingDir(workingDir);
   }

  public void uninstallInAppDb(Long userDatasetId, Path tmpDir, String projectId) throws WdkModelException {
    Path workingDir = createWorkingDir(tmpDir, userDatasetId);
    String[] command = getUninstallInAppDbCommand(userDatasetId, projectId);
    // For the case where no user dataset data was stored in the DB
    if(command.length > 0) {
      runCommand(command, workingDir); 
    }
    deleteWorkingDir(workingDir);
  }

  private void runCommand(String[] command, Path workingDir) throws WdkModelException {

    StringBuilder builder = new StringBuilder();
    for (String s : command) { builder.append(s + " "); }
    LOG.info("Running command: " + builder);

    Process p = null;
    Boolean success = false;
    try {
      p = new ProcessBuilder(command)
          .directory(workingDir.toFile())
          .redirectOutput(workingDir.resolve("stdout").toFile())
          .redirectError(workingDir.resolve("stderr").toFile())
          .start();
      p.waitFor();
      success = p.exitValue() == 0;
      p.destroy();
    }
    catch (IOException | InterruptedException e) {
      throw new WdkModelException(e);
    } finally {
      if (p != null) p.destroy();
    }
    if (!success) {
      throw new WdkModelException("Failed running command: " + builder + ". For details, see " + workingDir);
   }
  }

  private Path createWorkingDir(Path tmpDir, Long userDatasetId) throws WdkModelException {
    Path workingDir;
    try {
      workingDir = Files.createTempDirectory(tmpDir, "ud_" + userDatasetId + "_");
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }
    return workingDir;
  }
  
  private void deleteWorkingDir(Path workingDir) throws WdkModelException {
    try {
      Files.walkFileTree(workingDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
  
}
