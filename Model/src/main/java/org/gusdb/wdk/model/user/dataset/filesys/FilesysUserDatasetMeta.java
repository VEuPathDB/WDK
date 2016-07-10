package org.gusdb.wdk.model.user.dataset.filesys;

import java.nio.file.Path;

import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.json.JSONObject;

public class FilesysUserDatasetMeta implements UserDatasetMeta {

  private static final String NAME = "name";
  private static final String SUMMARY = "summary";
  private static final String DESCRIPTION = "description";
      
  private Path jsonFile;
  private String name;
  private String summary;
  private String description;

  public FilesysUserDatasetMeta(Path jsonFile) {
    this.jsonFile = jsonFile;
  }

  JSONObject toJson() {
    JSONObject jsonObj = new JSONObject();
    jsonObj.put(NAME, getName());
    return jsonObj;
  }

  public String getSummary() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }
}
