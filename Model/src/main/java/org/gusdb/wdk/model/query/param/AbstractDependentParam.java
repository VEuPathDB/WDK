package org.gusdb.wdk.model.query.param;

import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AbstractEnumParam.SelectMode;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

/**
 * Functionality shared by params that might depend on other parameters.
 *
 * @author steve
 */
public abstract class AbstractDependentParam extends Param {

  private static final Logger LOG = Logger.getLogger(AbstractDependentParam.class);

  protected static final String PARAM_SERVED_QUERY = "ServedQuery";

  // comma-delimited list of depended param names (found in XML)
  private String _dependedParamRef;

  // parsed list of depended param names
  private Set<String> _dependedParamRefs;

  // resolved list of depended Param objects
  private Set<Param> _dependedParams;

  @Override
  public abstract boolean isStale(Set<String> dependedParamsFullNames);

  public abstract String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode) throws WdkModelException;

  /**
   * A list of the &lt;query> objects used by this parameter.
   */
  public abstract Set<String> getContainedQueryFullNames();

  public abstract List<Query> getQueries();

  public AbstractDependentParam() {
    super();
    _dependedParamRefs = new LinkedHashSet<>();
  }

  public AbstractDependentParam(AbstractDependentParam param) {
    super(param);

    _dependedParamRef = param._dependedParamRef;
    _dependedParamRefs = new LinkedHashSet<>(param._dependedParamRefs);
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  public boolean isDependentParam() {
    return !_dependedParamRefs.isEmpty();
  }

  @Override
  public Set<Param> getDependedParams() {
    // FIXME: RRD this shouldn't need to happen but I screwed things up and now
    //        params don't have this value
    if (_dependedParams == null) {
      try {
        resolveDependedParamRefs();
      }
      catch (WdkModelException e) {
        throw new WdkRuntimeException(e);
      }
    }
    return _dependedParams;
  }


  // FIXME: finding default currently ALWAYS requires running vocab query (see:
  //  String getDefault(EnumParamVocabInstance), since we validate the XML
  //  default against the vocabulary.  This should probably be done only in the
  //  validateValue() method.  There is additional validation, editing as well
  //  (multi-pick, trimming whitespace).  Need to figure out if we can move all
  //  that to regular validation so this method can potentially return false.
  @Override
  protected boolean runningDependedQueriesRequiresRunnableParents() {
    return true;
  }

  /**
   * This method should be called only after the complete Resolve References
   * phase is done. Before then, we do not have the proper contexts.
   * <p>
   * We do not validate the existence of the param ref in the context because
   * some params that call this are contained by queries that are not "root"
   * queries (e.g. ID queries). They might have an incomplete context.  Instead,
   * validation is done as a dedicated post-process after resolve references.
   */
  @Override
  public synchronized void resolveDependedParamRefs() throws WdkModelException {

    if (!isDependentParam()) {
      _dependedParams = Collections.emptySet();
      return;
    }

    if (!isResolved())
      throw new WdkModelException(
          "This method can't be called before the references for the object are resolved.");

    _dependedParams = new LinkedHashSet<>();
    Map<String, Param> params = null;

    if (_contextQuestion != null) {
      params = _contextQuestion.getParamMap();
    }
    else if (_container != null) {
      params = _container.getParamMap();
    }
    for (String paramRef : _dependedParamRefs) {

      // find the best param available to fulfill the dependedParamRefs.
      // first try the question context, then the query context, and finally the wdk model
      String paramName = paramRef.split("\\.", 2)[1].trim();
      Param param = null;
      if (params != null)
        param = params.get(paramName);
      if (param == null)
        param = (Param) _wdkModel.resolveReference(paramRef);
      if (param != null) {
        _dependedParams.add(param);
        param.addDependentParam(this);
      }
      else {
        String message = "Dependent param " + getFullName() + " declares a depended param " + paramRef +
            ", but that depended param does not exist";
        throw new WdkModelException(message);
      }
    }
    if (LOG.isTraceEnabled()) {
      for (Param param : _dependedParams) {
        String vocab = "";
        if ((param instanceof FlatVocabParam)) {
          Query query = ((FlatVocabParam) param).getVocabularyQuery();
          vocab = (query != null) ? query.getFullName() : "N/A";
        }
        LOG.trace("Param " + getName() + " depends on " + param.getName() + "(" + vocab + ")");
      }
    }
  }

  public void setDependedParamRef(String dependedParamRef) {
    _dependedParamRef = dependedParamRef;
  }


  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // resolve depended param refs
    _dependedParamRefs.clear();
    if (_dependedParamRef != null && !_dependedParamRef.trim().isEmpty()) {
      for (String paramRef : _dependedParamRef.split(",\\s*")) {
        // make sure the param exists
        wdkModel.resolveReference(paramRef);

        // make sure the paramRef is unique
        if (_dependedParamRefs.contains(paramRef))
          throw new WdkModelException("Duplicate depended param [" + paramRef +
              "] defined in dependent param " + getFullName());
        _dependedParamRefs.add(paramRef);
      }
    }

    _resolved = true;
  }

  @Override
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    super.printDependencyContent(writer, indent);

    // print out depended params, if any
    if (isDependentParam()) {
      List<Param> dependedParams = new ArrayList<>(getDependedParams());
      writer.println(indent + "<dependedParams count=\"" + getDependedParams().size() + "\">");
      Collections.sort(dependedParams, (param1, param2) ->
        param1.getFullName().compareToIgnoreCase(param2.getFullName()));
      String indent1 = indent + WdkModel.INDENT;
      for (Param param : dependedParams) {
        param.printDependency(writer, indent1);
      }
      writer.println(indent + "</dependedParams>");
    }
  }

  /**
   * resolve a query that might have depended params.  such a query will be
   * declared by a dependent parameter, to provide data for that parameter.  for
   * example, the parameter might need a metadata or a vocabulary query. a
   * parameter might have multiple such queries.  each query can depend on any
   * subset (or none) of the parameter's declared parameters.  in theory, the
   * union of the query's dependencies would match those declared by the
   * parameter, but we do not enforce that.
   */
  protected Query resolveDependentQuery(WdkModel model, String queryName, String queryType) throws WdkModelException {
    queryType += " ";

    // the  query is cloned to keep a reference to the param
    Query query = (Query) model.resolveReference(queryName);
    query.resolveReferences(model);
    query = query.clone();
    query.setContextParam(this);

    // the query's params should be in the list of the param's depended params;
    for (Param param : query.getParams()) {
      String queryParamName = param.getFullName();

      if (queryParamName.equals(PARAM_SERVED_QUERY)
          || (param.getFullName().equals(Utilities.INTERNAL_PARAM_SET + "." + Utilities.PARAM_USER_ID)))
        continue;

      if (!_dependedParamRefs.contains(queryParamName)) {
        throw new WdkModelException("In parameter " + getFullName() + ", " +
            queryType + query.getFullName() + " has a parameter " + queryParamName +
            ", but " + getFullName() + " doesn't declare that in its depended params.");
      }
    }

    return query;
  }

  static JSONObject getDependedParamValuesJson(
      Map<String, String> dependedParamValues, Set<Param> dependedParams) {
    JSONObject dependedParamValuesJson = new JSONObject();
    if (dependedParams == null || dependedParams.isEmpty())
      return dependedParamValuesJson;
    // get depended param names in advance since getDependedParams() is expensive
    List<String> dependedParamNames = mapToList(dependedParams, NamedObject::getName);
    for (String paramName : dependedParamValues.keySet()) {
      if (dependedParamNames.contains(paramName)) {
        dependedParamValuesJson.put(paramName, dependedParamValues.get(paramName));
      }
    }
    return dependedParamValuesJson;
  }

  public void checkParam(String queryFullName, Map<String,Param> rootParamMap, List<String> ancestorParamNames) throws WdkModelException {

    // check for cyclic dependencies and add this param to dependency chain
    if (ancestorParamNames.contains(getFullName())) {
      throw new WdkModelException("The param " + getFullName() + " is a cyclic dependency under ID query " + queryFullName + ".");
    }
    ancestorParamNames.add(getFullName());

    // check to ensure params in required queries are referenced by the ID query (dependency relationships within a query are checked elsewhere)
    for (Query query : getQueries()) {
      Map<String,Param> paramMap = query.getParamMap();
      for (Param param : paramMap.values()) {
        if (param instanceof AbstractDependentParam) {

          // check to make sure a dependent param's query's params are also params on the ID query
          if (!rootParamMap.keySet().contains(param.getName())) {
            throw new WdkModelException("The dependent param " + param.getFullName() +
                " of query " + query.getFullName() + " (needed by param " + getFullName() +
                ") is not found among the parameters declared in the ID query " + queryFullName + ".");
          }

          // recurse through dependent param queries
          ((AbstractDependentParam) param).checkParam(queryFullName, rootParamMap, new ArrayList<>(ancestorParamNames));
        }
      }
    }
  }
}
