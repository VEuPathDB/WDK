<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set value="${requestScope.wdkAnswer}" var="wdkAnswer"/>
<c:set var="history_id" value="${requestScope.wdk_history_id}"/>

<c:set var="formats" value="${requestScope.wdkReportFormats}"/>
<c:set var="format" value="${requestScope.wdkReportFormat}"/>


<script language="JavaScript" type="text/javascript">
<!-- //
function changeFormat(e)
{
    document.formatForm.submit();
    return true;
}
//-->
</script>

<!-- display question and param values and result size for wdkAnswer -->
<table>

<c:choose>
  <c:when test="${wdkAnswer.isBoolean}">
    <!-- combined answer from history boolean expression -->
    <tr><td valign="top" align="left"><b>Combined Answer:</b></td>
        <td valign="top" align="left">${wdkAnswer.customName}</td></tr>
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
<c:if test='${wdkAnswer.resultSize != 0}'>

<!-- the supported format -->
<form name="formatForm" method="get" action="<c:url value='/downloadHistoryAnswer.do' />">
  <table>
    <tr>
      <td>
        <b>Format:</b>
        <input type="hidden" name="wdk_history_id" value="${history_id}"/>
      </td>
      <td>
        <select name="wdkReportFormat" onChange="return changeFormat();">
          <option value="">--- select a format ---</option>
          <c:forEach items="${formats}" var="fmt">
             <option value="${fmt.key}" ${(fmt.key == format) ? "selected" : ""}>${fmt.value}</option>
          </c:forEach>
        </select>
      </td>
    </tr>
  </table>
</form>

<hr>

</c:if>
