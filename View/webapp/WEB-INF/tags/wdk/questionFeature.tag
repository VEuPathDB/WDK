<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="question"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.QuestionBean"
              description="The question to be rendered"
%>


<c:choose>
  <c:when test="${question.new}">
    <img alt="New feature icon" title="This is a new search" 
         src="<c:url value='/wdk/images/new-feature.png' />" />
  </c:when>
  <c:when test="${question.revised}">
    <img alt="Revised feature icon" title="This search has been revised" 
         src="<c:url value='/wdk/images/revised-small.png' />" />
  </c:when>
</c:choose>
