<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<c:set var="strategy" value="${requestScope.wdkStrategy}"/>
<c:set var="importStrategy" value="${requestScope.importStrategy}" />
<c:set var="importStep" value="${requestScope.importStep}" />
<c:set var="allowBoolean" value="${requestScope.allowBoolean}"/>
<c:set var="wdkStep" value="${requestScope.wdkStep}"/>
<c:set var="action" value="${requestScope.action}"/>

<c:set var="spanOnly" value="false"/>
<c:set var="checked" value=""/>
<c:set var="buttonVal" value="Get Answer"/>


<c:if test="${importStep.dataType != wdkStep.dataType}">
	<c:set var="checked" value="checked=''"/>
	<c:set var="buttonVal" value="Continue"/>
	<c:set var="spanOnly" value="true"/>
</c:if>


<c:set var="wizard" value="${requestScope.wizard}"/>
<c:set var="stage" value="${requestScope.stage}"/>

<%-- determine current & next front step id --%>
<c:choose>
  <c:when test="${action eq 'add'}">
    <c:set var="rightFrontId" value="${wdkStep.frontId + 1}" />
  </c:when>
  <c:otherwise>
    <c:set var="rightFrontId" value="${wdkStep.frontId}" />
  </c:otherwise>
</c:choose>  
<c:set var="leftFrontId" value="${rightFrontId - 1}" />

<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processFilter.do"  onsubmit="callWizard('wizard.do?',this,null,null,'submit')">



<%-- display question param section --%>
<div class="filter params">
  <input type="hidden" name="importStrategy" value="${importStrategy.strategyId}"/>

  <div class="form_subtitle">
    Add Step ${wdkStep.frontId + 1} from existing strategy: ${importStrategy.name}
  </div>
  <br />
  <br />
</div>
  
  
  
<%-- display operators section --%>
<c:set var="type" value="${wdkStep.shortDisplayType}" />
<c:set var="allowSpan" value="${type eq 'Gene' || type eq 'Orf' || type eq 'SNP' || type eq 'Isolate'}" />

<div class="filter operators">
  <c:choose>
    <c:when test="${(wdkStep.isTransform || wdkStep.previousStep == null) && action == 'revise'}">
       <c:set var="nextStage" value="process_question" />
    </c:when>

    <c:otherwise>
      <h1>Combine ${wdkStep.displayType}s in Step ${leftFrontId} with ${importStrategy.displayType}s in Step ${rightFrontId}:</h1>
      <div style="text-align:center" id="operations">
                <c:choose>
                    <c:when test="${allowBoolean == false}">
                        <c:set var="nextStage" value="span_from_strategy" />
                        <c:set var="disabled" value="DISABLED"/>
                <c:set var="opaque" value="opacity:0.3;filters:alpha(opacity=30);"/>
                    <%--    <p><i>Set operations are not available because Step ${leftFrontId} is a set of ${wdkStep.displayType}s while Step ${rightFrontId} is a set of ${wdkQuestion.recordClass.displayName}s; these are disjoint sets</i></p> --%>
                <c:set var="explanation" value="Set operations are not available because your steps are of different types, and do not have IDs in common." />
                    </c:when>
                    <c:otherwise>
                        <c:set var="nextStage" value="process_boolean" />
                    </c:otherwise>
                </c:choose>

        <table style="margin-left:auto; margin-right:auto;">
            <tr style="${opaque}" title="${explanation}">

            <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="INTERSECT" type="radio" stage="process_boolean" ${disabled}></td>
            <td class="operation INTERSECT"></td>
            <td >&nbsp;${leftFrontId}&nbsp;<b style="font-size:120%">Intersect</b>&nbsp;${rightFrontId}</td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

                <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="UNION" type="radio" stage="process_boolean" ${disabled}></td>
                <td class="operation UNION"></td>
            <td>&nbsp;${leftFrontId}&nbsp;<b style="font-size:120%">Union</b>&nbsp;${rightFrontId}</td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

                <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="NOT" type="radio" stage="process_boolean" ${disabled}></td>
                <td class="operation MINUS"></td>
            <td>&nbsp;${leftFrontId}&nbsp;<b style="font-size:120%">Minus</b>&nbsp;${rightFrontId}</td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

                <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="RMINUS" type="radio" stage="process_boolean" ${disabled}></td>
                <td class="operation RMINUS"></td>
            <td>&nbsp;${rightFrontId}&nbsp;<b style="font-size:120%">Minus</b>&nbsp;${leftFrontId}</td>

                </tr>
            <tr style="${opaque}" title="${explanation}"><td colspan="15" style="text-align:center;font-size:120%;font-weight:bold;padding:10px;">Or</td></tr>
        </table>

        <c:if test="${allowSpan}">
            <table style="margin-left:auto; margin-right:auto;">
              <tr>	
               <td class="opcheck" valign="middle"><input ${checked} onclick="changeButtonText(this)" name="boolean" value="SPAN" type="radio" stage="span_from_strategy"></td>
               
               <%--	<td  colspan="11" style="text-align:left;">&nbsp;Genomic regions for ${wdkStep.displayType}s in Step ${leftFrontId}&nbsp;&nbsp;<span style="font-size:120%;font-weight:bold">Overlap</span>&nbsp; Genomic regions for ${wdkQuestion.recordClass.displayName}s in Step ${rightFrontId}</td> --%>
               <td style="text-align:left;padding-right:10px">&nbsp;<span style="font-size:120%;font-weight:bold">Use Genomic locations</span></td>
               <td style="padding-right:10px" title="Combine results (in your last step and the new step) using span and regional alignments" class="operation SPAN overlap"></td>
               <td style="padding-right:10px" title="Combine results (in your last step and the new step) using span and regional alignments" class="operation SPAN a_contain_b"></td>
               <td title="Combine results (in your last step and the new step) using span and regional alignments" class="operation SPAN b_contain_a"></td>

              </tr>
            </table>
        </c:if>

      </div>
    </c:otherwise>
  </c:choose>
</div>

<html:hidden property="stage" styleId="stage" value="${nextStage}" />

<div id="boolean_button" class="filter-button"><html:submit property="questionSubmit" value="${buttonVal}"/></div>
</html:form>
