package org.gusdb.wdk.service.filter;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

/**
 * Prometheus Metrics Filter
 * <p>
 * Collects and records metrics data about HTTP requests.  Data collected by
 * this filter is exposed by the <code>/metrics</code> endpoint.
 */
@Provider
@Priority(2)
@PreMatching
public class PrometheusFilter
implements ContainerRequestFilter, ContainerResponseFilter {

  private static final String TIMER_KEY = "prometheusRequestTimer";

  private static final double[] REQUEST_TIME_BINS = new double[] {
      0.005, 0.01, 0.1, 0.5, 1, 5, 10, Double.POSITIVE_INFINITY
  };

  private static final Counter REQUEST_COUNTER = Counter.build()
    .name("http_total_requests")
    .help("Total HTTP request count.")
    .labelNames("path", "method", "status")
    .register();

  private static final Histogram REQUEST_TIMER = Histogram.build()
    .name("http_request_duration")
    .help("Request times in milliseconds")
    .labelNames("path", "method")
    .buckets(REQUEST_TIME_BINS)
    .register();

  @Override
  public void filter(ContainerRequestContext request) {

    // add a timer child as a property on this request
    request.setProperty(TIMER_KEY, REQUEST_TIMER
        .labels(
            request.getUriInfo().getPath(),
            request.getMethod())
        .startTimer());
  }

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {

    // retrieve timer child and tell it to report request duration, then remove
    ((Timer)request.getProperty(TIMER_KEY)).observeDuration();
    request.removeProperty(TIMER_KEY);

    // increment count for requests of this type
    REQUEST_COUNTER
        .labels(
            request.getUriInfo().getPath(),
            request.getMethod(),
            String.valueOf(response.getStatus()))
        .inc();
  }
}

