package org.gusdb.wdk.service.request.user;

import static org.gusdb.wdk.service.formatter.Keys.DELETE;
import static org.gusdb.wdk.service.formatter.Keys.UNDELETE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.record.PrimaryKeyValue;

public class BasketRequests {

  public static class BasketActions {

    private static final List<String> ACTION_TYPES = Arrays.asList(DELETE, UNDELETE);

    private Map<String,List<PrimaryKeyValue>> _basketActionMap;

    public BasketActions(Map<String,List<PrimaryKeyValue>> basketActionMap) {
      _basketActionMap = basketActionMap;
      // clear out IDs that appear in both "delete" and "undelete"
      cleanData(_basketActionMap.get(DELETE), _basketActionMap.get(UNDELETE));
    }

    private void cleanData(List<PrimaryKeyValue> toDelete, List<PrimaryKeyValue> toUndelete) {
      for (int j, i = 0; i < toDelete.size(); i++) {
        PrimaryKeyValue id = toDelete.get(i);
        if ((j = toUndelete.indexOf(id)) != -1) {
          // found ID in both lists; remove from both
          toUndelete.remove(j);
          toDelete.remove(i);
          i--; // recheck at the current index
        }
      }
    }

    public List<PrimaryKeyValue> getRecordsToDelete() {
      return _basketActionMap.get(DELETE);
    }

    public List<PrimaryKeyValue> getRecordsToUndelete() {
      return _basketActionMap.get(UNDELETE);
    }
  }
}
