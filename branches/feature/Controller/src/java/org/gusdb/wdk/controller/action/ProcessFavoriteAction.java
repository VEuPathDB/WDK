/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         the actions for shopping basket include following:
 *         <ul>
 *         <li>add: add a list of records of a given type into basket;</li>
 *         <li>remove: remove a list of records of a given type into basket;</li>
 *         <li>add-all: add all records from a step into basket;</li>
 *         <li>remove-all:: remove all records from a step into basket;</li>
 *         <li>clear: remove all records from basket of a given type;</li>
 *         </ul>
 */
public class ProcessFavoriteAction extends Action {

    /**
     * the action to favorite
     */
    private static final String PARAM_ACTION = "action";
    /**
     * the record type of the favorite. It is a full recordClass name
     */
    private static final String PARAM_TYPE = "type";
    /**
     * the data for the corresponding action. It can be a JSON list of primary
     * keys, or a step display id.
     */
    private static final String PARAM_DATA = "data";

    /**
     * add a list of ids into favorite. It requires TYPE & DATA params, and DATA
     * is a JSON list of primary keys.
     */
    private static final String ACTION_ADD = "add";
    /**
     * remove a list of ids from favorite. It requires TYPE & DATA params, and
     * DATA is a JSON list of primary keys.
     */
    private static final String ACTION_REMOVE = "remove";
    /**
     * clear the favorite. it doesn't require any param.
     */
    private static final String ACTION_CLEAR = "clear";

    private static final Logger logger = Logger.getLogger(ProcessFavoriteAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessFavoriteAction...");

        UserBean user = ActionUtility.getUser(servlet, request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        String action = request.getParameter(PARAM_ACTION);
        if (action.equalsIgnoreCase(ACTION_ADD)) {
            // need type & data params, where data is a JSON list of record ids
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            List<String[]> records = getRecords(request, recordClass);
            user.addToFavorite(recordClass, records);
        } else if (action.equalsIgnoreCase(ACTION_REMOVE)) {
            // need type & data params, where data is a JSON list of record ids
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            List<String[]> records = getRecords(request, recordClass);
            user.removeFromFavorite(recordClass, records);
        } else if (action.equalsIgnoreCase(ACTION_CLEAR)) {
            // doesn't need any param, will remove all favorites
            user.clearFavorite();
        } else {
            throw new WdkUserException("Unknown Favorite operation: '" + action
                    + "'.");
        }

        logger.debug("Leaving ProcessFavoriteAction...");
        return null;
    }

    private RecordClassBean getRecordClass(HttpServletRequest request,
            WdkModelBean wdkModel) throws WdkUserException, WdkModelException {
        // get recordClass
        String type = request.getParameter(PARAM_TYPE);
        return wdkModel.findRecordClass(type);
    }

    private List<String[]> getRecords(HttpServletRequest request,
            RecordClassBean recordClass) throws JSONException, WdkUserException {
        String data = request.getParameter(PARAM_DATA);
        if (data == null)
            throw new WdkUserException("the record ids list is invalid: '"
                    + data + "'.");

        String[] pkColumns = recordClass.getPrimaryKeyColumns();
        JSONArray array = new JSONArray(data);
        List<String[]> ids = new ArrayList<String[]>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            String[] values = new String[pkColumns.length];
            for (int j = 0; j < values.length; j++) {
                values[j] = object.getString(pkColumns[j]);
            }
            ids.add(values);
        }
        return ids;
    }
}
