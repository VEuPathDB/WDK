<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
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

$(function() {
  "use strict";
  $(document.forms.downloadConfigForm).submit(function(e) {
    e.preventDefault();
    this.target = $("input[name='downloadType'][value='plain']", this)
        .is(":checked") ? "_blank" : "_self";
    this.submit();
  });
});
//-->
</script>

<c:set var="type" value="${step.question.recordClass.type}" />

<jsp:useBean id="typeMap" class="java.util.HashMap"/>
<c:set target="${typeMap}" property="singular" value="${step.question.recordClass.type}"/>
<imp:getPlural pluralMap="${typeMap}"/>
<c:set var="type" value="${typeMap['plural']}"/>
<div class="h2center">Download ${step.estimateSize} ${type} from the search:</div>

<i><div class="h3center">${step.displayName}</div></i>

<br><br>
<b>Please select a format from the dropdown list to create the download report. </b>
<br><i>**Note:  ${type} IDs will automatically be included in the report.</i>
<!-- handle empty result set situation -->
<c:choose>
<c:when test='${step.estimateSize != 0}'>

<br /><br />
<!-- the supported format -->
<form name="formatForm" method="get" action="<c:url value='/downloadStep.do' />">
	<input type="hidden" name="step_id" value="${step_id}"/>
	<select name="wdkReportFormat" onChange="return changeFormat();">
          	<option value="">--- Select a format ---</option>
          	<c:forEach items="${formats}" var="fmt">
             		<option value="${fmt.key}" ${(fmt.key == format) ? "selected" : ""}>${fmt.value}</option>
          	</c:forEach>
        </select>
</form>
<hr>
</c:when> <%-- end of ${step.estimateSize != 0} --%>

<c:otherwise>
  	This search doesn't contain any result.
</c:otherwise>
</c:choose>
