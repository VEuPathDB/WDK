<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set var="model" value="${applicationScope.wdkModel}" />
<c:set var="siteName" value="${model.name}" />
<c:set var="qSetName" value="none" />
<c:set var="qSets" value="${model.questionSetsMap}" />
<c:set var="qSet" value="${qSets[qSetName]}" />
<c:set var="user" value="${sessionScope.wdkUser}"/>
<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="recordClass" value="${step.question.recordClass}" />
<c:set var="strategyId" value="${requestScope.wdkStrategy}" />
<%-- the type is of the previous step, that is the input type of the new step  
<c:set var="recordClass" value="${model.recordClassMap[rcName]}" />--%>

<c:if test="${isAdd == 'false'}">
    <%-- insert a step in between, the transform cannot change type in this case --%>
    <jsp:setProperty name="recordClass" property="changeType" value="false" />
</c:if>

<div id="query_form" style="min-height:140px;">
	<wdk:addStepHeader/>
	<div id="qf_content">
	<div id="sections">
		<table id="sections-layout"><tr>
			<td id="section-1">
				<div id="span_logic" class="qf_section">
					<ul class="menu_section">
						<li class="category" onclick="callWizard(null,this,'sl_recordclasses',2)">Run a new Search</li>
						<li class="category" onclick="callWizard(null,this,'sl_strategies',2)">Add existing Strategy</li>
						<li>Add the Basket</li>
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
		</div>
		</div><!--Content Div -->
		<div class="bottom-close">
			<a class='close_window' href='javascript:closeAll(false)'>Close</a>
		</div>
	</div><!-- End of Query Form Div -->
<!--TRANSFORMS SECTION-->		
	<%--	<div class="original" id="transforms" style="display:none">
			<ul class="menu_section">
				<c:forEach items="${transformQuestions}" var="t">
			      <jsp:setProperty name="t" property="inputType" value="${rcName}" />
			      <c:set var="tparams" value="" />
			      <c:forEach items="${t.transformParams}" var="tp">
				<c:set var="tparams" value="${tparams}&${tp.name}=${prevStepNum}" />
			      </c:forEach>
			      <li onclick="callWizard('wizard.do?questionFullName=${q.fullName}&strategy=${strategyId.strategyId}&step=${step.stepId}&stage=transform')">${t.displayName}</li>
			    </c:forEach>
			</ul>
		</div>
