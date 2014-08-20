package org.gusdb.wdk.jmx.mbeans;

import org.apache.log4j.Logger;

public class OpenConnections extends BeanBase implements OpenConnectionsMBean {

  private static final Logger LOG = Logger.getLogger(OpenConnections.class);

  @Override
  public void dumpOpenUserDBConnections() {
     LOG.info(wdkModel.getUserDb().getUnclosedConnectionInfo());
  }

  @Override
  public String showOpenUserDBConnections() {
    return wdkModel.getUserDb().getUnclosedConnectionInfo();
  }

  @Override
  public String getOpenUserDBConnections() {
    return showOpenUserDBConnections();
  }

  @Override
  public void dumpOpenAppDBConnections() {
    LOG.info(wdkModel.getAppDb().getUnclosedConnectionInfo());
  }

  @Override
  public String showOpenAppDBConnections() {
    return wdkModel.getAppDb().getUnclosedConnectionInfo();
  }

  @Override
  public String getOpenAppDBConnections() {
    return showOpenAppDBConnections();
  }

}
