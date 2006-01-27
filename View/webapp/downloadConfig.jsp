<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

<!-- get wdkAnswer from requestScope -->
<jsp:useBean id="wdkUser" scope="session" type="org.gusdb.wdk.model.jspwrap.UserBean"/>
<c:set value="${sessionScope.wdkAnswer}" var="wdkAnswer"/>
<c:set var="ua_id" value="${requestScope.userAnswerId}"/>

<!-- display page header -->
<site:header banner="Create and download a report" />

<!-- display description for page -->
<p><b>Generate a tab delimited text report of selected attributes for the question below, with an optional header line of attribute names.</b></p>

<!-- display question and param values and result size for wdkAnswer -->
<table>

<c:choose>
  <c:when test="${wdkAnswer.isCombinedAnswer}">
    <!-- combined answer from history boolean expression -->
    <tr><td valign="top" align="left"><b>Combined Answer:</b></td>
        <td valign="top" align="left">${wdkAnswer.userAnswerName}</td></tr>
  </c:when>
  <c:otherwise>

    <c:choose>
      <c:when test="${wdkAnswer.isBoolean}">
        <!-- boolean question -->

        <tr><td valign="top" align="left"><b>Expanded Question:</b></td>
                   <td valign="top" align="left">
                     <nested:root name="wdkAnswer">
                       <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
                     </nested:root>
                   </td></tr>
      </c:when>
      <c:otherwise>
        <!-- simple question -->
        <c:set value="${wdkAnswer.params}" var="params"/>
        <c:set value="${wdkAnswer.question.displayName}" var="wdkQuestionName"/>
        <tr><td valign="top" align="left"><b>Query:</b></td>
                   <td valign="top" align="left">${wdkQuestionName}</td></tr>
               <tr><td valign="top" align="left"><b>Parameters:</b></td>
                   <td valign="top" align="left">
                     <table>
                       <c:forEach items="${params}" var="p">
                         <tr><td align="right">${p.key}:</td><td><i>${p.value}</i></td></tr> 
                       </c:forEach>
                     </table></td></tr>
      </c:otherwise>
    </c:choose>

  </c:otherwise>
</c:choose>
</table>

<hr>

<!-- handle empty result set situation -->
<c:choose>
  <c:when test='${wdkAnswer.resultSize == 0}'>
    No results for your query
  </c:when>
  <c:otherwise>

<!-- content of current page -->
<html:form method="get" action="configDownload">
  <table>
  <tr><td valign="top"><b>Attributes:</b></td>
      <td><table>
          <c:set var="numPerLine" value="2"/>
          <c:set var="i" value="0"/>

          <tr><td colspan="${numPerLine}">
          <html:multibox property="selectedFields">all</html:multibox>
          Default (same as in <a href="showSummary.do?user_answer_id=${ua_id}">result</a>), or...
          </td></tr>
          <tr><td colspan="${numPerLine}">&nbsp;</td></tr>

          <tr>
          <c:forEach items="${wdkAnswer.allReportMakerAttributes}" var="rmAttr">
            <c:set var="i" value="${i+1}"/>
            <c:set var="br" value=""/>
            <c:if test="${i % numPerLine == 0}"><c:set var="br" value="</tr><tr>"/></c:if>
            <td><html:multibox property="selectedFields">
                  ${rmAttr.name}
                </html:multibox>
                  ${rmAttr.displayName}</td>${br}
          </c:forEach>
          <c:if test="${i % numPerLine != 0 }">
              <c:set var="j" value="${i}"/>
              <c:forEach begin="${i+1}" end="${i+numPerLine}" step="1">
                  <c:set var="j" value="${j+1}"/>
                  <c:if test="${j % numPerLine != 0}"><td></td></c:if>
              </c:forEach>
              </tr>
          </c:if>
          </table>
        </td></tr>
  <tr><td valign="top"><b>Header line: </b></td>
      <td><html:radio property="includeHeader" value="yes">include</html:radio>
          <html:radio property="includeHeader" value="no">exclude</html:radio>
        </td></tr>
  <tr><td colspan="2">&nbsp;</td></tr>
  <tr><td></td>
      <td><html:submit property="downloadConfigSubmit" value="Continue"/>
          <html:reset property="downloadConfigReset" value="Reset"/>
      </td></tr></table>
</html:form>

  </c:otherwise>
</c:choose>

<site:footer/>
