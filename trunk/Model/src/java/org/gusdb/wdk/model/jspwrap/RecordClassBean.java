package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerFilterLayout;
import org.gusdb.wdk.model.answer.ReporterRef;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordView;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONException;

/**
 * A wrapper on a {@link RecordClass} that provides simplified access for
 * consumption by a view
 */
public class RecordClassBean {

    RecordClass recordClass;
    private boolean changeType = true;

    public RecordClassBean(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    public String getFullName() {
        return recordClass.getFullName();
    }

    public String getName() {
        return recordClass.getName();
    }

    public String getType() {
        return recordClass.getType();
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map<String, AttributeFieldBean> getAttributeFields() {
        Map<String, AttributeField> fields = recordClass.getAttributeFieldMap();
        Map<String, AttributeFieldBean> fieldBeans = new LinkedHashMap<String, AttributeFieldBean>(
                fields.size());
        for (AttributeField field : fields.values()) {
            fieldBeans.put(field.getName(), new AttributeFieldBean(field));
        }
        return fieldBeans;
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map<String, TableFieldBean> getTableFields() {
        TableField[] fields = recordClass.getTableFields();
        Map<String, TableFieldBean> fieldBeans = new LinkedHashMap<String, TableFieldBean>(
                fields.length);
        for (TableField field : fields) {
            fieldBeans.put(field.getName(), new TableFieldBean(field));
        }
        return fieldBeans;
    }

    public QuestionBean[] getQuestions() throws WdkModelException {
        WdkModel wdkModel = recordClass.getWdkModel();
        Question questions[] = wdkModel.getQuestions(recordClass);
        QuestionBean[] questionBeans = new QuestionBean[questions.length];
        for (int i = 0; i < questions.length; i++) {
            questionBeans[i] = new QuestionBean(questions[i]);
        }
        return questionBeans;
    }

    public Map<String, String> getReporters() {
        Map<String, ReporterRef> reporterMap = recordClass.getReporterMap();
        Map<String, String> reporters = new LinkedHashMap<String, String>();
        for (String name : reporterMap.keySet()) {
            ReporterRef ref = reporterMap.get(name);
            if (ref.isInReportMaker())
                reporters.put(name, ref.getDisplayName());
        }
        return reporters;
    }

    public PrimaryKeyAttributeFieldBean getPrimaryKeyAttribute() {
    	return new PrimaryKeyAttributeFieldBean(recordClass.getPrimaryKeyAttributeField());
    }
    
    public String[] getPrimaryKeyColumns() {
        return recordClass.getPrimaryKeyAttributeField().getColumnRefs();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.RecordClass#getFilterLayoutMap()
     */
    public Map<String, AnswerFilterLayoutBean> getFilterLayoutMap() {
        Map<String, AnswerFilterLayout> layouts = recordClass.getFilterLayoutMap();
        Map<String, AnswerFilterLayoutBean> beans = new LinkedHashMap<String, AnswerFilterLayoutBean>();
        for (String name : layouts.keySet()) {
            AnswerFilterLayout layout = layouts.get(name);
            beans.put(name, new AnswerFilterLayoutBean(layout));
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.RecordClass#getFilterLayouts()
     */
    public AnswerFilterLayoutBean[] getFilterLayouts() {
        AnswerFilterLayout[] layouts = recordClass.getFilterLayouts();
        AnswerFilterLayoutBean[] beans = new AnswerFilterLayoutBean[layouts.length];
        for (int i = 0; i < layouts.length; i++) {
            beans[i] = new AnswerFilterLayoutBean(layouts[i]);
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.RecordClass#getFilterMap()
     */
    public Map<String, AnswerFilterInstanceBean> getFilterMap() {
        Map<String, AnswerFilterInstance> instances = recordClass.getFilterMap();
        Map<String, AnswerFilterInstanceBean> beans = new LinkedHashMap<String, AnswerFilterInstanceBean>();
        for (String name : instances.keySet()) {
            AnswerFilterInstance instance = instances.get(name);
            beans.put(name, new AnswerFilterInstanceBean(instance));
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.RecordClass#getFilters()
     */
    public AnswerFilterInstanceBean[] getFilters() {
        AnswerFilterInstance[] instances = recordClass.getFilters();
        AnswerFilterInstanceBean[] beans = new AnswerFilterInstanceBean[instances.length];
        for (int i = 0; i < instances.length; i++) {
            beans[i] = new AnswerFilterInstanceBean(instances[i]);
        }
        return beans;
    }

    public AnswerFilterInstanceBean getFilter(String filterName)
            throws WdkModelException {
        AnswerFilterInstance instance = recordClass.getFilter(filterName);
        return new AnswerFilterInstanceBean(instance);
    }

    public QuestionBean getRealtimeBasketQuestion() throws WdkModelException {
        return new QuestionBean(recordClass.getRealtimeBasketQuestion());
    }

    public QuestionBean getSnapshotBasketQuestion() throws WdkModelException {
        return new QuestionBean(recordClass.getSnapshotBasketQuestion());
    }

    public boolean isUseBasket() {
        return recordClass.isUseBasket();
    }

    public QuestionBean[] getTransformQuestions() throws WdkModelException {
        Question[] questions = recordClass.getTransformQuestions(changeType);
        QuestionBean[] beans = new QuestionBean[questions.length];
        for (int i = 0; i < questions.length; i++) {
            beans[i] = new QuestionBean(questions[i]);
        }
        return beans;
    }

    public void setChangeType(boolean changeType) {
        this.changeType = changeType;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.RecordClass#getDisplayName()
     */
    public String getDisplayName() {
        return recordClass.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.RecordClass#getShortDisplayName()
     */
    public String getShortDisplayName() {
        return recordClass.getShortDisplayName();
    }

    public RecordView getDefaultRecordView() {
        return recordClass.getDefaultRecordView();
    }   

    public Map<String, RecordView> getRecordViews() {
        return recordClass.getRecordViews();
    } 

    public AttributeFieldBean getFavoriteNoteField() {
        return new AttributeFieldBean(recordClass.getFavoriteNoteField());
    }

    /**
     * @param user
     * @param pkValues
     * @return
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     * @see org.gusdb.wdk.model.RecordClass#hasMultipleRecords(org.gusdb.wdk.model.user.User, java.util.Map)
     */
    public boolean hasMultipleRecords(UserBean user, Map<String, Object> pkValues)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        return recordClass.hasMultipleRecords(user.getUser(), pkValues);
    }
    
    
}
