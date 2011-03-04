<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<c:set var="wdkQuestion" value="${requestScope.question}"/>
<c:set var="spanOnly" value="false"/>
<c:set var="checked" value=""/>
<c:set var="buttonVal" value="Run Step"/>
<c:set var="wdkStrategy" value="${requestScope.wdkStrategy}"/>
<c:set var="wdkStep" value="${requestScope.wdkStep}"/>
<c:set var="allowBoolean" value="${requestScope.allowBoolean}"/>
<c:set var="action" value="${requestScope.action}"/>

<c:if test="${wdkQuestion.recordClass.fullName != wdkStep.dataType}">
	<c:set var="checked" value="checked=''"/>
	<c:set var="buttonVal" value="Continue...."/>
	<c:set var="spanOnly" value="true"/>
</c:if>

<c:set var="webProps" value="${wdkQuestion.propertyLists['websiteProperties']}" />
<c:set var="hideOperation" value="${false}" />
<c:forEach var="prop" items="${webProps}">
  <c:choose>
    <c:when test="${prop == 'hideOperation'}"><c:set var="hideOperation" value="${true}" /></c:when>
  </c:choose>
</c:forEach>

<c:set var="wizard" value="${requestScope.wizard}"/>
<c:set var="stage" value="${requestScope.stage}"/>


<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processFilter.do" onsubmit="callWizard('wizard.do?action=${requestScope.action}&step=${wdkStep.stepId}&',this,null,null,'submit')">

<html:hidden property="stage" styleId="stage" value="${nextStage}" />

<span style="display:none" id="strategyId">${wdkStrategy.strategyId}</span>
<c:choose>
    <c:when test="${wdkStep.previousStep == null || action != 'revise'}">
        <c:set var="stepId" value="${wdkStep.stepId}" />
    </c:when>
    <c:otherwise>
        <c:set var="stepId" value="${wdkStep.previousStep.stepId}" />
    </c:otherwise>
</c:choose>
<span style="display:none" id="stepId">${stepId}</span>

<c:set var="Question_Header" scope="request">
<%-- has nothing --%>
</c:set>

<c:set var="Question_Footer" scope="request">
<%-- displays question description, can be overridden by the custom question form --%>
<wdk:questionDescription />
</c:set>

${Question_Header}

<%-- display question param section --%>
<div class="filter params">
  <span class="form_subtitle">
    <c:choose>
      <c:when test="${action == 'add'}">
        Add Step ${wdkStep.frontId + 1}
      </c:when>
      <c:when test="${action == 'insert'}">
        Insert Step ${wdkStep.frontId + 1}
      </c:when>
      <c:otherwise>
        Revise Step ${wdkStep.frontId}
      </c:otherwise>
    </c:choose>
    : ${wdkQuestion.displayName}
  </span>

  <wdk:questionForm />
</div>


<c:if test="${hideOperation == false}">


<%-- display operators section --%>
<c:set var="type" value="${wdkStep.shortDisplayType}" />
<c:set var="allowSpan" value="${type eq 'Gene' || type eq 'Orf' || type eq 'SNP' || type eq 'Isolate'}" />

<div class="filter operators">
  <c:choose>
    <c:when test="${(wdkStep.isTransform || wdkStep.previousStep == null) && action == 'revise'}">
       <c:set var="nextStage" value="process_question" />
    </c:when>

    <c:otherwise>
      <c:if test="${wdkStep.previousStep != null && action == 'revise'}">
        <c:set var="wdkStep" value="${wdkStep.previousStep}" />
      </c:if>
      <h1>Combine ${wdkStep.displayType}s in Step <span class="current_step_num"></span> with ${wdkQuestion.recordClass.displayName}s in Step <span class="new_step_num"></span>:</h1>
      <div style="text-align:center" id="operations">
                <c:choose>
                    <c:when test="${allowBoolean == false}">
                        <c:set var="nextStage" value="span_from_question" />
                        <c:set var="disabled" value="DISABLED"/>
                <c:set var="opaque" value="opacity:0.3;filters:alpha(opacity=30);"/>
                    <%--    <p><i>Set operations are not available because Step <span class="current_step_num"></span> is a set of ${wdkStep.displayType}s while Step <span class="new_step_num"></span> is a set of ${wdkQuestion.recordClass.displayName}s; these are disjoint sets</i></p> --%>
                <c:set var="explanation" value="Set operations are not available because your steps are of different types, and do not have IDs in common." />
                    </c:when>
                    <c:otherwise>
                        <c:set var="nextStage" value="process_boolean" />
                    </c:otherwise>
                </c:choose>

        <table>
            <tr style="${opaque}" title="${explanation}">

            <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="INTERSECT" type="radio" stage="process_boolean" ${disabled}></td>
            <td class="operation INTERSECT"></td>
            <td >&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Intersect</b>&nbsp;<span class="new_step_num"></span></td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

                <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="UNION" type="radio" stage="process_boolean" ${disabled}></td>
                <td class="operation UNION"></td>
            <td>&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Union</b>&nbsp;<span class="new_step_num"></span></td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

                <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="NOT" type="radio" stage="process_boolean" ${disabled}></td>
                <td class="operation MINUS"></td>
            <td>&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Minus</b>&nbsp;<span class="new_step_num"></span></td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

                <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="RMINUS" type="radio" stage="process_boolean" ${disabled}></td>
                <td class="operation RMINUS"></td>
            <td>&nbsp;<span class="new_step_num"></span>&nbsp;<b style="font-size:120%">Minus</b>&nbsp;<span class="current_step_num"></span></td>

                </tr>
        </table>

        <c:if test="${allowSpan}">
            <table style="margin-top:10px;">
              <tr>	
<%--           	<td  colspan="11" style="text-align:left;">&nbsp;Genomic regions for ${wdkStep.displayType}s in Step <span class="current_step_num"></span>&nbsp;&nbsp;<span style="font-size:120%;font-weight:bold">Overlap</span>&nbsp; Genomic regions for ${wdkQuestion.recordClass.displayName}s in Step <span class="new_step_num"></span></td>
               <td style="text-align:left;padding-right:10px">&nbsp;<span style="font-size:120%;font-weight:bold">Use Genomic locations</span></td>
               <td style="padding-right:10px" class="operation SPAN overlap"></td>
               <td style="padding-right:10px" class="operation SPAN a_contain_b"></td>
               <td class="operation SPAN b_contain_a"></td>
--%>
  		<td class="opcheck" valign="middle"><input ${checked} onclick="changeButtonText(this)" name="boolean" value="SPAN" type="radio" stage="span_from_question"></td>
		<td class="operation SPAN overlap"></td>
		<td>&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Relative to</b>&nbsp;<span class="new_step_num"></span> <span style="font-size:120%">, using genomic locations</span></td>
              </tr>
<%--
	      <tr>
		<td class="opcheck" valign="middle"><input ${checked} onclick="changeButtonText(this)" name="boolean" value="SPAN" type="radio" stage="span_from_question"></td>
		<td class="operation SPAN overlap"></td>
		<td>&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Overlap</b>&nbsp;<span class="new_step_num"></span> <b style="font-size:120%">using genomic locations</b></td>
              </tr>
--%>
        </table>
        </c:if>


    </div>
    </c:otherwise>
  </c:choose>
</div>


<div id="boolean_button" class="filter-button">
    <html:submit property="questionSubmit" value="${buttonVal}"/>
</div>

</c:if> <%-- End of hideOperation --%>

</html:form>


${Question_Footer}
