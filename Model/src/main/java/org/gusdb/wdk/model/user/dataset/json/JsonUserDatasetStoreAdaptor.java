package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import org.gusdb.wdk.model.WdkModelException;

public interface JsonUserDatasetStoreAdaptor {
  
  /**
   * Move a file
   * @param from
   * @param to
   * @throws WdkModelException if target already exists.
   */
  public void moveFileAtomic(Path from, Path to) throws WdkModelException;

  /**
   * Get the paths in the provided directory
   * @param dir
   * @return
   * @throws WdkModelException if dir does not exist, or can't be read.
   */
  public List<Path> getPathsInDir(Path dir) throws WdkModelException;

  /**
   * Read the contents of the provided file into a string
   * @param file
   * @return
   * @throws WdkModelException If file doesn't exist or can't be read.
   */
  public String readFileContents(Path file) throws WdkModelException;

  /**
   * Return true if the provided directory path exists. 
   * @param dir
   * @return
   * @throws WdkModelException If exists but is not a directory.
   */
  public boolean directoryExists(Path dir) throws WdkModelException;

  /**
   * Atomically write the provided contents to the provided path.  
   * @param file
   * @param contents
   * @throws WdkModelException
   */
  public void writeFileAtomic(Path file, String contents, boolean errorIfTargetExists) throws WdkModelException;
  
  /**
   * Create a new directory.
   * @param dir - the directory path
   * @throws WdkModelException if dir already exists
   */
  public void createDirectory(Path dir) throws WdkModelException;
  
  /**
   * Write an empty file.  No error if file already exists.
   * @param file
   * @param contents
   * @param errorIfTargetExists
   * @throws WdkModelException If can't write file.
   */
  public void writeEmptyFile(Path file) throws WdkModelException;
  
 /**
   * Delete the provided file or dir
   * @param fileOrDir
   * @throws WdkModelException
   */
  public void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException;
  
  /**
   * get the last mod time of a file or dir
   * @param fileOrDir
   * @return
   * @throws WdkModelException
   */
  public Date getModificationTime(Path fileOrDir) throws WdkModelException;
  
  public String readSingleLineFile(Path file) throws WdkModelException;
  
  /**
   * return true if the file exists
   * @param file
   * @return
   */
  public boolean fileExists(Path file) throws WdkModelException;

}
