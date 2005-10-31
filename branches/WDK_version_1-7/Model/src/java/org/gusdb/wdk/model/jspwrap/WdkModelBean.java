package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.RecordClass;

import java.util.Map;
import java.util.Vector;
import java.util.LinkedHashMap;
import java.util.Iterator;

/**
 * A wrapper on a {@link WdkModel} that provides simplified access for
 * consumption by a view
 */
public class WdkModelBean {

    WdkModel model;

    public WdkModelBean(WdkModel model) {
        this.model = model;
    }

    public String getName() {
        return model.getName();
    }

    public String getDisplayName() {
        return model.getDisplayName();
    }

    public String getIntroduction() {
        return model.getIntroduction();
    }

    public EnumParamBean getBooleanOps() {
        return new EnumParamBean(model.getBooleanOps());
    }

    // to do: figure out how to do this without using getModel()
    public WdkModel getModel() {
        return this.model;
    }

    /**
     * used by the controller
     */
    public RecordClassBean findRecordClass(String recClassRef)
            throws WdkUserException, WdkModelException {
        return new RecordClassBean(model.getRecordClass(recClassRef));
    }

    /**
     * @return Map of questionSetName --> {@link QuestionSetBean}
     */
    public Map getQuestionSetsMap() {
        Map qSets = model.getQuestionSets();
        Iterator it = qSets.keySet().iterator();

        Map qSetBeans = new LinkedHashMap();
        while (it.hasNext()) {
            Object qSetKey = it.next();
            QuestionSetBean qSetBean = new QuestionSetBean(
                    (QuestionSet) qSets.get(qSetKey));
            qSetBeans.put(qSetKey, qSetBean);
        }
        return qSetBeans;
    }

    public QuestionSetBean[] getQuestionSets() {
        Map qSets = model.getQuestionSets();
        Iterator it = qSets.keySet().iterator();

        QuestionSetBean[] qSetBeans = new QuestionSetBean[qSets.size()];
        int i = 0;
        while (it.hasNext()) {
            Object qSetKey = it.next();
            QuestionSetBean qSetBean = new QuestionSetBean(
                    (QuestionSet) qSets.get(qSetKey));
            qSetBeans[i++] = qSetBean;
        }
        return qSetBeans;
    }

    public RecordClassBean[] getRecordClasses() {

        Vector recordClassBeans = new Vector();
        RecordClassSet sets[] = model.getAllRecordClassSets();
        for (int i = 0; i < sets.length; i++) {
            RecordClassSet nextSet = sets[i];
            RecordClass recordClasses[] = nextSet.getRecordClasses();
            for (int j = 0; j < recordClasses.length; j++) {
                RecordClass nextClass = recordClasses[j];
                RecordClassBean bean = new RecordClassBean(nextClass);
                recordClassBeans.addElement(bean);
            }
        }

        RecordClassBean[] returnedBeans = new RecordClassBean[recordClassBeans.size()];
        for (int i = 0; i < recordClassBeans.size(); i++) {
            RecordClassBean nextReturnedBean = (RecordClassBean) recordClassBeans.elementAt(i);
            returnedBeans[i] = nextReturnedBean;
        }
        return returnedBeans;
    }

    public UserBean createUser(String userID) {
        User user = model.createUser(userID);
        return new UserBean(user);
    }

    public UserBean getUser(String userID) {
        User user = model.getUser(userID);
        return new UserBean(user);
    }

    public boolean deleteUser(String userID) {
        return model.deleteUser(userID);
    }
}
