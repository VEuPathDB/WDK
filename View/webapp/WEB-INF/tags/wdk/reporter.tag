<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>


<c:set value="${requestScope.wdkStep}" var="step"/>
<c:set var="step_id" value="${requestScope.step_id}"/>

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

<c:set var="type" value="${step.question.recordClass.type}" />

<!-- display question and param values and result size for step -->
<%-- wdk:showParams step="${step}" / --%>

<c:if test="${fn:contains(type, 'Assem') && (step.estimateSize > 1)  }">
        <c:set var="type" value="Assemblie" />
</c:if>

<hr>

<h2><center>Download ${type}s from the ${step.displayName} search:</center></h2> 
<p><center>${step.question.displayName}, ${step.estimateSize} ${type}(s)</center></p>

<!-- handle empty result set situation -->
<c:choose>
<c:when test='${step.estimateSize != 0}'>

<br />
<!-- the supported format -->
<form name="formatForm" method="get" action="<c:url value='/downloadStep.do' />">
  <table>
    <tr>
      <td>
        <b>Format:</b>
        <input type="hidden" name="step_id" value="${step_id}"/>
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

</c:when> <%-- end of ${step.estimateSize != 0} --%>
<c:otherwise>
  The step doesn't contain any result.
</c:otherwise>
</c:choose>
