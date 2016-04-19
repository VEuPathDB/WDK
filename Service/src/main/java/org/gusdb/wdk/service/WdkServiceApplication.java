package org.gusdb.wdk.service;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.gusdb.fgputil.SetBuilder;
import org.gusdb.wdk.service.filter.MethodRewritingFilter;
import org.gusdb.wdk.service.filter.RequestLoggingFilter;
import org.gusdb.wdk.service.provider.ExceptionMapper;
import org.gusdb.wdk.service.service.AnswerService;
import org.gusdb.wdk.service.service.ApiService;
import org.gusdb.wdk.service.service.DatasetService;
import org.gusdb.wdk.service.service.OntologyService;
import org.gusdb.wdk.service.service.ProjectService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.gusdb.wdk.service.service.SampleService;
import org.gusdb.wdk.service.service.StepService;
import org.gusdb.wdk.service.service.UserService;

public class WdkServiceApplication extends Application {

  // subclasses should override to turn GZipping on/off
  protected boolean compressResponses() { return false; }

  @Override
  public Set<Object> getSingletons() {
    return new SetBuilder<Object>()

    // add feature to GZip-compress responses
    .addIf(compressResponses(), new EncodingFeature(GZipEncoder.class))

    .toSet();
  }

  @Override
  public Set<Class<?>> getClasses() {
    return new SetBuilder<Class<?>>()

    // add provider classes
    .add(ExceptionMapper.class)

    // add filter classes
    .add(MethodRewritingFilter.class)
    .add(RequestLoggingFilter.class)
    .addIf(compressResponses(), EncodingFilter.class)

    // add service classes
    .add(ProjectService.class)
    .add(ApiService.class)
    .add(UserService.class)
    .add(RecordService.class)
    .add(QuestionService.class)
    .add(AnswerService.class)
    .add(OntologyService.class)
    .add(StepService.class)
    .add(DatasetService.class)

    // test
    .add(SampleService.class)

    .toSet();
  }

}
