package org.gusdb.wdk.service;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.gusdb.fgputil.SetBuilder;
import org.gusdb.wdk.service.filter.MethodRewritingFilter;
import org.gusdb.wdk.service.provider.ExceptionMapper;
import org.gusdb.wdk.service.service.ProjectService;
import org.gusdb.wdk.service.service.RecordService;
import org.gusdb.wdk.service.service.UserService;

public class WdkServiceApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return new SetBuilder<Class<?>>()

    // add provider classes
    .add(ExceptionMapper.class)

    // add filter classes
    .add(MethodRewritingFilter.class)

    // add service classes
    .add(ProjectService.class)
    .add(UserService.class)
    .add(RecordService.class)

    .toSet();
  }

}
