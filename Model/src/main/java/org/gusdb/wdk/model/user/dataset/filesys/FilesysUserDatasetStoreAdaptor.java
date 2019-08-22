package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetStoreAdaptor;

/**
 * An implementation of JsonUserDatasetStoreAdaptor that uses the java nio Files
 * operations
 *
 * @author steve
 */
public class FilesysUserDatasetStoreAdaptor implements UserDatasetStoreAdaptor {

  @Override
  public List<Path> getPathsInDir(Path dir) throws WdkModelException {
    List<Path> paths = new ArrayList<>();
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
      for (Path file : dirStream) {
        paths.add(file);
      }
      return paths;
    } catch (IOException | DirectoryIteratorException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public Long getModificationTime(Path fileOrDir) throws WdkModelException {
    Long modTime;
    try {
      modTime = Files.getLastModifiedTime(fileOrDir).toMillis();
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
    return modTime;
  }

  @Override
  public boolean fileExists(Path file) {
    return Files.exists(file);
  }

  @Override
  public String readSingleLineFile(Path file) throws WdkModelException {
    try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
      return reader.readLine();
    } catch (IOException x) {
      throw new WdkModelException(x);
    }
  }

  @Override
  public void writeFile(Path file, String contents, boolean errorIfAlreadyExists) throws WdkModelException {
    StandardOpenOption option = errorIfAlreadyExists? StandardOpenOption.CREATE_NEW : StandardOpenOption.CREATE;
    try {
      Files.write(file, contents.getBytes(), option);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public void writeEmptyFile(Path file) throws WdkModelException {
    try {
      Files.write(file, "".getBytes(), StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public boolean isDirectory(Path dir) {
    return Files.isDirectory(dir);
  }

  @Override
  public String readFileContents(Path file) throws WdkModelException {
    StringBuilder jsonStringBldr = new StringBuilder();

    try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
      String line;
      while ((line = reader.readLine()) != null)
        jsonStringBldr.append(line);
    } catch (IOException x) {
      throw new WdkModelException(x);
    }
    return jsonStringBldr.toString();
  }

  @Override
  public void createDirectory(Path dir) throws WdkModelException {
    try {
      Files.createDirectory(dir);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException {
    try {
      Files.delete(fileOrDir);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public void moveFileAtomic(Path from, Path to) throws WdkModelException {
    try {
      Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public String findUserDatasetStoreId(Path usersRootDir) throws WdkModelException {
    if(usersRootDir == null) {
      throw new WdkModelException("No users root directory provided.");
    }
    return usersRootDir.toString();
  }
}
