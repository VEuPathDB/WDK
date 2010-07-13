<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="model"  value="${applicationScope.wdkModel}" />
<c:set var="wizard" value="${requestScope.wizard}" />
<c:set var="stage"  value="${requestScope.stage}" />
<c:set var="step"   value="${requestScope.step}" />

<wdk:addStepheader />
<wdk:addStepCrumb totalStages="2" currentStage="1" stage="${stage}" />

<ul>
  <li>
    <div>Select a new search</div>
    <ul>
      <c:set var="rootCategories" value="${model.websiteRootCategories}" />
      <c:set var="categories" value="${rootCategories[step.type]}" />
      <c:forEach items="${categories}" var="item">
        <c:set var="category" value="${item.value}" />
        <li class="category">
          <div>${category.name}</div>
          <c:url var="wizardUrl" value="/wizard.do?wizard=${wizard.name}" />
          <a href="javascript:callWizard('${wizardUrl}')">${wizard.display}</a>
          <div class="description">${wizard.description}</div>
        </li>
      </c:forEach>
    </ul>
  </li>
  <li>
    <div>Select an existing strategy</div>
    <ul>
      
    </ul>
  </li>
</ul>
