package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;

public interface JsonUserDatasetStoreAdaptor {
  
  public void moveFileAtomic(Path from, Path to) throws WdkModelException;

  public List<Path> getPathsInDir(Path dir) throws WdkModelException;

  public void putFilesIntoMap(Path dir, Map<String, UserDatasetFile> filesMap) throws WdkModelException;

  /**
   * Read the contents of the provided file into a string
   * @param file
   * @return
   * @throws WdkModelException
   */
  public String readFileContents(Path file) throws WdkModelException;

  /**
   * Return true if the provided path is a directory that exists
   * @param dir
   * @return
   * @throws WdkModelException
   */
  public boolean isDirectory(Path dir) throws WdkModelException;
  /**
   * Atomically write the provided contents to the provided path
   * @param file
   * @param contents
   * @throws WdkModelException
   */
  public void writeFileAtomic(Path file, String contents) throws WdkModelException;
  
  /**
   * Creates a new directory.  If one already exists, an exception is thrown.
   * @param dir - the directory path
   * @throws WdkModelException
   */
  public void createDirectory(Path dir) throws WdkModelException;
  
  public void writeFile(Path file, String contents) throws WdkModelException;
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
