/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

/**
 * @author Jerric Gao
 * 
 */
public class SummaryTable extends WdkModelBase {

    private class Cell {

        String row;
        String column;

        Cell(String row, String column) {
            this.row = row;
            this.column = column;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Cell) {
                Cell cell = (Cell) obj;
                return cell.row.equals(row) && cell.column.equals(column);
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return (row.hashCode() ^ column.hashCode());
        }

    }

    private String name;
    private String displayName;
    private String rowParamRef;
    private AbstractEnumParam rowParam;
    private String columnParamRef;
    private AbstractEnumParam columnParam;
    private boolean visible = true;
    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<SummaryView> summaryViewList = new ArrayList<SummaryView>();
    private Map<Cell, SummaryView> summaryViewMap = new LinkedHashMap<Cell, SummaryView>();

    private RecordClass recordClass;

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the rowParam
     */
    public AbstractEnumParam getRowParam() {
        return this.rowParam;
    }

    /**
     * @return the columnParam
     */
    public AbstractEnumParam getColumnParam() {
        return this.columnParam;
    }

    /**
     * @param rowParamRef
     *            the rowParamRef to set
     */
    public void setRowParamRef(String rowParamRef) {
        this.rowParamRef = rowParamRef;
    }

    /**
     * @param columnParamRef
     *            the columnParamRef to set
     */
    public void setColumnParamRef(String columnParamRef) {
        this.columnParamRef = columnParamRef;
    }

    public void addDescription(WdkModelText description) {
        this.descriptionList.add(description);
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /**
     * @param recordClass
     *            the recordClass to set
     */
    public void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    public void addView(SummaryView summaryView) {
        summaryView.setSummaryTable(this);
        this.summaryViewList.add(summaryView);
    }

    public SummaryView[] getViews() {
        SummaryView[] array = new SummaryView[summaryViewMap.size()];
        summaryViewMap.values().toArray(array);
        return array;
    }

    public SummaryView getView(String rowTerm, String columnTerm)
            throws WdkModelException {
        Cell cell = new Cell(rowTerm, columnTerm);
        SummaryView view = summaryViewMap.get(cell);
        if (view == null)
            throw new WdkModelException("The view ['" + rowTerm + "', '"
                    + columnTerm + "'] in summaryTable "
                    + recordClass.getFullName() + "." + name + " doesn't exist");
        return view;
    }

    public SummaryView getDefaultView() {
        for (SummaryView summaryView : summaryViewMap.values()) {
            if (summaryView.isDefault()) return summaryView;
        }
        return null;
    }

    public SummaryView getDefaultBooleanView() {
        for (SummaryView summaryView : summaryViewMap.values()) {
            if (summaryView.isBooleanDefault()) return summaryView;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        for (WdkModelText desc : descriptionList) {
            if (desc.include(projectId)) {
                desc.excludeResources(projectId);
                this.description = desc.getText();
                break;
            }
        }
        descriptionList = null;

        for (SummaryView summaryView : summaryViewList) {
            if (summaryView.include(projectId)) {
                summaryView.excludeResources(projectId);
                String row = summaryView.getRowTerm();
                String column = summaryView.getColumnTerm();
                Cell cell = new Cell(row, column);
                if (summaryViewMap.containsKey(cell))
                    throw new WdkModelException("Summary view [" + row + ", "
                            + column + "] defined more than once.");
                summaryViewMap.put(cell, summaryView);
            }
        }
        summaryViewList = null;

        // summary table must have at least one view
        if (summaryViewMap.size() == 0)
            throw new WdkModelException("Please define at least one view in "
                    + "summaryTable " + name);
    }

    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // resolve the references of row/column params
        rowParam = (AbstractEnumParam) wdkModel.resolveReference(rowParamRef);
        columnParam = (AbstractEnumParam) wdkModel.resolveReference(columnParamRef);

        Map<String, String> rowMap = rowParam.getVocabMap();
        Map<String, String> columnMap = columnParam.getVocabMap();

        // resolve the references
        for (Cell cell : summaryViewMap.keySet()) {
            // check if the cell key is valid
            if (!rowMap.containsKey(cell.row))
                throw new WdkModelException("the row term '" + cell.row
                        + "' in summaryTable " + name + " is invalid.");
            if (!columnMap.containsKey(cell.column))
                throw new WdkModelException("the column term '" + cell.column
                        + "' in summaryTable " + name + " is invalid.");

            // resolve references in the summaryView
            SummaryView summaryView = summaryViewMap.get(cell);
            summaryView.resolveReferences(wdkModel);
        }
    }
}
