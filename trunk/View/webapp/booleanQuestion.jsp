<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- get wdkModel saved in application scope -->
<c:set var="wdkModel" value="${applicationScope.wdkModel}"/>

<!-- get seedQuestion -->
<c:set value="${sessionScope.booleanSeedQuestionName}" var="seedQuestion"/>



<!-- display page header with seedQuestion displayName as banner -->
<site:header banner="Boolean Question" />

<!-- display description for seedQuestion -->
<p><b><jsp:getProperty name="seedQuestion" property="description"/></b></p>

<c:set value="${sessionScope.currentBooleanRoot}" var="currentRecursiveRoot" scope="session"/>
<html:form method="get" action="/growBoolean.do">
    <wdk:booleanDisplay nodePath="-1" currentIndent="0"/> 
</html:form>







<site:footer/>
