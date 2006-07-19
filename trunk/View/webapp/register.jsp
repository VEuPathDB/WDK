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
<c:choose>
  <c:when test="${requestScope.registerSucceed != null}">

  <h1>
    <b>You have registered successfully.</b>
  </h1>

  <p>Please check your email box for the temporary password, and please 
     change it as soon as possible after logging into
     <a href="<c:url value='/'/>">The website</a>.
  </p>

  </c:when>

  <c:otherwise>
  <!-- continue registration fomr -->

<html:form method="POST" action='/processRegister.do' >

  <c:if test="${requestScope.refererUrl != null}">
     <input type="hidden" name="refererUrl" value="${requestScope.refererUrl}">
  </c:if>

  <table width="400">
    <tr>
      <th colspan="2"> Registration Form </th>
    </tr>

<c:choose>
  <c:when test="${wdkUser != null && wdkUser.guest != true}">

    <tr>
      <td colspan="2">You have already logged in. <br>
	If you want to register a new account, please log out first and click on the "Register" link.</td>
    </tr>

  </c:when>

  <c:otherwise>

    <!-- check if there's an error message to display -->
    <c:if test="${requestScope.registerError != null}">
       <tr>
          <td colspan="2">
             <font color="red">${requestScope.registerError}</font>
          </td>
       </tr>
    </c:if>

    <tr>
      <td align="right" width="100" nowrap>Email: </td>
      <td align="left"><input type="text" name="email" value="${requestScope.email}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>First Name: </td>
      <td align="left"><input type="text" name="firstName" value="${requestScope.firstName}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Middle Name: </td>
      <td align="left"><input type="text" name="middleName" value="${requestScope.middleName}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Last Name: </td>
      <td align="left"><input type="text" name="lastName" value="${requestScope.lastName}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Title: </td>
      <td align="left"><input type="text" name="title" value="${requestScope.title}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Organization: </td>
      <td align="left"><input type="text" name="organization" value="${requestScope.organization}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Department: </td>
      <td align="left"><input type="text" name="department" value="${requestScope.department}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Address: </td>
      <td align="left"><input type="text" name="address" value="${requestScope.address}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>City: </td>
      <td align="left"><input type="text" name="city" value="${requestScope.city}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>State: </td>
      <td align="left"><input type="text" name="state" value="${requestScope.state}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Zip Code: </td>
      <td align="left"><input type="text" name="zipCode" value="${requestScope.zipCode}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Phone Number: </td>
      <td align="left"><input type="text" name="phoneNumber" value="${requestScope.phoneNumber}"></td>
    </tr>
    <tr>
      <td align="right" width="100" nowrap>Country: </td>
      <td align="left"><input type="text" name="country" value="${requestScope.country}"></td>
    </tr>
    <tr>
       <td colspan="2" align="center"><input type="submit" value="Register"></td>
    </tr>

  </c:otherwise>

</c:choose>

  </table>
</html:form>


  </c:otherwise>

</c:choose>

</div>
<site:footer/>
