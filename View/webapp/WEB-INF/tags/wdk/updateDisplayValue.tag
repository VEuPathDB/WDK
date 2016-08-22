<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="displayValue"
              required="true"
              description="column value to be updated, passed from wdkAttribute"
%>

<c:choose>
<c:when test="${displayValue == null || fn:length(displayValue) == 0}">
  <span style="color:gray;">N/A</span>
</c:when>
<c:otherwise>
  ${displayValue}
</c:otherwise>
</c:choose>
