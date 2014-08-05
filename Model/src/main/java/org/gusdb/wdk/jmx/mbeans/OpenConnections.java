package org.gusdb.wdk.jmx.mbeans;

import org.apache.log4j.Logger;

public class OpenConnections extends BeanBase implements OpenConnectionsMBean {

  private static final Logger LOG = Logger.getLogger(OpenConnections.class);

  public void dumpOpenUserDBConnections() {
     LOG.info(wdkModel.getUserDb().getUnclosedConnectionInfo());
  }

  public String showOpenUserDBConnections() {
    return wdkModel.getUserDb().getUnclosedConnectionInfo();
  }

  public String getOpenUserDBConnections() {
    return showOpenUserDBConnections();
  }

  public void dumpOpenAppDBConnections() {
    LOG.info(wdkModel.getAppDb().getUnclosedConnectionInfo());
  }

  public String showOpenAppDBConnections() {
    return wdkModel.getAppDb().getUnclosedConnectionInfo();
  }

  public String getOpenAppDBConnections() {
    return showOpenAppDBConnections();
  }

}
