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
<div id="title-links" class="h3left">
  <span id="text_step_count">${wdkAnswer.displayResultSize}</span>
  <span id="text_data_type">${recordName}</span>

  <c:if test="${strategy != null}">
    from Step <span id="text_step_number">${strategy.length}</span> <br/>Strategy:
    <span
      class="wdk-editable strategy-name"
      data-id="${strategy.strategyId}"
      data-save="wdk.strategy.controller.updateStrategyName"
      id="text_strategy_number"
      title="Click to edit">${strategy.name}</span>
  </c:if>
</div>
