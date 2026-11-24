package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.User;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

/**
 * Prometheus Metrics Filter
 * <p>
 * Collects and records metrics data about HTTP requests.  Data collected by
 * this filter is exposed by the <code>/metrics</code> endpoint.
 * </p>
 */
@Provider
public class PrometheusFilter extends AbstractBookendFilter {

  private static final String TIMER_KEY = "prometheusRequestTimer";
  private static final String MATCHED_URL_KEY = "jerseyMatchedUrl";

  private static final double[] REQUEST_TIME_BINS = new double[] {
      0.005, 0.01, 0.1, 0.5, 1, 5, 10, Double.POSITIVE_INFINITY
  };

  private static final Counter REQUEST_COUNTER = Counter.build()
    .name("wdk_http_request_count")
    .help("WDK service request count")
    .labelNames("path", "method", "status")
    .register();

  private static final Histogram REQUEST_TIMER = Histogram.build()
    .name("wdk_http_request_duration")
    .help("WDK service request duration in milliseconds")
    .labelNames("path", "method")
    .buckets(REQUEST_TIME_BINS)
    .register();

  @Override
  protected void onRequestBegin(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel, User user,
      ContainerRequestContext requestContext) throws IOException {

    // find the matched path template for this request's URL and set on request
    String pathTemplate = getPathTemplate(uriInfo);
    requestContext.setProperty(MATCHED_URL_KEY, pathTemplate);

    // add a timer child as a property on this request
    requestContext.setProperty(TIMER_KEY, REQUEST_TIMER
        .labels(
            pathTemplate,
            request.getMethod().toString())
        .startTimer());
  }

  @Override
  protected void onRequestCompleteNoContent(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel,
      User user, int httpStatus) {
    recordRequestComplete(request, httpStatus);
  }

  @Override
  protected void performContentWrite(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel, User user,
      int httpStatus, WriterInterceptorContext writerContext) throws IOException {
    try {
      writerContext.proceed();
    }
    finally {
      // record request completion even if write fails
      recordRequestComplete(request, httpStatus);
    }
  }

  public void recordRequestComplete(RequestData request, int responseStatus) {

    String matchedUrlTemplate = getAndRemoveProp(request, MATCHED_URL_KEY, String.class);

    // response filter executes for all responses, but only matched requests were
    //   processed by the request filter above; unmatched requests can be safely ignored
    if (matchedUrlTemplate != null) {

      // retrieve timer child and tell it to report request duration, then remove
      getAndRemoveProp(request, TIMER_KEY, Timer.class).observeDuration();

      // increment count for requests of this type
      REQUEST_COUNTER
          .labels(
              matchedUrlTemplate,
              request.getMethod().toString(),
              String.valueOf(responseStatus))
          .inc();
    }
  }
}
