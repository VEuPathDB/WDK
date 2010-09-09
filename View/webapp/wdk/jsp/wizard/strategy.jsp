<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<c:set var="strategy" value="${requestScope.wdkStrategy}"/>
<c:set var="step" value="${requestScope.wdkStep}"/>
<c:set var="insertStrategyId" value="${requestScope.insertStrategy}" />
<c:set var="insertStep" value="${requestScope.insertStep}" />


<c:set var="wizard" value="${requestScope.wizard}"/>
<c:set var="stage" value="${requestScope.stage}"/>

<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processFilter.do"  onsubmit="callWizard('wizard.do?,this,null,null,'submit')">

<input type="hidden" name="insertStrategy" value="${insertStrategyId}"/>


<div id="operations">
    <table>
     <c:if test="${step.type == insertStep.type}">
          <tr>
            <td class="opcheck" valign="middle"><input onclick="changeButtonText(this)" name="booleanExpression" value="INTERSECT" type="radio"></td>
            <td class="operation INTERSECT"></td><td valign="middle">&nbsp;1&nbsp;<b>INTERSECT</b>&nbsp;2</td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <td class="opcheck"><input onclick="changeButtonText(this)" name="booleanExpression" value="UNION" type="radio"></td>
            <td class="operation UNION"></td><td>&nbsp;1&nbsp;<b>UNION</b>&nbsp;2</td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <td class="opcheck"><input onclick="changeButtonText(this)" name="booleanExpression" value="NOT" type="radio"></td>
            <td class="operation MINUS"></td><td>&nbsp;1&nbsp;<b>MINUS</b>&nbsp;2</td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <td class="opcheck"><input onclick="changeButtonText(this)" name="booleanExpression" value="RMINUS" type="radio"></td>
            <td class="operation RMINUS"></td><td>&nbsp;2&nbsp;<b>MINUS</b>&nbsp;1</td>
         </tr>
         <tr><td colspan="15" align="center"><hr><b>OR</b><hr></td></tr>
     </c:if>
     <tr>	
		<td class="opcheck" valign="middle"><input onclick="changeButtonText(this)" name="booleanExpression" value="SPAN" type="radio"></td>
        <td class="operation SPAN"></td><td valign="middle">&nbsp;&nbsp;<b>Span Logic</b></td>
        <td colspan="12" align="left">&nbsp;&nbsp;&nbsp;Combine using span and regional alignments</td>
      </tr>
    </table>
</div>
<c:choose>
    <c:when test="${step.type == insertStep.type}">
        <div id="span_button" class="filter-button" style="display:none"><html:submit property="questionSubmit" value="Continue"/></div>
        <div id="boolean_button" class="filter-button"><html:submit property="questionSubmit" value="Get Answer"/></div>
    </c:when>
    <c:otherwise>
        <div id="span_button" class="filter-button"><html:submit property="questionSubmit" value="Continue"/></div>
    </c:otherwise>
</c:choose>
</html:form>
