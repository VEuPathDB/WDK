package org.gusdb.wdk.service.request.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Parses the JSON object returned by either a PATCH REST request for managing user dataset sharing
 * 
 * @author crisl-adm
 *
 */
public class UserDatasetShareRequest {

  private static Logger LOG = Logger.getLogger(UserDatasetShareRequest.class);

  public static final List<String> SHARE_TYPES = new ArrayList<>(Arrays.asList("add", "delete"));

  private final UserFactory _userFactory;
  private Map<String, Map<Long, Set<Long>>> _userDatasetShareMap;
  private List<Object> _invalidActions;
  private List<Object> _malformedDatasetIds;
  private Map<Object, String> _malformedUserIds;
  private List<Long> _invalidUserIds;
  private List<Long> _invalidDatasetIds;

  public UserDatasetShareRequest(WdkModel wdkModel) {
    _userFactory = wdkModel.getUserFactory();
  }

  public Map<String, Map<Long, Set<Long>>> getUserDatasetShareMap() {
    return _userDatasetShareMap;
  }

  public void setUserDatasetShareMap(Map<String, Map<Long, Set<Long>>> userDatasetShareMap) {
    _userDatasetShareMap = userDatasetShareMap;
  }

  public void verifyUserIds(JSONObject userDatasetShare) {
    Set<Long> userIds = new HashSet<>();
    _invalidUserIds = new ArrayList<>();
    for (Object shareType : userDatasetShare.keySet()) {
      JSONObject userDatasets = userDatasetShare.getJSONObject((String) shareType);
      for (Object userDataset : userDatasets.keySet()) {
        JSONArray userIdsJsonArray = userDatasets.getJSONArray(userDataset.toString());
        ObjectMapper mapper = new ObjectMapper();
        String userIdsJson = null;
        try {
          userIdsJson = userIdsJsonArray.toString();
          CollectionType setType = mapper.getTypeFactory().constructCollectionType(Set.class, Long.class);
          userIds.addAll(mapper.readValue(userIdsJson, setType));
        }
        catch (IOException ioe) {
          // Will catch these later in second pass
          continue;
        }
      }
    }
    Map<Long, Boolean> _userIdMap = _userFactory.verifyUserids(userIds);
    _invalidUserIds = _userIdMap.keySet().stream()
        .filter(userId -> !_userIdMap.get(userId))
        .collect(Collectors.toList());
  }

  public JSONObject getErrors() {
    JSONObject jsonErrors = new JSONObject();
    if (!_invalidActions.isEmpty()) {
      jsonErrors.put("invalid actions", FormatUtil.join(_invalidActions.toArray(), ","));
    }
    if (!_malformedDatasetIds.isEmpty()) {
      jsonErrors.put("malformed dataset ids", FormatUtil.join(_malformedDatasetIds.toArray(), ","));
    }
    if (!_malformedUserIds.isEmpty()) {
      JSONArray jsonArray = new JSONArray();
      for (Object dataset : _malformedUserIds.keySet()) {
        jsonArray.put(
            new JSONObject().put("dataset", dataset).put("user id list", _malformedUserIds.get(dataset)));
      }
      jsonErrors.put("malformed user id lists", jsonArray);
    }
    if (!_invalidUserIds.isEmpty()) {
      jsonErrors.put("invalid user ids", FormatUtil.join(_invalidUserIds.toArray(), ","));
    }
    if (!_invalidDatasetIds.isEmpty()) {
      jsonErrors.put("invalid dataset ids", FormatUtil.join(_invalidDatasetIds.toArray(), ","));
    }
    LOG.error(jsonErrors.toString());
    return jsonErrors;
  }

  /**
   * Input Format:
   *
   * { "add": { "dataset_id1": [ "userEmail1", "userEmail2" ] "dataset_id2": [ "userEmail1" ] }, "delete" {
   * "dataset_id3": [ "userEmail1", "userEmail3" ] } }
   *
   * @param json
   * @return
   * @throws RequestMisformatException
   */
  public static UserDatasetShareRequest createFromJson(JSONObject json, WdkModel wdkModel,
      Set<Long> ownedDatasetIds) throws RequestMisformatException, DataValidationException {
    try {
      UserDatasetShareRequest request = new UserDatasetShareRequest(wdkModel);
      request.verifyUserIds(json);
      request.setUserDatasetShareMap(request.parseUserDatasetShare(json, ownedDatasetIds));
      JSONObject errors = request.getErrors();
      if (errors.length() != 0) {
        throw new DataValidationException(errors.toString(2));
      }
      return request;
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

  /**
   * Parses the share types, the participating datasets and the users on the receiving end of the share into a
   * Java structure. Bad shareTypes, datasets and users are ignored (warnings only in the log).
   * 
   * @param userDatasetShare
   *          - the input json object - see above for structure
   * @return - the Java map representing this JSON object
   * @throws JSONException
   */
  protected Map<String, Map<Long, Set<Long>>> parseUserDatasetShare(JSONObject userDatasetShare,
      Set<Long> ownedDatasetIds) throws JSONException {
    List<String> shareTypes = SHARE_TYPES;
    _invalidActions = new ArrayList<>();
    _malformedDatasetIds = new ArrayList<>();
    _invalidDatasetIds = new ArrayList<>();
    _malformedUserIds = new HashMap<Object, String>();
    Map<String, Map<Long, Set<Long>>> map = new HashMap<>();
    for (Object shareType : userDatasetShare.keySet()) {
      if (shareTypes.contains(((String) shareType).trim())) {
        JSONObject userDatasets = userDatasetShare.getJSONObject((String) shareType);
        Map<Long, Set<Long>> innerMap = new HashMap<>();
        for (Object userDataset : userDatasets.keySet()) {
          Long datasetId = 0L;
          try {
            datasetId = Long.valueOf(((String) userDataset).trim());
          }
          catch (NumberFormatException nfe) {
            _malformedDatasetIds.add(userDataset);
            continue;
          }
          if (!ownedDatasetIds.contains(datasetId)) {
            _invalidDatasetIds.add(datasetId);
            continue;
          }
          JSONArray userIdsJsonArray = userDatasets.getJSONArray(datasetId.toString());
          ObjectMapper mapper = new ObjectMapper();
          String userIdsJson = null;
          try {
            userIdsJson = userIdsJsonArray.toString();
            CollectionType setType = mapper.getTypeFactory().constructCollectionType(Set.class, Long.class);
            Set<Long> userIds = mapper.readValue(userIdsJson, setType);
            innerMap.put(datasetId, userIds);
          }
          catch (IOException ioe) {
            _malformedUserIds.put(datasetId, userIdsJson);
            continue;
          }
        }
        map.put(((String) shareType).trim(), innerMap);
      }
      else {
        _invalidActions.add(shareType);
      }
    }
    return map;
  }
}
