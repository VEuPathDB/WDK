package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.SummaryTable;
import org.gusdb.wdk.model.SummaryView;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

/**
 * BooleanQueryInstance.java
 * 
 * Instance instantiated by a BooleanQuery. Takes Answers as values for its
 * parameters along with a boolean operation, and returns a result.
 * 
 * Created: Wed May 19 15:11:30 2004
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-09 22:24:36 -0400 (Tue, 09 Aug
 *          2005) $ $Author$
 */

public class BooleanQueryInstance extends SqlQueryInstance {

    private BooleanQuery booleanQuery;
    private SummaryView booleanView;

    /**
     * @param query
     * @param values
     * @throws WdkModelException
     */
    protected BooleanQueryInstance(BooleanQuery query,
            Map<String, Object> values) throws WdkModelException {
        super(query, values);
        this.booleanQuery = query;
        // set default view
        booleanView = query.getRecordClass().getDefaultBooleanView();
    }

    public void setBooleanView(String tableName, String row, String column)
            throws WdkModelException {
        RecordClass recordClass = booleanQuery.getRecordClass();
        SummaryTable summaryTable = recordClass.getSummaryTable(tableName);
        booleanView = summaryTable.getView(row, column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.SqlQueryInstance#getUncachedSql()
     */
    @Override
    public String getUncachedSql() throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        if (booleanView == null) return super.getUncachedSql();

        // needs to apply the view to each operand before boolean
        StringBuffer sql = new StringBuffer();

        // construct the filter query for the first child
        Object leftOperand = values.get(booleanQuery.getLeftOperandParam().getName());
        constructOperandSql(sql, leftOperand);

        Object operator = values.get(booleanQuery.getOperatorParam().getName());
        sql.append(" ").append(operator).append(" ");

        Object rightOperand = values.get(booleanQuery.getRightOperandParam().getName());
        constructOperandSql(sql, rightOperand);

        return sql.toString();
    }

    private void constructOperandSql(StringBuffer sql, Object answerChecksum)
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        // prepare the filter query
        SummaryTable summaryTable = booleanView.getSummaryTable();
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(summaryTable.getRowParam().getName(),
                booleanView.getRowTerm());
        params.put(summaryTable.getColumnParam().getName(),
                booleanView.getColumnTerm());
        params.put(booleanView.getAnswerParam().getName(), answerChecksum);
        Query query = booleanView.getSummaryQuery();
        QueryInstance instance = query.makeInstance(params);
        String subSql = instance.getSql();

        sql.append("SELECT ");
        boolean first = true;
        for (Column column : this.booleanQuery.getColumns()) {
            if (first) first = false;
            else sql.append(", ");
            sql.append(column.getName());
        }
        sql.append(" FROM (").append(subSql).append(") f");
    }
}