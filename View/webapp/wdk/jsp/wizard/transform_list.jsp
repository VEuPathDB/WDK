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

<wdk:addStepCrumbs wizard="${wizard}" stage="${stage}"/>

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


	<c:set var="recordClass" value="${model.recordClassMap[rcName]}" />
	<c:if test="${isAdd == 'false'}">
	    <%-- insert a step in between, the transform cannot change type in this case --%>
	    <jsp:setProperty name="recordClass" property="changeType" value="false" />
	</c:if>		
	<c:set var="transformQuestions" value="${recordClass.transformQuestions}" />		
<div class="original" id="transform" style="display:none">
	<ul class="menu_section">
		<c:forEach items="${transformQuestions}" var="t">
	      <jsp:setProperty name="t" property="inputType" value="${rcName}" />
	      <c:set var="tparams" value="" />
	      <c:forEach items="${t.transformParams}" var="tp">
		<c:set var="tparams" value="${tparams}&${tp.name}=${prevStepNum}" />
	      </c:forEach>
	      <li onclick="getQueryForm('showQuestion.do?questionFullName=${t.fullName}${tparams}&partial=true', true)">${t.displayName}</li>
	    </c:forEach>
	</ul>
</div>

</div><!-- Sections Data -->
