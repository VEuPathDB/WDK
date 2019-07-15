package org.gusdb.wdk.service;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.gusdb.fgputil.SetBuilder;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.wdk.service.filter.ClientCacheExpirationFilter;
import org.gusdb.wdk.service.filter.MethodRewritingFilter;
import org.gusdb.wdk.service.filter.RequestLoggingFilter;
import org.gusdb.wdk.service.provider.ExceptionMapper;
import org.gusdb.wdk.service.provider.JsonSchemaProvider;
import org.gusdb.wdk.service.provider.LoggingWriterInterceptor;
import org.gusdb.wdk.service.service.*;
import org.gusdb.wdk.service.service.user.*;

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
    .add(LoggingWriterInterceptor.class)

    // add filter classes
    .add(MethodRewritingFilter.class)
    .add(RequestLoggingFilter.class)
    .add(ClientCacheExpirationFilter.class)
    .addIf(compressResponses(), EncodingFilter.class)

    // add service classes
    .add(ProjectService.class)
    .add(ApiService.class)
    .add(SystemService.class)
    .add(OAuthService.class)
    .add(ProfileService.class)
    .add(PreferenceService.class)
    .add(RecordService.class)
    .add(QuestionService.class)
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
    .add(StepAnalysisService.class)
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
