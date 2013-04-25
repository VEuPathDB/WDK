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

<%--  name could be a string with several words (e.g. "external links",  which generates several class names... not needed? --%>
<div class="wdk-toggle ui-accordion ui-widget ui-helper-reset ui-accordion-icons" show="${show}">
  <h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-all"><a href="#">${name}</a></h3>
  <div class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom">${content}</div>
</div>
