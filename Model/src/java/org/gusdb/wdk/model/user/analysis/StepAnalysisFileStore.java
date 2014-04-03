package org.gusdb.wdk.model.user.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModelException;

public class StepAnalysisFileStore {

  private static final Logger LOG = Logger.getLogger(StepAnalysisFileStore.class);
  
  private final Path _fileStoreDirectory;
  
  /**
   * Creates new file store around the passed path.  Checks to make sure the
   * given path exists and is read/writable.
   * 
   * @param fileStoreDirectory path to step analysis result cache directory
   * @throws WdkModelException if passed path does not exist or have the needed permissions
   */
  public StepAnalysisFileStore(Path fileStoreDirectory) throws WdkModelException {
    _fileStoreDirectory = fileStoreDirectory;
  }

  
  /**
   * Deletes all sub-directories of this file store.
   * 
   * @throws WdkModelException if unable to delete storage cache
   */
  public void deleteAllExecutions() throws WdkModelException {
    try {
      String[] cacheDirNames = _fileStoreDirectory.toFile().list();
      for (String cacheDirName : cacheDirNames) {
        Path cacheDirPath = Paths.get(_fileStoreDirectory.toString(), cacheDirName);
        IoUtil.deleteDirectoryTree(cacheDirPath);
      }
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to delete step analysis results " +
      		"cache on filesystem at path: " + _fileStoreDirectory, e);
    }
  }

  /**
   * Deletes existing contents of storage directory for the execution with the
   * passed hash (if it exists).  If the directory does not exist, it is created.
   * 
   * @param hash hash value identifying a step analysis execution
   * @throws WdkModelException if unable to recreate store
   */
  public void recreateStore(String hash) throws WdkModelException {
    try {
      Path storageDir = getStorageDirPath(hash);
      if (Files.exists(storageDir)) {
        IoUtil.deleteDirectoryTree(storageDir);
      }
      // TODO: test image href in results; extra permissions don't work,
      //       probably because of tomcat user's umask restrictions
      //Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
      //FileAttribute<Set<PosixFilePermission>> permsAttr = PosixFilePermissions.asFileAttribute(perms);
      Files.createDirectory(storageDir);//, permsAttr);
    }
    catch (IOException ioe) {
      throw new WdkModelException("Unable to clear or recreate results store for hash " + hash, ioe);
    }
  }

  /**
   * Returns a storage directory path that can be used to store data associated
   * with a step analysis execution
   * 
   * @param hash subdir of the main storage directory
   * @return path to storage directory for execution with the given hash
   */
  public Path getStorageDirPath(String hash) {
    return Paths.get(_fileStoreDirectory.toString(), hash);
  }

  /**
   * Returns whether the storage directory for the given hash exists.
   * 
   * @param hash hash value identifying a step analysis execution
   * @return true if dir exists, else false
   */
  public boolean storageDirExists(String hash) {
    return getStorageDirPath(hash).toFile().exists();
  }


  public void testFileStore() throws WdkModelException {
    try {
      LOG.info("Testing step analysis file store...");
      String sampleHash = UUID.randomUUID().toString();
      Path sampleDir = getStorageDirPath(sampleHash);
      recreateStore(sampleHash);
      Path sampleFile = Paths.get(sampleDir.toString(), "sampleFile.txt");
      Files.createFile(sampleFile);
      IoUtil.deleteDirectoryTree(sampleDir);
    }
    catch (IOException e) {
      String message = "File store test failed.  Unable to write " +
          "dir or file to configured file store: " + _fileStoreDirectory;
      LOG.error(message, e);
      throw new WdkModelException(message, e);
    }
  }
}
