package org.gusdb.wdk.model.query.param;

import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

/**
 * functionality shared by params that might have depend on other parameters.
 * 
 * @author steve
 *
 */
public abstract class AbstractDependentParam extends Param {

  private static final Logger LOG = Logger.getLogger(AbstractDependentParam.class);
 
  protected static final String PARAM_SERVED_QUERY = "ServedQuery";

  private String _dependedParamRef;
  private Set<String> _dependedParamRefs; 
  private Set<Param> _dependedParams;
 
  public AbstractDependentParam() {
    super();
    _dependedParamRefs = new LinkedHashSet<>();
  }

  public AbstractDependentParam(AbstractDependentParam param) {
    super(param);

    this._dependedParamRef = param._dependedParamRef;
    this._dependedParamRefs = new LinkedHashSet<>(param._dependedParamRefs);
  }


  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  public boolean isDependentParam() {
    return (_dependedParamRefs.size() > 0);
  }

  /**
   * This method should be called only after the complete Resolve References phase is done.
   * Before then, we do not have the proper contexts.
   * 
   * We do not validate the existance of the param ref in the context because
   * some params that call this are contained by queries that are not "root" queries (eg, ID queries).
   * They might have an incomplete context.  Instead, validation is done as a dedicated
   * post-process after resolve references.
   * 
   * 
   * @return
   * @throws WdkModelException
   */
  public Set<Param> getDependedParams() throws WdkModelException {
    if (!isDependentParam())
      return null;

    if (!isResolved())
      throw new WdkModelException(
          "This method can't be called before the references for the object are resolved.");

    if (_dependedParams == null) {  
      _dependedParams = new LinkedHashSet<>();
      Map<String, Param> params = null;
      
      if (_contextQuestion != null) {
        params = _contextQuestion.getParamMap();
      }
      else if (_container != null)
        params = _container.getParamMap();
      
      for (String paramRef : _dependedParamRefs) {
        
        // find the best param available to fulfill the dependedParamRefs.
        // first try the question context, then the query context, and finally the wdk model
        String paramName = paramRef.split("\\.", 2)[1].trim();
        Param param = null;
        if (params != null) param = params.get(paramName);
        if (param == null) param = (Param) _wdkModel.resolveReference(paramRef);
        if (param != null) {
          _dependedParams.add(param);
          param.addDependentParam(this);
        } else {
          String message = "Dependent param " + getFullName() + " declares a depended param " + paramRef +
              ", but that depended param does not exist";
          throw new WdkModelException(message);
        }
      }
    }
    if (LOG.isTraceEnabled()) {
      for (Param param : _dependedParams) {
        String vocab = "";
        if ((param instanceof FlatVocabParam)) {
          Query query = ((FlatVocabParam) param).getQuery();
          vocab = (query != null) ? query.getFullName() : "N/A";
        }
        LOG.trace("param " + getName() + " depends on " + param.getName() + "(" + vocab + ")");
      }
    }
    return _dependedParams;
  }

  public void setDependedParamRef(String dependedParamRef) {
    this._dependedParamRef = dependedParamRef;
  }


  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  protected Map<String,String> ensureRequiredContext(User user, Map<String, String> contextParamValues) {
    Map<String, String> newContextParamValues = contextParamValues == null?
        new LinkedHashMap<String, String>() :
        new LinkedHashMap<String, String>(contextParamValues);

    if (isDependentParam()) {
      try {
        // for each depended param, ensure it has a value in contextParamValues
        for (Param dependedParam : getDependedParams()) {

          String dependedParamVal = newContextParamValues.get(dependedParam.getName());
          if (dependedParamVal == null) {
            dependedParamVal = (dependedParam instanceof AbstractEnumParam)
                ? ((AbstractEnumParam) dependedParam).getDefault(user, newContextParamValues)
                : dependedParam.getDefault();
            if (dependedParamVal == null)
              throw new NoDependedValueException(
                  "Attempt made to retrieve values of " + dependedParam.getName() + " in dependent param " +
                      getName() + " without setting depended value.");
            newContextParamValues.put(dependedParam.getName(), dependedParamVal);
          }
        }
      }
      catch (Exception ex) {
        throw new NoDependedValueException(ex);
      }
    }
    return newContextParamValues;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // resolve depended param refs
    _dependedParamRefs.clear();
    if (_dependedParamRef != null && _dependedParamRef.trim().length() > 0) {
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
      Collections.sort(dependedParams, new Comparator<Param>() {
        @Override
        public int compare(Param param1, Param param2) {
          return param1.getFullName().compareToIgnoreCase(param2.getFullName());
        }
      });
      String indent1 = indent + WdkModel.INDENT;
      for (Param param : dependedParams) {
        param.printDependency(writer, indent1);
      }
      writer.println(indent + "</dependedParams>");
    }
  }

  
  /**
   * resolve a query that might have depended params.  such a query will be declared by a dependent parameter, to provide
   * data for that parameter.  for example, the parameter might need a metadata or a vocabulary query.
   * a parameter might have multiple such queries.  each query can depend on any subset (or none) of the parameter's declared
   * parameters.  in theory, the union of the query's dependencies would match those declared by the parameter, but we do not enforce that.
   * @param model
   * @param queryName
   * @param queryType
   * @param _dependedParams
   * @param paramFullName
   * @return
   * @throws WdkModelException
   */
  protected Query resolveDependentQuery(WdkModel model, String queryName, String queryType) throws WdkModelException {
    queryType += " ";

    // the  query is cloned to keep a reference to the param
    Query query = (Query) model.resolveReference(queryName);
    query.resolveReferences(model);
    query = query.clone();

    /* I don't think we should call getDependedParams() during resolve references, as the query context
     * is not set.  we were only doing so here to get the names of the param's depended params.  but we can
     * get that from their paramrefs.
     
    // get set of names of declared depended params
    getDependedParams();
    Set<String> dependedParamNames = new HashSet<>();
    if (_dependedParams != null) {
      for (Param param : _dependedParams) {
        dependedParamNames.add(param.getName());
      }
    }
    
    */
    
    // the query's params should be in the list of the param's depended params;
    for (Param param : query.getParams()) {
      String queryParamName = param.getFullName();
      
      if (queryParamName.equals(PARAM_SERVED_QUERY) 
          || (param.getFullName().equals(Utilities.INTERNAL_PARAM_SET + "." + Utilities.PARAM_USER_ID)))
        continue;

      if (!_dependedParamRefs.contains(queryParamName)) {
        WdkModelException ex = new WdkModelException("In parameter " + getFullName() + ", " + queryType + query.getFullName() + 
            " declares a depended param " +
            queryParamName + ", but " + getFullName() + " doesn't declare that in its depended params.");
        ex.printStackTrace(); // TODO: temporary for debugging filter param new bug
        throw ex;
      }
    }

    return query;
  }
  
