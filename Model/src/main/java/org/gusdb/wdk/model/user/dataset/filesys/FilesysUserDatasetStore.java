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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetCompatibility;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDataset;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetMeta;
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
        meta.json      # we have this as a separate file to avoid race conditions with sharing updates
    externalDatasets/
      43425.12592    # reference to dataset in another user.  empty file, name: user_id.dataset_id
      691056.53165 
    removedExternalDatasets/   # holds shares this user no longer wants to see.
      502401.90112

 * @author steve
 *
 */

public class FilesysUserDatasetStore implements org.gusdb.wdk.model.user.dataset.UserDatasetStore {

  private static final String DATASET_JSON_FILE = "dataset.json";
  private static final String META_JSON_FILE = "meta.json";
  private static final String EXTERNAL_DATASETS_DIR = "externalDatasets";
  private static final String REMOVED_EXTERNAL_DATASETS_DIR = "removedExternalDatasets";
  
  private Path usersRootDir;
  Map<UserDatasetType, UserDatasetTypeHandler> typeHandlersMap;

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
    
    for (UserDatasetTypeHandler handler : typeHandlers) typeHandlersMap.put(handler.getUserDatasetType(), handler);
  }

  @Override
  public Map<Integer, UserDataset> getUserDatasets(Integer userId) throws WdkModelException {

    Path userDatasetsDir = getUserDatasetsDir(userId, false);

    // iterate through datasets, creating a UD from each
    Map<Integer, UserDataset> datasetsMap = new HashMap<Integer, UserDataset>();
    try (DirectoryStream<Path> userDatasetsDirStream = Files.newDirectoryStream(userDatasetsDir)) {
      for (Path datasetDir : userDatasetsDirStream) {
        UserDataset dataset = getUserDataset(datasetDir);
        datasetsMap.put(dataset.getUserDatasetId(), dataset);
      }
    }
    catch (IOException | DirectoryIteratorException e) {
      throw new WdkModelException(e);
    }

    return Collections.unmodifiableMap(datasetsMap);
  }
  
  @Override
  public JsonUserDataset getUserDataset(Integer userId, Integer datasetId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDir(userId, false);
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return getUserDataset(datasetDir);
  }
  
  /**
   * Check if a dataset is compatible with this application, based on its type and data dependencies.
   * @param userDataset
   * @return
   * @throws WdkModelException 
   */
  @Override
  public UserDatasetCompatibility getCompatibility(UserDataset userDataset) throws WdkModelException {
    UserDatasetType type = userDataset.getType();
    if (!typeHandlersMap.containsKey(type)) 
      return new UserDatasetCompatibility(false, "Type " + type + " is not supported.");
    return typeHandlersMap.get(type).getCompatibility(userDataset);
  }

  @Override
  public Date getModificationTime(Integer userId) throws WdkModelException {
    Date modTime = null;
    try {
      modTime = new Date(Files.getLastModifiedTime(getUserDatasetsDir(userId, false)).toMillis());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
    return modTime;
  }

  @Override
  public Integer getQuota(Integer userId) throws WdkModelException {
    Path quotaFile;
    
    Path defaultQuotaFile = usersRootDir.resolve("default_quota");
    Path userQuotaFile = getUserDir(userId, false).resolve("quota");
    quotaFile = Files.exists(userQuotaFile)? userQuotaFile : defaultQuotaFile;
    
    try (BufferedReader reader = Files.newBufferedReader(quotaFile, Charset.defaultCharset())) {
      String line = reader.readLine();
      if (line == null) throw new WdkModelException("Empty quota file " + quotaFile);
      return new Integer(line.trim());
    }
    catch (IOException x) {
      throw new WdkModelException(x);
    }
  }

  @Override
  public void updateMetaFromJson(Integer userId, Integer datasetId, JSONObject metaJson) throws WdkModelException {
    JsonUserDatasetMeta metaObj = new JsonUserDatasetMeta(metaJson);  // validate the input json
    Path metaJsonFile = getUserDatasetsDir(userId, false).resolve(datasetId.toString()).resolve(META_JSON_FILE);
    writeFileAtomic(metaJsonFile, metaObj.getJsonObject().toString());
  }
  
  private void writeJsonUserDataset(JsonUserDataset dataset) throws WdkModelException {
    Path datasetJsonFile = getUserDatasetsDir(dataset.getOwnerId(), false).resolve(dataset.getUserDatasetId().toString()).resolve(DATASET_JSON_FILE);
    writeFileAtomic(datasetJsonFile, dataset.getDatasetJsonObject().toString());
  }
  
  private void writeFileAtomic(Path file, String contents) throws WdkModelException {
    Path tempFile = file.resolve("." + Long.toString(System.currentTimeMillis()));
    try {
      Files.write(tempFile, contents.getBytes(), StandardOpenOption.CREATE_NEW);
      Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE);
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
  
  private Path getUserDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException {
    Path userDir = usersRootDir.resolve(userId.toString());
    try {
      if (!Files.isDirectory(userDir))
        if (createPathIfAbsent) Files.createDirectory(userDir);
        else throw new WdkModelException("Can't find user directory " + userDir);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
    return userDir;
  }
  
  private Path getUserDatasetsDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException {
    Path userDatasetsDir = getUserDir(userId, createPathIfAbsent).resolve("datasets");

    try {
      if (!Files.isDirectory(userDatasetsDir))    
        if (createPathIfAbsent) Files.createDirectory(userDatasetsDir);
        else throw new WdkModelException("Can't find user datasets directory " + userDatasetsDir);
      
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
    
    return userDatasetsDir;
  }
 
  /**
   * Construct a user dataset object, given its location in the store.
   * @param datasetDir
   * @return
   * @throws WdkModelException
   */
  private JsonUserDataset getUserDataset(Path datasetDir) throws WdkModelException {

    Integer datasetId;
    try {
      datasetId = Integer.parseInt(datasetDir.getFileName().toString());
    }
    catch (NumberFormatException e) {
      throw new WdkModelException("Found file or directory '" + datasetDir.getFileName() +
          "' in user datasets directory " + datasetDir.getParent() + ". It is not a dataset ID");
    }
    
    JSONObject datasetJson = parseJsonFile(datasetDir.resolve("dataset.json")); ;
    JSONObject metaJson = parseJsonFile(datasetDir.resolve("meta.json"));

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

    return new JsonUserDataset(datasetId, datasetJson, metaJson, dataFiles);
  }
  
  /**
   * Read a dataset.json file, and return the JSONObject that it parses to.
   * @param jsonFile
   * @return
   * @throws WdkModelException
   */
  private JSONObject parseJsonFile(Path jsonFile) throws WdkModelException {

    StringBuilder jsonStringBldr = new StringBuilder();
    try (BufferedReader reader = Files.newBufferedReader(jsonFile, Charset.defaultCharset())) {
      String line = null;
      while ((line = reader.readLine()) != null)
        jsonStringBldr.append(line);
    }
    catch (IOException x) {
      throw new WdkModelException(x);
    }

    JSONObject json;
    try {
      json = new JSONObject(jsonStringBldr);
    }
    catch (JSONException e) {
      throw new WdkModelException("Could not parse " + jsonFile, e);
    }
    return json;
  }

  @Override
  public void shareUserDatasets(Integer ownerUserId, Set<Integer> datasetIds, Set<Integer> recipientUserIds) throws WdkModelException {
    Set<Integer[]> externalDatasetLinks = new HashSet<Integer[]>();
    for (Integer datasetId : datasetIds) {
      JsonUserDataset dataset = getUserDataset(ownerUserId, datasetId);
      for (Integer recipientUserId : recipientUserIds) {
        dataset.shareWith(recipientUserId);
        Integer[] linkInfo = {ownerUserId, datasetId};
        externalDatasetLinks.add(linkInfo);
      }
      writeJsonUserDataset(dataset);  // write this before the links
      for (Integer[] linkInfo : externalDatasetLinks) 
        writeExternalDatasetLink(ownerUserId, linkInfo[0], linkInfo[1]);
    }    
  }
  
  /**
   * Write a file in a user's space, indicating that this user can see another user's dataset.
   * @param ownerUserId
   * @param datasetId
   * @param recipientUserId
   * @throws WdkModelException
   */
  private void writeExternalDatasetLink(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getUserDatasetsDir(recipientUserId, true).resolve(EXTERNAL_DATASETS_DIR);
    try {
      if (!Files.isDirectory(recipientExternalDatasetsDir)) Files.createDirectory(recipientExternalDatasetsDir);
      Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
      Files.write(externalDatasetFileName, new String("").getBytes(), StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
 
  /**
   * Get a file name to use as a link from a recipient to a shared dataset in the owner's space.
   * @param ownerUserId
   * @param datasetId
   * @return
   */
  private String getExternalDatasetFileName(Integer ownerUserId, Integer datasetId) {
    return ownerUserId + "." + datasetId;
  }

  /**
   * Delete a dataset.  But, don't delete externalUserDataset references to it.  The UI
   * will let the recipient user of them know they are dangling.
   */
  @Override
  public void deleteUserDataset(Integer userId, Integer datasetId) throws WdkModelException {
    try {
      Files.delete(getUserDatasetsDir(userId, false).resolve(datasetId.toString()));
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public void deleteExternalUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getUserDatasetsDir(recipientUserId, true).resolve(EXTERNAL_DATASETS_DIR);
    Path recipientRemovedDatasetsDir = getUserDatasetsDir(recipientUserId, true).resolve(REMOVED_EXTERNAL_DATASETS_DIR);    try {
      if (!Files.isDirectory(recipientRemovedDatasetsDir)) Files.createDirectory(recipientRemovedDatasetsDir);
      Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
      Path moveToFileName = recipientRemovedDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
      Files.move(externalDatasetFileName,  moveToFileName, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }

    
  }

} 


