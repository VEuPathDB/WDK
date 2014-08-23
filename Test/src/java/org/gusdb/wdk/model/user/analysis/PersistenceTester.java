package org.gusdb.wdk.model.user.analysis;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.user.Step;

/**
 * This class was used to test the step analysis framework during development.
 * It is NOT a unit test and operates on the database configured in the model
 * it is passed.  It will fail without very specific pre-conditions and may
 * corrupt the DB if run independently.  Probably want to convert to real unit
 * tests at some point but tough since it is testing specific SQL.  Need to use
 * hypersonic or some other test DB (which we do not currently have Platform
 * support for).
 * 
 * @author rdoherty
 */
public class PersistenceTester {

  private static final String TEST_USER_EMAIL = "rdoherty@pcbi.upenn.edu";
  private static final String TEST_PLUGIN_NAME = "go-enrichment-dummy";
  private static final int TEST_STEP_ID = 1;
  
  private static final String TEST_CUSTOM_TAB_NAME = "My test name";
  
  private WdkModel _wdkModel;
  
  public PersistenceTester(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  /* Methods tested:
   * 
   * public Map<Integer,StepAnalysisContext> getAnalysesByStepId(int stepId, StepAnalysisFileStore fileStore) throws WdkModelException;
   * public StepAnalysisContext getAnalysisById(int analysisId, StepAnalysisFileStore fileStore) throws WdkModelException {
   * public int getNextId() throws WdkModelException;
   * public void insertAnalysis(int analysisId, int stepId, String displayName, boolean isNew, boolean hasParams, String invalidStepReason, String contextHash, String serializedContext) throws WdkModelException;
   * public void deleteAnalysis(int analysisId) throws WdkModelException;
   * public void renameAnalysis(int analysisId, String displayName) throws WdkModelException;
   * public void setNewFlag(int analysisId, boolean isNew) throws WdkModelException;
   * public void setHasParams(int analysisId, boolean hasParams) throws WdkModelException;
   * public void updateContext(int analysisId, String contextHash, String serializedContext) throws WdkModelException;
   * public List<StepAnalysisContext> getAllAnalyses(StepAnalysisFileStore fileStore) throws WdkModelException;
   * public boolean insertExecution(String contextHash, ExecutionStatus status, Date startDate) throws WdkModelException;  
   * public void updateExecution(String contextHash, ExecutionStatus status, Date updateDate, String charData, byte[] binData) throws WdkModelException;
   * public void resetStartDate(String contextHash, Date startDate) throws WdkModelException;
   * public void deleteExecution(String contextHash) throws WdkModelException;
   * public AnalysisResult getRawAnalysisResult(String contextHash) throws WdkModelException;
   * public String getAnalysisLog(String contextHash) throws WdkModelException;
   * public void setAnalysisLog(String contextHash, String str) throws WdkModelException;
   * public void resetExecution(String contextHash, ExecutionStatus status) throws WdkModelException;
   */
  private void runTests() throws WdkModelException, WdkUserException, IllegalAnswerValueException, InterruptedException {
    StepAnalysisFactory analysisMgr = _wdkModel.getStepAnalysisFactory();
    System.out.println("Getting user from email: " + TEST_USER_EMAIL);
    UserBean user = new UserBean(_wdkModel.getUserFactory().getUserByEmail(TEST_USER_EMAIL));
    System.out.println("Creating context in memory.");
    StepAnalysisContext context = StepAnalysisContext.createNewContext(user, TEST_PLUGIN_NAME, TEST_STEP_ID);
    Step step = context.getStep();
    System.out.println("Adding created context to DB; context id = " + context.getAnalysisId());
    analysisMgr.createAnalysis(context);
    System.out.println("Trying to change display name.");
    context.setDisplayName(TEST_CUSTOM_TAB_NAME);
    analysisMgr.renameContext(context);
    
    // context created and inserted; retrieve by multiple means
    System.out.println("Getting freshly created/modified context from DB using ID: " + context.getAnalysisId());
    context = StepAnalysisContext.createFromId(context.getAnalysisId(), analysisMgr);
    System.out.println("Comparing display name values...");
    assertEquals(TEST_CUSTOM_TAB_NAME, context.getDisplayName());
    System.out.println("Running getAllAnalyses() to look for errors.");
    analysisMgr.getAllAnalyses();
    System.out.println("Running getAppliedAnalyses() to look for errors.");
    analysisMgr.getAppliedAnalyses(step);
    
    // apply some params and run
    Map<String,String[]> params = new MapBuilder<String,String[]>()
        .put("analysisId", new String[]{ String.valueOf(context.getAnalysisId()) })
        .put("dummyParam", new String[]{ "dummy param value" }).toMap();
    System.out.println("Running step analysis plugin with original param");
    context = run(context, params, analysisMgr, 1);
    String hash1 = context.createHash(); // save this run's hash for later
    
    // complete! Run again with different params
    params.put("dummyParam", new String[]{ "different dummy param value" });
    System.out.println("Running step analysis plugin with different param value.");
    context = run(context, params, analysisMgr, 2);
    
    // now delete file store to force rerun with same params
    System.out.println("Creating file store.");
    StepAnalysisFileStore fileStore = new StepAnalysisFileStore(
        Paths.get(_wdkModel.getStepAnalysisPlugins().getExecutionConfig().getFileStoreDirectory()));
    String hash2 = context.createHash();
    System.out.println("Deleting execution dir for hash: " + hash2);
    fileStore.deleteExecutionDir(hash2);
    System.out.println("Running step analysis plugin with same params but 'forced' rerun due to cache removal.");
    context = run(context, params, analysisMgr, 3);
    
    // everything seems ok; test delete methods
    System.out.println("Creating data store.");
    StepAnalysisDataStore dataStore = new StepAnalysisPersistentDataStore(_wdkModel);
    System.out.println("Deleting execution record for hash: " + hash1);
    dataStore.deleteExecution(hash1);
    System.out.println("Deleting execution record for hash: " + hash2);
    dataStore.deleteExecution(hash2);
    System.out.println("Deleting context with ID: " + context.getAnalysisId());
    analysisMgr.deleteAnalysis(context);

    System.out.println("Tests complete. Cleaning up.");
    System.out.println("Deleting execution dir for hash: " + hash1);
    fileStore.deleteExecutionDir(hash1);
    System.out.println("Deleting execution dir for hash: " + hash2);
    fileStore.deleteExecutionDir(hash2);
    
    System.out.println("Done.");
  }

  private static StepAnalysisContext run(StepAnalysisContext context,
      Map<String, String[]> params, StepAnalysisFactory analysisMgr,
      int testRunId) throws WdkModelException, WdkUserException, InterruptedException {
    System.out.println("Test run #" + testRunId);
    context = StepAnalysisContext.createFromForm(params, analysisMgr);
    System.out.println("Context right before running: " + context.serializeContext());
    context = analysisMgr.runAnalysis(context);
    ExecutionStatus status = analysisMgr.getSavedContext(context.getAnalysisId()).getStatus();
    while (!status.equals(ExecutionStatus.COMPLETE)) {
      status = analysisMgr.getSavedContext(context.getAnalysisId()).getStatus();
      System.out.println("Checking run status: " + status);
      Thread.sleep(1000);
    }
    System.out.println("Complete!  Checking results.");
    analysisMgr.getAnalysisResult(context);
    return analysisMgr.getSavedContext(context.getAnalysisId());
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("USAGE: fgpJava " + PersistenceTester.class.getName() +
          " <project_id> (e.g. PlasmoDB)");
    }
    WdkModel model = null;
    try {
      model = WdkModel.construct("PlasmoDB", GusHome.getGusHome());
      PersistenceTester tester = new PersistenceTester(model);
      tester.runTests();
    }
    catch (Exception e) {
      System.err.println("Error during processing: " + FormatUtil.getStackTrace(e));
    }
    finally {
      if (model != null) model.releaseResources();
    }
  }
}
