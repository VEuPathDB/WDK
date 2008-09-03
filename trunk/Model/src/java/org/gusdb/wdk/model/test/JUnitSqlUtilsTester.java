package org.gusdb.wdk.model.test;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;

public class JUnitSqlUtilsTester extends TestCase {

    private static String questionFullName;
    private static String[] params = null;
    private static String[] rows = null;
    private static String modelName;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public JUnitSqlUtilsTester() {
        modelName = "model";
        questionFullName = "GeneQuestions.GeneByAnnotatedKeyword";
        params = new String[4];
        params[0] = "organism";
        params[1] = "ALL";
        params[2] = "keyword";
        params[3] = "phosphoenolpyruvate";
        rows = new String[2];
        rows[0] = "1";
        rows[1] = "1000";
    }

    public void testSummary() {
        // String commandLine ="-model model -question
        // GeneQuestions.GeneByAnnotatedKeyword -rows 1 70000 -params organism
        // \"ALL\" keyword \"phosphoenolpyruvate\"";
        String result = JUnitSqlUtilsTester.getResult();
        System.out.println(result);
        assertNotNull(result);
    }

    public static void main(String[] args) {
        System.out.println(JUnitSqlUtilsTester.getResult());
    }

    private static String getResult() {
        StringBuffer result = new StringBuffer("");
        try {
            Reference ref = new Reference(questionFullName);
            String questionSetName = ref.getSetName();
            String questionName = ref.getElementName();
            WdkModel wdkModel = WdkModel.construct(modelName);

            QuestionSet questionSet = wdkModel.getQuestionSet(questionSetName);
            Question question = questionSet.getQuestion(questionName);

            Map<String, Object> paramValues = parseParamArgs(params);

            int pageCount = 1;

            for (int i = 0; i < rows.length; i += 2) {
                int nextStartRow = Integer.parseInt(rows[i]);
                int nextEndRow = Integer.parseInt(rows[i + 1]);

                Answer answer = question.makeAnswer(paramValues, nextStartRow,
                        nextEndRow, question.getDefaultSortingAttributes(),
                        null);

                if (rows.length != 2) System.out.println("page " + pageCount);

                // System.out.println(answer.printAsRecords());
                // System.out.println(answer.printAsTable());
                String partResult = answer.printAsTable();
                if (partResult != null) result.append(partResult);

                pageCount++;
            }
        } catch (WdkUserException e) {
            System.err.println(e.formatErrors());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            // System.exit(1);
        }

        return result.toString();
    }

    static Map<String, Object> parseParamArgs(String[] params) {

        Map<String, Object> h = new LinkedHashMap<String, Object>();
        if (params[0].equals("NONE")) {
            return h;
        } else {
            if (params.length % 2 != 0) {
                throw new IllegalArgumentException(
                        "The -params option must be followed by key value pairs only");
            }
            for (int i = 0; i < params.length; i += 2) {
                h.put(params[i], params[i + 1]);
            }
            return h;
        }
    }

}
