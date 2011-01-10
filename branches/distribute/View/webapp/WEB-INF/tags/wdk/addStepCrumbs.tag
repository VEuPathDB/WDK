<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="wizard"
			  required="false"
     		  type="org.gusdb.wdk.model.wizard.Wizard"
			  description="Wizard that is being displayed"
%>

<%@ attribute name="stage"
              required="false"
              type="org.gusdb.wdk.model.wizard.Stage"
              description="The current stage"
%>

<c:set var="stageCount" value="${fn:length(wizard.stages)}"/>

<div id="process-crumbs">
	<c:set var="stepImage" value="step_bw_1.png"/>
	<c:set var="dispClass" value=""/>
	<c:if test="${stage == null}">
		<c:set var="stepImage" value="step1.png"/>
		<c:set var="dispClass" value="active"/>
	</c:if>
	<div id="pStep_1" class="${dispClass}">
		<img class="step-image" src="<c:url value="wdk/images/${stepImage}"/>"/>
        <p>Please select a Wizard</p>
	</div>
	<c:if test="${stageCount > 1}">
		<img class="transition" id="${i}-${i+1}" src="<c:url value="wdk/images/sStepTrans.png"/>"/>
	</c:if>
	<c:set var="i" value="2"/>
	<c:forEach var="st" items="${wizard.stages}">
	  <c:if test="${i > 0}">
		<c:set var="stepImage" value="step_bw_${i}.png"/>
		<c:set var="dispClass" value=""/>
		<c:if test="${stage.name == st.name}">
			<c:set var="stepImage" value="step${i}.png"/>
			<c:set var="dispClass" value="active"/>
		</c:if>
		<div id="pStep_${i}" class="${dispClass}">
			<img class="step-image" src="<c:url value="wdk/images/${stepImage}"/>"/>
            <c:choose>
				<c:when test="${stage != null}">
					<p>${st.display}</p>
				</c:when>
				<c:otherwise>
					<p>Stage ${i}</p>
				</c:otherwise>
			</c:choose>
		</div>
		<c:if test="${st.branched}"><c:set var="i" value="-1"/></c:if>
		<c:if test="${i < stageCount}">
			<img class="transition" id="${i}-${i+1}" src="<c:url value="wdk/images/sStepTrans.png"/>"/>
		</c:if>
		<c:set var="i" value="${i+1}"/>
	  </c:if>
	</c:forEach>
</div>
