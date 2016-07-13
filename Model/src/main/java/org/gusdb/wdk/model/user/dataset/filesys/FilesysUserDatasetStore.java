package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDataset;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 rootPath/
  default_quota
  u12345/              # files in this directory are user readable, but not writeable
    quota              # can put this here, if increased from default
    datasets/
      d34541/
      d67690/
        datafiles/        
           Blah.bigwig    
           blah.profile
        dataset.json
    externalDatasets/
      43425.12592    # reference to dataset in another user.  empty file, name: user_id.dataset_id
      691056.53165 

 * @author steve
 *
 */

public class FilesysUserDatasetStore implements org.gusdb.wdk.model.user.dataset.UserDatasetStore {

  private Path usersRootDir;

  @Override
  public void initialize(Map<String, String> configuration, Set<UserDatasetTypeHandler> typeHandlers)
      throws WdkModelException {
    String pathName = configuration.get("rootPath");
    if (pathName == null)
      throw new WdkModelException("Required configuration 'rootPath' not found.");
    usersRootDir = Paths.get(pathName);

    if (Files.isDirectory(usersRootDir))
      throw new WdkModelException(
          "Provided property 'rootPath' has value '" + pathName + "' which is not an existing directory");
  }

  @Override
  public Map<Integer, UserDataset> getUserDatasets(Integer userId) throws WdkModelException {

    // find user directory
    Path userDir = usersRootDir.resolve(userId.toString());
    if (!Files.isDirectory(userDir))
      throw new WdkModelException("Can't find user directory " + userDir);

    // find datasets dir
    Path userDatasetsDir = userDir.resolve("datasets");
    if (!Files.isDirectory(userDatasetsDir))
      throw new WdkModelException("Can't find user datasets directory " + userDatasetsDir);

    // iterate through datasets, creating a UD from each
    Map<Integer, UserDataset> datasetsMap = new HashMap<Integer, UserDataset>();
    try (DirectoryStream<Path> userDatasetsDirStream = Files.newDirectoryStream(userDatasetsDir)) {
      for (Path datasetDir : userDatasetsDirStream) {
        UserDataset dataset = makeUserDataset(datasetDir);
        datasetsMap.put(dataset.getUserDatasetId(), dataset);
      }
    }
    catch (IOException | DirectoryIteratorException e) {
      throw new WdkModelException(e);
    }

    return Collections.unmodifiableMap(datasetsMap);
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
  
  private JsonUserDataset makeUserDataset(Path datasetDir) throws WdkModelException {

    Integer datasetId;
    try {
      datasetId = Integer.parseInt(datasetDir.getFileName().toString());
    }
    catch (NumberFormatException e) {
      throw new WdkModelException("Found file or directory '" + datasetDir.getFileName() +
          "' in user datasets directory " + datasetDir.getParent() + ". It is not a dataset ID");
    }
    
    Path datasetJsonFile = datasetDir.resolve("dataset.json");
    JSONObject datasetJson = makeDatasetJson(datasetJsonFile);

    Path datafilesDir = datasetDir.resolve("datafiles");
    if (!Files.isDirectory(datafilesDir))
      throw new WdkModelException("Can't find datafiles directory " + datafilesDir);

    Map<String, UserDatasetFile> dataFiles = new HashMap<String, UserDatasetFile>();

    // iterate through data files, creating a datafile obj for each
    try (DirectoryStream<Path> datafileStream = Files.newDirectoryStream(datafilesDir)) {
      for (Path datafile : datafileStream) {
        dataFiles.put(datafile.getFileName().toString(), new FilesysUserDatasetFile(datafile));
      }
    }
    catch (IOException | DirectoryIteratorException e) {
      throw new WdkModelException(e);
    }

    return new JsonUserDataset(datasetId, datasetJson, dataFiles);
  }
  
  private JSONObject makeDatasetJson(Path datasetJsonFile) throws WdkModelException {

    StringBuilder jsonStringBldr = new StringBuilder();
    try (BufferedReader reader = Files.newBufferedReader(datasetJsonFile, Charset.forName("US-ASCII"))) {
      String line = null;
      while ((line = reader.readLine()) != null)
        jsonStringBldr.append(line);
    }
    catch (IOException x) {
      throw new WdkModelException(x);
    }

    JSONObject datasetJson;
    try {
      datasetJson = new JSONObject(jsonStringBldr);
    }
    catch (JSONException e) {
      throw new WdkModelException("Could not parse " + datasetJsonFile, e);
    }
    return datasetJson;
  }

} 


