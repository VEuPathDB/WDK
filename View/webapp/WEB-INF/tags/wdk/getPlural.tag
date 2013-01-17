<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="pluralMap"
              required="true"
              type="java.util.Map"
%>

<%-- Get the plural form of the "singular" property, store it in the "plural" property --%>
<c:set var="value" value="${pluralMap['singular']}"/>
<c:set var="plural" value="${value}s" />
<c:set target="${pluralMap}" property="plural" value="${plural}" />

