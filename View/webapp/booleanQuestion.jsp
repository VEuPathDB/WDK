<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

<!-- get seedQuestion -->
<c:set value="${sessionScope.booleanSeedQuestionName}" var="seedQuestion"/>

<!-- display page header with seedQuestion displayName as banner -->
<site:header banner="Boolean Question" />

<!-- display description for seedQuestion -->
<p><b><jsp:getProperty name="seedQuestion" property="description"/></b></p>

<c:set value="${sessionScope.currentBooleanRoot}" var="currentRecursiveRoot" scope="session"/>
<nested:form method="get" action="/growBoolean.do">
  <nested:root name="currentRecursiveRoot">
    <jsp:include page="/WEB-INF/includes/booleanQuestionNode.jsp"/>
  </nested:root>
</nested:form>

<site:footer/>
