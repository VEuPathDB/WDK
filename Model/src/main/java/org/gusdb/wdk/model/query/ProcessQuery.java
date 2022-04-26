package org.gusdb.wdk.model.query;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONException;
import org.json.JSONObject;

import static java.util.Objects.isNull;

/**
 * A ProcessQuery represents a WSF based Web Service resource. A process query
 * can only be used as ID query or param query.
 * <p>
 * The columns defined in the ProcessQuery should also have type and width
 * defined, otherwise the default of type and width will be used; if your actual
 * result is larger than that, the value will be truncated.
 * <p>
 * If you don't specify an web service URL in the &lt;processQuery> tag, the
 * default web service url in model-config.xml will be used.
 * <p>
 * If the local flag is true, WDK assumes that the WSF service is installed in
 * the same webapp as the site, and it will bypass any web service url, and try
 * to invoke the service within the webapp context.
 * <p>
 * A ProcessQuery that is used as ID query is always cached, so that it can be
 * used to join with attribute queries for joining and pagination.
 *
 * @author Jerric Gao
 */
public class ProcessQuery extends Query {

  private String processName;
  private String webServiceUrl;
  private int cacheInsertBatchSize = 1000;
  private boolean local;

  public ProcessQuery() {
    super();
  }

  private ProcessQuery(ProcessQuery query) {
    super(query);
    this.processName = query.processName;
    this.webServiceUrl = query.webServiceUrl;
    this.cacheInsertBatchSize = query.cacheInsertBatchSize;
    this.local = query.local;
  }

  @Override
  public void addColumn(Column column) throws WdkModelException {
    if (isNull(column.getType()))
      column.setType(ColumnType.STRING);
    super.addColumn(column);
  }

  @Override
  protected ProcessQueryInstance makeInstance(RunnableObj<QueryInstanceSpec> spec) {
    return new ProcessQueryInstance(spec);
  }

  @Override
  public boolean isCacheable() {
    return true;
  }

  /**
   * @return the name of the WSF plugin that will be invoked by the service.
   */
  public String getProcessName() {
    return this.processName;
  }

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
   * @return batch size to be used during insertion into WDK cache table
   */
  public int getCacheInsertBatchSize() {
    return cacheInsertBatchSize;
  }

  public void setCacheInsertBatchSize(int cacheInsertBatchSize) {
    this.cacheInsertBatchSize = cacheInsertBatchSize;
  }

  /**
   * @return the local
   */
  public boolean isLocal() {
    return this.local;
  }

  @Override
  public void resolveQueryReferences(WdkModel wdkModel) {
    configureProcessLocation(wdkModel);

    // Set default column type for columns that don't have a type defined in XML
    _columnMap.values()
      .stream()
      .filter(c -> isNull(c.getType()))
      .forEach(c -> c.setType(ColumnType.STRING));
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

  @Override
  protected void appendChecksumJSON(JSONObject jsQuery, boolean extra)
      throws JSONException {
    if (extra) {
      jsQuery.put("process", this.processName);
      if (!local) jsQuery.put("url", this.webServiceUrl);
    }
  }

  @Override
  public Query clone() {
    return new ProcessQuery(this);
  }
}
