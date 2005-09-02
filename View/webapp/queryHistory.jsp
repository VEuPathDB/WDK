<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- get wdkUser saved in session scope -->
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>
<c:set var="userAnswers" value="${wdkUser.answers}"/>

<site:header banner="History" />

<hr>

<table>
    <tr><td>Answer History (UserID = ${wdkUser.userID}</td></tr>
</table>

<!-- show user answers one per line -->
<c:set var="NAME_TRUNC" value="80"/>
<table>
    <tr><th>ID</th> <th>Name</th> <th>Report</th> <th>Download</th> <th>&nbsp;</th></tr>
    <c:forEach items="${userAnswers}" var="ua">
      <jsp:setProperty name="ua" property="nameTruncateTo" value="${NAME_TRUNC}"/>
      <tr><td>${ua.answerID}</td>
          <td><c:choose>
                <c:when test="${ua.isNameTruncatable}">
                  <a href="">${ua.truncatedName}...</a>
                </c:when>
                <c:otherwise>${ua.name}</c:otherwise>
              </c:choose></td>
          <td><a href='<c:url value="/showAnswer.jsp"/>'>View this answer</a></td>
          <td><a href='<c:url value="/downloadAnswer.jsp"/>'>Download this answer</a></td>
          <td>delete</td>
      </tr>
    </c:forEach>
</table>

<html:form method="get" action="/processBooleanExpression.do">
    <html:text property="booleanExpression" value="#1 or (#2 and $3)"/>
</html:form>

<site:footer/>
