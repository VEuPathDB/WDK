package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Map;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;

/**
 * An implementation of JsonUserDatasetStore that uses the java nio Files operations
 * @author steve
 *
 */

public class FilesysUserDatasetStore extends org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore {
  
  @Override
  protected Path initialize(Map<String, String> configuration) throws WdkModelException {
    String pathName = configuration.get("rootPath");
    if (pathName == null)
      throw new WdkModelException("Required configuration 'rootPath' not found.");
    Path usersRootDir = Paths.get(pathName);

    if (Files.isDirectory(usersRootDir))
      throw new WdkModelException(
          "Provided property 'rootPath' has value '" + pathName + "' which is not an existing directory");
    return usersRootDir;
  }
  
  @Override
  protected void fillDatasetsMap(Path userDatasetsDir, Map<Integer, UserDataset> datasetsMap) throws WdkModelException {
    try (DirectoryStream<Path> userDatasetsDirStream = Files.newDirectoryStream(userDatasetsDir)) {
      for (Path datasetDir : userDatasetsDirStream) {
        UserDataset dataset = getUserDataset(datasetDir);
        datasetsMap.put(dataset.getUserDatasetId(), dataset);
      }
    }
    catch (IOException | DirectoryIteratorException e) {
      throw new WdkModelException(e);
    }
  }
  
  protected Date getModificationTime(Path fileOrDir) throws WdkModelException {
    Date modTime = null;
    try {
      modTime = new Date(Files.getLastModifiedTime(fileOrDir).toMillis());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
    return modTime;  
  }
  
  @Override
  protected boolean fileExists(Path file) {
    return Files.exists(file);
  }
  
  @Override
  protected String readSingleLineFile(Path file) throws WdkModelException {
    try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
      return reader.readLine();
    }
    catch (IOException x) {
      throw new WdkModelException(x);
    }
  }

  protected void writeFileAtomic(Path file, String contents) throws WdkModelException {
    Path tempFile = file.resolve("." + Long.toString(System.currentTimeMillis()));
    try {
      Files.write(tempFile, contents.getBytes(), StandardOpenOption.CREATE_NEW);
      Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE);
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
  
  @Override
  protected void writeFile(Path file, String contents) throws WdkModelException {
    try {
      Files.write(file, contents.getBytes(), StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
    
  @Override
  protected boolean isDirectory(Path dir) throws WdkModelException {
    return Files.isDirectory(dir);
  }
  
  @Override
  protected void putFilesIntoMap(Path dir, Map<String, UserDatasetFile> filesMap) throws WdkModelException {
    // iterate through data files, creating a datafile obj for each
    try (DirectoryStream<Path> datafileStream = Files.newDirectoryStream(dir)) {
      for (Path datafile : datafileStream) {
        filesMap.put(datafile.getFileName().toString(), new FilesysUserDatasetFile(datafile));
      }
    }
    catch (IOException | DirectoryIteratorException e) {
      throw new WdkModelException(e);
    }
  }
  
  @Override
  protected String readFileContents(Path file) throws WdkModelException {
    StringBuilder jsonStringBldr = new StringBuilder();

    try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
      String line = null;
      while ((line = reader.readLine()) != null)
        jsonStringBldr.append(line);
    }
    catch (IOException x) {
      throw new WdkModelException(x);
    }
    return jsonStringBldr.toString();
  }
  
  @Override
  protected void createDirectory(Path dir) throws WdkModelException {
    try {
      Files.createDirectory(dir);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }    
  }
  
  @Override
  protected void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException {
    try {
      Files.delete(fileOrDir);
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }    
  }
  
  @Override
  protected void moveFileAtomic(Path from, Path to) throws WdkModelException {
    try {
      Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
   
  }

} 


