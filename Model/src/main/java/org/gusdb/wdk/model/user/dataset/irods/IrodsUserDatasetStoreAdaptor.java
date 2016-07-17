package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor;

public class IrodsUserDatasetStoreAdaptor implements JsonUserDatasetStoreAdaptor {

  @Override
  public void moveFileAtomic(Path from, Path to) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public List<Path> getPathsInDir(Path dir) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void putFilesIntoMap(Path dir, Map<String, UserDatasetFile> filesMap) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public String readFileContents(Path file) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isDirectory(Path dir) throws WdkModelException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void writeFileAtomic(Path file, String contents) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public void createDirectory(Path dir) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeFile(Path file, String contents) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getModificationTime(Path fileOrDir) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String readSingleLineFile(Path file) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean fileExists(Path file) {
    // TODO Auto-generated method stub
    return false;
  }

}
