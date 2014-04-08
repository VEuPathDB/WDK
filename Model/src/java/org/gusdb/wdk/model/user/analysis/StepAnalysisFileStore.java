package org.gusdb.wdk.model.user.analysis;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
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
    createStoreIfAbsent(10); // pass in number of attempts remaining
    testFileStore();
  }

  
  private void createStoreIfAbsent(int remainingAttempts) throws WdkModelException {
    // check if storage directory already exists
    if (Files.exists(_fileStoreDirectory)) {
      return;
    }
    // check whether number of attempts expired
    if (remainingAttempts == 0) {
      throw new WdkModelException("Unable to create missing file store at" +
          " location: " + _fileStoreDirectory);
    }
    // check if parent dir can be referenced so lock file can be created
    if (_fileStoreDirectory.getParent() == null) {
      throw new WdkModelException("Cannot provide raw file or root directory " +
      		"as file store.  Parent directory required for: " + _fileStoreDirectory);
    }
    
    // create lock file in attempt to 
    String lockFileName = _fileStoreDirectory.getFileName().toString() + ".lock";
    Path lockFile = Paths.get(_fileStoreDirectory.getParent().toString(), lockFileName);
    boolean lockFileNeedsDeletion = false;
    try {
      Files.createFile(lockFile);
      lockFileNeedsDeletion = true;
      createOpenPermsDirectory(_fileStoreDirectory);
    }
    catch (FileAlreadyExistsException faee) {
      try {
        // unable to create lock file; someone else is creating a directory; try again
        Thread.sleep(20); // delay between attempts
        createStoreIfAbsent(remainingAttempts - 1);
      }
      catch (InterruptedException e) {
        throw new WdkModelException("Thread interrupted while attempting " +
        		"to create data store dir at: " + _fileStoreDirectory);
      }
    }
    catch (IOException ioe) {
      throw new WdkModelException("Unable to create data store dir at: " +
          _fileStoreDirectory);
    }
    finally {
      if (lockFileNeedsDeletion) {
        try {
          Files.delete(lockFile);
        }
        catch (IOException ioe) {
          throw new WdkModelException("Created lock file at " + lockFile +
              " but now unable to delete.", ioe);
        }
      }
    }
  }

  private static void createOpenPermsDirectory(Path directory) throws IOException {
    Files.createDirectory(directory);
    // apply file permissions after the fact in case umask restrictions prevent it during creation
    Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
    Files.setPosixFilePermissions(directory, perms);
  }

  /**
   * Deletes all sub-directories of this file store that it can.  If permission
   * is denied on a particular subdir, it is skipped and the process continues.
   * 
   * @throws WdkModelException if unable to delete storage cache
   */
  public void deleteAllExecutions() throws WdkModelException {
    String[] cacheDirNames = _fileStoreDirectory.toFile().list();
    if (cacheDirNames == null) {
      throw new WdkModelException("Unable to delete step analysis results " +
          "cache on filesystem at path: " + _fileStoreDirectory +
          ", directory does not exist.");
    }
    for (String cacheDirName : cacheDirNames) {
      Path cacheDirPath = Paths.get(_fileStoreDirectory.toString(), cacheDirName);
      try {
        IoUtil.deleteDirectoryTree(cacheDirPath);
      }
      catch (Exception e) {
        LOG.warn("Unable to delete execution storage directory: " + cacheDirPath.toString());
      }
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
      createOpenPermsDirectory(storageDir);
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

  private void testFileStore() throws WdkModelException {
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
