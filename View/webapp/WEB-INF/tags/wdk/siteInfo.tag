<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- some of this comes from Workspace.jsp; since it is generic enough to be used in any page, we move it here --%>

<c:set var="siteName" value="${applicationScope.wdkModel.name}" />
<c:set var="scheme" value="${pageContext.request.scheme}" />
<c:set var="serverName" value="${pageContext.request.serverName}" />
<c:set var="request_uri" value="${requestScope['javax.servlet.forward.request_uri']}" />
<c:set var="request_uri" value="${fn:substringAfter(request_uri, '/')}" />
<c:set var="request_uri" value="${fn:substringBefore(request_uri, '/')}" />
<c:set var="exportBaseUrl" value = "${scheme}://${serverName}/${request_uri}/im.do?s=" />
<c:set var="webAppUrl" value = "${scheme}://${serverName}/${request_uri}/" />
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>

<div style="display:none">
  <!-- used by goToIsolate()    -->
  <div id="modelName" name="${siteName}"></div>

  <!-- used by view-JSON.js and controller-JSON.js   -->
  <div id="guestUser" name="${wdkUser.guest}"></div>
  <div id="exportBaseURL" name="${exportBaseUrl}"></div>

  <!-- for future uses -->
  <div id="wdk-user"
    data-id="${wdkUser.userId}" 
    data-name="${wdkUser.firstName} ${wdkUser.lastName}" 
    data-country="${wdkUser.country}" 
    data-email="${wdkUser.email}" 
    data-is-guest="${wdkUser.guest}"></div>

  <div id="wdk-web-app-url" value="<c:url value='/'/>"></div>
  <div id="wdk-assets-url" value="${applicationScope.assetsUrl}"></div>
</div>

