<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- some of this comes from Workspace.jsp; since it is generic enough to be used in any page, we move it here --%>

<c:set var="scheme" value="${pageContext.request.scheme}" />
<c:set var="serverName" value="${pageContext.request.serverName}" />
<c:set var="serverPort" value="${pageContext.request.serverPort}" />
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="useServerPort" value="${serverPort ne 80 and serverPort ne 443}"/>
<c:set var="serverPortStr" value=":${serverPort}"/>
<c:set var="serverPortToAdd" value="${useServerPort ? serverPortStr : ''}"/>
<c:set var="exportBaseUrl" value="${scheme}://${serverName}${serverPortToAdd}${contextPath}/im.do?s=" />

<c:set var="wdkServiceUrl" value="${contextPath}${initParam.wdkServiceEndpoint}"/>

<c:set var="siteName" value="${applicationScope.wdkModel.name}" />
<c:set var="config" value="${wdkModel.model.modelConfig}"/>
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>

<c:set var="isGuest">
  <c:choose>
    <c:when test="${wdkUser.guest eq true}">true</c:when>
    <c:otherwise>false</c:otherwise>
  </c:choose>
</c:set>

<c:set var="buildNumber" value="${wdkModel.model.buildNumber}"/>
<!-- Don't let empty build number cause JS syntax error -->
<c:set var="buildNumber" value="${empty buildNumber ? '\\'\\'' : buildNumber}"/>

<script>
  var wdkConfig = {
    modelName: '${fn:replace(siteName, "'", "\\'")}',
    version: '${fn:replace(buildNumber, "'", "\\'")}',
    assetsUrl: '${applicationScope.assetsUrl ne null ? applicationScope.assetsUrl : pageContext.request.contextPath}',
    webappUrl: '${fn:replace(pageContext.request.contextPath, "'", "\\'")}',
    wdkServiceUrl: '${wdkServiceUrl}',
    guestUser: ${isGuest},
    exportBaseUrl: '${fn:replace(exportBaseUrl, "'", "\\'")}'
  };
  <c:if test="${!isGuest}">
    wdkConfig.wdkUser = {
      id: '${fn:replace(wdkUser.userId, "'", "\\'")}',
      name: '${fn:replace(wdkUser.firstName, "'", "\\'")} ${fn:replace(wdkUser.lastName, "'", "\\'")}',
      email: '${fn:replace(wdkUser.email, "'", "\\'")}',
      isGuest: ${isGuest}
    };
  </c:if>
  <c:choose>
    <c:when test="${config.authenticationMethodEnum eq 'OAUTH2'}">
      wdkConfig.auth = {
        method: 'OAUTH2',
        oauthClientId: '${config.oauthClientId}',
        oauthUrl: '${config.oauthUrl}'
      };
    </c:when>
    <c:otherwise>
      wdkConfig.auth = { method: 'USERDB' };
    </c:otherwise>
  </c:choose>
</script>

<%--
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
--%>
