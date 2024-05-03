package org.gusdb.wdk.service.statustype;

import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.Response.StatusType;

/**
 * Extending StatusType to accommodate the http status code of 405.  This is
 * intended for the case where we must declare (with annotations) that we support
 * a particular method at a path, but in fact we do not in certain cases
 * 
 * @author rdoherty
 */
public class MethodNotAllowedStatusType implements StatusType {

  @Override
  public Family getFamily() {
    return Family.CLIENT_ERROR;
  }

  @Override
  public String getReasonPhrase() {
    return "Method Not Allowed";
  }

  @Override
  public int getStatusCode() {
    return 405;
  }

}
