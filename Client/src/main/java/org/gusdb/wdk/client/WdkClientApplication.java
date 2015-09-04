package org.gusdb.wdk.client;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.gusdb.fgputil.SetBuilder;

public class WdkClientApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return new SetBuilder<Class<?>>()

    // add service classes
    .add(WdkClient.class)

    .toSet();
  }

}
