package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.json.JSONObject;

public class FilesysUserDatasetStore implements org.gusdb.wdk.model.user.dataset.UserDatasetStore {

  private File userRootDir;
  
  @Override
  public void initialize(Map<String, String> configuration) throws WdkModelException {
    String pathName = configuration.get("rootPath");
    if (pathName == null) throw new WdkModelException("Required configuration 'rootPath' not found.");
    userRootDir = Paths.get(pathName).toFile();
    
    if (!userRootDir.isDirectory()) throw new WdkModelException("Provided property 'rootPath' has value '" + pathName + "' which is not an existing directory");
  }

  @Override
  public Map<Integer, UserDataset> getUserDatasets(Integer userId) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getModificationTime(Integer userId) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Integer getQuota(Integer userId) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateMetaFromJson(Integer userId, Integer datasetId, JSONObject metaJson) {
    // TODO Auto-generated method stub
    
  }
  
  

}
