package org.gusdb.wdk.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Hashtable;

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
	
	public JUnitSqlUtilsTester(){
		String GUS_HOME = System.getenv("GUS_HOME");
		if(GUS_HOME == null) GUS_HOME = "/home/samwzm/WDK/WDK_WEB/GUS_HOME";
		System.setProperty("cmdName", "wdkSummaryTest");
		System.setProperty("GUS_HOME", GUS_HOME);
		System.setProperty("configDir", GUS_HOME + "/config/apidb_lime");
		System.setProperty("schemaFile", GUS_HOME + "/lib/rng/wdkModel.rng");
		System.setProperty("xmlSchemaFile", GUS_HOME + "/lib/xml/wdkModelSchema.xsd");
		System.setProperty("xmlDataDir", GUS_HOME + "/lib/xml");
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
	
	public void testSummary(){
		//String commandLine ="-model model -question GeneQuestions.GeneByAnnotatedKeyword -rows 1 70000 -params organism \"ALL\" keyword \"phosphoenolpyruvate\"";
		JUnitSqlUtilsTester tester = new JUnitSqlUtilsTester();
		String result = tester.getResult();
		System.out.println(result);
		assertNotNull(result);
	}
	
	public static void main(String[] args){
		JUnitSqlUtilsTester tester = new JUnitSqlUtilsTester();
		System.out.println(tester.getResult());
	}
	
	private static String getResult(){
		StringBuffer result = new StringBuffer("");
        try {
            Reference ref = new Reference(questionFullName);
            String questionSetName = ref.getSetName();
            String questionName = ref.getElementName();
            WdkModel wdkModel = WdkModel.construct(modelName);

            QuestionSet questionSet = wdkModel.getQuestionSet(questionSetName);
            Question question = questionSet.getQuestion(questionName);

            Hashtable paramValues = new Hashtable();
            paramValues = parseParamArgs(params);

            int pageCount = 1;


            for (int i = 0; i < rows.length; i += 2) {
                int nextStartRow = Integer.parseInt(rows[i]);
                int nextEndRow = Integer.parseInt(rows[i + 1]);

                Answer answer = question.makeAnswer(paramValues, nextStartRow,
                        nextEndRow);


                if (rows.length != 2) System.out.println("page " + pageCount);

                //System.out.println(answer.printAsRecords());
                //System.out.println(answer.printAsTable());
                String partResult = answer.printAsTable();
                if(partResult != null) result.append( partResult);

                pageCount++;
            }
        } catch (WdkUserException e) {
            System.err.println(e.formatErrors());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            //System.exit(1);
        }
        
        return result.toString();
	}
	
    static Hashtable<String, String> parseParamArgs(String[] params) {

        Hashtable<String, String> h = new Hashtable<String, String>();
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
