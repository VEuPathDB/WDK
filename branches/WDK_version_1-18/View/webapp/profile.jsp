<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.servletsuite.com/servlets/wraptag" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="logic" uri="http://jakarta.apache.org/struts/tags-logic" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>

<script language="JavaScript" type="text/javascript">
<!--
function submit(e)
{
    if (typeof e != 'undefined' && !enter_key_trap(e)) {
        return;
    }

    if (document.profile.firstName.value == "") {
        alert('Please provide your first name.');
        document.profile.firstName.focus();
        return false;
    } else if (document.profile.lastName.value == "") {
        alert('Please provide your last name.');
        document.profile.lastName.focus();
        return false;
    } else if (document.profile.organization.value == "") {
        alert('Please provide the name of the organization you belong to.');
        document.profile.organization.focus();
        return false;
    } else {
        document.profile.saveButton.disabled = true;
        document.profile.submit();
        return true;
    }
}
//-->
</script>



<!-- get user object from session scope -->
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>

<!-- display page header with recordClass type in banner -->
<c:set value="${wdkRecord.recordClass.type}" var="recordType"/>
<site:header banner="${recordType}"/>
<div align="center">

<!-- display the success information, if the user registered successfully -->
<c:if test="${requestScope.profileSucceed != null}">

  <p><font color="blue">Your profile has been updated successfully.</font> </p>

</c:if>

<html:form name="profile" method="POST" action='/processProfile.do' >

  <c:if test="${requestScope.refererUrl != null}">
     <input type="hidden" name="refererUrl" value="${requestScope.refererUrl}">
  </c:if>

  <table width="400">
    <tr>
      <th colspan="2"> User Profile </th>
    </tr>

<c:choose>
  <c:when test="${wdkUser.guest == true}">

    <tr>
      <td colspan="2">Please login to view or update your profile.</td>
    </tr>

  </c:when>

  <c:otherwise>

    <!-- check if there's an error message to display -->
    <c:if test="${requestScope.profileError != null}">
       <tr>
          <td colspan="2">
             <font color="red">${requestScope.profileError}</font>
          </td>
       </tr>
    </c:if>

    <tr>
      <td align="right" width="100" nowrap>Email: </td>
      <td align="left">${wdkUser.email}</td>
    </tr>
    <tr>
      <td align="right" colspan="2" align="center">
         <a href="<c:url value='/showPassword.do'/>">Change Password</a>
      </td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>First Name: </td>
      <td align="left"><input type="text" name="firstName" value="${wdkUser.firstName}" length="20"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Middle Name: </td>
      <td align="left"><input type="text" name="middleName" value="${wdkUser.middleName}" length="20"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Last Name: </td>
      <td align="left"><input type="text" name="lastName" value="${wdkUser.lastName}" length="20"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Title: </td>
      <td align="left"><input type="text" name="title" value="${wdkUser.title}" length="10"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Organization: </td>
      <td align="left"><input type="text" name="organization" value="${wdkUser.organization}" length="50"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Department: </td>
      <td align="left"><input type="text" name="department" value="${wdkUser.department}" length="20"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Address: </td>
      <td align="left"><input type="text" name="address" value="${wdkUser.address}" length="50"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>City: </td>
      <td align="left"><input type="text" name="city" value="${wdkUser.city}" length="20"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>State: </td>
      <td align="left"><input type="text" name="state" value="${wdkUser.state}" length="20"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Zip Code: </td>
      <td align="left"><input type="text" name="zipCode" value="${wdkUser.zipCode}" length="10"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Phone Number: </td>
      <td align="left"><input type="text" name="phoneNumber" value="${wdkUser.phoneNumber}" length="10"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Country: </td>
      <td align="left"><input type="text" name="country" value="${wdkUser.country}" length="20"></td>
    </tr>
    <tr>
       <td colspan="2" align="center"><input name="saveButton" type="submit" value="Save"  onclick="return submit();" ></td>
    </tr>

  </c:otherwise>

</c:choose>

  </table>
</html:form>

</div>
<site:footer/>
