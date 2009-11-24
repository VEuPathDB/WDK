<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.servletsuite.com/servlets/wraptag" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="logic" uri="http://jakarta.apache.org/struts/tags-logic" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>

<!-- display page header with recordClass type in banner -->
<c:set value="${wdkRecord.recordClass.type}" var="recordType"/>
<site:header banner="${recordType}"/>
<div align="center">

<!-- display the success information, if the user registered successfully -->
<c:choose>
  <c:when test="${requestScope.resetPasswordSucceed != null}">

  <p>
    <font color="blue">Your password has been reset, and an notification email
    has been sent out to you, which contains a random password for your account.
    Please change your password as soon as possible by login to our website.</font>
  </p>

  </c:when>

  <c:otherwise>
  <!-- continue reset password form -->

<html:form method="POST" action='/processResetPassword.do' >

  <c:if test="${requestScope.refererUrl != null}">
     <input type="hidden" name="refererUrl" value="${requestScope.refererUrl}">
  </c:if>

  <table width="400">
    <tr>
      <th colspan="2"> Reset Your Password </th>
    </tr>

    <!-- check if there's an error message to display -->
    <c:if test="${requestScope.changePasswordError != null}">
       <tr>
          <td colspan="2">
             <font color="red">${requestScope.resetPasswordError}</font>
          </td>
       </tr>
    </c:if>

    <tr>
      <td align="right" width="100" nowrap>Your email: </td>
      <td align="left"><input type="text" name="email"></td>
    </tr>
    <tr>
       <td colspan="2" align="center"><input type="submit" value="Reset"></td>
    </tr>

  </table>
</html:form>


  </c:otherwise>

</c:choose>

</div>
<site:footer/>
