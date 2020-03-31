package org.gusdb.wdk.model.fix;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.json.JsonIterators.arrayStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionComparison {

  private static final String RECORDTYPES_URL = "/service/record-types";
  private static final String RECORDTYPES_REQUEST_URL = RECORDTYPES_URL + "?format=expanded";
  private static final String QUESTION_URL = RECORDTYPES_URL + "/%s/searches/%s";
  private static final String STANDARD_REPORT_URL = QUESTION_URL + "/reports/standard";

  private static final String ORGANISM_RECORD_TYPE = "organism";
  private static final String ORGANISM_QUESTION = "GenomeDataTypes";
  private static final String ORGANISM_ATTRIBUTE_NAME = "organism";

  private static String getQuestionUrl(String recordType, String question) {
    return String.format(QUESTION_URL, recordType, question);
  }

  private static String getStandardReportUrl(String recordType, String question) {
    return String.format(STANDARD_REPORT_URL, recordType, question);
  }

  private static class QuestionReference extends ThreeTuple<List<String>,Map<String,String>,Map<String,List<String>>> {
    QuestionReference(
        List<String> questionNames,
        Map<String, String> recordClassMap,
        Map<String,List<String>> paramNamesMap) {
      super(questionNames, recordClassMap, paramNamesMap);
    }
    List<String> sortedNames() { return getFirst(); }
    String getRecordType(String questionName) { return getSecond().get(questionName); }
    List<String> getParamNames(String questionName) { return getThird().get(questionName); }
  }

  private ComparisonBean questionNameComparison;
  private ComparisonBean organismComparison;

  private final List<ComparisonBean> parameterNameComparisons = new ArrayList<>();
  private final List<ComparisonBean> parameterValueComparisons = new ArrayList<>();
  private final List<ComparisonBean> parameterValueOptionComparisons = new ArrayList<>();

  private final Map<String, List<String>> commonParameterMap = new HashMap<>();

  private final List<Error> errors = new ArrayList<>();

  /**
   * Command line method for comparing various elements of the WDK model between sites. The first site (the
   * first arg) is expected to be the site with newer data whereas the second site (the second arg) is
   * expected to be the site with current data.
   */
  public static void main(String[] args) {
    if (args.length < 2 || args.length > 3) {
      System.err.println(
          "Usage: wdkQuestionComparisonReport <qa_url> <prod_url> <new | invalid>\nExample: questionComparison https://qa.plasmodb.org/plasmo https://plasmodb.org/plasmo invalid");
      System.exit(1);
    }
    validateUrl(args[0]);
    validateUrl(args[1]);
    try {
      new QuestionComparison().execute(args[0], args[1], args.length == 3 ? args[2] : "");
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Runs the site comparison. Provides some logging to indicate progress, follows by a report of differences
   * between the sites provided. The assumption is that the first site is typically a qa site and the second
   * is typically a production site.
   */
  private void execute(String qaUrl, String prodUrl, String displayOption) throws WdkModelException {

    System.out.println("Starting site comparison");
    Instant start = Instant.now();

    System.out.println(NL + "Logging Comparisons between " + qaUrl + " and " + prodUrl + NL);

    // Order is important. Compare question names first.
    Map<String,String> commonQuestionMap = compareQuestionAndParamNames(qaUrl, prodUrl);

    // Not all WDK sites necessarily contain the organism question or if in the rare event of
    // the sites having no questions in common, there is nothing to do here.
    if (commonQuestionMap.containsKey(ORGANISM_QUESTION))
      compareOrganisms(qaUrl, prodUrl);

    // Although an unlikely scenario, there are no parameter value to compare if the sites have no
    // questions in common or no parameter in common for the questions they do have in common.
    if (!commonParameterMap.isEmpty())
      compareParameterValues(qaUrl, prodUrl, commonQuestionMap);

    // Display results
    System.out.print(NL + "Result of Comparisons between " + qaUrl + " and " + prodUrl + NL);
    if (questionNameComparison != null)
      questionNameComparison.display(displayOption);
    if (organismComparison != null)
      organismComparison.display(displayOption);
    parameterNameComparisons.forEach(comparison -> comparison.display(displayOption));
    parameterValueComparisons.forEach(comparison -> comparison.display(displayOption));
    parameterValueOptionComparisons.forEach(comparison -> comparison.display(displayOption));

    // Display errors
    System.out.println(NL + "Trapped Errors");
    errors.forEach(Error::display);

    Instant end = Instant.now();
    System.out.println("Site comparison\t" + Duration.between(start, end).toNanos() / 1E9 + "\tsec");
  }

  /**
   * Very basic validation to insure that the given arguments are urls. Does not insure that the urls
   * represent WDK sites.
   */
  private static void validateUrl(String url) {
    String pattern = "^(http|https)://.*\\.(org|net)/.*";
    if (!Pattern.matches(pattern, url)) {
      System.err.println("URL provided '" + url + "' is not a valid URL.");
      System.exit(1);
    }
  }

  /**
   * Finds the names of those questions in one url and not in the other and uses that data to populate a
   * comparison object for eventual display.
   */
  private Map<String,String> compareQuestionAndParamNames(String qaUrl, String prodUrl) throws WdkModelException {

    System.out.println("Starting question name comparison");
    Instant start = Instant.now();

    // Create the question name lists from the JSONArrays returned by service calls
    QuestionReference qaQuestionRef = createQuestionReference(
        new JSONArray(callGetService(qaUrl + RECORDTYPES_REQUEST_URL)));
    QuestionReference prodQuestionRef = createQuestionReference(
        new JSONArray(callGetService(prodUrl + RECORDTYPES_REQUEST_URL)));

    // Contains qa question names not in prod
    List<String> newQuestionList = new ArrayList<>(qaQuestionRef.sortedNames());
    newQuestionList.removeAll(prodQuestionRef.sortedNames());

    // Contains prod question names not in qa
    List<String> invalidQuestionList = new ArrayList<>(prodQuestionRef.sortedNames());
    invalidQuestionList.removeAll(qaQuestionRef.sortedNames());

    // Contains question names common to both sites. Needed to identify which questions
    // to use when comparing parameters.
    List<String> commonQuestionList = new ArrayList<>(qaQuestionRef.sortedNames());
    commonQuestionList.retainAll(prodQuestionRef.sortedNames());
    commonQuestionList.sort(String.CASE_INSENSITIVE_ORDER);

    // Confirm record classes are the same for all common questions; if different, don't compare params
    List<String> questionsWithDiffRcs = new ArrayList<>();
    for (String commonQuestion : commonQuestionList) {
      if (!qaQuestionRef.getRecordType(commonQuestion).equals(prodQuestionRef.getRecordType(commonQuestion))) {
        questionsWithDiffRcs.add(commonQuestion);
        System.err.println("Warning: Question " + commonQuestion + " exists in both sites but has different types (" +
            qaQuestionRef.getRecordType(commonQuestion) + " vs. " + prodQuestionRef.getRecordType(commonQuestion));
      }
    }
    commonQuestionList.removeAll(questionsWithDiffRcs);

    // Create a comparison object to be used for displaying results.
    questionNameComparison = new ComparisonBean("", "New Questions",
        "Invalid Questions", newQuestionList, invalidQuestionList);

    Instant end = Instant.now();
    System.out.println("Concluding question name comparison\t" + Duration.between(start, end).toNanos() / 1E9 + "\tsec");

    // use param name data from the GET to compare names
    compareParameterNames(commonQuestionList, qaQuestionRef, prodQuestionRef);

    return Functions.getMapFromKeys(commonQuestionList, qaQuestionRef::getRecordType);
  }

  /**
   * Creates an alphabetically sorted list of question names given the JSONArray returned by a question
   * service call
   */
  private static QuestionReference createQuestionReference(JSONArray recordTypeDataArray) {
    List<String> questionNames = new ArrayList<>();
    Map<String,String> questionToRecordClassMap = new HashMap<>();
    Map<String,List<String>> questionToParamNamesMap = new HashMap<>();
    arrayStream(recordTypeDataArray)
      // map each rc json to its array of searches
      .map(rcJson -> rcJson.getJSONObject().getJSONArray(JsonKeys.SEARCHES))
      .forEach(searchesArray -> arrayStream(searchesArray)
        // map each search json to its name
        .map(searchJson -> new ThreeTuple<>(
            searchJson.getJSONObject().getString(JsonKeys.URL_SEGMENT),
            searchJson.getJSONObject().getString(JsonKeys.OUTPUT_RECORD_CLASS_NAME),
            Arrays.asList(JsonUtil.toStringArray(searchJson.getJSONObject().getJSONArray(JsonKeys.PARAM_NAMES)))))
        .forEach(tuple -> {
          questionNames.add(tuple.getFirst());
          questionToRecordClassMap.put(tuple.getFirst(), tuple.getSecond());
          List<String> parameterNames = tuple.getThird();
          parameterNames.sort(String.CASE_INSENSITIVE_ORDER);
          questionToParamNamesMap.put(tuple.getFirst(), parameterNames);
        }));
    // sort for later comparison to another list
    questionNames.sort(String.CASE_INSENSITIVE_ORDER);
    return new QuestionReference(questionNames, questionToRecordClassMap, questionToParamNamesMap);
  }

  /**
   * Finds those organisms in one url and not in the other and uses that data to populate a comparison object
   * for eventual display.
   */
  private void compareOrganisms(String qaUrl, String prodUrl) {
    System.out.println("Starting organism comparison");
    Instant start = Instant.now();
    JSONObject organismRequest = getOrganismRequest();

    try {
      // Get a list of organisms from the answer service (for a question returning all organisms) for each site.
      String organismReportUrl = getStandardReportUrl(ORGANISM_RECORD_TYPE, ORGANISM_QUESTION);
      JSONObject qaData = new JSONObject(callPostService(qaUrl + organismReportUrl, organismRequest));
      JSONObject prodData = new JSONObject(callPostService(prodUrl + organismReportUrl, organismRequest));

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
      organismComparison = new ComparisonBean("", "New Organisms", "Invalid Organisms", newOrganismList,
          invalidOrganismList);
    }
    catch (WdkModelException wme) {
      // An error due to a bad response to a service call may not affect the rest of the report. So we
      // continue
      // with the program and log the error for later display.
      errors.add(new Error("While processing compareOrganisms", wme.getMessage()));
    }
    Instant end = Instant.now();
    System.out.println(
        "Concluding organism comparison\t" + Duration.between(start, end).toNanos() / 1E9 + "\tsec");
  }

  /**
   * Creates the body for a post service call to retrieve organism data.
   *
   * @return - request object for post
   */
  private static JSONObject getOrganismRequest() {
    return new JSONObject()
      .put(JsonKeys.SEARCH_CONFIG, new JSONObject()
        .put(JsonKeys.PARAMETERS, new JSONObject()))
      .put(JsonKeys.REPORT_CONFIG, new JSONObject()
        .put(JsonKeys.ATTRIBUTES, new JSONArray()
           .put(ORGANISM_ATTRIBUTE_NAME)));
  }

  /**
   * Creates an alphabetically sorted list of organisms given the JSONObject returned by an answer service
   * call for a question that returns organisms
   */
  private static List<String> createOrganismList(JSONObject data) {
    List<String> organisms = new ArrayList<>();
    JSONArray records = data.getJSONArray(JsonKeys.RECORDS);
    for (int i = 0; i < records.length(); i++) {
      organisms.add(records.getJSONObject(i)
          .getJSONObject(JsonKeys.ATTRIBUTES)
          .getString(ORGANISM_ATTRIBUTE_NAME));
    }
    organisms.sort(String.CASE_INSENSITIVE_ORDER);
    return organisms;
  }

  /**
   * Finds the names of those parameters, for each question common to both urls, that exist in one url and not
   * in the other and uses that data to populate a list of comparison objects for eventual display.
   */
  private void compareParameterNames(List<String> commonQuestionList, QuestionReference qaQuestionRef,
      QuestionReference prodQuestionRef) {
    System.out.println("Starting parameter name comparison");
    Instant start = Instant.now();

    Duration serviceCallDuration = Duration.ZERO;

    // Iterate over those questions both site have in common
    for (String question : commonQuestionList) {

      // Create the parameter name lists from the JSONObjects returned by the service calls.
      List<String> qaParameterNames = qaQuestionRef.getParamNames(question);
      List<String> prodParameterNames = prodQuestionRef.getParamNames(question);

      // Contains qa parameter names not in prod
      List<String> newParameterList = new ArrayList<>(qaParameterNames);
      newParameterList.removeAll(prodParameterNames);

      // Contains prod parameter names not in qa
      List<String> invalidParameterList = new ArrayList<>(prodParameterNames);
      invalidParameterList.removeAll(qaParameterNames);

      // Contains parameter names common to both sites for each question. Needed to identify which parameters
      // to use when comparing parameter values for a given question and parameter.
      List<String> commonParameterList = new ArrayList<>(qaParameterNames);
      commonParameterList.retainAll(prodParameterNames);
      commonParameterMap.put(question, commonParameterList);

      // Create a list of parameter name comparisons for each question for display
      ComparisonBean comparison = new ComparisonBean(question, "New Parameters", "Invalid Parameters",
          newParameterList, invalidParameterList);
      parameterNameComparisons.add(comparison);
    }
    Instant end = Instant.now();
    System.out.println("Concluding parameter name comparison: Elapsed times...");
    System.out.println("Method:\t" + Duration.between(start, end).toNanos() / 1E9 +
        "\tsec\tService Calls:\t" + serviceCallDuration.toNanos() / 1E9 + "\tsec");
  }

  /**
   * Finds the parameter values for each parameter common to both urls, for each question common to both urls,
   * that exist in one url and not in the other and uses that data to populate a list of comparison objects
   * for eventual display.
   */
  private void compareParameterValues(String qaUrl, String prodUrl, Map<String,String> questionToRcMap) {
    System.out.println("Starting parameter value comparison");
    Instant start = Instant.now();

    Duration serviceCallDuration = Duration.ZERO;

    int questionCounter = 0;
    for (String question : commonParameterMap.keySet()) {

      questionCounter++;

      // Service call takes the question name in its path along with a query string to provide parameter
      // metadata.
      String questionPath = getQuestionUrl(questionToRcMap.get(question), question);

      JSONObject qaData;
      JSONObject prodData;

      Instant serviceCallStart = Instant.now();
      try {
        // Get additional parameter metadata containing parameter values from the question service for
        // question for each site.
        qaData = new JSONObject(callGetService(qaUrl + questionPath));
        prodData = new JSONObject(callGetService(prodUrl + questionPath));
      }
      catch (WdkModelException wme) {
        errors.add(
            new Error("While processing the " + question + " in compareParameterValue", wme.getMessage()));
        continue;
      }
      finally {
        Instant serviceCallEnd = Instant.now();
        serviceCallDuration = serviceCallDuration.plusNanos(
            Duration.between(serviceCallStart, serviceCallEnd).toNanos());
      }

      // Extract the parameters JSONArray from each JSONObject
      JSONArray qaParameters = qaData.getJSONObject(JsonKeys.SEARCH_DATA).getJSONArray(JsonKeys.PARAMETERS);
      JSONArray prodParameters = prodData.getJSONObject(JsonKeys.SEARCH_DATA).getJSONArray(JsonKeys.PARAMETERS);

      // Use one of the parameters JSONArrays to collect a list of dependent parameter names. We are not comparing
      // dependent parameter name lists between the sites but rather just choosing one of the sites. Hopefully, for
      // those parameters the sites have in common for this question, dependencies will not have changed.
      List<String> dependentParamList = new ArrayList<>();
      for (int i = 0; i < qaParameters.length(); i++) {
        JSONObject parameter = qaParameters.getJSONObject(i);
        JSONArray dependentParams = parameter.getJSONArray(JsonKeys.DEPENDENT_PARAMS);
        for (int j = 0; j < dependentParams.length(); j++) {
          dependentParamList.add(dependentParams.getString(j));
        }
      }

      // Create maps of parameters to a listing of parameter values from the JSONObjects returned by the
      // service calls.
      Map<String, List<ParameterValue>> qaParameterMap = createParameterValueMap(question, qaParameters, dependentParamList);
      Map<String, List<ParameterValue>> prodParameterMap = createParameterValueMap(question, prodParameters, dependentParamList);

      // Iterate over the question parameters common to both sites. Can use the key set of either site mapping
      // because
      // both maps were already limited to the parameters the sites have in common.
      for (String parameter : qaParameterMap.keySet()) {
        System.out.println("New parameter: " + parameter);
        // This exception should not occur unless somehow, a parameter with a name common to both sites is of
        // a different type in both sites.
        if (!prodParameterMap.containsKey(parameter)) {
          throw new RuntimeException(
              "Should only be dealing with common parameters - could be a change in parameter type for " +
                  parameter + " in question " + question);
        }

        // Pull the string values out of the ParameterValue objects for comparison.
        List<String> qaParameterValues = qaParameterMap.get(parameter).stream()
            .map(ParameterValue::getValue).collect(Collectors.toList());
        List<String> prodParameterValues = prodParameterMap.get(parameter).stream()
            .map(ParameterValue::getValue).collect(Collectors.toList());

        // Contains qa parameter values not in prod
        List<String> newParameterValueList = new ArrayList<>(qaParameterValues);
        newParameterValueList.removeAll(prodParameterValues);

        // Contains prod parameter values not in qa
        List<String> invalidParameterValueList = new ArrayList<>(prodParameterValues);
        invalidParameterValueList.removeAll(qaParameterValues);

        // Create a list of parameter value comparisons for each question and parameter combination for display
        ComparisonBean comparison = new ComparisonBean(question + "/" + parameter, "New Parameter Values",
            "Invalid Parameter Values", newParameterValueList, invalidParameterValueList);
        parameterValueComparisons.add(comparison);

        // Contains parameter values common to both sites for each question and parameter combination. Needed
        // to identify which parameter values to use when comparing options for a given question, parameter
        // and parameter value in the case of a filter param new parameter.
        List<String> commonParameterValueList = new ArrayList<>(qaParameterValues);
        commonParameterValueList.retainAll(prodParameterValues);

        // Iterate over the list of parameter values for this question/parameter combination that are common
        // to both sites.
        for (String parameterValue : commonParameterValueList) {

          // Find the corresponding ParameterValue object from the previously created parameter maps
          Optional<ParameterValue> optionalQaParameterValue = qaParameterMap.get(parameter).stream().filter(
              v -> v.getValue().equals(parameterValue)).findFirst();
          Optional<ParameterValue> optionalProdParameterValue = prodParameterMap.get(
              parameter).stream().filter(v -> v.getValue().equals(parameterValue)).findFirst();

          // A single matching ParameterValue object should be found for each site.
          if (optionalQaParameterValue.isPresent() && optionalProdParameterValue.isPresent()) {
            ParameterValue qaParameterValue = optionalQaParameterValue.get();
            ParameterValue prodParameterValue = optionalProdParameterValue.get();

            // Bypass any ParameterValue objects that are not FilterParamNewValue objects
            if (qaParameterValue instanceof FilterParamNewValue &&
                prodParameterValue instanceof FilterParamNewValue) {

              // Furthermore bypass any FilterParamNewValue objects that are histograms
              if (!((FilterParamNewValue) qaParameterValue).isHistogram() &&
                  !((FilterParamNewValue) qaParameterValue).isHistogram()) {

                // Retrieve the parameter value options list from both objects
                List<String> qaOntologyTermValues = ((FilterParamNewValue) qaParameterValue).getOntologyTermValues();
                List<String> prodOntologyTermValues = ((FilterParamNewValue) prodParameterValue).getOntologyTermValues();

                // Contains qa parameter value options not in prod
                List<String> newParameterOntologyTermValueList = new ArrayList<>(qaOntologyTermValues);
                newParameterOntologyTermValueList.removeAll(prodOntologyTermValues);

                // Contains prod parameter value options not in qa
                List<String> invalidParameterOntologyTermValueList = new ArrayList<>(prodOntologyTermValues);
                invalidParameterOntologyTermValueList.removeAll(qaOntologyTermValues);

                // Create a list of parameter value option comparisons for each meaningful
                // question/parameter/parameter value
                // combination for display
                ComparisonBean parameterValueOptionComparison = new ComparisonBean(
                    question + "/" + parameter + "/" + parameterValue, "New Parameter Ontology Term Values",
                    "Invalid Parameter Ontology Term Values", newParameterOntologyTermValueList,
                    invalidParameterOntologyTermValueList);
                parameterValueOptionComparisons.add(parameterValueOptionComparison);
              }
            }
          }
        }

      }
      // Poor woman's spinner
      if (questionCounter % 10 == 0) {
        System.out.print("Completed " + questionCounter + " questions of " +
            commonParameterMap.keySet().size() + " total.  ");
        System.out.println("Last question completed: " + question);
      }
    }
    Instant end = Instant.now();
    System.out.println(
        "Concluding parameter value comparison\t" + Duration.between(start, end).toNanos() / 1E9 +
            "\tsec\tService Calls:\t" + serviceCallDuration.toNanos() / 1E9 + "\tsec");
  }

  /**
   * For a given question, create a map of parameter values, keyed on parameter name for those parameters that
   * are (1) common to both of the provided urls (2) not dependent on other parameters and (3) are either flat
   * vocabulary parameters or enum parameters.
   *
   * @param question
   *          - question to which the parameter values map applies
   * @param parameters
   *          - the question's parameter data
   * @param dependentParameterList
   *          - list of names of those parameters that depend on other parameters
   * @return - parameter values map
   */
  private Map<String, List<ParameterValue>> createParameterValueMap(String question, JSONArray parameters,
      List<String> dependentParameterList) {
    Map<String, List<ParameterValue>> parameterValueMap = new HashMap<>();
    for (int i = 0; i < parameters.length(); i++) {
      JSONObject parameter = parameters.getJSONObject(i);
      String parameterName = parameter.getString("name");

      // Bypass those parameters that do not fulfill the requirements stated above.
      if (commonParameterMap.get(question).contains(parameterName)) {
        if (("FlatVocabParam".equals(parameter.getString("type")) ||
            "EnumParam".equals(parameter.getString("type"))) &&
            !dependentParameterList.contains(parameterName)) {

          // Need to descend the tree to the leaves for parameter values
          if ("treeBox".equals(parameter.getString("displayType"))) {
            List<ParameterValue> parameterValues = new ArrayList<>();
            getLeaves(parameter.getJSONObject("vocabulary"), parameterValues);
            parameterValueMap.put(parameterName, parameterValues);
          }
          // Just taking the first item of each JSON Array in each vocabulary list item
          if ("select".equals(parameter.getString("displayType"))) {
            JSONArray vocabulary = parameter.getJSONArray("vocabulary");
            List<ParameterValue> parameterValues = new ArrayList<>();
            for (int j = 0; j < vocabulary.length(); j++) {
              parameterValues.add(new ParameterValue(vocabulary.getJSONArray(j).getString(0)));
            }
            parameterValueMap.put(parameterName, parameterValues);
          }
        }
        if ("FilterParamNew".equals(parameter.getString("type"))) {
          List<ParameterValue> parameterValues = new ArrayList<>();
          JSONArray ontology = parameter.getJSONArray("ontology");
          JSONObject values = parameter.getJSONObject("values");
          for (int j = 0; j < ontology.length(); j++) {
            JSONObject ontologyItem = ontology.getJSONObject(j);
            if (ontologyItem.has("type")) {
              String term = ontologyItem.getString("term");
              if (ontologyItem.getBoolean("isRange")) {
                parameterValues.add(new FilterParamNewValue(term, true, new ArrayList<>()));
              }
              else {
                JSONArray termValueArray = new JSONArray();
                try {
                  termValueArray = values.getJSONArray(term);
                }
                catch (JSONException je) {
                  errors.add(new Error("Inconsistency while processing the " + question + "/" +
                      parameterName + " in compareParameterValueMap", je.getMessage()));
                }
                List<String> termValues = new ArrayList<>();
                for (int k = 0; k < termValueArray.length(); k++) {
                  termValues.add(termValueArray.getString(k));
                }
                parameterValues.add(new FilterParamNewValue(term, false, termValues));
              }
            }
          }
          parameterValueMap.put(parameterName, parameterValues);
        }
      }
    }
    return parameterValueMap;
  }

  /**
   * Populate a tree box flat vocabulary parameter's list of values with leaves only.
   *
   * @param item
   *          - parent node
   * @param leaves
   *          - list of parameter values
   */
  private static void getLeaves(JSONObject item, List<ParameterValue> leaves) {
    JSONArray children = item.getJSONArray(JsonKeys.CHILDREN);
    if (children.length() == 0) {
      leaves.add(new ParameterValue(item.getJSONObject(JsonKeys.DATA).getString(JsonKeys.TERM)));
      return;
    }
    for (int i = 0; i < children.length(); i++) {
      JSONObject next = children.getJSONObject(i);
      getLeaves(next, leaves);
    }
  }

  /**
   * Issues a generic GET call to the designated service endpoint.
   */
  private static String callGetService(String serviceUrl) throws WdkModelException {
    Client client = ClientBuilder.newBuilder().build();
    Response response = client.target(serviceUrl).request().get();
    try {
      if (response.getStatus() == 200) {
        InputStream resultStream = (InputStream) response.getEntity();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IoUtil.transferStream(buffer, resultStream);
        return new String(buffer.toByteArray());
      }
      else {
        throw new WdkModelException("Call to get service (url: " + serviceUrl + ") failure - " +
            response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    catch (IOException ioe) {
      throw new WdkModelException("Failed to parse get service (url: " + serviceUrl + ") response.", ioe);
    }
    finally {
      response.close();
      client.close();
    }
  }

  /**
   * Issues a generic POST call to the designated service endpoint with the provided JSONObject as the request
   * body.
   */
  private static String callPostService(String serviceUrl, JSONObject body) throws WdkModelException {
    Client client = ClientBuilder.newBuilder().build();
    Response response = client.target(serviceUrl).request().post(
        Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
    try {
      if (response.getStatus() == 200) {
        InputStream resultStream = (InputStream) response.getEntity();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IoUtil.transferStream(buffer, resultStream);
        return new String(buffer.toByteArray());
      }
      else {
        throw new WdkModelException("Call to post service (url: " + serviceUrl + ") failed - " +
            response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    catch (IOException ioe) {
      throw new WdkModelException("Failed to parse post service (url: " + serviceUrl + ") response.", ioe);
    }
    finally {
      response.close();
      client.close();
    }
  }

  /**
   * Simple class to use to allow polymorphism for parameter values.
   *
   * @author crisl-adm
   */
  private static class ParameterValue {
    private final String _value;

    ParameterValue(String value) {
      _value = value;
    }

    public String getValue() {
      return _value;
    }
  }

  /**
   * Extension of ParameterValue class that can also hold value options
   *
   * @author crisl-adm
   */
  private static class FilterParamNewValue extends ParameterValue {
    private final boolean _histogram;
    private final List<String> _ontologyTermValues;

    FilterParamNewValue(String value, boolean histogram, List<String> ontologyTermValues) {
      super(value);
      _histogram = histogram;
      _ontologyTermValues = ontologyTermValues;
    }

    public boolean isHistogram() {
      return _histogram;
    }

    List<String> getOntologyTermValues() {
      return _ontologyTermValues;
    }
  }

  /**
   * Simple error class to gather error messages and the context in which they occur and salt away for later
   * presentation.
   *
   * @author crisl-adm
   */
  private static class Error {
    private final String _context;
    private final String _message;

    Error(String context, String message) {
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
