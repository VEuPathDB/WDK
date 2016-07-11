package org.gusdb.wdk.model.user.dataset.json;

import java.util.Date;
import java.util.List;
import java.util.Set;


import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;

public class JsonUserDataset implements UserDataset {

  @Override
  public UserDatasetMeta getMeta() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateMeta(UserDatasetMeta metainfo) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Integer getNumberOfDataFiles() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<UserDatasetFile> getFiles() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UserDatasetFile getFile(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<UserDatasetShare> getSharedWith() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void share(Integer userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void unshare(Integer userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void unshareAllUsers() {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<UserDatasetDependency> getDependencies() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Boolean getIsCompatible() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getIncompatibleReason() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Integer getSize() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Integer getPercentQuota() {
    // TODO Auto-generated method stub
    return null;
  }

}
