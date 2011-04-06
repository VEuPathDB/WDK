<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%@ attribute name="allowSpan"
              required="true"
              description=""
%>
<%@ attribute name="operation"
              required="true"
              description=""
%>


<c:if test="${allowBoolean == false}">
                <c:set var="disabled" value="DISABLED"/>
                <c:set var="opaque" value="opacity:0.3;filters:alpha(opacity=30);"/>
                <c:set var="explanation" value="Set operations are not available because your steps are of different types, and do not have IDs in common." />
</c:if>


      <center><table>

       <tr style="${opaque}" title="${explanation}">

            <c:set var="checked"><c:if test="${param.operation == 'INTERSECT'}">checked="checked"</c:if></c:set>
            <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="INTERSECT" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td class="operation INTERSECT"></td>
            <td >&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Intersect</b>&nbsp;<span class="new_step_num"></span></td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

            <c:set var="checked"><c:if test="${param.operation == 'NOT'}">checked="checked"</c:if></c:set>
            <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="NOT" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td class="operation MINUS"></td>
            <td>&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Minus</b>&nbsp;<span class="new_step_num"></span></td>
      </tr>
      <tr style="${opaque}" title="${explanation}">

 	    <c:set var="checked"><c:if test="${param.operation == 'UNION'}">checked="checked"</c:if></c:set>
            <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="UNION" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td class="operation UNION"></td>
            <td>&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Union</b>&nbsp;<span class="new_step_num"></span></td>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

            <c:set var="checked"><c:if test="${param.operation == 'RMINUS'}">checked="checked"</c:if></c:set>
            <td class="opcheck"><input onclick="changeButtonText(this)" name="boolean" value="RMINUS" type="radio" stage="process_boolean" ${disabled} ${checked}></td>
            <td class="operation RMINUS"></td>
            <td>&nbsp;<span class="new_step_num"></span>&nbsp;<b style="font-size:120%">Minus</b>&nbsp;<span class="current_step_num"></span></td>

      </tr>


<%-- SPAN LOGIC OPERATION IS POSSIBLE --%>
       <c:if test="${allowSpan}">
      <tr>	
        <c:set var="checked"><c:if test="${param.operation == 'SPAN'}">checked="checked"</c:if></c:set>
  	<td class="opcheck" valign="middle"><input ${checked} onclick="changeButtonText(this)" name="boolean" value="SPAN" type="radio" stage="span_from_question"></td>
	<td class="operation SPAN overlap"></td>
	<td colspan="5">&nbsp;<span class="current_step_num"></span>&nbsp;<b style="font-size:120%">Relative to</b>&nbsp;<span class="new_step_num"></span> <span style="font-size:120%">, using genomic locations</span></td>
      </tr>
      </c:if>

      </table></center>

