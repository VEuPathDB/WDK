<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

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
<c:choose>
    <c:when test="${wdkAnswer.isBoolean}">
        <div>
            <%-- boolean question --%>
            <nested:root name="wdkAnswer">
                <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
            </nested:root>
	    </div>
    </c:when>
    <c:otherwise>
       <wdk:showParams wdkAnswer="${wdkAnswer}" />
    </c:otherwise>
</c:choose>

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