--%>
<!-- SPAN LOGIC SECTION -->
		<div id="span_logic" style="display:none" class="original">
			<ul class="menu_section">
				<li class="category" onclick="callWizard(null,this,'sl_recordclasses',2)">Run a new Search</li>
				<li class="category" onclick="callWizard(null,this,'sl_strategies',2)">Add existing Strategy</li>
				<li>Add the Basket</li>
			</ul>
		</div>

		<div class="original" id="sl_strategies" style="display:none">
			<ul class="menu_section">
				<li class="category" onclick="callWizard(null,this,'sl_open',3)">Opened Strategies</li>
				<c:set var="cls" value=""/>
				<c:set var="cats" value=""/>
				<c:set var="count_s" value=""/>
				<c:forEach items="${model.websiteRootCategories}" var="rcs">
					<c:set var="count_s" value="${count_s + fn:length(user.savedStrategiesByCategory[rcs.value.name])}"/>
				</c:forEach>
				<c:if test="${count_s > 0}">
					<c:set var="cls" value="showNewSection(this,'sl_saved',3)"/>
					<c:set var="cats" value="category"/>
				</c:if>
				<li class="${cats}" onclick="${cls}">Saved Strategies</li>
				<c:set var="clr" value=""/>
				<c:set var="catr" value=""/>
				<c:set var="count_r" value=""/>
				<c:forEach items="${model.websiteRootCategories}" var="rcs">
					<c:set var="count_r" value="${count_r + fn:length(user.recentStrategiesByCategory[rcs.value.name])}"/>
				</c:forEach>
				<c:if test="${count_r > 0}">
					<c:set var="clr" value="showNewSection(this,'sl_recent',3)"/>
					<c:set var="catr" value="category"/>
				</c:if>
				<li class="${catr}" onclick="${clr}">Recent Strategies</li>
			</ul>
		</div>
		
		<div class="original" id="sl_open" style="display:none">
					<ul class="menu_section">
						<c:forEach items="${user.activeStrategies}" var="storedStrategy">
								<c:set var="displayName" value="${storedStrategy.name}" />
								<c:if test="${fn:length(displayName) > 30}">
		                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
		                        </c:if>
								<li><a href="javascript:void(0)" onclick="callWizard('wizard.do?strategyId=${storedStrategy.strategyId}&strategy=${strategyId.strategyId}&step=${step.stepId}&stage=strategy')">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
						</c:forEach>
					</ul>
				</div>
				<!-- Display the Saved Strategies -->
				
			<div class="original" id="sl_saved" style="display:none">
					<ul class="menu_section">
						<c:forEach items="${model.websiteRootCategories}" var="rcs">
							<c:forEach items="${user.savedStrategiesByCategory[rcs.value.name]}" var="storedStrategy">
								<c:set var="displayName" value="${storedStrategy.name}" />
								<c:if test="${fn:length(displayName) > 30}">
			                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
			                    </c:if>
								<li><a href="javascript:void(0)" onclick="callWizard('wizard.do?strategyId=${storedStrategy.strategyId}&strategy=${strategyId.strategyId}&step=${step.stepId}&stage=strategy')">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
							</c:forEach>
						</c:forEach>
					</ul>
				</li>
					<!-- Display the recent Strategies (Opened  viewed in the last 24 hours) -->

				<div class="original" id="sl_recent" style="display:none">
					<ul class="menu_section">
						<c:forEach items="${model.websiteRootCategories}" var="rcs">
							<c:forEach items="${user.recentStrategiesByCategory[rcs.value.name]}" var="storedStrategy">
								<c:set var="displayName" value="${storedStrategy.name}" />
								<c:if test="${fn:length(displayName) > 30}">
			                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
			                    </c:if>
								<li><a href="javascript:void(0)" onclick="callWizard('wizard.do?strategyId=${storedStrategy.strategyId}&strategy=${strategyId.strategyId}&step=${step.stepId}&stage=strategy')">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
							</c:forEach>
						</c:forEach>
					</ul>
				</div>
		

		<div id="sl_recordclasses" class="original">
			<ul class="menu_section">
			<c:forEach var="rcs" items="${model.websiteRootCategories}">
				<c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
<c:if test="${fn:containsIgnoreCase(rcs.value.displayName, 'gene') || 
		fn:containsIgnoreCase(rcs.value.displayName, 'orf') || 
		fn:containsIgnoreCase(rcs.value.displayName, 'snp') ||
		fn:containsIgnoreCase(rcs.value.displayName, 'isolate')}">
				<li class="category" onclick="callWizard(null,this,'sl_${classId}',3)">${rcs.value.displayName}</li>
</c:if>
			</c:forEach>
			</ul>
		</div>
		
		<c:forEach var="rcs" items="${model.websiteRootCategories}">
			<c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
			<div class="original" id="sl_${classId}" style="display:none">
				<ul class="menu_section">
					<c:choose>
					<c:when test="${rcs.value.multiCategory}">
					<c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
			    		<c:set var="cat" value="${catEntry.value}" />
						<li class="category" onclick="callWizard(null,this,'sl_${cat.name}',4)">${cat.displayName}</li>
					</c:forEach>
					</c:when>
					<c:otherwise>
					<c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
			    	<c:set var="cat" value="${catEntry.value}" />
					<c:forEach items="${cat.websiteQuestions}" var="q">
						<li onclick="callWizard('wizard.do?questionFullName=${q.fullName}&strategy=${strategyId.strategyId}&step=${step.stepId}&stage=question')">${q.displayName}</li>
					</c:forEach>
					</c:forEach>
					</c:otherwise>
					</c:choose>
				</ul>
				
				
			</div>
		</c:forEach>
		
		<c:forEach var="rcs" items="${model.websiteRootCategories}">
			<c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
    			<c:set var="cat" value="${catEntry.value}" />
				<div class="original" id="sl_${cat.name}" style="display:none">
					<ul class="menu_section">
						<c:forEach items="${cat.websiteQuestions}" var="q">
        					<li onclick="callWizard('wizard.do?questionFullName=${q.fullName}&strategy=${strategyId.strategyId}&step=${step.stepId}&stage=question')">${q.displayName}</li>
						</c:forEach>
					</ul>
				</div>
			</c:forEach>
		</c:forEach>
		
	</div>

	



	<%-- the first column 
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
				</div>
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
	</div>--%>

