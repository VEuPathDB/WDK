<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="pluralMap"
              required="true"
              type="java.util.Map"
%>

<%-- Get the plural form of the "singular" property, store it in the "plural" property --%>
<c:set var="value" value="${pluralMap['singular']}"/>
<c:choose>
	<c:when test="${fn:endsWith(value,'y')}">
		<c:set var="suffix" value="ies" />
	</c:when>
	<c:otherwise>
		<c:set var="suffix" value="s" />
	</c:otherwise>	
</c:choose>
<c:set target="${pluralMap}" property="plural" value="${value}${suffix}" />
