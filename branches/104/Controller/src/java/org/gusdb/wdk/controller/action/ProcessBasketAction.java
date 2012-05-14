/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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
import org.gusdb.wdk.model.jspwrap.StepBean;
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
 *         <li>check: check if given record is already in basket and return boolean;</li>
 *         </ul>
 */
public class ProcessBasketAction extends Action {

    /**
     * the action to shopping basket
     */
    private static final String PARAM_ACTION = "action";
    /**
     * the record type of the basket. It is a full recordClass name
     */
    private static final String PARAM_TYPE = "type";
    /**
     * the data for the corresponding action. It can be a JSON list of primary
     * keys, or a step display id.
     */
    private static final String PARAM_DATA = "data";

    /**
     * add a list of ids into basket. It requires TYPE & DATA params, and DATA
     * is a JSON list of primary keys.
     */
    private static final String ACTION_ADD = "add";
    /**
     * remove a list of ids into basket. It requires TYPE & DATA params, and
     * DATA is a JSON list of primary keys.
     */
    private static final String ACTION_REMOVE = "remove";
    /**
     * Add all records from a step into shopping basket. it requires only DATA
     * param, and DATA is a step display id.
     */
    private static final String ACTION_ADD_ALL = "add-all";
    /**
     * remove all records from a step into shopping basket. it requires only
     * DATA param, and DATA is a step display id.
     */
    private static final String ACTION_REMOVE_ALL = "remove-all";
    /**
     * clear the shopping basket. it requires only TYPE param, and TYPE is the
     * full recordClass name.
     */
    private static final String ACTION_CLEAR = "clear";

    private static final String ACTION_CHECK = "check";
    
    private static final Logger logger = Logger.getLogger(ProcessBasketAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessBasketAction...");

        try {
        UserBean user = ActionUtility.getUser(servlet, request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        String action = request.getParameter(PARAM_ACTION);
        int numProcessed = 0;
        if (action.equalsIgnoreCase(ACTION_ADD)) {
            // need type & data params, where data is a JSON list of record ids
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            List<String[]> records = getRecords(request, recordClass);
            user.addToBasket(recordClass, records);
        } else if (action.equalsIgnoreCase(ACTION_REMOVE)) {
            // need type & data params, where data is a JSON list of record ids
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            List<String[]> records = getRecords(request, recordClass);
            user.removeFromBasket(recordClass, records);
        } else if (action.equalsIgnoreCase(ACTION_ADD_ALL)) {
            // only need the data param, and it is a step display id
            StepBean step = getStep(request, user);
            user.addToBasket(step);
        } else if (action.equalsIgnoreCase(ACTION_REMOVE_ALL)) {
            // only need the data param, and it is a step display id
            StepBean step = getStep(request, user);
            user.removeFromBasket(step);
        } else if (action.equalsIgnoreCase(ACTION_CLEAR)) {
            // only need the type param, and it is the recordClass full name
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            user.clearBasket(recordClass);
        } else if (action.equalsIgnoreCase(ACTION_CHECK)) {
        	RecordClassBean recordClass = getRecordClass(request, wdkModel);
        	List<String[]> records = getRecords(request, recordClass);
        	numProcessed = user.getBasketCount(records, recordClass);
        } else {
            throw new WdkUserException("Unknown Basket operation: '" + action
                    + "'.");
        }

        // output the total count
        int count = user.getBasketCount();
        JSONObject jsMessage = new JSONObject();
        jsMessage.put("count", count);
        jsMessage.put("countProcessed", numProcessed);
        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());

        logger.debug("Leaving ProcessBasketAction...");
        return null;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        }
    }

    private RecordClassBean getRecordClass(HttpServletRequest request,
            WdkModelBean wdkModel) throws WdkUserException, WdkModelException {
        // get recordClass
        String type = request.getParameter(PARAM_TYPE);
        return wdkModel.findRecordClass(type);
    }

    private StepBean getStep(HttpServletRequest request, UserBean user)
            throws WdkUserException, NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException {
        // get the step from step id
        String data = request.getParameter(PARAM_DATA);
        if (data == null || !data.matches("^\\d+$"))
            throw new WdkUserException("The content for '" + PARAM_DATA
                    + "' is not a valid step display id: '" + data + "'.");

        int stepId = Integer.parseInt(data);
        return user.getStep(stepId);
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
