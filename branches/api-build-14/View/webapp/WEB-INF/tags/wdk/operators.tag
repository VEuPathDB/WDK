<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="allowSpan"
              required="true"
              description=""
%>
<%@ attribute name="operation"
              required="true"
              description=""
%>
<%@ attribute name="spanStage"
              required="true"
              description=""
%>

<%--
I think we do not really need operation, it can be read from param.operation
--%>

<c:set var="action" value="${requestScope.action}"/>
<c:set var="newStepId">
  <c:choose>
    <c:when test="${action == 'add'}">${wdkStep.frontId + 1}</c:when>
    <c:otherwise>${wdkStep.frontId}</c:otherwise>
  </c:choose>
</c:set>
<c:set var="currentStepId" value="${newStepId - 1}" />

<%--  Set operations are not available because your steps are of different types, and do not have IDs in common. --%>
<c:choose>
<c:when test="${allowBoolean == false}">
                <c:set var="disabled" value="DISABLED"/>
                <c:set var="opaque" value="opacity:0.3;filter:alpha(opacity=30);"/> 
                <c:set var="explanation" value="
Set operations (intersect, union and minus) are not available (grayed out) because your most recent search returns IDs that are not the same type as those in  your previous result.  Set operations only work between sets of IDs that have the same type (e.g., a set of Genes with a set of Genes). You may still combine your new search with your previous results by using -Relative to-, which uses relative locations on the genome. Relative operations work on mixed results such as Genes and SNPs.
"/>
	<c:set var="intersectHelp" value="
		${explanation}
	"/>
	<c:set var="unionHelp" value="
		${explanation}
	"/>
	<c:set var="minusHelp" value="
		${explanation}
	"/>

</c:when>
<c:otherwise>
	<c:set var="intersectHelp" value="
		Use -Intersect- to combine the results of your new search with your previous results, keeping only records that are in both.
	"/>
	<c:set var="unionHelp" value="
		Use -Union- to combine the results of your new search with your previous results, keeping all records from both.
	"/>
	<c:set var="minusHelp" value="
		Use -Minus- to combine the results of your new search with your previous results, keeping only the records found in one, but not the other.
	"/>
</c:otherwise>
</c:choose>

<c:set var="relativeToHelp" value="
	Use -Relative to- to combine the results of your new search with your previous results, using relative genomic location. In a following screen you will be prompted to specify details of genomic relationships (such as contains or overlaps), distances apart and which step to return results from.
"/>

      <center><table>
	   <tr title="${explanation}"> 

            <c:set var="checked"><c:if test="${param.operation == 'INTERSECT'}">checked="checked"</c:if></c:set>
            <td style="${opaque}" title="${intersectHelp}" class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="INTERSECT" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td style="${opaque}"  title="${intersectHelp}" class="operation INTERSECT"></td>
            <td style="${opaque}" title="${intersectHelp}" >&nbsp;<span class="current_step_num">${currentStepId}</span>&nbsp;<b style="font-size:120%">Intersect</b>&nbsp;<span class="new_step_num">${newStepId}</span></td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

            <c:set var="checked"><c:if test="${param.operation == 'NOT'}">checked="checked"</c:if></c:set>
            <td style="${opaque}"  title="${minusHelp}" class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="NOT" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td style="${opaque}" title="${minusHelp}" class="operation MINUS"></td>
            <td style="${opaque}" title="${minusHelp}" >&nbsp;<span class="current_step_num">${currentStepId}</span>&nbsp;<b style="font-size:120%">Minus</b>&nbsp;<span class="new_step_num">${newStepId}</span></td>
      </tr>
      <tr title="${explanation}">

 	    <c:set var="checked"><c:if test="${param.operation == 'UNION'}">checked="checked"</c:if></c:set>
            <td style="${opaque}" title="${unionHelp}" class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="UNION" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td style="${opaque}" title="${unionHelp}" class="operation UNION"></td>
            <td style="${opaque}" title="${unionHelp}" >&nbsp;<span class="current_step_num">${currentStepId}</span>&nbsp;<b style="font-size:120%">Union</b>&nbsp;<span class="new_step_num">${newStepId}</span></td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

            <c:set var="checked"><c:if test="${param.operation == 'RMINUS'}">checked="checked"</c:if></c:set>
            <td style="${opaque}" title="${minusHelp}" class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="RMINUS" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td style="${opaque}" title="${minusHelp}" class="operation RMINUS"></td>
            <td style="${opaque}" title="${minusHelp}" >&nbsp;<span class="new_step_num">${newStepId}</span>&nbsp;<b style="font-size:120%">Minus</b>&nbsp;<span class="current_step_num">${currentStepId}</span></td>

      </tr>


<%-- SPAN LOGIC OPERATION IS POSSIBLE --%>
       <c:if test="${allowSpan}">
      <tr title="${relativeToHelp}" >	
        <c:set var="checked"><c:if test="${param.operation == 'SPAN' || allowBoolean == false}">checked="checked"</c:if></c:set>
  	<td class="opcheck" valign="middle"><input ${checked} onclick="changeButtonText(this)" name="boolean" value="SPAN" type="radio" stage="${spanStage}"></td>
	<td class="operation SPAN overlap"></td>
	<td colspan="5">&nbsp;<span class="current_step_num">${currentStepId}</span>&nbsp;<b style="font-size:120%">Relative to</b>&nbsp;<span class="new_step_num">${newStepId}</span> <span style="font-size:120%">, using genomic colocation</span></td>
      </tr>
      </c:if>

      </table></center>

