package org.gusdb.wdk.model.user.dataset;

import java.nio.file.Path;
import java.util.List;
import org.gusdb.wdk.model.WdkModelException;

public interface UserDatasetStoreAdaptor {

  /**
   * Move a file, atomically
   *
   * @throws WdkModelException
   *   if target already exists.
   */
  void moveFileAtomic(Path from, Path to) throws WdkModelException;

  /**
   * Get the absolute paths of the entries in the provided directory.  Must
   * close all resources before returning. Not recursive.
   *
   * @throws WdkModelException
   *   if dir does not exist, or can't be read.
   */
  List<Path> getPathsInDir(Path dir) throws WdkModelException;

  /**
   * Read the contents of the provided file into a string. Must close all
   * resources before returning.
   *
   * @throws WdkModelException
   *   If file doesn't exist or can't be read.
   */
  String readFileContents(Path file) throws WdkModelException;

  /**
   * Return true if the provided path exists and is a directory
   */
  boolean isDirectory(Path dir) throws WdkModelException;

  /**
   * Write the provided contents to the provided path.
   */
  void writeFile(Path file, String contents, boolean errorIfTargetExists) throws WdkModelException;

  /**
   * Create a new directory.
   *
   * @param dir
   *   the directory path
   *
   * @throws WdkModelException
   *   if dir already exists
   */
  void createDirectory(Path dir) throws WdkModelException;

  /**
   * Write a file with no contents (like "touch" in unix).  No error if file
   * already exists.
   *
   * @throws WdkModelException
   *   If can't write file.
   */
  void writeEmptyFile(Path file) throws WdkModelException;

  /**
   * Delete the provided file or dir
   */
 void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException;

  /**
   * get the last mod time of a file or dir
   */
  Long getModificationTime(Path fileOrDir) throws WdkModelException;

  /**
   * Read the first line of a file.  Must close all resources before returning.
   *
   * @throws WdkModelException
   *   if file doesn't exist.
   */
  String readSingleLineFile(Path file) throws WdkModelException;

  /**
   * Return true if the file (or directory) exists
   */
  boolean fileExists(Path file) throws WdkModelException;

  /**
   * Returns the unique id of the user dataset store.
   */
  String findUserDatasetStoreId(Path dir) throws WdkModelException;
}
