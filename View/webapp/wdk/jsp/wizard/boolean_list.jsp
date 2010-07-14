<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<c:set var="model"  value="${applicationScope.wdkModel}" />
<c:set var="user"   value="${sessionScope.wdkUser}" />
<c:set var="wizard" value="${requestScope.wizard}" />
<c:set var="stage"  value="${requestScope.stage}" />
<c:set var="step"   value="${requestScope.step}" />

<c:set var="type" value="${step.type}" />


<wdk:addStepheader />
<wdk:addStepCrumb totalStages="2" currentStage="1" stage="${stage}" />

<ul>
  <li>
    <div>Select a new search</div>
    <ul>
      <c:set var="rootCategories" value="${model.websiteRootCategories}" />
      <c:set var="categories" value="${rootCategories[type]}" />
      <c:forEach items="${categories}" var="item">
        <c:set var="category" value="${item.value}" />
        <li class="category">
          <nested:root name="category">
            <jsp:include page="show_category.jsp"/>
          </nested:root/
        </li>
      </c:forEach>
    </ul>
  </li>
  <li>
    <div>Select an existing strategy</div>
    <ul>
      <c:set var="allStrategies" value="${user.strategiesByCategory}" />
      <c:set var="strategies" value="${allStrategies[type]}" />
      <c:forEach items="${strategies}" var="strategy">
        <c:url var="strategyUrl" value="/wizard.do?wizard=boolean&step=strategy&strategy=${strategy.strategyId}" />
        <a href="javascript:callWizard('${strategyUrl}')">${strategy.display}</a>
      </c:forEach>
    </ul>
  </li>
</ul>
