<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">

  <jsp:directive.attribute name="helpContent" required="true"
              description="Help content to show in tooltip."/>
  <jsp:directive.attribute name="tooltipPlacement" required="false"
              description="Where to place tooltip, relative to icon. Can be one of [ 'above', 'right', 'below', 'left' ]. Defaults to 'below'."/>

  <c:set
    var="at"
    value="${
      tooltipPlacement eq 'below' ? 'bottom center'
    : tooltipPlacement eq 'top' ? 'top center'
    : tooltipPlacement eq 'left' ? 'left center'
    : tooltipPlacement eq 'right' ? 'right center'
    : ''
    }"/>

  <c:set
    var="my"
    value="${
      tooltipPlacement eq 'below' ? 'top center'
    : tooltipPlacement eq 'top' ? 'bottom center'
    : tooltipPlacement eq 'left' ? 'right center'
    : tooltipPlacement eq 'right' ? 'left center'
    : ''
    }"/>

  <div class="HelpTrigger wdk-tooltip" data-content=".help-content-container" data-at="${at}" data-my="${my}">
    <i class="fa fa-question-circle"><jsp:text/></i>
    <div style="display:none;" class="help-content-container">
      ${helpContent}
    </div>
  </div>

</jsp:root>