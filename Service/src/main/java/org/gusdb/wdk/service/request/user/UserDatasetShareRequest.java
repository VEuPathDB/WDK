package org.gusdb.wdk.service.request.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Parses the JSON object returned by either a PATCH REST request for
 * managing user dataset sharing
 * @author crisl-adm
 *
 */
public class UserDatasetShareRequest {
	
  private static Logger LOG = Logger.getLogger(UserDatasetShareRequest.class);
	
  public static final List<String> SHARE_TYPES = new ArrayList<>(Arrays.asList("add","delete"));
			
  private Map<String, Map<Long, Set<Long>>> _userDatasetShareMap;
	
  public Map<String, Map<Long, Set<Long>>> getUserDatasetShareMap() {
	return _userDatasetShareMap;
  }

  public void setUserDatasetShareMap(Map<String, Map<Long, Set<Long>>> userDatasetShareMap) {
    _userDatasetShareMap = userDatasetShareMap;
  }
	
  /**
   * Input Format:
   *
   *    {
   *	  "add": {
   *	    "dataset_id1": [ "user1", "user2" ]
   *	    "dataset_id2": [ "user1" ]
   *	  },
   *	  "delete" {
   *	    "dataset_id3": [ "user1", "user2" ]
   *	  }
   *	}	
   *
   * 
   * @param json
   * @return
   * @throws RequestMisformatException
   */
  public static UserDatasetShareRequest createFromJson(JSONObject json) throws RequestMisformatException {
    try {
	  UserDatasetShareRequest request = new UserDatasetShareRequest();
	  request.setUserDatasetShareMap(parseUserDatasetShare(json));
	  return request;
	}
	catch (JSONException e) {
	  String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
	  throw new RequestMisformatException(detailMessage, e);
	}
  }

  /**
   * Parses the share types, the participating datasets and the users on the receiving end of the share into
   * a Java structure.  Bad shareTypes, datasets and users are ignored (warnings only in the log).
   * @param userDatasetShare - the input json object - see above for structure
   * @return - the Java map representing this JSON object
   * @throws JSONException
   */
  protected static Map<String, Map<Long, Set<Long>>> parseUserDatasetShare(JSONObject userDatasetShare) throws JSONException {
    List<String> shareTypes = SHARE_TYPES;
    List<Object> unrecognizedActions = new ArrayList<>();
    List<Object> improperDatasets = new ArrayList<>();
    Map<String, Map<Long, Set<Long>>> map = new HashMap<>();
    for(Object shareType : userDatasetShare.keySet()) {
      if(shareTypes.contains(((String)shareType).trim())) {
        JSONObject userDatasets = userDatasetShare.getJSONObject((String)shareType);
        Map<Long, Set<Long>> innerMap = new HashMap<>();
        for(Object userDataset : userDatasets.keySet()) {
          Long dataset = 0L;
          try {
            dataset = new Long(((String)userDataset).trim());
          }
          catch(NumberFormatException nfe) {
            improperDatasets.add(userDataset);
          }
          JSONArray usersJsonArray = userDatasets.getJSONArray(dataset.toString()); 
          ObjectMapper mapper = new ObjectMapper();
          String usersJson = null;
          try {
            usersJson = usersJsonArray.toString();
            CollectionType setType = mapper.getTypeFactory().constructCollectionType(Set.class, Integer.class);
            Set<Long> users = mapper.readValue(usersJson, setType);
            innerMap.put(dataset, users);  
          }
          catch(IOException jpe) {
            LOG.warn("The user array associated with dataset id " + dataset
                + " is not parseable (they may not all be integers: " + usersJson + "). "
                + " Skipping this dataset for " + shareType);
            continue;
          }
        }
        map.put(((String)shareType).trim(), innerMap);
      }
      else {
        unrecognizedActions.add(shareType);
      }
    }
    if(!unrecognizedActions.isEmpty()) {
      String unrecognized = FormatUtil.join(unrecognizedActions.toArray(), ",");
      LOG.warn("This user service request contains the following unrecognized sharing actions: " + unrecognized);
    }
    if(!improperDatasets.isEmpty()) {
      String improper = FormatUtil.join(improperDatasets.toArray(), ",");
      LOG.warn("This user dataset share service request contains the following improper datasets: " + improper);
    }
    return map;
  }
}
