package org.gusdb.wdk.model.user.dataset;

import static org.gusdb.fgputil.functional.Functions.fSwallow;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;

/**
 * A handler for a particular type of dataset.  These are plugged in to the wdk.
 * If a particular type is not plugged in, then datasets of that type are not
 * compatible with this wdk application, for that reason.
 *
 * @author steve
 */
public abstract class UserDatasetTypeHandler {

  private static final Logger LOG = Logger.getLogger(UserDatasetTypeHandler.class);

  /**
   * Check if a dataset is compatible with this application, based on its data
   * dependencies.
   */
  public abstract UserDatasetCompatibility getCompatibility(UserDataset userDataset, DataSource appDbDataSource) throws WdkModelException;

  /**
   * The user dataset type this handler handles.
   */
  public abstract UserDatasetType getUserDatasetType();

  public abstract String[] getInstallInAppDbCommand(UserDataset userDataset, Map<String, Path> fileNameToTempFileMap, String project);

  public abstract Set<String> getInstallInAppDbFileNames(UserDataset userDataset);

  public abstract String[] getUninstallInAppDbCommand(Long userDatasetId, String projectName);

  public abstract String[] getRelevantQuestionNames(UserDataset userDataset);

  public abstract String getDisplay();

  /**
   * Returns detailed type-specific data for a single user dataset for use in a
   * detailed display page
   */
  public JsonType getDetailedTypeSpecificData(WdkModel wdkModel, UserDataset userDataset, User user) throws WdkModelException {
    return new JsonType(null);
  }

  /**
   * Returns small-scale type-specific data for a collection of user datasets
   * for use in a non-detailed user dataset listing page
   */
  public List<JsonType> getTypeSpecificData(
    final WdkModel wdkModel,
    final List<UserDataset> userDatasets,
    final User user
  ) throws WdkModelException {
    return mapToList(userDatasets, fSwallow(ud -> getDetailedTypeSpecificData(wdkModel, ud, user)));
  }

  public Map<String, Path> copyFilesToTemp(
    final UserDatasetSession session,
    final UserDataset dataset,
    final Path cwd
  ) throws WdkModelException {
    final var out = new HashMap<String, Path>();

    for (var userDatasetFileName : getInstallInAppDbFileNames(dataset)) {
      var udf = dataset.getFile(session, userDatasetFileName);

      if (udf == null)
        throw new WdkModelException(
          "File name requested by type handler, '" + userDatasetFileName
            + "' is not found in user dataset " + dataset.getUserDatasetId()
            + " of type " + dataset.getType()
        );

      var tmpFile = udf.getLocalCopy(session, cwd);
      out.put(userDatasetFileName, tmpFile);
    }

    return out;
  }

  public void installInAppDb(
    final UserDataset userDataset,
    final Path workingDir,
    final String projectId,
    final Map<String, Path> files
  ) throws WdkModelException {
    LOG.trace("UserDatasetTypeHandler#installInAppDb");
    String[] command = getInstallInAppDbCommand(userDataset, files, projectId);

    // For the case where no user dataset file data is installed into the DB
    if(command.length > 0) {
      runCommand(command, workingDir);
    }
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

  public Path createWorkingDir(Path tmpDir, Long userDatasetId) throws WdkModelException {
    try {
      return Files.createTempDirectory(tmpDir, "ud_" + userDatasetId + "_");
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  private void runCommand(String[] command, Path workingDir) throws WdkModelException {

    StringBuilder builder = new StringBuilder();
    for (String s : command) { builder.append(s).append(" "); }
    LOG.info("Running command: " + builder);

    Process p = null;
    boolean success;
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
      LOG.debug("Command execution completed.");
    }
    if (!success) {
      throw new WdkModelException("Failed running command: " + builder + ". For details, see " + workingDir + "/stderr");
   }
  }

  public void deleteWorkingDir(Path workingDir) throws WdkModelException {
    try {
      Files.walkFileTree(workingDir, new SimpleFileVisitor<>() {
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
