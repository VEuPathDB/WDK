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
  

  /**
   * Command line method for comparing various elements of the WDK model between sites.  The first site
   * (the first arg) is expected to be the site with newer data whereas the second site (the second arg)
   * is expected to be the site with current data.
   * @param args
   */
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

  
  /**
   * Runs the site comparison.  Provides some logging to indicate progress,
   * follows by a report of differences between the sites provided.  The assumption
   * is that the first site is typically a qa site and the second is typically a
   * production site.
   * @param qaUrl
   * @param prodUrl
   * @throws Exception
   */
  protected void execute(String qaUrl, String prodUrl) throws Exception {
    System.out.println("Starting site comparison");
	Instant start = Instant.now();
	
    validateParameters(qaUrl, prodUrl);
    
    try {
      System.out.println(NL + "Logging Comparisons between " + qaUrl + " and " + prodUrl + NL);
      
      // Order is important.  Compare question names first.
      compareQuestionNames(qaUrl, prodUrl);
      
      // Not all WDK sites necessarily contain the organism question or if in the rare event of
      // the sites having no questions in common, there is nothing to do here.
      if(!commonQuestionList.isEmpty() && commonQuestionList.contains(ORGANISM_QUESTION)) compareOrganisms(qaUrl, prodUrl);
      
      // Although an unlikely scenario, there are no parameter names to compare if the sites have no
      // questions in common.
      if(!commonQuestionList.isEmpty()) compareParameterNames(qaUrl, prodUrl);
      
      // Although an unlikely scenario, there are no parameter value to compare if the sites have no
      // questions in common or no parameter in common for the questions they do have in common.
      if(!commonParameterMap.isEmpty()) compareParameterValues(qaUrl, prodUrl);
      
      // Display results
      System.out.print(NL + "Result of Comparisons between " + qaUrl + " and " + prodUrl);
      if(questionNameComparison != null)  questionNameComparison.display();     
      if(organismComparison != null)  organismComparison.display();  
      parameterNameComparisons.stream().forEach(comparison -> comparison.display());
      parameterValueComparisons.stream().forEach(comparison -> comparison.display());
      
      // Display errors
      System.out.println(NL + "Trapped Errors");
      errors.stream().forEach(error -> error.display());
    }
    catch(WdkModelException wme) {
      throw new RuntimeException(wme);
    }
    Instant end = Instant.now();
    System.out.println("Site comparison\t" + Duration.between(start, end).getSeconds() + "\tsec");  
  }

  
  /**
   * Very basic validation to insure that the given arguments are urls.  Does not insure that
   * the urls represent WDK sites.
   * @param qaUrl
   * @param prodUrl
   */
  protected void validateParameters(String qaUrl, String prodUrl) {
    String pattern = "^(http|https)://.*\\.(org|net)/.*";
    if(!Pattern.matches(pattern, qaUrl) || !Pattern.matches(pattern, prodUrl)) {
	  throw new RuntimeException("One or both of the urls provided is not recognized as such.");
	}
  }

  
  /**
   * Finds the names of those questions in one url and not in the other and uses that data to
   * populate a comparison object for eventual display.
   * @param qaUrl
   * @param prodUrl
   * @throws WdkModelException
   */
  protected void compareQuestionNames(String qaUrl, String prodUrl) throws WdkModelException {
	System.out.println("Starting question name comparison");
	Instant start = Instant.now();

	try {
	  // Get a list of question names from the question service for each site.	
      JSONArray qaData = new JSONArray(callGetService(qaUrl + QUESTION_SERVICE_URL));
      JSONArray prodData = new JSONArray(callGetService(prodUrl + QUESTION_SERVICE_URL));
    
      // Create the question name lists from the JSONArrays returned by the service calls.
      List<String> qaQuestionNames = createQuestionNameList(qaData);    
      List<String> prodQuestionNames = createQuestionNameList(prodData);

      // Contains qa question names not in prod
      List<String> newQuestionList = new ArrayList<>(qaQuestionNames);
      newQuestionList.removeAll(prodQuestionNames);
  
      // Contains prod question names not in qa
      List<String> invalidQuestionList = new ArrayList<>(prodQuestionNames);
      invalidQuestionList.removeAll(qaQuestionNames);
    
      // Contains question names common to both sites.  Needed to identify which questions
      // to use when comparing parameters.
      commonQuestionList.addAll(qaQuestionNames);
      commonQuestionList.retainAll(prodQuestionNames);
      commonQuestionList.sort(String.CASE_INSENSITIVE_ORDER);
      
      // Create a comparison object to be used for displaying results.
      questionNameComparison = new ComparisonBean("", "New Questions", "Invalid Questions", newQuestionList, invalidQuestionList);
	}
	catch(WdkModelException wme) {
	  // An error at this point will most likely end any reporting.
      errors.add(new Error("While processing compareQuestionNames", wme.getMessage()));
      questionNameComparison = null;
	}
   
    Instant end = Instant.now();
    System.out.println("Concluding question name comparison\t" + Duration.between(start, end).getSeconds() + "\tsec");  
  }

  
  /**
   * Creates an alphabetically sorted list of question names given the JSONArray returned by
   * a question service call
   * @param data
   * @return
   */
  protected List<String> createQuestionNameList(JSONArray data) {
    List<String> questionNames = new ArrayList<>();
    for(int i = 0; i < data.length(); i++) {
  	  questionNames.add(data.getString(i));
    }
    questionNames.sort(String.CASE_INSENSITIVE_ORDER);
    return questionNames;
  }
  
  
  /**
   * Finds those organisms in one url and not in the other and uses that data to populate a
   * comparison object for eventual display.
   * @param qaUrl
   * @param prodUrl
   * @throws WdkModelException
   */
  protected void compareOrganisms(String qaUrl, String prodUrl) throws WdkModelException {
	System.out.println("Starting organism comparison");
	Instant start = Instant.now();
    JSONObject organismRequest = getOrganismRequest();
    
    try {
      // Get a list of organisms from the answer service (for a question returning all organisms) for each site.	
   	  JSONObject qaData = new JSONObject(callPostService(qaUrl + ANSWER_SERVICE_URL, organismRequest));
      JSONObject prodData = new JSONObject(callPostService(prodUrl + ANSWER_SERVICE_URL, organismRequest));
   
      // Create the organism lists from the JSONObject returned by the service calls.
   	  List<String> qaOrganisms = createOrganismList(qaData);
   	  List<String> prodOrganisms = createOrganismList(prodData);
      
      // Contains qa organisms not in prod
      List<String> newOrganismList = new ArrayList<>(qaOrganisms);
      newOrganismList.removeAll(prodOrganisms);
      
      // Contains prod organisms not in qa
      List<String> invalidOrganismList = new ArrayList<>(prodOrganisms);
      invalidOrganismList.removeAll(qaOrganisms);

      // Create a comparison object to be used for displaying results.
      organismComparison = new ComparisonBean("", "New Organisms", "Invalid Organisms", newOrganismList, invalidOrganismList);
    }
    catch(WdkModelException wme) {
      // An error due to a bad response to a service call may not affect the rest of the report.  So we continue
      // with the program and log the error for later display.
      errors.add(new Error("While processing compareOrganisms", wme.getMessage()));
    }
    Instant end = Instant.now();
    System.out.println("Concluding organism comparison\t" + Duration.between(start, end).getSeconds() + "\tsec");
  }


  /**
   * Creates the body for a post service call to retrieve organism data.
   * @return - request object for post
   */
  protected JSONObject getOrganismRequest() {
	return new JSONObject()
	  .put("answerSpec", new JSONObject()
		.put("questionName", ORGANISM_QUESTION)
		.put("parameters", new JSONObject()))
	  .put("formatting", new JSONObject()
		.put("formatConfig", new JSONObject()
		  .put("attributes", new JSONArray().put("organism"))));
  }


  /**
   * Creates an alphabetically sorted list of organisms given the JSONObject returned by
   * an answer service call for a question that returns organisms
   * @param data
   * @return
   */
  protected List<String> createOrganismList(JSONObject data) {
    List<String> organisms = new ArrayList<>();
    JSONArray records = data.getJSONArray("records");
    for(int i = 0; i < records.length(); i++) {
      organisms.add(records.getJSONObject(i).getJSONObject("attributes").getString("organism"));
    }
    organisms.sort(String.CASE_INSENSITIVE_ORDER);
    return organisms;
  }
  
 
  /**
   * Finds the names of those parameters, for each question common to both urls, that exist in one url
   * and not in the other and uses that data to populate a list of comparison objects for eventual display.
   * @param qaUrl
   * @param prodUrl
   * @throws WdkModelException
   */
  protected void compareParameterNames(String qaUrl, String prodUrl) throws WdkModelException {
    System.out.println("Starting parameter name comparison");
	Instant start = Instant.now();
	
	Duration serviceCallDuration = Duration.ZERO;

	// Iterate over those questions both site have in common
    for(String question : commonQuestionList) {	
    	
      // Service call takes the question name in its path	
      String questionPath = Paths.get(QUESTION_SERVICE_URL, question).toString();
      
      JSONObject qaData;
      JSONObject prodData;
      
      Instant serviceCallStart = Instant.now();
      try {
    	// Get additional question metadata containing parameter names from the question service for question for each site.
        qaData = new JSONObject(callGetService(qaUrl + questionPath));
        prodData = new JSONObject(callGetService(prodUrl + questionPath));
      }
      catch(WdkModelException wme) {
        // An error due to a bad response to a service call may not affect the rest of the report.  So we continue
        // with the next question and log the error for later display.
        errors.add(new Error("While processing the " + question + " in compareParameterName", wme.getMessage()));
      	continue;
      }
      finally {
         Instant serviceCallEnd = Instant.now();
         System.out.println("Service calls: " + Duration.between(serviceCallStart, serviceCallEnd).getSeconds());
         serviceCallDuration.plus(Duration.between(serviceCallStart, serviceCallEnd));
      }
      
      // Create the parameter name lists from the JSONObjects returned by the service calls.
      List<String> qaParameterNames = createParameterNameList(qaData);
      List<String> prodParameterNames = createParameterNameList(prodData);

      // Contains qa parameter names not in prodge
      List<String> newParameterList = new ArrayList<>(qaParameterNames);
      newParameterList.removeAll(prodParameterNames);
      
      // Contains prod parameter names not in qa
      List<String> invalidParameterList = new ArrayList<>(prodParameterNames);
      invalidParameterList.removeAll(qaParameterNames);
      
      // Contains parameter names common to both sites for each question.  Needed to identify which parameters
      // to use when comparing parameter values for a given question and parameter.
      List<String> commonParameterList = new ArrayList<>(qaParameterNames);
      commonParameterList.retainAll(prodParameterNames);
      commonParameterMap.put(question, commonParameterList);
   
      // Create a list of parameter comparisons for each question for display
      ComparisonBean comparison = new ComparisonBean(question, "New Parameters", "Invalid Parameters", newParameterList, invalidParameterList);
      parameterNameComparisons.add(comparison);
    }
    Instant end = Instant.now();
    System.out.println("Concluding parameter name comparison: Elapsed times...");
    System.out.println("Method:\t" + Duration.between(start, end).getSeconds()	+ "\tsec\tService Calls:\t" + serviceCallDuration.getSeconds() + "\tsec");
  }


  /**
   * Creates an alphabetically sorted list of parameter names given the JSONObject returned by
   * a question service call for a specific question
   * @param data
   * @return
   */
  protected List<String> createParameterNameList(JSONObject data) {
    List<String> parameterNames = new ArrayList<>();
    JSONArray parameters = data.getJSONArray("parameters");
    for(int i = 0; i < parameters.length(); i++) {
      parameterNames.add(parameters.getString(i));
    }
    parameterNames.sort(String.CASE_INSENSITIVE_ORDER);
    return parameterNames;
  }
  

  /**
   * Finds the parameter values for each parameter common to both urls, for each question common to both urls,
   * that exist in one url and not in the other and uses that data to populate a list of comparison objects
   * for eventual display.
   * @param qaUrl
   * @param prodUrl
   * @throws WdkModelException
   */
  protected void compareParameterValues(String qaUrl, String prodUrl) throws WdkModelException {
    System.out.println("Starting parameter value comparison");
	Instant start = Instant.now();
	
	int questionCounter = 0;
    for(String question : commonParameterMap.keySet()) {
    	
      questionCounter++;    	

      // Service call takes the question name in its path along with a query string to provide parameter metadata.
      String questionPath = Paths.get(QUESTION_SERVICE_URL, question + "?expandParams=true").toString();
      
      JSONObject qaData;
      JSONObject prodData;
      
      try {
    	// Get additional parameter metadata containing parameter values from the question service for question for each site.  
        qaData = new JSONObject(callGetService(qaUrl + questionPath));
        prodData = new JSONObject(callGetService(prodUrl + questionPath));
      }
      catch(WdkModelException wme) {
        errors.add(new Error("While processing the " + question + " in compareParameterValue", wme.getMessage()));
    	continue;
      }
      
      // Extract the parameters JSONArray from each JSONObject
	  JSONArray qaParameters = qaData.getJSONArray("parameters");
	  JSONArray prodParameters = prodData.getJSONArray("parameters");
	  
	  // Use one of the parameters JSONArrays to collect a list of dependent parameter names.  We are not comparing
	  // dependent parameter name lists between the sites but rather just choosing one of the sites.  Hopefully, for
	  // those parameters the sites have in common for this question, dependencies will not have changed.
	  List<String> dependentParamList = new ArrayList<>();
      for(int i = 0; i < qaParameters.length(); i++) {
        JSONObject parameter = qaParameters.getJSONObject(i);
	    JSONArray dependentParams = parameter.getJSONArray("dependentParams");
	    for(int j = 0; j < dependentParams.length(); j++) {
	      dependentParamList.add(dependentParams.getString(j));
	    }
      }
	  
      Map<String, List<String>> qaParameterMap = createParameterValueMap(question, qaParameters, dependentParamList);
	  Map<String, List<String>> prodParameterMap = createParameterValueMap(question, prodParameters, dependentParamList);
	  
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
  

  /**
   * For a given question, create a map of parameter values, keyed on parameter name for those parameters that are
   * (1) common to both of the provided urls (2) not dependent on other parameters and (3) are either flat vocabulary
   * parameters or enum parameters.
   * @param question - question to which the parameter values map applies
   * @param parameters - the question's parameter data
   * @param dependentParameterList - list of names of those parameters that depend on other parameters
   * @return - parameter values map
   */
  protected Map<String, List<String>> createParameterValueMap(String question, JSONArray parameters, List<String> dependentParameterList) {
    Map<String,List<String>> parameterValueMap = new HashMap<>();
    for(int i = 0; i < parameters.length(); i++) {
      JSONObject parameter = parameters.getJSONObject(i);
      String parameterName = parameter.getString("name");
      
      // Bypass those parameters that do not fulfill the requirements stated above.
      if(commonParameterMap.get(question).contains(parameterName)
          && ("FlatVocabParam".equals(parameter.getString("type")) || "EnumParam".equals(parameter.getString("type")))
          && !dependentParameterList.contains(parameterName)) {
    	  
    	// Need to descend the tree to the leaves for parameter values  
        if("treeBox".equals(parameter.getString("displayType"))) {	
          List<String> parameterValues = new ArrayList<>();
          getLeaves(parameter.getJSONObject("vocabulary"), parameterValues);
          parameterValueMap.put(parameterName, parameterValues);
        }
        // Just taking the first item of each JSON Array in each vocabulary list item
        if("select".equals(parameter.getString("displayType"))) {
          JSONArray vocabulary = parameter.getJSONArray("vocabulary");
          List<String> parameterValues = new ArrayList<>();
          for(int j = 0; j < vocabulary.length(); j++) {
             parameterValues.add(vocabulary.getJSONArray(j).getString(0));
          }
          parameterValueMap.put(parameterName, parameterValues);
        }
      }
    }
    return parameterValueMap;
  }


  /**
   * Populate a tree box flat vocabulary parameter's list of values with leaves only. 
   * @param item - parent node
   * @param leaves - list of parameter values
   */
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


  /**
   * Issues a generic GET call to the designated service endpoint.
   * @param serviceUrl
   * @return
   * @throws WdkModelException
   */
  protected String callGetService(String serviceUrl) throws WdkModelException {
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
        throw new WdkModelException("Call to get service (url: " + serviceUrl + ") failure - " + response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    catch(IOException ioe) {
  	  throw new WdkModelException("Failed to parse get service (url: " + serviceUrl + ") response.", ioe);
    }
    finally {
      response.close();
      client.close();
    }
  }
  
  
  /**
   * Issues a generic POST call to the designated service endpoint with the provided JSONObject as the request body.
   * @param serviceUrl
   * @param body
   * @return
   * @throws WdkModelException
   */
  protected String callPostService(String serviceUrl, JSONObject body) throws WdkModelException {
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
        throw new WdkModelException("Call to post service (url: " + serviceUrl + ") failed - " + response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    catch(IOException ioe) {
      throw new WdkModelException("Failed to parse post service (url: " + serviceUrl + ") response.", ioe);
    }
    finally {
      response.close();
      client.close();
    }
  }
  
  /**
   * Simple error class to gather error messages and the context in which they occur and salt away for later presentation.
   * @author crisl-adm
   *
   */
  private class Error {
	private String _context;  
	private String _message;

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
