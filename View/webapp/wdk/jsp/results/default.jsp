<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="wdkStep" value="${requestScope.wdkStep}" />

<imp:resultTable step="${wdkStep}" />

