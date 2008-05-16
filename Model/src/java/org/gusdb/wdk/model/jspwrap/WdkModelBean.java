package org.gusdb.wdk.model.jspwrap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;

/**
 * A wrapper on a {@link WdkModel} that provides simplified access for
 * consumption by a view
 */
public class WdkModelBean  {

    WdkModel model;

    public WdkModelBean(WdkModel model) {
        this.model = model;
    }

    @Deprecated
    public Map getProperties() {
        return model.getProperties();
    }
    
    public String getName() {
        return model.getName();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModel#getVersion()
     */
    public String getVersion() {
        return model.getVersion();
    }

    public String getDisplayName() {
        return model.getDisplayName();
    }

    public String getIntroduction() {
        return model.getIntroduction();
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
     * @return Map of recordClassFullName --> Map of question category -->
     *         {array of
     * @link QuestionBean}
     */
    public Map getQuestionsByCategory() {
        Map<String, Map<String, Question[]>> qByCat = model
                .getQuestionsByCategories();

        Map<String, Map<String, QuestionBean[]>> qBeanByCat = new LinkedHashMap<String, Map<String, QuestionBean[]>>();
        Iterator recI = qByCat.keySet().iterator();
        while (recI.hasNext()) {
            String recType = (String) recI.next();
            Map<String, Question[]> recMap = qByCat.get(recType);
            Iterator catI = recMap.keySet().iterator();
            while (catI.hasNext()) {
                String cat = (String) catI.next();
                Question[] questions = recMap.get(cat);

                QuestionBean[] qBeans = new QuestionBean[questions.length];
                for (int i = 0; i < questions.length; i++) {
                    qBeans[i] = new QuestionBean(questions[i]);
                }

                if (null == qBeanByCat.get(recType)) {
                    qBeanByCat.put(recType,
                            new LinkedHashMap<String, QuestionBean[]>());
                }

                qBeanByCat.get(recType).put(cat, qBeans);
            }
        }

        return qBeanByCat;
    }

    /**
     * @return Map of questionSetName --> {@link QuestionSetBean}
     */
    public Map getQuestionSetsMap() {
        Map qSets = model.getQuestionSets();
        Iterator it = qSets.keySet().iterator();

        Map<String, QuestionSetBean> qSetBeans = new LinkedHashMap<String, QuestionSetBean>();
        while (it.hasNext()) {
            String qSetKey = (String) it.next();
            QuestionSetBean qSetBean = new QuestionSetBean((QuestionSet) qSets
                    .get(qSetKey));
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
            QuestionSetBean qSetBean = new QuestionSetBean((QuestionSet) qSets
                    .get(qSetKey));
            qSetBeans[i++] = qSetBean;
        }
        return qSetBeans;
    }

    public RecordClassBean[] getRecordClasses() {

        Vector<RecordClassBean> recordClassBeans = new Vector<RecordClassBean>();
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

        RecordClassBean[] returnedBeans = new RecordClassBean[recordClassBeans
                .size()];
        for (int i = 0; i < recordClassBeans.size(); i++) {
            RecordClassBean nextReturnedBean = recordClassBeans.elementAt(i);
            returnedBeans[i] = nextReturnedBean;
        }
        return returnedBeans;
    }
    
    public Map<String, RecordClassBean> getRecordClassMap() {
        Map<String, RecordClassBean> recordClassMap = new LinkedHashMap<String, RecordClassBean>();
        RecordClassSet[] rcsets = model.getAllRecordClassSets();
        for (RecordClassSet rcset : rcsets) {
            RecordClass[] rcs = rcset.getRecordClasses();
            for (RecordClass rc : rcs) {
                recordClassMap.put(rc.getFullName(), new RecordClassBean(rc));
            }
        }
        return recordClassMap;
    }

    public Map<String, String> getRecordClassTypes() {
        RecordClassBean[] recClasses = getRecordClasses();
        Map<String, String> types = new LinkedHashMap<String, String>();
        for (RecordClassBean r : recClasses) {
            types.put(r.getFullName(), r.getType());
        }
        return types;
    }

    public XmlQuestionSetBean[] getXmlQuestionSets() {
        XmlQuestionSet[] qsets = model.getXmlQuestionSets();
        XmlQuestionSetBean[] qsetBeans = new XmlQuestionSetBean[qsets.length];
        for (int i = 0; i < qsets.length; i++) {
            qsetBeans[i] = new XmlQuestionSetBean(qsets[i]);
        }
        return qsetBeans;
    }

    /**
     * @return Map of questionSetName --> {@link XmlQuestionSetBean}
     */
    public Map getXmlQuestionSetsMap() {
        XmlQuestionSetBean[] qSets = getXmlQuestionSets();
        Map<String, XmlQuestionSetBean> qSetsMap = new LinkedHashMap<String, XmlQuestionSetBean>();
        for (int i = 0; i < qSets.length; i++) {
            qSetsMap.put(qSets[i].getName(), qSets[i]);
        }
        return qSetsMap;
    }

    public XmlRecordClassSetBean[] getXmlRecordClassSets() {
        XmlRecordClassSet[] rcs = model.getXmlRecordClassSets();
        XmlRecordClassSetBean[] rcBeans = new XmlRecordClassSetBean[rcs.length];
        for (int i = 0; i < rcs.length; i++) {
            rcBeans[i] = new XmlRecordClassSetBean(rcs[i]);
        }
        return rcBeans;
    }

    public UserFactoryBean getUserFactory() throws WdkUserException {
        return new UserFactoryBean(model.getUserFactory());
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModel#getBooleanOperators()
     */
    public Map<String, String> getBooleanOperators() {
        return model.getBooleanOperators();
    }

    /**
     * @param paramName
     * @return
     * @see org.gusdb.wdk.model.WdkModel#getParamDisplayName(java.lang.String)
     */
    public String getParamDisplayName( String paramName ) {
        return model.getParamDisplayName( paramName );
    }

    /**
     * @param questionFullName
     * @return
     * @see org.gusdb.wdk.model.WdkModel#getQuestionDisplayName(java.lang.String)
     */
    public String getQuestionDisplayName( String questionFullName ) {
        return model.getQuestionDisplayName( questionFullName );
    }

    public String getProjectId() {
        return model.getProjectId();
    }
}
