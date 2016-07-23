package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import org.gusdb.wdk.model.WdkModelException;
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
  public String readFileContents(Path file) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean directoryExists(Path dir) throws WdkModelException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void writeFileAtomic(Path file, String contents, boolean errorIfTargetExists) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public void createDirectory(Path dir) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeEmptyFile(Path file) throws WdkModelException {
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
