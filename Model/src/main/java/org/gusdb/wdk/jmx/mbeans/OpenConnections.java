package org.gusdb.wdk.jmx.mbeans;

import org.apache.log4j.Logger;
import org.gusdb.wdk.jmx.BeanBase;

public class OpenConnections extends BeanBase implements OpenConnectionsMBean {

  private static final Logger LOG = Logger.getLogger(OpenConnections.class);

  @Override
  public void dumpOpenUserDBConnections() {
     LOG.info(getWdkModel().getUserDb().getUnclosedConnectionInfo());
  }

  @Override
  public String showOpenUserDBConnections() {
    return getWdkModel().getUserDb().getUnclosedConnectionInfo();
  }

  @Override
  public String getOpenUserDBConnections() {
    return showOpenUserDBConnections();
  }

  @Override
  public void dumpOpenAppDBConnections() {
    LOG.info(getWdkModel().getAppDb().getUnclosedConnectionInfo());
  }

  @Override
  public String showOpenAppDBConnections() {
    return getWdkModel().getAppDb().getUnclosedConnectionInfo();
  }

  @Override
  public String getOpenAppDBConnections() {
    return showOpenAppDBConnections();
  }

}
