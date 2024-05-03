package org.gusdb.wdk.service.filter;

import java.util.List;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.uri.UriTemplate;

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
public class PrometheusFilter implements ContainerRequestFilter, ContainerResponseFilter {

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
  public void filter(ContainerRequestContext request) {

    // find the matched path template for this request's URL and set on request
    String pathTemplate = getPathTemplate(request);
    request.setProperty(MATCHED_URL_KEY, pathTemplate);

    // add a timer child as a property on this request
    request.setProperty(TIMER_KEY, REQUEST_TIMER
        .labels(
            pathTemplate,
            request.getMethod())
        .startTimer());
  }

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {

    String matchedUrlTemplate = (String)request.getProperty(MATCHED_URL_KEY);

    // response filter executes for all responses, but only matched requests were
    //   processed by the request filter above; unmatched requests can be safely ignored
    if (matchedUrlTemplate != null) {

      // retrieve timer child and tell it to report request duration, then remove
      ((Timer)request.getProperty(TIMER_KEY)).observeDuration();
      request.removeProperty(TIMER_KEY);

      // increment count for requests of this type
      REQUEST_COUNTER
          .labels(
              matchedUrlTemplate,
              request.getMethod(),
              String.valueOf(response.getStatus()))
          .inc();
      request.removeProperty(MATCHED_URL_KEY);
    }
  }

  private String getPathTemplate(ContainerRequestContext request) {
    //LOG.info("Trying to find matching template for " + request.getUriInfo().getPath());
    List<UriTemplate> uriTemplates = ((ContainerRequest)request).getUriInfo().getMatchedTemplates();
    if (uriTemplates != null && !uriTemplates.isEmpty()) {
      String fullPath = "";
      for (UriTemplate uriTemplate : uriTemplates) {
        fullPath = uriTemplate.getTemplate() + fullPath;
      }
      //LOG.info("Found match: " + fullPath);
      return fullPath;
    }
    else {
      return "<unknown>";
    }
  }
}
