package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerFilterLayout;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.ReporterRef;
import org.gusdb.wdk.model.SummaryView;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

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

    public QuestionBean[] getQuestions() {
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

    public AttributeFieldBean getPrimaryKeyAttribute() {
    	return new AttributeFieldBean(recordClass.getPrimaryKeyAttributeField());
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

    public boolean getHasBasket() {
        return recordClass.hasBasket();
    }

    public QuestionBean[] getTransformQuestions() {
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

    public SummaryView getDefaultRecordView() {
        return recordClass.getDefaultRecordView();
    }   

    public Map<String, SummaryView> getRecordViews() {
        return recordClass.getRecordViews();
    } 
}
