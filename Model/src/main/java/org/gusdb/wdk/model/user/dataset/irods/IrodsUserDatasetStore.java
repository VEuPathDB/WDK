package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore;

public class IrodsUserDatasetStore extends JsonUserDatasetStore {

  @Override
  protected void fillDatasetsMap(Path userDatasetsDir, Map<Integer, UserDataset> datasetsMap)
      throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  protected Path getUserDatasetsDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String readFileContents(Path file) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean isDirectory(Path dir) throws WdkModelException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected void putDataFilesIntoMap(Path dataFilesDir, Map<String, UserDatasetFile> dataFilesMap)
      throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void writeFileAtomic(Path file, String contents) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void writeExternalDatasetLink(Path recipientExternalDatasetsDir, Integer ownerUserId,
      Integer datasetId) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteExternalUserDataset(Path recipientExternalDatasetsDir, Path recipientRemovedDatasetsDir,
      Integer ownerUserId, Integer datasetId) throws WdkModelException {
    // TODO Auto-generated method stub

  }

  @Override
  protected Date getModificationTime(Path fileOrDir) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean fileExists(Path file) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected Integer getQuota(Path quotaFile) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void putFilesIntoMap(Path dir, Map<String, UserDatasetFile> filesMap) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void createDirectory(Path dir) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void writeFile(Path file, String contents) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void moveFileAtomic(Path from, Path to) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected String readSingleLineFile(Path file) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

}