  /**
   * 
   * @param user
   * @param contextParamValues map from name to stable values
   * @param instances
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public void fillContextParamValues(User user, Map<String, String> contextParamValues,
      Map<String, DependentParamInstance> instances) throws WdkModelException, WdkUserException {

    //logger.debug("Fixing value " + name + "='" + contextParamValues.get(name) + "'");

    // make sure the values for depended params are fetched first.
    if (isDependentParam()) {
      for (Param dependedParam : getDependedParams()) {
        LOG.debug(_name + " depends on " + dependedParam.getName());
        if (dependedParam instanceof AbstractDependentParam) {
          ((AbstractDependentParam) dependedParam).fillContextParamValues(user, contextParamValues, instances);
        }
      }
    }

    // check if the value for this param is correct
    DependentParamInstance instance = instances.get(_name);
    if (instance == null) {
      instance = createDependentParamInstance(user, contextParamValues);
      instances.put(_name, instance);
    }

    String stableValue = contextParamValues.get(_name);
    String value = instance.getValidStableValue(user, stableValue, contextParamValues);

    if (value != null) {
      contextParamValues.put(_name, value);
      //logger.debug("Corrected " + name + "\"" + contextParamValues.get(name) + "\"");
    }
  }


  public abstract String getDefault(User user, Map<String, String> contextParamValues) throws WdkModelException;
  
  @Override
  public abstract boolean isStale(Set<String> dependedParamsFullNames);
  
  protected abstract DependentParamInstance createDependentParamInstance(User user, Map<String, String> dependedParamValues)
      throws WdkModelException, WdkUserException;

  public abstract String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode); 
  
  static JSONObject getDependedParamValuesJson(
      Map<String, String> dependedParamValues, Set<Param> dependedParams) {
    JSONObject dependedParamValuesJson = new JSONObject();
    if (dependedParams == null || dependedParams.isEmpty())
      return dependedParamValuesJson;
    // get depended param names in advance since getDependedParams() is expensive
    List<String> dependedParamNames = mapToList(dependedParams, obj -> obj.getName());
    for (String paramName : dependedParamValues.keySet()) {
      if (dependedParamNames.contains(paramName)) {
        dependedParamValuesJson.put(paramName, dependedParamValues.get(paramName));
      }
    }
    return dependedParamValuesJson;
  }

  /**
   * A list of the <query> objects used by this parameter.  
   * @return
   */
  public abstract Set<String> getContainedQueryFullNames();
  
  public void checkParam(String queryFullName, String parentParamName, Map<String,Param> rootParamMap, List<String> ancestorParamNames) throws WdkModelException {
	if(!rootParamMap.keySet().contains(getName())) {
	  throw new WdkModelException("The param " + getFullName() + " is not found in the param map of the root query " + queryFullName + ".");
	}
	if(ancestorParamNames.contains(getFullName())) {
	  throw new WdkModelException("The param " + getFullName() + " is a cyclic dependency in the root query " + queryFullName + ".");
	}
	ancestorParamNames.add(getFullName());
	List<Query> queries = getQueries();
	for(Query query : queries) {
	  Map<String,Param> paramMap = query.getParamMap();
	  for(Param param : paramMap.values()) {
		if (param instanceof AbstractDependentParam) {
		  ((AbstractDependentParam) param).checkParam(queryFullName, parentParamName, rootParamMap, new ArrayList<String>(ancestorParamNames));
		}
	  }
	}
	
  }
  
  public abstract List<Query> getQueries();

}
