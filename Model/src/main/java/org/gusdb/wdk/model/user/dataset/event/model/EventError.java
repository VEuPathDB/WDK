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
    out.append("<hr>\n<b>Event ID:</b>&nbsp;");
    out.append(event.getEventID());
    out.append("\n<br>\n<b>User Dataset ID:</b>&nbsp;");
    out.append(event.getUserDatasetID());
    out.append("\n<br>\n<b>Type Name:</b>&nbsp;");
    out.append(event.getType().getName());
    out.append("\n<br>\n<b>Type Version:</b>&nbsp;");
    out.append(event.getType().getVersion());
    out.append("\n<br>\n<b>Error:</b>\n<pre>\n");
    out.append(ExceptionUtils.getStackTrace(this.exception));
    out.append("\n</pre>");
  }
}
