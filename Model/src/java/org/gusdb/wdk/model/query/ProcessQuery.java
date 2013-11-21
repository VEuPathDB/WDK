/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A ProcessQuery represents a WSF based Web Service resource. A process query
 * can only be used as ID query or param query.
 * 
 * The columns defined in the ProcessQuery should also have type and width
 * defined, otherwise the default of type and width will be used; if your actual
 * result is larger than that, the value will be truncated.
 * 
 * If you don't specify an web service URL in the <processQuery> tag, the
 * default web service url in model-config.xml will be used.
 * 
 * If the local flag is true, WDK assumes that the WSF service is installed in
 * the same webapp as the site, and it will bypass any web service url, and try
 * to invoke the service within the webapp context.
 * 
 * A ProcessQuery that is used as ID query is always cached, so that it can be
 * used to join with attribute queries for joining and pagination.
 * 
 * @author Jerric Gao
 */
public class ProcessQuery extends Query {

  private String processName;
  private String webServiceUrl;
  private boolean local = false;

  public ProcessQuery() {
    super();
  }

  private ProcessQuery(ProcessQuery query) {
    super(query);
    this.processName = query.processName;
    this.webServiceUrl = query.webServiceUrl;
    this.local = query.local;
  }

  /**
   * @return the name of the WSF plugin that will be invoked by the service.
   */
  public String getProcessName() {
    return this.processName;
  }

  /**
   * @param processClass
   *          the processClass to set
   */
  public void setProcessName(String processName) {
    this.processName = processName;
  }

  /**
   * @return the webServiceUrl
   */
  public String getWebServiceUrl() {
    return this.webServiceUrl;
  }

  public void setWebServiceUrl(String webServiceUrl) {
    this.webServiceUrl = webServiceUrl;
  }

  /**
   * @return the local
   */
  public boolean isLocal() {
    return this.local;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#resolveReferences(org.gusdb.wdk.model
   * .WdkModel)
   */
  @Override
  public void resolveQueryReferences(WdkModel wdkModel)
      throws WdkModelException {
    configureProcessLocation(wdkModel);
  }

  /**
   * Configures how this ProcessQuery is run (i.e. local/remote, which service
   * is used).  First assess whether a webServiceUrl was provided; if so, use
   * it and assume remote execution.  If not, set url to null and run locally.
   * 
   * @param wdkModel loaded wdk model
   */
  private void configureProcessLocation(WdkModel wdkModel) {
    // if user did not set specific URL in model for this particular query,
    //   use default URL in model config
    if (webServiceUrl == null || webServiceUrl.isEmpty())
      webServiceUrl = wdkModel.getModelConfig().getWebServiceUrl();

    // if no default URL was set in model config, assume local execution
    if (webServiceUrl == null || webServiceUrl.isEmpty())
      webServiceUrl = ModelConfig.WSF_LOCAL;
      
    // set local to true if no URL provided or local explicitly set
    local = webServiceUrl.equals(ModelConfig.WSF_LOCAL);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#makeInstance()
   */
  @Override
  public QueryInstance makeInstance(User user, Map<String, String> values,
      boolean validate, int assignedWeight, Map<String, String> context)
      throws WdkModelException {
    return new ProcessQueryInstance(user, this, values, validate,
        assignedWeight, context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#appendJSONContent(org.json.JSONObject)
   */
  @Override
  protected void appendJSONContent(JSONObject jsQuery, boolean extra)
      throws JSONException {
    if (extra) {
      jsQuery.put("process", this.processName);
      if (!local)
        jsQuery.put("url", this.webServiceUrl);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.Query#clone()
   */
  @Override
  public Query clone() {
    return new ProcessQuery(this);
  }

  /**
   * Process Query is always cached.
   */
  @Override
  public boolean isCached() {
    return true;
  }
}
