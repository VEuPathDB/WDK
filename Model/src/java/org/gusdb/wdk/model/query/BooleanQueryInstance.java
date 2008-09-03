package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.gusdb.wdk.model.user.AnswerInfo;
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

        // construct the filter query for the first child
        AnswerParam leftParam = booleanQuery.getLeftOperandParam();
        String leftChecksum = (String)values.get(leftParam.getName());
        String leftFilter = (String)values.get(booleanQuery.getLeftFilterParam().getName());
        constructOperandSql(sql, leftParam, leftChecksum, leftFilter);

        Object operator = values.get(booleanQuery.getOperatorParam().getName());
        sql.append(" ").append(operator).append(" ");

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        String rightChecksum = (String)values.get(rightParam.getName());
        String rightFilter = (String)values.get(booleanQuery.getRightFilterParam().getName());
        constructOperandSql(sql, rightParam, rightChecksum, rightFilter);

        return sql.toString();
    }

    private void constructOperandSql(StringBuffer sql, AnswerParam answerParam,
            String answerChecksum, String filterName)
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        // put columns in
        boolean firstColumn = true;
        for (Column column : booleanQuery.getColumns()) {
            if (firstColumn) firstColumn = false;
            else sql.append(", ");
            sql.append(column.getName());
        }

        // put from & where clause
        sql.append(" FROM ");
        if (filterName != null) {
            // use a filter
            AnswerFactory answerFactory = booleanQuery.getWdkModel().getAnswerFactory();
            AnswerInfo answerInfo = answerFactory.getAnswerInfo(answerChecksum);
            Answer answer = answerFactory.getAnswer(answerInfo);
            RecordClass recordClass = booleanQuery.getRecordClass();
            AnswerFilterInstance filter = recordClass.getFilter(filterName);
            QueryInstance instance = filter.makeQueryInstance(answer);
            sql.append("(").append(instance.getSql()).append(") f"); 
        } else {
            // do not use a filter
            String clause = "$$" + answerParam.getName() + "$$ WHERE $$"
                    + answerParam.getName() + ".condition$$";
            clause = answerParam.replaceSql(clause, answerChecksum);
            sql.append(clause);
        }
    }
}