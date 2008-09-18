package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.StringParam;
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

    private static final Logger logger = Logger.getLogger(BooleanQueryInstance.class);
    
    private BooleanQuery booleanQuery;

    /**
     * @param query
     * @param values
     * @throws WdkModelException
     */
    protected BooleanQueryInstance(BooleanQuery query,
            Map<String, Object> values) throws WdkModelException {
        super(query, values);
        this.booleanQuery = query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.SqlQueryInstance#getUncachedSql()
     */
    @Override
    public String getUncachedSql() throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {

        // needs to apply the view to each operand before boolean
        StringBuffer sql = new StringBuffer();

        // get the use_boolean filter param
        StringParam useBooleanFilter = booleanQuery.getUseBooleanFilter();
        String strBooleanFlag = (String) values.get(useBooleanFilter.getName());
        boolean booleanFlag = Boolean.parseBoolean(strBooleanFlag);
        
        logger.info("Boolean expansion flag: " + booleanFlag);

        // construct the filter query for the first child
        AnswerParam leftParam = booleanQuery.getLeftOperandParam();
        String leftChecksum = (String) values.get(leftParam.getName());
        String leftSql = constructOperandSql(leftParam, leftChecksum,
                booleanFlag);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        String rightChecksum = (String) values.get(rightParam.getName());
        String rightSql = constructOperandSql(rightParam, rightChecksum,
                booleanFlag);

        Object operator = values.get(booleanQuery.getOperatorParam().getName());
        BooleanOperator op = BooleanOperator.parse((String) operator);

        if (op == BooleanOperator.RIGHT_MINUS) {
            sql.append("(").append(rightSql).append(") ");
            sql.append(op.getOperator());
            sql.append(" (").append(leftSql).append(")");
        } else {
            sql.append("(").append(leftSql).append(")");
            sql.append(op.getOperator());
            sql.append("(").append(rightSql).append(")");
        }

        return sql.toString();
    }

    private String constructOperandSql(AnswerParam answerParam,
            String answerChecksum, boolean booleanFlag)
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        RecordClass recordClass = booleanQuery.getRecordClass();

        // create a template sql, and use answerParam to do the replacement
        String innerSql = "$$" + answerParam.getName() + "$$";
        innerSql = answerParam.replaceSql(innerSql, answerChecksum);

        // apply the filter query if needed
        if (booleanFlag && recordClass.getBooleanExpansionFilter() != null)
            innerSql = constructBooleanExpansionSql(innerSql);

        // limit the column oupt
        StringBuffer sql = new StringBuffer("SELECT ");

        // put columns in
        boolean firstColumn = true;
        for (Column column : booleanQuery.getColumns()) {
            if (firstColumn) firstColumn = false;
            else sql.append(", ");
            sql.append(column.getName());
        }
        sql.append(" FROM (").append(innerSql).append(") f");
        return sql.toString();
    }

    private String constructBooleanExpansionSql(String sql)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        RecordClass recordClass = booleanQuery.getRecordClass();
        AnswerFilterInstance filter = recordClass.getBooleanExpansionFilter();
        SqlQuery query = (SqlQuery) filter.getFilterQuery();
        Map<String, Param> params = query.getParamMap();
        AnswerParam answerParam = filter.getAnswerParam();
        Map<String, Object> paramValues = filter.getParamValueMap();

        String filterSql = query.getSql();
        // replace the answer param
        String answerName = answerParam.getName();
        filterSql = filterSql.replaceAll("\\$\\$" + answerName + "\\$\\$", "("
                + sql + ")");

        // replace the rest of the params
        for (Param param : params.values()) {
            String value = (String) paramValues.get(param.getName());
            filterSql = param.replaceSql(filterSql, value);
        }
        
        logger.info("Boolean expanded SQL:\n" + filterSql);
        
        return filterSql;
    }
}