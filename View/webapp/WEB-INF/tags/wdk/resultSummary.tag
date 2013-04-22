<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="strategy"
              type="org.gusdb.wdk.model.jspwrap.StrategyBean"
              required="true"
              description="Strategy bean we are looking at"
%>
<%@ attribute name="step"
              type="org.gusdb.wdk.model.jspwrap.StepBean"
              required="true"
              description="Step bean we are looking at"
%>

<c:set var="wdkModel" value="${applicationScope.wdkModel}" />
<c:set var="wdkUser" value="${sessionScope.wdkUser}" />
<c:set var="projectId" value="${wdkModel.projectId}" />
<c:set var="dispModelName" value="${wdkModel.displayName}" />
<c:set var="wdkAnswer" value="${step.answerValue}"/>
<c:set var="recordClass" value="${wdkAnswer.question.recordClass}" />
<c:set var="recordName" value="${recordClass.fullName}" />
<c:set var="recHasBasket" value="${recordClass.useBasket}" />
<c:set var="recordName" value="${wdkStep.recordClass.displayNamePlural}"/>

<!-- ================ RESULTS TITLE AND LINKS TO BASKET AND DOWNLOADS   =============== -->
<table id = "title-links" width="100%"><tr>


<td class="h3left" style="vertical-align:middle;padding-bottom:7px;">
  <span id="text_step_count">${wdkAnswer.resultSize} </span><span id="text_data_type">${recordName}</span>&nbsp;from<br>
  <c:if test="${strategy != null}">
    Step <span id="text_step_number">${strategy.length}</span> of Strategy:	
			<span 
          class="wdk-editable strategy-name" 
          data-id="${strategy.strategyId}"
          data-save="wdk.strategy.controller.updateStrategyName" 
          id="text_strategy_number" 
          title="Click to edit">
				${strategy.name}</span>
  </c:if>
</td>

<td  style="vertical-align:middle;text-align:right;white-space:nowrap;">
  <div style="float:right">
   <c:set var="r_count" value="${wdkAnswer.resultSize} ${recordName}" />
   <c:if test="${strategy != null}">
    <c:choose>
      <c:when test="${wdkUser.guest}">
        <c:set var="basketClick" value="wdk.user.login();" />
      </c:when>
      <c:otherwise>
        <c:set var="basketClick" value="wdk.basket.updateBasket(this, '${step.stepId}', '0', '0', '${recordName}');" /> <!-- fourth param is unused (basket.js) -->
      </c:otherwise>
    </c:choose>
    <c:if test="${recHasBasket}"><a id="basketStep" style="font-size:120%" href="javascript:void(0)" onClick="${basketClick}"><b>Add ${r_count} to Basket</b></a>&nbsp;|&nbsp;</c:if>
   </c:if>
    <a style="font-size:120%" href="downloadStep.do?step_id=${step.stepId}&signature=${wdkUser.signature}"><b>Download ${r_count}</b></a>
  <c:if test="${!empty sessionScope.GALAXY_URL}">
    &nbsp;|&nbsp;<a href="downloadStep.do?step_id=${step.stepId}&wdkReportFormat=tabular"><b>SEND TO GALAXY</b></a>
  </c:if>
  </div>
</td>
</tr></table>

