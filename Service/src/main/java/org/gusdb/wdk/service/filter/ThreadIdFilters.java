package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;

import org.gusdb.fgputil.runtime.ThreadId;

/**
 * Contains filters to assign and unassign ThreadId values to threads handling
 * service requests.
 * 
 * @author rdoherty
 */
public class ThreadIdFilters {

  /**
   * Adds a thread ID to this thread to identify processing done during this
   * request.
   */
  @PreMatching
  @Priority(10)
  public static class AssignmentFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
      ThreadId.assign();
    }
  }

  /**
   * Removes the thread ID assigned to this thread before the thread is returned
   * to Tomcat's request handling thread pool.
   */
  @Priority(9999)
  public static class RemovalFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
        throws IOException {
      ThreadId.unassign();
    }
  }
}
