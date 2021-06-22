package org.gusdb.wdk.model.user.dataset.event.model;

import org.apache.commons.lang.exception.ExceptionUtils;

public class EventError
{
  private final EventRow  event;
  private final Exception exception;

  public EventError(EventRow event, Exception exception) {
    this.event     = event;
    this.exception = exception;
  }

  public void toString(StringBuilder out) {
    // Event ID
    out.append("<hr><b>Event ID:</b>&nbsp;")
      .append(event.getEventID());

    // Event Type
    out.append("<br><b>Event Type:</b>&nbsp;")
      .append(event.getEventType().internalValue());

    // User Dataset ID
    out.append("<br><b>User Dataset ID:</b>&nbsp;")
      .append(event.getUserDatasetID());

    // User Dataset Type Name
    out.append("<br><b>Type Name:</b>&nbsp;")
      .append(event.getUserDatasetType().getName());

    // User Dataset Type Version
    out.append("<br><b>Type Version:</b>&nbsp;")
      .append(event.getUserDatasetType().getVersion());

    // Error Stacktrace
    out.append("<br><b>Error:</b><pre>")
      .append(ExceptionUtils.getStackTrace(this.exception))
      .append("</pre>");
  }
}
