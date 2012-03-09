<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="wdkStrategy" value="${requestScope.wdkStrategy}" />
<c:set var="wdkStep" value="${requestScope.wdkStep}" />
<imp:results strategy="${wdkStrategy}" step="${wdkStep}"/>

