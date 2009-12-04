package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.BasketFactory;

public class ProcessBasketQuestionAction extends Action {

    private static final String PARAM_RECORD_CLASS = "recordClass";
    private static final String MAPKEY_PROCESS_QUESTION = "recordClass";

    private static Logger logger = Logger.getLogger(ShowBasketAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessBasketQuestionAction...");

        UserBean user = ActionUtility.getUser(servlet, request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String rcName = request.getParameter(PARAM_RECORD_CLASS);
            RecordClassBean recordClass = wdkModel.findRecordClass(rcName);
            QuestionBean question = recordClass.getSnapshotBasketQuestion();
            String dsParam = recordClass.getFullName().replace('.', '_')
                    + BasketFactory.PARAM_DATASET_SUFFIX;
            DatasetBean dataset = getDataset(user, recordClass);

            ActionForward forward = mapping.findForward(MAPKEY_PROCESS_QUESTION);
            StringBuffer url = new StringBuffer(forward.getPath());
            url.append("?" + CConstants.QUESTION_FULLNAME_PARAM);
            url.append("=" + question.getFullName());
            url.append(URLEncoder.encode("&myProp(" + dsParam + ")=", "UTF-8"));
            url.append(dataset.getUserDatasetId());
            Enumeration<?> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = (String) paramNames.nextElement();
                if (paramName.equals(PARAM_RECORD_CLASS)) continue;
                String paramValue = request.getParameter(paramName);
                url.append("&" + paramName + "=" + paramValue);
            }
            forward = new ActionForward(url.toString());
            forward.setRedirect(false);
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.debug("Leaving ProcessBasketQuestionAction...");
        }
    }

    private DatasetBean getDataset(UserBean user, RecordClassBean recordClass)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException {
        List<String[]> records = user.getBasket(recordClass);
        return user.createDataset(recordClass, null, records);
    }
}
