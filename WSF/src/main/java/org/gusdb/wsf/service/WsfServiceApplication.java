package org.gusdb.wsf.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class WsfServiceApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();
    classes.add(WsfService.class);
    return classes;
  }

}
