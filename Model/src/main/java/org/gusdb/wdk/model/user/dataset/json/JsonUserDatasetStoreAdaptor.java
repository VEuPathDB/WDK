package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.util.List;
import org.gusdb.wdk.model.WdkModelException;

public interface JsonUserDatasetStoreAdaptor {
  
  /**
   * Move a file, atomically
   * @param from
   * @param to
   * @throws WdkModelException if target already exists.
   */
  public void moveFileAtomic(Path from, Path to) throws WdkModelException;

  /**
   * Get the absolute paths of the entries in the provided directory.  Must close all resources before returning.
   * Not recursive.
   * @param dir
   * @return
   * @throws WdkModelException if dir does not exist, or can't be read.
   */
  public List<Path> getPathsInDir(Path dir) throws WdkModelException;

  /**
   * Read the contents of the provided file into a string. Must close all resources
   * before returning.
   * @param file
   * @return
   * @throws WdkModelException If file doesn't exist or can't be read.
   */
  public String readFileContents(Path file) throws WdkModelException;
  
  /**
   * Return true if the provided path exists and is a directory
   * @param dir
   * @return
   * @throws WdkModelException
   */
  public boolean isDirectory(Path dir) throws WdkModelException;
  

  /**
   * rite the provided contents to the provided path.  
   * @param file
   * @param contents
   * @throws WdkModelException
   */
  public void writeFile(Path file, String contents, boolean errorIfTargetExists) throws WdkModelException;
  
  /**
   * Create a new directory.
   * @param dir - the directory path
   * @throws WdkModelException if dir already exists
   */
  public void createDirectory(Path dir) throws WdkModelException;
  
  /**
   * Write a file with no contents (like "touch" in unix).  No error if file already exists.
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
  public Long getModificationTime(Path fileOrDir) throws WdkModelException;
  
  /**
   * Read the first line of a file.  Must close all resources before returning.
   * @param file
   * @return
   * @throws WdkModelException if file doesn't exist.
   */
  public String readSingleLineFile(Path file) throws WdkModelException;
  
  /**
   * Return true if the file (or directory) exists
   * @param file
   * @return
   */
  public boolean fileExists(Path file) throws WdkModelException;

}
