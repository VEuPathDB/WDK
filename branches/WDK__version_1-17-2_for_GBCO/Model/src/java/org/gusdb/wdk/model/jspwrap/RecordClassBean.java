package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.ReporterRef;
import org.gusdb.wdk.model.SubType;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A wrapper on a {@link RecordClass} that provides simplified access for
 * consumption by a view
 */
public class RecordClassBean {

    RecordClass recordClass;

    // the variables used to store the ids for the record instance to be created
    private String projectId;
    private String recordId;

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
        AttributeField[] fields = recordClass.getAttributeFields();
        Map<String, AttributeFieldBean> fieldBeans = new LinkedHashMap<String, AttributeFieldBean>(
                fields.length);
        for (AttributeField field : fields) {
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

    public RecordBean makeRecord(String recordId) {
        try {
            return new RecordBean(recordClass.makeRecordInstance(recordId));
        } catch (WdkModelException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * used by the controller
     */
    public RecordBean makeRecord(String projectId, String recordId) {
        try {
            return new RecordBean(recordClass.makeRecordInstance(projectId,
                    recordId));
        } catch (WdkModelException ex) {
            throw new RuntimeException(ex);
        }
    }

    public QuestionBean[] getQuestions() {

        Question questions[] = recordClass.getQuestions();
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

    /**
     * @param projectId
     *          the projectId to set
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * @param recordId
     *          the recordId to set
     */
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    /**
     * Get the newly created record instance from the project id and primary key
     * set to the class
     * 
     * @return
     */
    public RecordBean getRecord() {
        return makeRecord(projectId, recordId);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.RecordClass#getSubType()
     */
    public SubTypeBean getSubType() {
        SubType subType = recordClass.getSubType();
        if (subType == null) return null;
        else return new SubTypeBean(subType);
    }
    
    public boolean isHasSubType() {
        return (recordClass.getSubType() != null);
    }
}
