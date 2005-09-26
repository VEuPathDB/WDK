<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- get wdkUser saved in session scope -->
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>
<c:set var="userAnswers" value="${wdkUser.recordAnswerMap}"/>

<site:header banner="History" />

<!-- show error messages, if any -->
<wdk:errors/>

<!-- decide whether history is empty -->
<c:choose>
  <c:when test="${wdkUser.answerCount == 0}">

<table align="center"><tr><td> *** Your history is empty! *** </td></tr></table>

  </c:when>
  <c:otherwise>

<!-- show user answers grouped by RecordTypes -->

<c:set var="typeC" value="0"/>
<c:forEach items="${userAnswers}" var="recAnsEntry">
<c:set var="rec" value="${recAnsEntry.key}"/>
<c:set var="recAns" value="${recAnsEntry.value}"/>
<c:set var="recDispName" value="${recAns[0].answer.question.recordClass.type}"/>

  <!-- deciding whether to show only selected sections of history -->
  <c:choose>
    <c:when test="${param.historySectionId != null && param.historySectionId != rec}">
    </c:when>
    <c:otherwise>

<c:set var="typeC" value="${typeC+1}"/>
<c:choose><c:when test="${typeC != 1}"><hr></c:when></c:choose>

<h3>${recDispName} history</h3>

  <!-- show user answers one per line -->
  <c:set var="NAME_TRUNC" value="80"/>
  <table border="0" cellpadding="2">
      <tr class="headerRow"><th>ID</th> <th>Name</th> <th>Report</th> <th>Download</th><th>Size</th><th>&nbsp;</th></tr>

      <c:set var="i" value="0"/>
      <c:forEach items="${recAns}" var="ua">
        <jsp:setProperty name="ua" property="nameTruncateTo" value="${NAME_TRUNC}"/>

        <c:choose>
          <c:when test="${i % 2 == 0}"><tr class="rowLight"></c:when>
          <c:otherwise><tr class="rowDark"></c:otherwise>
        </c:choose>

        <td>${ua.answerID}</td>
            <td><c:choose>
                  <c:when test="${ua.isNameTruncatable}">
                    ${ua.truncatedName}...
                  </c:when>
                  <c:otherwise>
                    ${ua.name}
                  </c:otherwise>
                </c:choose></td>
            <td><a href="showSummary.do?user_answer_id=${ua.answerID}">View Result</a></td>
            <td><a href="downloadHistoryAnswer.do?user_answer_id=${ua.answerID}">Download Data</a></td>
            <td>${ua.answer.resultSize}</td>
            <td><a href="deleteHistoryAnswer.do?user_answer_id=${ua.answerID}">Delete</a></td>
        </tr>
      <c:set var="i" value="${i+1}"/>
      </c:forEach>

      <tr><td colspan="4" align="left">
            <br>
            <html:form method="get" action="/processBooleanExpression.do">
              Combine answers in the query history:
              <html:text property="booleanExpression" value=""/>
                <font size="-1">(eg. "#1 OR (#4 AND #3 NOT #2)", <a href="http://www.ncbi.nlm.nih.gov/entrez/query/static/help/helpdoc.html#Boolean_Operators">see NCBI</a>)</font><br>
              <html:hidden property="historySectionId" value="${rec}"/>
              <html:reset property="reset" value="Clear Expression"/>
              <html:submit property="submit" value="Get Combined Answer"/>
            </html:form>
          </td>
          <td colspan="1"></td></tr>
  </table>

    </c:otherwise>
  </c:choose> <!-- end of deciding sections to show -->

</c:forEach>

  </c:otherwise>
</c:choose> <!-- end of deciding history emptiness -->

<site:footer/>
