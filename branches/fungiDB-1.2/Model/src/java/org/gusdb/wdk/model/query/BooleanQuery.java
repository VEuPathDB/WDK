package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import org.gusdb.wdk.model.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.RecordClassReference;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * BooleanQuery.java
 * 
 * A Query representing the pairing of two other Queries (known as 'boolean
 * operands') in a boolean operation (Union, Intersect, Subtract, etc.). The
 * expected use of a BooleanQuery is as the ID Query for a Question and thus a
 * BooleanQuery is fundamentally tied to other Questions and their Answers. A
 * BooleanQuery has three parameters. Two are AnswerParams, representing Answers
 * to Questions whose Queries are the boolean operands. The Questions must have
 * the same RecordClasses in order to be operands. The third is the operation to
 * be performed which is a StringParam. BooleanQueries are recursive, and thus
 * the operand Queries can themselves be BooleanQueries.
 * 
 * BooleanQueries are used like any other Query by making BooleanQueryInstances;
 * the result is the result of the two operand Queries joined by the operation.
 * BooleanQueries differ from other Queries in that a different one should be
 * used every time a BooleanQuery is run (rather than the normal use of one
 * Query providing many QueryInstances).
 * 
 * Queries need to declare their columns, so a BooleanQuery provides this, but
 * only when a BooleanQueryInstance has been created and its AnswerParameters
 * have been set. The columns of the BooleanQuery then become the Columns of the
 * ID Queries in the Answer's Question. The two Query operands in a BooleanQuery
 * must have the same declared columns.
 * 
 * It is the responsibility of whoever creates a BooleanQuery to set its
 * RDBMSPlatform and ResultFactory (this differs from other Queries whose
 * resources are set by the WdkModel upon instantiation).
 * 
 * Created: Fri May 21 1821:30 EDT 2004
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2007-05-30 14:07:48 -0400 (Wed, 30 May
 *          2007) $ $Author$
 */

public class BooleanQuery extends SqlQuery {

    public static final String QUERY_NAME_PREFIX = "bq_";
    public static final String LEFT_OPERAND_PARAM_PREFIX = "bq_left_op_";
    public static final String RIGHT_OPERAND_PARAM_PREFIX = "bq_right_op_";
    public static final String USE_BOOLEAN_FILTER_PARAM = "use_boolean_filter";

    public static final String OPERATOR_PARAM = "bq_operator";

    public static String getQueryName(RecordClass recordClass) {
        String rcName = recordClass.getFullName().replace('.', '_');
        return QUERY_NAME_PREFIX + rcName;
    }

    private AnswerParam leftOperand;
    private AnswerParam rightOperand;
    private StringParam operator;
    private StringParam useBooleanFilter;
    private RecordClass recordClass;

    public BooleanQuery(RecordClass recordClass) throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException {
        this.recordClass = recordClass;
        this.wdkModel = recordClass.getWdkModel();
        String rcName = recordClass.getFullName().replace('.', '_');

        // create or get the historyParam for the query
        ParamSet internalParamSet =
                wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
        leftOperand =
                prepareOperand(internalParamSet, recordClass,
                        LEFT_OPERAND_PARAM_PREFIX + rcName);
        leftOperand.setPrompt("Left Operand");
        rightOperand =
                prepareOperand(internalParamSet, recordClass,
                        RIGHT_OPERAND_PARAM_PREFIX + rcName);
        rightOperand.setPrompt("Right Operand");

        // create the stringParam for the others
        operator = prepareStringParam(internalParamSet, OPERATOR_PARAM);
        operator.setPrompt("Operator");
        useBooleanFilter =
                prepareStringParam(internalParamSet, USE_BOOLEAN_FILTER_PARAM);
        useBooleanFilter.setPrompt("Use Expand Filter");

        // create the query
        this.setName(BooleanQuery.getQueryName(recordClass));
        this.addParam(leftOperand);
        this.addParam(rightOperand);
        this.addParam(operator);
        this.addParam(useBooleanFilter);

        prepareColumns(recordClass);

        this.setSql(constructSql());
    }

    private BooleanQuery(BooleanQuery query) {
        super(query);

        this.recordClass = query.recordClass;
        this.leftOperand =
                (AnswerParam) paramMap.get(query.leftOperand.getName());
        this.operator = (StringParam) paramMap.get(query.operator.getName());
        this.rightOperand =
                (AnswerParam) paramMap.get(query.rightOperand.getName());
        this.useBooleanFilter =
                (StringParam) paramMap.get(query.useBooleanFilter.getName());
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /**
     * @return the leftOperand
     */
    public AnswerParam getLeftOperandParam() {
        return leftOperand;
    }

    /**
     * @return the rightOperand
     */
    public AnswerParam getRightOperandParam() {
        return rightOperand;
    }

    /**
     * @return the operator
     */
    public StringParam getOperatorParam() {
        return operator;
    }

    /**
     * @return the useBooleanFilter
     */
    public StringParam getUseBooleanFilter() {
        return useBooleanFilter;
    }

    private AnswerParam prepareOperand(ParamSet paramSet,
            RecordClass recordClass, String paramName)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        AnswerParam operand;
        if (paramSet.contains(paramName)) {
            operand = (AnswerParam) paramSet.getParam(paramName);
        } else {
            operand = new AnswerParam();
            operand.setName(paramName);
            String rcName = recordClass.getFullName();
            operand.addRecordClassRef(new RecordClassReference(rcName));
            paramSet.addParam(operand);
            operand.resolveReferences(wdkModel);
            operand.setResources(wdkModel);
        }
        return operand;
    }

    private StringParam prepareStringParam(ParamSet paramSet, String paramName)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        StringParam param;
        if (paramSet.contains(paramName)) {
            param = (StringParam) paramSet.getParam(paramName);
        } else {
            param = new StringParam();
            param.setName(paramName);
            param.setNumber(false);
            param.setNoTranslation(true);
            param.resolveReferences(wdkModel);
            param.setResources(wdkModel);
            paramSet.addParam(param);
        }
        return param;
    }

    private void prepareColumns(RecordClass recordClass) {
        PrimaryKeyAttributeField primaryKey =
                recordClass.getPrimaryKeyAttributeField();

        for (String columnName : primaryKey.getColumnRefs()) {
            Column column = new Column();
            column.setName(columnName);
            column.setQuery(this);
            columnMap.put(columnName, column);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.Query#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsQuery, boolean extra)
            throws JSONException {
        super.appendJSONContent(jsQuery, extra);
        jsQuery.append("recordClass", recordClass.getFullName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#clone()
     */
    @Override
    public Query clone() {
        return new BooleanQuery(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#makeInstance()
     */
    @Override
    public QueryInstance makeInstance(User user, Map<String, String> values,
            boolean validate, int assignedWeight, Map<String, String> context)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        return new BooleanQueryInstance(user, this, values, validate,
                assignedWeight, context);
    }

    private String constructSql() {
        StringBuffer sql = new StringBuffer();
        constructOperandSql(sql, leftOperand.getName());
        sql.append(" $$").append(operator.getName()).append("$$ ");
        constructOperandSql(sql, leftOperand.getName());
        return sql.toString();
    }

    private void constructOperandSql(StringBuffer sql, String operand) {
        sql.append("SELECT ");
        boolean first = true;
        for (String column : columnMap.keySet()) {
            if (first) first = false;
            else sql.append(", ");
            sql.append(column);
        }
        sql.append(" FROM $$").append(operand).append("$$");
        sql.append(" WHERE $$").append(operand).append(".condition$$");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#isBoolean()
     */
    @Override
    public boolean isBoolean() {
        return true;
    }

}
