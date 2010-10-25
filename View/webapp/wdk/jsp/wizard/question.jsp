<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<c:set var="wdkQuestion" value="${requestScope.question}"/>
<c:set var="spanOnly" value="false"/>
<c:set var="checked" value=""/>
<c:set var="buttonVal" value="Get Answer"/>
<c:set var="wdkStep" value="${requestScope.wdkStep}"/>
<c:set var="isRevise" value="false"/>
<c:set var="allowBoolean" value="${requestScope.allowBoolean}"/>
<c:set var="action" value="${requestScope.action}"/>

<c:if test="${wdkQuestion.recordClass.fullName != wdkStep.dataType}">
	<c:set var="checked" value="checked=''"/>
	<c:set var="buttonVal" value="Continue"/>
	<c:set var="spanOnly" value="true"/>
</c:if>

<c:set var="wizard" value="${requestScope.wizard}"/>
<c:set var="stage" value="${requestScope.stage}"/>
<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processFilter.do" onsubmit="callWizard('wizard.do?action=${requestScope.action}&step=${wdkStep.stepId}&',this,null,null,'submit')">

<h3>${wdkQuestion.displayName}</h3>
<%-- the following sections are copied from <question.tag>, need to refactor into a separate tag --%>

<input type="hidden" name="questionFullName" value="${wdkQuestion.fullName}"/>

<!-- show error messages, if any -->
<wdk:errors/>

<%-- the js has to be included here in order to appear in the step form --%>
<script type="text/javascript" src='<c:url value="/wdk/js/wdkQuestion.js"/>'></script>
<script type="text/javascript">
    $(document).ready(function() { initParamHandlers(); });
</script>

<div class="params">
      <wdk:questionParams />
</div> <%-- end of params div --%>		


<%--<c:set target="${helps}" property="${fromAnchorQ}" value="${helpQ}"/>--%>
<%-- set the weight --%>
<div align="center">
  Assign Weight: <html:text property="weight" maxlength="9" />
</div>
<%-- end of the copied content --%>

<c:choose>
    <c:when test="${!wdkStep.isBoolean && wdkStep.parentOrNextStep == null && action == 'revise'}">
        <c:set var="nextStage" value="process_question" />
    </c:when>
    <c:otherwise>
        <div id="operations">
            <c:choose>
                <c:when test="${allowBoolean == false}">
                    <c:set var="nextStage" value="span_from_question" />
                    <c:set var="disabled" value="DISABLED"/>
                    <p>Boolean operations are disabled because the input are of different record types.</p>
                </c:when>
                <c:otherwise>
                    <c:set var="nextStage" value="process_boolean" />
                </c:otherwise>
            </c:choose>
    <table>
      <tr>
        <td class="opcheck" valign="middle"><input onclick="changeButtonText(this)" name="boolean" value="INTERSECT" type="radio" stage="process_boolean" ${disabled}></td>
        <td class="operation INTERSECT"></td><td valign="middle">&nbsp;1&nbsp;<b>INTERSECT</b>&nbsp;2</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="UNION" type="radio" stage="process_boolean" ${disabled}></td>
        <td class="operation UNION"></td><td>&nbsp;1&nbsp;<b>UNION</b>&nbsp;2</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="NOT" type="radio" stage="process_boolean" ${disabled}></td>
        <td class="operation MINUS"></td><td>&nbsp;1&nbsp;<b>MINUS</b>&nbsp;2</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="RMINUS" type="radio" stage="process_boolean" ${disabled}></td>
        <td class="operation RMINUS"></td><td>&nbsp;2&nbsp;<b>MINUS</b>&nbsp;1</td>
      </tr>
	<tr><td colspan="15" align="center"><hr><b>OR</b><hr></td></tr>

	  <tr>	
		<td class="opcheck" valign="middle"><input ${checked} onclick="changeButtonText(this)" name="boolean" value="SPAN" type="radio" stage="span_from_question"></td>
        <td class="operation SPAN"></td><td valign="middle">&nbsp;&nbsp;<b>Span Logic</b></td>
        <td colspan="12" align="left">&nbsp;&nbsp;&nbsp;Combine using span and regional alignments</td>
      </tr>
    </table>
        </div>
    </c:otherwise>
</c:choose>

<html:hidden property="stage" styleId="stage" value="${nextStage}" />
<div id="boolean_button" class="filter-button"><html:submit property="questionSubmit" value="${buttonVal}"/></div>
<div id="description">${wdkQuestion.description}</div>
</html:form>
