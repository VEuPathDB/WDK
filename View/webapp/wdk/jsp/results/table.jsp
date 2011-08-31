<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="wdkStep" value="${requestScope.wdkStep}" />

<wdk:resultTable wdkStep="${wdkStep}" />

