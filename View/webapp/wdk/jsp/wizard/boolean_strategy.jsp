<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="strategy" value="${requestScope.strategy}" />
<c:set var="step" value=${requestScope.step}" />
<c:set var="otherStratId" value="${requestScope.otherStratId}" />

<wdk:addStepheader />
<wdk:addStepCrumb totalStages="2" currentStage="1" stage="${stage}" />

<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processFilter.do">
<%-- the following sections are copied from <question.tag>, need to refactor into a separate tag --%>
<input type="hidden" name="insertStrategy" value="${otherStratId}"/>
<input type="hidden" name="strategy" value="${strategy.strategyId}"/>
<input type="hidden" name="state" value="..."/>
<input type="hidden" name="checksum" value="..."/>
<input type="hidden" name="strategy_checksum" value="..."/>

<div id="operations">
    <table>
      <tr>
        <td class="opcheck" valign="middle"><input name="booleanExpression" value="INTERSECT" type="radio"></td>
        <td class="operation INTERSECT"></td><td valign="middle">&nbsp;1&nbsp;<b>INTERSECT</b>&nbsp;2</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td class="opcheck"><input name="booleanExpression" value="UNION" type="radio"></td>
        <td class="operation UNION"></td><td>&nbsp;1&nbsp;<b>UNION</b>&nbsp;2</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td class="opcheck"><input name="booleanExpression" value="NOT" type="radio"></td>
        <td class="operation MINUS"></td><td>&nbsp;1&nbsp;<b>MINUS</b>&nbsp;2</td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td class="opcheck"><input name="booleanExpression" value="RMINUS" type="radio"></td>
        <td class="operation RMINUS"></td><td>&nbsp;2&nbsp;<b>MINUS</b>&nbsp;1</td>
      </tr>
    </table>
</div>

<div class="filter-button"><html:submit property="questionSubmit" value="Get Answer"/></div>
</html:form>
