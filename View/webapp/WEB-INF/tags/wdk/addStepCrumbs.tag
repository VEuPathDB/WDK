<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="currentStep"
              required="true"
              description="Step to be highlighted"
%>
<c:set var="curr" value="${currentStep}"/>
<div id="process-crumbs">
	<c:forEach begin="1" end="4" var="i">
		<c:set var="stepImage" value="step_bw_${i}.png"/>
		<c:set var="dispClass" value=""/>
		<c:if test="${i == curr}">
			<c:set var="stepImage" value="step${i}.png"/>
			<c:set var="dispClass" value="active"/>
		</c:if>
		<div id="pStep_${i}" class="${dispClass}">
			<img class="step-image" src="<c:url value="wdk/images/${stepImage}"/>"/>
			<c:choose>
				<c:when test="${i == 1}">
					<p>Select Step Type</p>
				</c:when>
				<c:when test="${i == 2}">
					<p>Select Step Search</p>
				</c:when>
				<c:when test="${i == 3}">
					<p>Run Search</p>
				</c:when>
				<c:when test="${i == 4}">
					<p>Select Operations</p>
				</c:when>
			</c:choose>
		</div>
		<c:if test="${i != 4}">
			<img class="transition" id="${i}-${i+1}" src="<c:url value="wdk/images/sStepTrans.png"/>"/>
		</c:if>
	</c:forEach>
</div>
