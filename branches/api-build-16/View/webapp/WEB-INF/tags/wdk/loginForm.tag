<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="showError"/>
  <jsp:directive.attribute name="showCancel"/>

  <form name="loginForm" method="post" action="${pageContext.request.contextPath}/processLogin.do">
    <input type="hidden" name="redirectUrl" value="${redirectUrl}"/>
    <table>
      <c:set var="errorMessage" value="${requestScope.loginError}"/>
      <c:if test="${showError and errorMessage ne null and not empty errorMessage}">
        <tr>
          <td align="center" colspan="2">
            <div class="small">
              <font color="red">${errorMessage}<br/>Note email and password are case-sensitive.</font>
            </div>
          </td>
        </tr>
      </c:if>
      <tr>
        <td align="right" width="45%"><div class="small"><b>Email:</b></div></td>
        <td align="left"><div class="small"><input id="email" type="text" name="email" size="20"/></div></td>
      </tr>
      <tr>
        <td align="right"><div class="small"><b>Password:</b></div></td>
        <td align="left"><div class="small"><input id="password" type="password" name="password" size="20"/></div></td>
      </tr>
      <!-- Remove until we add OpenID back in -->
      <!--<tr><td style="text-align:center" colspan="2"><div class="small"><b>- OR -</b></div></td></tr>
      <tr>
        <td align="right">
          <div class="small">
            <b>Open ID:</b><br/>
            <span class="tiny-text">(<a class="open-dialog-about-openid" href="javascript:void(0)">What is this?</a>)</span>
          </div>
        </td>
        <td align="left"><div class="small"><input id="openid" type="text" size="20" name="openid"/></div></td>
      </tr>-->
      <tr>
        <td colspan="2" align="center" nowrap="nowrap">
          <input type="checkbox" id="remember" name="remember" size="11"/> Remember me on this computer.
        </td>
      </tr>
      <tr>
        <td colspan="2" align="center" nowrap="nowrap">
          <span class="small">
            <input type="submit" value="Login" id="login" style="width:76px;"/>
            <c:if test="${showCancel}">
              <input type="button" value="Cancel" style="width:76px;" onclick="jQuery(this).closest('.ui-dialog-content').dialog('close');"/>
            </c:if>
          </span>
        </td>
      </tr>
      <tr>
        <td colspan="2" align="center" valign="top">
          <span class="small">
            <a style="padding-right:15px;" href="${pageContext.request.contextPath}/showResetPassword.do">Forgot Password?</a>
            <a href="${pageContext.request.contextPath}/showRegister.do">Register/Subscribe</a>
          </span>
        </td>
      </tr>
    </table>
  </form>
</jsp:root>
