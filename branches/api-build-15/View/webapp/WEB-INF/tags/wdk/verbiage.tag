<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="key"
              type="java.lang.String"
              required="true"
              description="Identifier for string" %>

<c:set var="keyParts" value="${fn:split(key, '.')}"/>

<c:if test="${fn:length(keyParts) == 3}">
    <c:set var="id" value="${keyParts[0]}.${keyParts[1]}"/>
    <c:set var="attr" value="${keyParts[2]}"/>
    <c:out value="${wdkModel.xmlQuestionSetsMap['XmlQuestions'].questionsMap['Verbiage'].fullAnswer.recordInstanceMap[id].attributesMap[attr]}" default="" escapeXml="false" />
</c:if>
