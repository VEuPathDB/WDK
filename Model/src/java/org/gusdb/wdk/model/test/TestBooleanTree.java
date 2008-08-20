package org.gusdb.wdk.model.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;

/**
 * TestBooleanTree.java
 * 
 * Class with static method that returns a tree of BooleanQuestionNodes
 * containing Questions and parameter values specifically designed for the toy
 * model.
 * 
 * Tree is composed entirely of the RnaQuestions.ByNumSeqs Questions and looks
 * like this:
 * 
 * q1 minus / \ q2union q3union / \ / \ q4 q5 q6 q7 leaf nodes
 * 
 * (Update this picture if changing any relevant data below).
 * 
 * Created: Fri 22 October 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2006-04-05 09:50:16 -0400 (Wed, 05 Apr
 * 2006) $Author$
 */

public class TestBooleanTree {

    // ------------------------------------------------------------------
    // Static method
    // ------------------------------------------------------------------

    /**
     * @param model
     * WdkModel representing the Toy Model.
     * 
     * @return Root of tree described above with six other BooleanQuestionNodes
     * in its trees.
     */

    static BooleanQuestionNode getTestTree(WdkModel model)
            throws WdkModelException, WdkUserException {

        // leaf nodes
        Question q4 = makeNumSeqsQuestion(model);
        Map<String, Object> q4values = makeNumSeqsValues("5", "Eimeria tenella");

        Question q5 = makeNumSeqsQuestion(model);
        Map<String, Object> q5values = makeNumSeqsValues("5",
                "Neospora caninum");

        Question q6 = makeNumSeqsQuestion(model);
        Map<String, Object> q6values = makeNumSeqsValues("25",
                "Eimeria tenella");

        Question q7 = makeNumSeqsQuestion(model);
        Map<String, Object> q7values = makeNumSeqsValues("25",
                "Neospora caninum");

        // boolean nodes
        Question q2 = model.getBooleanQuestion(q7.getRecordClass());
        Map<String, Object> q2values = makeBooleanValues("Union", model);

        Question q3 = model.getBooleanQuestion(q7.getRecordClass());
        Map<String, Object> q3values = makeBooleanValues("Union", model);

        // root boolean node
        Question q1 = model.getBooleanQuestion(q7.getRecordClass());
        Map<String, Object> q1values = makeBooleanValues("Minus", model);
        // Hashtable q1values = makeBooleanValues("Union", model);

        BooleanQuestionNode bqn4 = new BooleanQuestionNode(q4, null);
        BooleanQuestionNode bqn5 = new BooleanQuestionNode(q5, null);
        BooleanQuestionNode bqn6 = new BooleanQuestionNode(q6, null);
        BooleanQuestionNode bqn7 = new BooleanQuestionNode(q7, null);

        BooleanQuestionNode bqn3 = new BooleanQuestionNode(q3, bqn6, bqn7, null);
        BooleanQuestionNode bqn2 = new BooleanQuestionNode(q2, bqn5, bqn3, null);
        BooleanQuestionNode bqn1 = new BooleanQuestionNode(q1, bqn2, bqn4, null);

        bqn1.setValues(q1values);
        bqn2.setValues(q2values);
        bqn3.setValues(q3values);
        bqn4.setValues(q4values);
        bqn5.setValues(q5values);
        bqn6.setValues(q6values);
        bqn7.setValues(q7values);

        return bqn1;
    }

    public static BooleanQuestionNode makeNewLeafNode(WdkModel model)
            throws WdkUserException, WdkModelException {

        Question q4 = makeNumSeqsQuestion(model);
        Map<String, Object> q4values = makeNumSeqsValues("7", "Eimeria tenella");
        BooleanQuestionNode bqn = new BooleanQuestionNode(q4, null);
        bqn.setValues(q4values);
        return bqn;

    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    private static Map<String, Object> makeBooleanValues(String operation,
            WdkModel model) {
        // need to translate the operators
        if (operation.equalsIgnoreCase("Minus")
                || operation.equalsIgnoreCase("Except"))
            operation = model.getQueryPlatform().getMinusOperator();
        Map<String, Object> h = new LinkedHashMap<String, Object>();
        h.put(BooleanQuery.OPERATOR_PARAM, operation);
        return h;
    }

    // this method is never used locally
    // private static Question makeBooleanQuestion(WdkModel model, RecordClass
    // rc){
    //	
    // Question q = new Question();
    // q.setName("BooleanQuestion");
    // q.setRecordClass(rc);
    // BooleanQuery bq = model.makeBooleanQuery();
    //	
    // q.setQuery(bq);
    // return q;
    // }

    private static Question makeNumSeqsQuestion(WdkModel model)
            throws WdkUserException, WdkModelException {

        Reference numSeqsRef = new Reference("RnaQuestions.ByNumSeqs");
        Question q = model.getQuestionSet(numSeqsRef.getSetName()).getQuestion(
                numSeqsRef.getElementName());
        return q;
    }

    private static Map<String, Object> makeNumSeqsValues(String numSeqs,
            String taxonName) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("NumSeqs", numSeqs);
        values.put("ApiTaxon", taxonName);
        return values;
    }
}
