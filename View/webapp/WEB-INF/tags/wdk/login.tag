<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <c:set var="wdkUser" value="${sessionScope.wdkUser}"/>
  <c:set var="isLoggedIn" value="${wdkUser ne null and wdkUser.guest ne true}"/>
  <c:set var="userName" value="${wdkUser.firstName} ${wdkUser.lastName}"/>
  <c:set var="userName" value="${fn:escapeXml(userName)}"/>
  <c:set var="functionArg" value="{ &quot;isLoggedIn&quot;: ${isLoggedIn}, &quot;userName&quot;: &quot;${userName}&quot; }"/>

  <span class="onload-function"
    data-function="User.populateUserControl"
    data-arguments="${fn:escapeXml(functionArg)}"><jsp:text/>
  </span>

  <!-- This is the visible content area, to be populated by populateUserControl -->
  <span id="user-control"><jsp:text/></span>
  
  <!-- This is an invisible tag that contains the current status -->
  <span id="login-status" data-logged-in=""/>

	<script id="user-not-logged-in" type="text/x-handlebars-template">
    <li><a href="javascript:void(0)" onclick="User.login()">Login</a></li>
    <li><a href="${pageContext.request.contextPath}/showRegister.do">Register</a></li>
	</script>
	
	<script id="user-logged-in" type="text/x-handlebars-template">
    <li><a href="${pageContext.request.contextPath}/showProfile.do"><span id="user-name">{{userName}}</span>'s Profile</a></li>
    <li>
      <form name="logoutForm" method="post" action="${pageContext.request.contextPath}/processLogout.do"></form>
      <a href="javascript:void(0)" onclick="User.logout()">Logout</a>
    </li>
	</script>
	
	<script id="user-login-message" type="text/x-handlebars-template">
    <div id="login-message">
      <div class="title">User Message</div>
      <span>{{message}}</span>
    </div>
	</script>
	
	<script id="user-login-form" type="text/x-handlebars-template">
    <div id="login" title="EuPathDB Account Login">
      <imp:loginForm showError="false" showCancel="true"/>
    </div>
	</script>
	
</jsp:root>
