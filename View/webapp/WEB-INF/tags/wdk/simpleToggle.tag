<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%@ attribute name="name"
              required="true"
              description="display to appear at top of the toggle section"
%>

<%@ attribute name="content"
              required="true"
              description="text appearing inside toggle block in 'show' mode"
%>

<%@ attribute name="show"
              required="false"
              description="Whether toggle block should initially be open"
%>

<c:if test="${show == null || show == ''}">
  <c:set var="show" value="${true}" />
</c:if>

<div class="wdk-toggle ${name}" show="${show}">
  <h3><a href="#">${name}</a></h3>
  <div>${content}</div>
</div>
