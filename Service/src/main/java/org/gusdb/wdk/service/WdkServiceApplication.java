package org.gusdb.wdk.service;

import java.util.Set;
import java.util.function.Predicate;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.gusdb.fgputil.SetBuilder;
import org.gusdb.wdk.service.filter.CheckLoginFilter;
import org.gusdb.wdk.service.filter.ClientCacheExpirationFilter;
import org.gusdb.wdk.service.filter.LoggingContextFilter;
import org.gusdb.wdk.service.filter.MethodRewritingFilter;
import org.gusdb.wdk.service.filter.PrometheusFilter;
import org.gusdb.wdk.service.filter.RequestLoggingFilter;
import org.gusdb.wdk.service.provider.ExceptionMapper;
import org.gusdb.wdk.service.provider.JsonSchemaProvider;
import org.gusdb.wdk.service.service.AnswerService;
import org.gusdb.wdk.service.service.ClientErrorReportingService;
import org.gusdb.wdk.service.service.OAuthService;
import org.gusdb.wdk.service.service.OntologyService;
import org.gusdb.wdk.service.service.ProjectService;
import org.gusdb.wdk.service.service.PublicStrategyService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.gusdb.wdk.service.service.SampleService;
import org.gusdb.wdk.service.service.SessionService;
import org.gusdb.wdk.service.service.SystemService;
import org.gusdb.wdk.service.service.TemporaryFileService;
import org.gusdb.wdk.service.service.TemporaryResultService;
import org.gusdb.wdk.service.service.XmlAnswerService;
import org.gusdb.wdk.service.service.search.ColumnFilterService;
import org.gusdb.wdk.service.service.search.ColumnReporterService;
import org.gusdb.wdk.service.service.search.ColumnService;
import org.gusdb.wdk.service.service.user.BasketService;
import org.gusdb.wdk.service.service.user.DatasetService;
import org.gusdb.wdk.service.service.user.FavoritesService;
import org.gusdb.wdk.service.service.user.PreferenceService;
import org.gusdb.wdk.service.service.user.ProfileService;
import org.gusdb.wdk.service.service.user.StepAnalysisFormService;
import org.gusdb.wdk.service.service.user.StepAnalysisInstanceService;
import org.gusdb.wdk.service.service.user.StepService;
import org.gusdb.wdk.service.service.user.StrategyService;
import org.gusdb.wdk.service.service.user.UserDatasetService;
import org.gusdb.wdk.service.service.user.UserUtilityServices;

public class WdkServiceApplication extends Application {

  // subclasses should override to turn GZipping on/off
  protected boolean compressResponses() { return false; }

  @Override
  public Set<Object> getSingletons() {
    return new SetBuilder<>()

    // add feature to GZip-compress responses
    .addIf(compressResponses(), new EncodingFeature(GZipEncoder.class))

    .toSet();
  }

  @Override
  public Set<Class<?>> getClasses() {
    return new SetBuilder<Class<?>>()

    // add provider classes
    .add(JsonSchemaProvider.class)
    .add(ExceptionMapper.class)

    // add filter classes
    .add(PrometheusFilter.class)
    .add(CheckLoginFilter.class)
    .add(LoggingContextFilter.class)
    .add(MethodRewritingFilter.class)
    .add(RequestLoggingFilter.class)
    .add(ClientCacheExpirationFilter.class)
    .addIf(compressResponses(), EncodingFilter.class)

    // add service classes
    .add(ProjectService.class)
    .add(SystemService.class)
    .add(OAuthService.class)
    .add(ProfileService.class)
    .add(PreferenceService.class)
    .add(RecordService.class)
    .add(QuestionService.class)
    .add(ColumnService.class)
    .add(ColumnReporterService.class)
    .add(ColumnFilterService.class)
    .add(AnswerService.class)
    .add(OntologyService.class)
    .add(StepService.class)
    .add(DatasetService.class)
    .add(UserDatasetService.class)
    .add(BasketService.class)
    .add(UserUtilityServices.class)
    .add(FavoritesService.class)
    .add(PublicStrategyService.class)
    .add(SessionService.class)
    .add(StrategyService.class)
    .add(StepAnalysisInstanceService.class)
    .add(StepAnalysisFormService.class)
    .add(ClientErrorReportingService.class)
    .add(TemporaryResultService.class)
    .add(TemporaryFileService.class)
    .add(XmlAnswerService.class)

    // add extra features to basic Jersey functionality
    .add(MultiPartFeature.class)

    // test
    .add(SampleService.class)

    .toSet();
  }


  /**
   * Convenience method for subclasses to filter WDK services by class
   * object, either for removal or replacement
   * 
   * @param classes array of classes which should not be added to the application
   * @return predicate which filters those classes
   */
  protected static Predicate<Class<?>> acceptAllExcept(Class<?>... classesToFilterOut) {
    return clazz -> {
      for (Class<?> classToFilterOut : classesToFilterOut) {
        if (clazz.getName().equals(classToFilterOut.getName())) {
          return false; // do not accept
        }
      }
      return true;
    };
  }
}
