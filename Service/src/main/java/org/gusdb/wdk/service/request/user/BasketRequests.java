package org.gusdb.wdk.service.request.user;

import static java.util.Arrays.stream;

import java.util.Collection;
import java.util.Optional;

import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONObject;

public class BasketRequests {

  public enum ActionType {
    ADD("add"),
    REMOVE("remove"),
    REMOVE_ALL("removeAll");

    public final String value;

    ActionType(String s) {
      value = s;
    }

    public static Optional<ActionType> fromString(String s) {
      return stream(values()).filter(v -> v.value.equals(s)).findFirst();
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public static class BasketActions extends PatchMap<ActionType, PrimaryKeyValue> {

    private final RecordClass _recordClass;

    /**
     * Creates set of actions, each associated with a list of basket records
     * <p>
     * Input Format:
     * <pre>
     * {
     *   action: add|remove|removeAll,
     *   primaryKeys?: PrimaryKey[]
     * }
     * </pre>
     * Where PrimaryKey is <code>[ { name: String, value: String } ]</code>.
     *
     * @param json input object
     * @return parsed actions to perform on IDs
     * @throws WdkModelException
     * @throws DataValidationException
     */
    public BasketActions(JSONObject json, RecordClass recordClass)
        throws DataValidationException, WdkModelException {
      super(json, ActionType.values());
      _recordClass = recordClass;
    }

    public BasketActions(ActionType action, Collection<PrimaryKeyValue> records)
        throws DataValidationException {
      super(ActionType.values());
      _recordClass = null;
      setAction(action);
      setIdentifiers(records);
    }

    @Override
    protected ActionType parseAction(Object obj) throws DataValidationException {
      return ActionType.fromString(obj.toString())
          .orElseThrow(DataValidationException::new);
    }

    @Override
    protected PrimaryKeyValue parsePrimaryKey(JsonType obj)
        throws DataValidationException, WdkModelException {
      return RecordRequest.parsePrimaryKey(obj.getJSONArray(), _recordClass);
    }
  }

}
