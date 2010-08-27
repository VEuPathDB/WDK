<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/function" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="model" value="${applicationScope.wdkModel}" />
<c:set var="user" value="${sessionScope.wdkUser}" />
<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="recordClass" value="${step.question.recordClass}" />
<c:set var="stratgyId" value="${requestScope.strategy}" />

<%-- the first column --%>
<div id="wizard-list">
  <ul>
    <li>
      <div class="title" onclick="showMenu(this, 1)">Choose a search</div>
      <c:set var="categories" value="${wdkModel.websiteRootCategories}" />
      <ul>
        <c:forEach items="${categories}" var="category">
          <li>
            <div class="title" onclick="showMenu(this, 2)">${category.key}</div>
            <c:set var="subCats" value="${category.value.websiteChildren}" />
            <c:if test="${fn:length(subCats) > 0}">
              <ul>
                <c:forEach items="${subCat}" var="subCat">
              </ul>
            </c:if>
          </li>
        </c:forEach>
      </ul>
    </li>
    <li onclick="showMenu(this)">Choose a strategy</li>
    <li onclick="showMenu(this)">Choose from basket</li>
    <c:url var="url" value="/wizard.do?strategy=${strategyId}&step=${step.stepId}&stage=transform&questionFullName=InternalQuestions.GenesByOrthologs&gene_result=${step.stepId}" />
    <li onclick="showWizard('${url}')">Ortholog</li>
    <c:url var="url" value="/wizard.do?strategy=${strategyId}&step=${step.stepId}&stage=transform&questionFullName=InternalQuestions.GenesByWeightFilter&gene_result=${step.stepId}" />
    <li onclick="showWizard('${url}')">Limit by weight</li>
  </ul>
</div>


