/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author xingao
 * 
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

    private static final Logger logger = Logger.getLogger(ProcessBasketAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessBasketAction...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        String action = request.getParameter(PARAM_ACTION);
        if (action.equalsIgnoreCase(ACTION_ADD)) {
            // need type & data params, where data is a JSON list of record ids
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            List<Map<String, String>> records = getRecords(request, recordClass);
            add(wdkUser, recordClass, records);
        } else if (action.equalsIgnoreCase(ACTION_REMOVE)) {
            // need type & data params, where data is a JSON list of record ids
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            List<Map<String, String>> records = getRecords(request, recordClass);
            remove(wdkUser, recordClass, records);
        } else if (action.equalsIgnoreCase(ACTION_ADD_ALL)) {
            // only need the data param, and it is a step display id
            StepBean step = getStep(request, wdkUser);
            addAll(wdkUser, step);
        } else if (action.equalsIgnoreCase(ACTION_REMOVE_ALL)) {
            // only need the data param, and it is a step display id
            StepBean step = getStep(request, wdkUser);
            removeAll(wdkUser, step);
        } else if (action.equalsIgnoreCase(ACTION_CLEAR)) {
            // only need the type param, and it is the recordClass full name
            RecordClassBean recordClass = getRecordClass(request, wdkModel);
            clear(wdkUser, recordClass);
        } else {
            throw new WdkUserException("Unknown Basket operation: '" + action
                    + "'.");
        }

        logger.debug("Leaving ProcessBasketAction...");
        return null;
    }

    private RecordClassBean getRecordClass(HttpServletRequest request,
            WdkModelBean wdkModel) {
        // TODO
        return null;
    }

    private StepBean getStep(HttpServletRequest request, UserBean user) {
        // TODO
        return null;
    }

    private List<Map<String, String>> getRecords(HttpServletRequest request,
            RecordClassBean recordClass) {
        // TODO
        return null;
    }

    private void add(UserBean user, RecordClassBean recordClass,
            List<Map<String, String>> records) {
    // TODO
    }

    private void remove(UserBean user, RecordClassBean recordClass,
            List<Map<String, String>> records) {
    // TODO
    }

    private void addAll(UserBean user, StepBean step) {
    // TODO
    }

    private void removeAll(UserBean user, StepBean step) {

    }

    private void clear(UserBean user, RecordClassBean recordClass) {
    // TODO
    }
}
