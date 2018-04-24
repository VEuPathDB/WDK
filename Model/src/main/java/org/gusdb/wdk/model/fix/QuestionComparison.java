package org.gusdb.wdk.model.fix;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuestionComparison {
  public static String NL = System.getProperty("line.separator");
  private final String QUESTION_SERVICE_URL = "/service/questions";
  private final String ANSWER_SERVICE_URL = "/service/answer";
  private final String ORGANISM_QUESTION = "OrganismQuestions.GenomeDataTypes";
  private ComparisonBean questionNameComparison = null;
  private ComparisonBean organismComparison = null;
  private List<ComparisonBean> parameterNameComparisons = new ArrayList<>();
  private List<ComparisonBean> parameterValueComparisons = new ArrayList<>();
  private List<String> commonQuestionList = new ArrayList<>();
  private Map<String,List<String>> commonParameterMap = new HashMap<>();
  private List<Error> errors = new ArrayList<>();
  

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: questionComparison <qa url> <prod url>");
      System.exit(1);
    }
    try {
      new QuestionComparison().execute(args[0], args[1]);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
	
  protected void execute(String qaUrl, String prodUrl) throws Exception {
    System.out.println("Starting site comparison");
	Instant start = Instant.now();
	
    validateParameters(qaUrl, prodUrl);
    
    try {
      System.out.println(NL + "Logging Comparisons between " + qaUrl + " and " + prodUrl + NL);
      compareQuestionNames(qaUrl, prodUrl);
      if(commonQuestionList.contains(ORGANISM_QUESTION)) compareOrganisms(qaUrl, prodUrl);
      if(!commonQuestionList.isEmpty()) compareParameterNames(qaUrl, prodUrl); 
      if(!commonParameterMap.isEmpty()) compareParameterValues(qaUrl, prodUrl);
      
      System.out.print(NL + "Result of Comparisons between " + qaUrl + " and " + prodUrl);
      if(questionNameComparison != null)  questionNameComparison.display();     
      if(organismComparison != null)  organismComparison.display();  
      parameterNameComparisons.stream().forEach(comparison -> comparison.display());
      parameterValueComparisons.stream().forEach(comparison -> comparison.display());
      
      System.out.println(NL + "Trapped Errors");
      errors.stream().forEach(error -> error.display());
    }
    catch(WdkModelException wme) {
      throw new RuntimeException(wme);
    }
    Instant end = Instant.now();
    System.out.println("Site comparison\t" + Duration.between(start, end).getSeconds() + "\tsec");  
  }
  
  protected void validateParameters(String qaUrl, String prodUrl) {
    String pattern = "^(http|https)://.*\\.(org|net)/.*";
    if(!Pattern.matches(pattern, qaUrl) || !Pattern.matches(pattern, prodUrl)) {
	  throw new RuntimeException("One or both of the urls provided is not recognized as such.");
	}
  }
  
  protected void compareQuestionNames(String qaUrl, String prodUrl) throws WdkModelException {
	System.out.println("Starting question name comparison");
	Instant start = Instant.now();

	try {
      JSONArray qaData = new JSONArray(callQuestionService(qaUrl + QUESTION_SERVICE_URL));
      JSONArray prodData = new JSONArray(callQuestionService(prodUrl + QUESTION_SERVICE_URL));
    
      List<String> qaQuestionNames = new ArrayList<>();
      for(int i = 0; i < qaData.length(); i++) {
  	    qaQuestionNames.add(qaData.getString(i));
      }
      qaQuestionNames.sort(String.CASE_INSENSITIVE_ORDER);
    
      List<String> prodQuestionNames = new ArrayList<>();
      for(int i = 0; i < prodData.length(); i++) {
   	    prodQuestionNames.add(prodData.getString(i));
      }
      prodQuestionNames.sort(String.CASE_INSENSITIVE_ORDER);
    
      List<String> newQuestionList = new ArrayList<>(qaQuestionNames);
      newQuestionList.removeAll(prodQuestionNames);
  
      List<String> invalidQuestionList = new ArrayList<>(prodQuestionNames);
      invalidQuestionList.removeAll(qaQuestionNames);
    
      commonQuestionList.addAll(qaQuestionNames);
      commonQuestionList.retainAll(prodQuestionNames);
      commonQuestionList.sort(String.CASE_INSENSITIVE_ORDER);
      
      questionNameComparison = new ComparisonBean("", "New Questions", "Invalid Questions", newQuestionList, invalidQuestionList);
	}
	catch(WdkModelException wme) {
      errors.add(new Error("While processing compareQuestionNames", wme.getMessage()));
      questionNameComparison = null;
	}
   
    Instant end = Instant.now();
    System.out.println("Concluding question name comparison\t" + Duration.between(start, end).getSeconds() + "\tsec");  
  }
  
  protected void compareParameterNames(String qaUrl, String prodUrl) throws WdkModelException {
    System.out.println("Starting parameter name comparison");
	Instant start = Instant.now();

    for(String question : commonQuestionList) {	
    	
      String questionPath = Paths.get(QUESTION_SERVICE_URL, question).toString();
      
      JSONObject qaData;
      JSONObject prodData;
      
      try {
        qaData = new JSONObject(callQuestionService(qaUrl + questionPath));
        prodData = new JSONObject(callQuestionService(prodUrl + questionPath));
      }
      catch(WdkModelException wme) {
        errors.add(new Error("While processing the " + question + " in compareParameterName", wme.getMessage()));
      	continue;
      }
      
      JSONArray qaParameters = qaData.getJSONArray("parameters");
      List<String> qaParameterNames = new ArrayList<>();
      for(int i = 0; i < qaParameters.length(); i++) {
    	qaParameterNames.add(qaParameters.getString(i));
      }
      
      JSONArray prodParameters = prodData.getJSONArray("parameters");
      List<String> prodParameterNames = new ArrayList<>();
      for(int i = 0; i < prodParameters.length(); i++) {
    	prodParameterNames.add(prodParameters.getString(i));
      }
      
      List<String> newParameterList = new ArrayList<>(qaParameterNames);
      newParameterList.removeAll(prodParameterNames);
      
      List<String> invalidParameterList = new ArrayList<>(prodParameterNames);
      invalidParameterList.removeAll(qaParameterNames);
      
      List<String> commonParameterList = new ArrayList<>(qaParameterNames);
      commonParameterList.retainAll(prodParameterNames);
      commonParameterMap.put(question, commonParameterList);
   
      ComparisonBean comparison = new ComparisonBean(question, "New Parameters", "Invalid Parameters", newParameterList, invalidParameterList);
      parameterNameComparisons.add(comparison);
    }
    Instant end = Instant.now();
    System.out.println("Concluding parameter name comparison\t" + Duration.between(start, end).getSeconds() + "\tsec"); 
  }
  
  protected void compareOrganisms(String qaUrl, String prodUrl) throws WdkModelException {
	System.out.println("Starting organism comparison");
	Instant start = Instant.now();
    JSONObject organismRequest = getOrganismRequest();
    
    try {
   	  JSONObject qaData = new JSONObject(callAnswerService(qaUrl + ANSWER_SERVICE_URL, organismRequest));
      JSONObject prodData = new JSONObject(callAnswerService(prodUrl + ANSWER_SERVICE_URL, organismRequest));
   
   	  List<String> qaOrganisms = new ArrayList<>();
      JSONArray qaRecords = qaData.getJSONArray("records");
      for(int i = 0; i < qaRecords.length(); i++) {
        qaOrganisms.add(qaRecords.getJSONObject(i).getJSONObject("attributes").getString("organism"));
      }
      qaOrganisms.sort(String.CASE_INSENSITIVE_ORDER);

   	  List<String> prodOrganisms = new ArrayList<>();
      JSONArray prodRecords = prodData.getJSONArray("records");
      for(int i = 0; i < prodRecords.length(); i++) {
        prodOrganisms.add(prodRecords.getJSONObject(i).getJSONObject("attributes").getString("organism"));
      }
      prodOrganisms.sort(String.CASE_INSENSITIVE_ORDER);
      
      List<String> newOrganismList = new ArrayList<>(qaOrganisms);
      newOrganismList.removeAll(prodOrganisms);
      
      List<String> invalidOrganismList = new ArrayList<>(prodOrganisms);
      invalidOrganismList.removeAll(qaOrganisms);

      organismComparison = new ComparisonBean("", "New Organisms", "Invalid Organisms", newOrganismList, invalidOrganismList);
    }
    catch(WdkModelException wme) {
      errors.add(new Error("While processing compareOrganisms", wme.getMessage()));
    }
    Instant end = Instant.now();
    System.out.println("Concluding organism comparison\t" + Duration.between(start, end).getSeconds() + "\tsec");
  }
  
  protected void compareParameterValues(String qaUrl, String prodUrl) throws WdkModelException {
    System.out.println("Starting parameter value comparison");
	Instant start = Instant.now();
	
	int questionCounter = 0;
    for(String question : commonParameterMap.keySet()) {
    	
      questionCounter++;    	

      String questionPath = Paths.get(QUESTION_SERVICE_URL, question + "?expandParams=true").toString();
      JSONObject qaData;
      JSONObject prodData;
      
      try {
        qaData = new JSONObject(callQuestionService(qaUrl + questionPath));
        prodData = new JSONObject(callQuestionService(prodUrl + questionPath));
      }
      catch(WdkModelException wme) {
        errors.add(new Error("While processing the " + question + " in compareParameterValue", wme.getMessage()));
    	continue;
      }
      
	  JSONArray qaParameters = qaData.getJSONArray("parameters");
	  
	  List<String> dependentParamList = new ArrayList<>();
      for(int i = 0; i < qaParameters.length(); i++) {
        JSONObject parameter = qaParameters.getJSONObject(i);
	    JSONArray dependentParams = parameter.getJSONArray("dependentParams");
	    for(int j = 0; j < dependentParams.length(); j++) {
	      dependentParamList.add(dependentParams.getString(j));
	    }
      }
	  
	  Map<String,List<String>> qaParameterMap = new HashMap<>();
	  for(int i = 0; i < qaParameters.length(); i++) {
        JSONObject qaParameter = qaParameters.getJSONObject(i);
        String qaParameterName = qaParameter.getString("name");
        if(commonParameterMap.get(question).contains(qaParameterName)
        		&& "FlatVocabParam".equals(qaParameter.getString("type"))
        		&& !dependentParamList.contains(qaParameterName)) {
          if("treeBox".equals(qaParameter.getString("displayType"))) {	
            List<String> qaValues = new ArrayList<>();
            getLeaves(qaParameter.getJSONObject("vocabulary"), qaValues);
            qaParameterMap.put(qaParameterName, qaValues);
          }
          if("select".equals(qaParameter.getString("displayType"))) {
            JSONArray qaVocabulary = qaParameter.getJSONArray("vocabulary");
            List<String> qaValues = new ArrayList<>();
            for(int j = 0; j < qaVocabulary.length(); j++) {
               qaValues.add(qaVocabulary.getJSONArray(j).getString(0));
            }
            qaParameterMap.put(qaParameterName, qaValues);
          }
        }
	  }
	  
	  JSONArray prodParameters = prodData.getJSONArray("parameters");
	  Map<String,List<String>> prodParameterMap = new HashMap<>();
	  for(int i = 0; i < prodParameters.length(); i++) {
        JSONObject prodParameter = prodParameters.getJSONObject(i);
        String prodParameterName = prodParameter.getString("name");
        if(commonParameterMap.get(question).contains(prodParameterName)
        		&& "FlatVocabParam".equals(prodParameter.getString("type"))
        		&& !dependentParamList.contains(prodParameterName)) {
          if("treeBox".equals(prodParameter.getString("displayType"))) {	
            List<String> prodValues = new ArrayList<>();
            getLeaves(prodParameter.getJSONObject("vocabulary"), prodValues);
            prodParameterMap.put(prodParameterName, prodValues);
          }
          if("select".equals(prodParameter.getString("displayType"))) {
            JSONArray prodVocabulary = prodParameter.getJSONArray("vocabulary");
            List<String> prodValues = new ArrayList<>();
            for(int j = 0; j < prodVocabulary.length(); j++) {
               prodValues.add(prodVocabulary.getJSONArray(j).getString(0));
            }
            prodParameterMap.put(prodParameterName, prodValues);
          }
        }
	  }
	  
	  for(String parameter : qaParameterMap.keySet()) {
		if(!prodParameterMap.containsKey(parameter)) {
		  throw new RuntimeException("Should only be dealing with common parameters - could be a change in parameter type for " + parameter + " in question " + question);
		}
		List<String> qaParameterValues = qaParameterMap.get(parameter);
		List<String> prodParameterValues = prodParameterMap.get(parameter);
		
		List<String> newParameterValueList = new ArrayList<>(qaParameterValues);
        newParameterValueList.removeAll(prodParameterValues);
	      
        List<String> invalidParameterValueList = new ArrayList<>(prodParameterValues);
        invalidParameterValueList.removeAll(qaParameterValues);
        
        ComparisonBean comparison = new ComparisonBean(question + "/" + parameter, "New Parameter Values", "Invalid Parameter Values", newParameterValueList, invalidParameterValueList);
        parameterValueComparisons.add(comparison);
	  }
	  if(questionCounter % 10 == 0) {
          System.out.print("Completed " + questionCounter + " questions of " + commonParameterMap.keySet().size() + " total.  ");
          System.out.println("Last question completed: " + question);
      }
    }  
    Instant end = Instant.now();
    System.out.println("Concluding parameter value comparison\t" + Duration.between(start, end).getSeconds() + "\tsec");
  }
  
  protected void getLeaves(JSONObject item, List<String> leaves) {
	JSONArray children = item.getJSONArray("children");
	if(children.length() == 0) {
	  leaves.add(item.getJSONObject("data").getString("term"));
	  return;  
	}
	for(int i = 0; i < children.length(); i++) {
      JSONObject next = children.getJSONObject(i);
      getLeaves(next, leaves);
	}
  }
  
  protected String callQuestionService(String serviceUrl) throws WdkModelException {
    Client client = ClientBuilder.newBuilder().build();
    Response response = client
      	.target(serviceUrl)
        .request()
        .get();
	try {
      if (response.getStatus() == 200) {
  	    InputStream resultStream = (InputStream) response.getEntity();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IoUtil.transferStream(buffer, resultStream);
        return new String(buffer.toByteArray());
      }
      else {
        throw new WdkModelException("Call to question service (url: " + serviceUrl + ") failure - " + response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    catch(IOException ioe) {
  	  throw new WdkModelException("Failed to parse question service (url: " + serviceUrl + ") response.", ioe);
    }
    finally {
      response.close();
      client.close();
    }
  }
  
  protected JSONObject getOrganismRequest() {
	return new JSONObject()
	  .put("answerSpec", new JSONObject()
		.put("questionName", ORGANISM_QUESTION)
		.put("parameters", new JSONObject()))
	  .put("formatting", new JSONObject()
		.put("formatConfig", new JSONObject()
		  .put("attributes", new JSONArray().put("organism"))));
  }
  
  protected String callAnswerService(String serviceUrl, JSONObject body) throws WdkModelException {
    Client client = ClientBuilder.newBuilder().build();
    Response response = client
        .target(serviceUrl)
        .request()
        .post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
    try {
      if (response.getStatus() == 200) {
        InputStream resultStream = (InputStream) response.getEntity();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IoUtil.transferStream(buffer, resultStream);
        return new String(buffer.toByteArray());
      }
      else {
        throw new WdkModelException("Call to answer service (url: " + serviceUrl + ") failed - " + response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    catch(IOException ioe) {
      throw new WdkModelException("Failed to parse answer service (url: " + serviceUrl + ") response.", ioe);
    }
    finally {
      response.close();
      client.close();
    }
  }
  
  private class Error {
	private String _context;  
	private String _message;
	
	public Error() {}
	
	public Error(String context, String message) {
	  _context = context;
	  _message = message;
	}
	
	public String getContext() {
		return _context;
	}

	public String getMessage() {
		return _message;
	}
	
	public void display() {	
      System.out.println(NL + "Error context: " + getContext());
      System.out.println("Error message: " + getMessage());
	}
	
  }

}
