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
    
    <c:set var="wdkAnswer" value="${wdkModel.xmlQuestionSetsMap['XmlQuestions'].questionsMap['WdkVerbiage'].fullAnswer.recordInstanceMap[id]}"/>
    <c:set var="siteAnswer" value="${wdkModel.xmlQuestionSetsMap['XmlQuestions'].questionsMap['SiteVerbiage'].fullAnswer.recordInstanceMap[id]}"/>

    <c:choose>
      <c:when test="${siteAnswer ne null}">
        <c:out value="${siteAnswer.attributesMap[attr]}" default="" escapeXml="false" />
      </c:when>
      <c:otherwise>
        <c:out value="${wdkAnswer.attributesMap[attr]}" default="" escapeXml="false" />
      </c:otherwise>
    </c:choose>
</c:if>
