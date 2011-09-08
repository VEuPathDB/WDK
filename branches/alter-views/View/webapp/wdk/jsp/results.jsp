<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="wdkStrategy" value="${requestScope.wdkStrategy}" />
<c:set var="wdkStep" value="${requestScope.wdkStep}" />
<wdk:results strategy="${wdkStrategy}" step="${wdkStep}"/>

