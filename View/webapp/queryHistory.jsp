<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- get wdkUser saved in session scope -->
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>
<c:set var="userAnswers" value="${wdkUser.recordAnswerMap}"/>

<site:header banner="History" />

<c:choose>
  <c:when test="${wdkUser.answerCount == 0}">

<table align="center"><tr><td> *** your history is empty *** </td></tr></table>

  </c:when>
  <c:otherwise>

<!-- show user answers grouped by RecordTypes -->

<c:forEach items="${userAnswers}" var="recAnsEntry">
<c:set var="rec" value="${recAnsEntry.key}"/>
<c:set var="recAns" value="${recAnsEntry.value}"/>
<h3>Answers of record type: ${rec}</h3>

  <!-- show user answers one per line -->
  <c:set var="NAME_TRUNC" value="80"/>
  <table border="0" cellpadding="2">
      <tr><th>ID</th> <th>Name</th> <th>Report</th> <th>Download</th> <th>&nbsp;</th></tr>
      <c:forEach items="${recAns}" var="ua">
        <jsp:setProperty name="ua" property="nameTruncateTo" value="${NAME_TRUNC}"/>
        <tr><td>${ua.answerID}</td>
            <td><c:choose>
                  <c:when test="${ua.isNameTruncatable}">
                    <a href="">${ua.truncatedName}...</a>
                  </c:when>
                  <c:otherwise>${ua.name}</c:otherwise>
                </c:choose></td>
            <td><a href="showSummary.do?user_answer_id=${ua.answerID}">View this answer</a></td>
            <td><a href="downloadHistoryAnswer.do?user_answer_id=${ua.answerID}">Download this answer</a></td>
            <td><a href="deleteHistoryAnswer.do?user_answer_id=${ua.answerID}">delete</a></td>
        </tr>
      </c:forEach>

      <tr><td colspan="2" align="left">
            <br>
            <html:form method="get" action="/processBooleanExpression.do">
              Combine answers in the query history:
              <html:text property="booleanExpression" value=""/> (eg. "#1 or (#2 and #3)")
              <br>
              <html:reset property="reset" value="Clear Expression"/>
              <html:submit property="submit" value="Get Combined Answer"/>
            </html:form>
          </td>
          <td colspan="3"></td></tr>
  </table>

</c:forEach>

  </c:otherwise>
</c:choose>

<site:footer/>
