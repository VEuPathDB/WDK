<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.servletsuite.com/servlets/wraptag" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="logic" uri="http://jakarta.apache.org/struts/tags-logic" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>

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

<html:form method="POST" action='/processProfile.do' >

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
         <a href="<c:url value='/password.jsp'/>">Change Password</a>
      </td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>First Name: </td>
      <td align="left"><input type="text" name="firstName" value="${wdkUser.firstName}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Middle Name: </td>
      <td align="left"><input type="text" name="middleName" value="${wdkUser.middleName}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Last Name: </td>
      <td align="left"><input type="text" name="lastName" value="${wdkUser.lastName}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Title: </td>
      <td align="left"><input type="text" name="title" value="${wdkUser.title}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Organization: </td>
      <td align="left"><input type="text" name="organization" value="${wdkUser.organization}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Department: </td>
      <td align="left"><input type="text" name="department" value="${wdkUser.department}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Address: </td>
      <td align="left"><input type="text" name="address" value="${wdkUser.address}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>City: </td>
      <td align="left"><input type="text" name="city" value="${wdkUser.city}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>State: </td>
      <td align="left"><input type="text" name="state" value="${wdkUser.state}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Zip Code: </td>
      <td align="left"><input type="text" name="zipCode" value="${wdkUser.zipCode}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Phone Number: </td>
      <td align="left"><input type="text" name="phoneNumber" value="${wdkUser.phoneNumber}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Country: </td>
      <td align="left"><input type="text" name="country" value="${wdkUser.country}"></td>
    </tr>
    <tr>
       <td colspan="2" align="center"><input type="submit" value="Save"></td>
    </tr>

  </c:otherwise>

</c:choose>

  </table>
</html:form>

</div>
<site:footer/>
