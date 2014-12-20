package org.gusdb.wdk.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.gusdb.wdk.service.services.RecordsService;

public class WdkServiceApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();
    classes.add(RecordsService.class);
    return classes;
  }

}
