package org.gusdb.wdk.service.statustype;

import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.Response.StatusType;

/**
 * Extending StatusType to accommodate the http status code of 300.  This is
 * intended for the case where multiple rows are returned for a given id
 * @author crisl-adm
 *
 */
public class MultipleChoicesStatusType implements StatusType {
  
  /** Get the class of the status code  */
  @Override
  public Family getFamily() {
    return Family.REDIRECTION;
  }

  /** Get the short description for the status code */
  @Override
  public String getReasonPhrase() {
    return "Multiple Choices";
  }

  /** Get the status code */
  @Override
  public int getStatusCode() {
    return 300;
  }

}
