package org.gusdb.wdk.service.statustype;

import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.Response.StatusType;

/**
 * Extending StatusType to accommodate the http status code of 206.  This is
 * intended for successful responses to requests for only a certain range of
 * a resource.
 * 
 * @author rdoherty
 */
public class PartialContentStatusType implements StatusType {

  @Override
  public Family getFamily() {
    return Family.SUCCESSFUL;
  }

  @Override
  public String getReasonPhrase() {
    return "Partial Content";
  }

  @Override
  public int getStatusCode() {
    return 206;
  }

}
