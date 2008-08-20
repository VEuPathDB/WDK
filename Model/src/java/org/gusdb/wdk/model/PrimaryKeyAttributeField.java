package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PrimaryKeyAttributeField extends AttributeField {

    private RecordClass recordClass;

    private List<WdkModelText> columnRefList = new ArrayList<WdkModelText>();
    private Set<String> columnRefSet = new LinkedHashSet<String>();

    private List<WdkModelText> textList = new ArrayList<WdkModelText>();
    private String text;

    public PrimaryKeyAttributeField() {
        // add project id into the column list
        columnRefSet.add(Utilities.COLUMN_PROJECT_ID);
    }

    public void addColumnRef(WdkModelText columnRef) {
        this.columnRefList.add(columnRef);
    }

    public String[] getColumnRefs() {
        String[] array = new String[columnRefSet.size()];
        columnRefSet.toArray(array);
        return array;
    }

    public void addText(WdkModelText text) {
        this.textList.add(text);
    }

    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Field#setRecordClass(org.gusdb.wdk.model.RecordClass)
     */
    @Override
    public void setRecordClass(RecordClass recordClass) {
        super.setRecordClass(recordClass);
        this.recordClass = recordClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude columnRefs
        for (WdkModelText columnRef : columnRefList) {
            if (columnRef.include(projectId)) {
                columnRef.excludeResources(projectId);
                String columnName = columnRef.getText();

                // skip the project id, since it's already in the set
                if (columnName.equals(Utilities.COLUMN_PROJECT_ID)) continue;

                if (columnRefSet.contains(columnName)) {
                    throw new WdkModelException("The columnRef " + columnRef
                            + " is duplicated in primaryKetAttribute in "
                            + "recordClass " + recordClass.getFullName());
                } else columnRefSet.add(columnName);
            }
        }
        columnRefList = null;
        if (columnRefSet.size() == 0)
            throw new WdkModelException("No primary key column defined in "
                    + "recordClass " + recordClass.getFullName());

        // exclude format
        for (WdkModelText text : textList) {
            if (text.include(projectId)) {
                text.excludeResources(projectId);
                this.text = text.getText();
                break;
            }
        }
        textList = null;
        if (text == null)
            throw new WdkModelException("No primary key format string defined"
                    + " in recordClass " + recordClass.getFullName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Field#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // do nothing. the columns will be verified by each attribute query
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributeField#getDependents()
     */
    @Override
    public Collection<ColumnAttributeField> getDependents() {
        return new ArrayList<ColumnAttributeField>();
    }
}
