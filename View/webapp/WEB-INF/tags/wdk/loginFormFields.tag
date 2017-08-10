<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">

  <jsp:directive.attribute name="showError"/>
  <jsp:directive.attribute name="showCancel"/>

  <input type="hidden" id="redirectUrl" name="redirectUrl" value="${redirectUrl}"/>
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
    <tr>
      <td colspan="2" align="center" nowrap="nowrap">
        <input type="checkbox" id="remember" name="remember" size="11"/> Remember me on this computer.
      </td>
    </tr>
    <tr>
      <td colspan="2" align="center" nowrap="nowrap">
        <span class="small">
          <input type="submit" value="Login" id="login" style="width:76px;height:30px;font-size:1em"/>
          <c:if test="${showCancel}">
            <input type="button" value="Cancel" style="width:76px;height:30px;font-size:1em" onclick="jQuery(this).closest('.ui-dialog-content').dialog('close');"/>
          </c:if>
        </span>
      </td>
    </tr>
    <tr>
      <td colspan="2" align="center" valign="top">
        <span class="small">
          <a style="padding-right:15px;" href="${pageContext.request.contextPath}/app/user/forgot-password">Forgot Password?</a>
          <a href="${pageContext.request.contextPath}/app/user/registration">Register/Subscribe</a>
        </span>
      </td>
    </tr>
  </table>

</jsp:root>
