package org.gusdb.wdk.model.query.param;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.User;

/**
 * functionality shared by params that might have depend on other parameters.
 * 
 * @author steve
 *
 */
public abstract class AbstractDependentParam extends Param {

  // private static final Logger LOG = Logger.getLogger(AbstractDependentParam.class);
 
  static final String PARAM_SERVED_QUERY = "ServedQuery";

  private String dependedParamRef;
  private Set<String> dependedParamRefs;
  private Set<Param> dependedParams;
 
  public AbstractDependentParam() {
    super();
    dependedParamRefs = new LinkedHashSet<>();
  }

  public AbstractDependentParam(AbstractDependentParam param) {
    super(param);

    this.dependedParamRef = param.dependedParamRef;
    this.dependedParamRefs = new LinkedHashSet<>(param.dependedParamRefs);
  }


  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  public boolean isDependentParam() {
    return (dependedParamRefs.size() > 0);
  }

  /**
   * TODO - describe why we get depended param dynamically every time.
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

    if (dependedParams == null) {
      dependedParams = new LinkedHashSet<>();
      Map<String, Param> params = null;
      
      if (contextQuestion != null) {
        params = contextQuestion.getParamMap();
      }
      else if (contextQuery != null)
        params = contextQuery.getParamMap();
      
      for (String paramRef : dependedParamRefs) {
        String paramName = paramRef.split("\\.", 2)[1].trim();
        Param param = (params != null) ? params.get(paramName) : (Param) _wdkModel.resolveReference(paramRef);
        if (param != null) {
          dependedParams.add(param);
          param.addDependentParam(this);
        }
        else {
          String message = "Missing depended param: " + paramRef + " for enum param " + getFullName();
          if (contextQuestion != null)
            message += ", in context question " + contextQuestion.getFullName();
          if (contextQuery != null)
            message += ", in context query " + contextQuery.getFullName() +
                ". Please check the context query, and make sure " + paramRef +
                " is a valid param, and is declared in the context query.";
          throw new WdkModelException(message);
        }
      }
    }
    if (logger.isTraceEnabled()) {
      for (Param param : dependedParams) {
        String vocab = "";
        if ((param instanceof FlatVocabParam)) {
          Query query = ((FlatVocabParam) param).getQuery();
          vocab = (query != null) ? query.getFullName() : "N/A";
        }
        logger.trace("param " + getName() + " depends on " + param.getName() + "(" + vocab + ")");
      }
    }
    return dependedParams;
  }

  public void setDependedParamRef(String dependedParamRef) {
    this.dependedParamRef = dependedParamRef;
  }


  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////


 

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // resolve depended param refs
    dependedParamRefs.clear();
    if (dependedParamRef != null && dependedParamRef.trim().length() > 0) {
      for (String paramRef : dependedParamRef.split(",")) {
        // make sure the param exists
        wdkModel.resolveReference(paramRef);

        // make sure the paramRef is unique
        if (dependedParamRefs.contains(paramRef))
          throw new WdkModelException("Duplicate depended param [" + paramRef +
              "] defined in dependent param " + getFullName());
        dependedParamRefs.add(paramRef);
      }
    }

    _resolved = true;

    // make sure the depended params exist in the context query.
    if (isDependentParam() && contextQuery != null) {
      Map<String, Param> params = contextQuery.getParamMap();
      Set<Param> dependedParams = getDependedParams();
      for (Param param : dependedParams) {
        if (!params.containsKey(param.getName()))
          throw new WdkModelException("Param " + getFullName() + " depends on param " + param.getFullName() +
              ", but the depended param doesn't exist in the same query " + contextQuery.getFullName());
      }
    }

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
   * resolve a query that might have depended params.
   * @param model
   * @param queryName
   * @param queryType
   * @param dependedParams
   * @param paramFullName
   * @return
   * @throws WdkModelException
   */
  protected Query resolveQuery(WdkModel model, String queryName, String queryType) throws WdkModelException {
    queryType += " ";

    // the  query is cloned to keep a reference to the param
    Query query = (Query) model.resolveReference(queryName);
    query.resolveReferences(model);
    query = query.clone();

    // get set of names of declared depended params
    Set<String> dependedParamNames = new HashSet<>();
    if (dependedParams != null) {
      for (Param param : getDependedParams()) {
        dependedParamNames.add(param.getName());
      }
    }

    // the query's params should match the depended params;
    for (Param param : query.getParams()) {
      String queryParamName = param.getName();
      if (queryParamName.equals(PARAM_SERVED_QUERY) || queryParamName.equals(Utilities.PARAM_USER_ID))
        continue;

      if (!dependedParamNames.contains(queryParamName))
        throw new WdkModelException("The " + queryType + query.getFullName() + " requires a depended param " +
            queryParamName + ", but the param " + getFullName() + " doesn't depend on it.");
    }
    
    // and depended params should match query's params
    Map<String, Param> queryParams = query.getParamMap();
    for (String dependedParamName : dependedParamNames) {
      if (!queryParams.containsKey(dependedParamName))
        throw new WdkModelException("The dependent param " + getFullName() + " depends on param " +
            dependedParamName + ", but the " + queryType + query.getFullName() + " doesn't use this depended param.");
    }
    return query;
  }

  public abstract String getDefault(User user, Map<String, String> contextParamValues) throws WdkModelException;
  
}
