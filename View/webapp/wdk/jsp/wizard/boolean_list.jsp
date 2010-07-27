<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="model" value="${applicationScope.wdkModel}" />
<c:set var="siteName" value="${model.name}" />
<c:set var="qSetName" value="none" />
<c:set var="qSets" value="${model.questionSetsMap}" />
<c:set var="qSet" value="${qSets[qSetName]}" />
<c:set var="user" value="${sessionScope.wdkUser}"/>
<c:set var="wizards" value="${model.wizardModel.wizards}" />
<c:set var="wizard" value="${requestScope.wizard}" />
<c:set var="stage"  value="${requestScope.stage}" />
<c:set var="step"   value="${requestScope.step}" />



<c:set var="rcName" value="${step.type}" />

<wdk:addStepCrumbs wizard="${wizard}" stage="${stage}" />

<div id="sections">
	<table id="sections-layout"><tr>
		<td id="section-1">
		<div class="qf_section" id="step_type">
				<ul class="menu_section">
					<c:forEach items="${wizards}" var="wizard">
						<c:url var="wizardUrl" value="/wizard.do?wizard=${wizard.name}&stage=&label=${wizard.firstStage.name}" />
						<li class="category" onclick="callWizard('${wizardUrl}',this,'${wizard.name}',2);">${wizard.display}</li>
					</c:forEach>	
				</ul>
			</div>
		</td>
		<td id="section-2"><div class="qf_section"></div></td>
		<td id="section-3"><div class="qf_section"></div></td>
		<td id="section-4"><div class="qf_section"></div></td>
		<td id="section-5"><div class="qf_section"></div></td>	
	</tr></table>
	</div> <!--End Section Div-->
	<div id="sections_data">
	



<div class="original" id="boolean" style="display:none">
			<ul class="menu_section">
				<li class="category" onclick="callWizard(null,this,'boolean_searches',3)">Run a new Search</li>
				<li class="category" onclick="callWizard(null,this,'boolean_strategies',3)">Add existing Strategy</li>
				<c:if test="${recordClass.hasBasket}">
					<c:set var="q" value="${recordClass.snapshotBasketQuestion}" />
					<c:url var="wizardUrl" value="/wizard.do?questionFullName=${q.fullName}&wizard=${wizard.name}&stage=list&label=basket" />
					<li style="width:auto;z-index:40;" onclick="callWizard('${wizardUrl}')">Add the Basket</li>
				</c:if>
			</ul>
</div>

<div class="original" id="boolean_searches" style="display:none">
	<ul class="menu_section">
		<c:set var="rootCat" value="${model.websiteRootCategories[rcName]}" />
		<c:choose>
		<c:when test="${rootCat.multiCategory}">
		<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
    		<c:set var="cat" value="${catEntry.value}" />
			<li class="category" onclick="callWizard(null,this,'${cat.name}',4)">${cat.displayName}</li>
		</c:forEach>
		</c:when>
		<c:otherwise>
		<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
    	<c:set var="cat" value="${catEntry.value}" />
		<c:forEach items="${cat.websiteQuestions}" var="q">
			<c:url var="wizardUrl" value="/wizard.do?questionFullName=${q.fullName}&wizard=${wizard.name}&stage=list&label=question" />
			<li onclick="callWizard('${wizardUrl}')">${q.displayName}</li>
		</c:forEach>
		</c:forEach>
		</c:otherwise>
		</c:choose>
	</ul>
</div>

<c:set var="rootCat" value="${model.websiteRootCategories[rcName]}" />
<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
	<c:set var="cat" value="${catEntry.value}" />
	<div class="original" id="${cat.name}" style="display:none">
		<ul class="menu_section">
			<c:forEach items="${cat.websiteQuestions}" var="q">
				<c:url var="wizardUrl" value="/wizard.do?questionFullName=${q.fullName}&wizard=${wizard.name}&stage=list&label=question" />
				<li onclick="callWizard('${wizardUrl}')">${q.displayName}</li>
			</c:forEach>
		</ul>
	</div>
</c:forEach>

<div class="original" id="boolean_strategies" style="display:none">
	<ul class="menu_section">
		<li class="category" onclick="callWizard(null,this,'boolean_open',4)">Opened Strategies</li>
		<c:set var="cls" value=""/>
		<c:set var="cats" value=""/>
		<c:if test="${fn:length(user.savedStrategiesByCategory[rcName]) > 0}">
			<c:set var="cls" value="callWizard(null,this,'boolean_saved',4)"/>
			<c:set var="cats" value="category"/>
		</c:if>
		<li class="${cats}" onclick="${cls}">Saved Strategies (${fn:length(user.savedStrategiesByCategory[rcName])})</li>
		<c:set var="clr" value=""/>
		<c:set var="catr" value=""/>
		<c:if test="${fn:length(user.recentStrategiesByCategory[rcName]) > 0}">
			<c:set var="clr" value="callWizard(null,this,'boolean_recent',4)"/>
			<c:set var="catr" value="category"/>
		</c:if>
		<li class="${catr}" onclick="${clr}">Recent Strategies (${fn:length(user.recentStrategiesByCategory[rcName])})</li>
	</ul>
</div>
<div class="original" id="boolean_open" style="display:none">
			<ul class="menu_section">
				<c:forEach items="${user.activeStrategies}" var="storedStrategy">
		 			<c:if test="${storedStrategy.type == rcName}">
						<c:set var="displayName" value="${storedStrategy.name}" />
						<c:if test="${fn:length(displayName) > 30}">
                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                        </c:if>			
						<c:url var="wizardUrl" value="/wizard.do?strategy=${storedStrategy.strategyId}&wizard=${wizard.name}&stage=list&label=strategy" />
						<li><a href="javascript:void(0)" onclick="callWizard('${wizardUrl}')">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
					</c:if>
				</c:forEach>
			</ul>
</div>
		<!-- Display the Saved Strategies -->
		
<div class="original" id="boolean_saved" style="display:none">
			<ul class="menu_section">
				<c:forEach items="${user.savedStrategiesByCategory[rcName]}" var="storedStrategy">
					<c:set var="displayName" value="${storedStrategy.name}" />
					<c:if test="${fn:length(displayName) > 30}">
                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                    </c:if>
					<c:url var="wizardUrl" value="/wizard.do?strategy=${storedStrategy.strategyId}&wizard=${wizard.name}&stage=list&label=strategy" />
					<li><a href="javascript:void(0)" onclick="callWizard('${wizardUrl}')">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
				</c:forEach>
			</ul>
</div>
		<!-- Display the recent Strategies (Opened  viewed in the last 24 hours) -->
		
<div class="original" id="boolean_recent" style="display:none">
			<ul class="menu_section">
				<c:forEach items="${user.recentStrategiesByCategory[rcName]}" var="storedStrategy">
					<c:set var="displayName" value="${storedStrategy.name}" />
					<c:if test="${fn:length(displayName) > 30}">
                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                    </c:if>
					<c:url var="wizardUrl" value="/wizard.do?strategy=${storedStrategy.strategyId}&wizard=${wizard.name}&stage=list&label=strategy" />
					<li><a href="javascript:void(0)" onclick="callWizard('${wizardUrl}')">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
				</c:forEach>
			</ul>
</div>
</div><!-- Sections Data -->
<%--<ul>
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
          </nested:root>
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
        <a href="javascript:callWizard('${strategyUrl}')">${strategy.name}</a>
      </c:forEach>
    </ul>
  </li>
</ul>
--%>