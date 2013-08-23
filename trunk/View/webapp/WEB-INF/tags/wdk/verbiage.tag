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

    <c:set var="xmlQuestions" value="${wdkModel.xmlQuestionSetsMap['XmlQuestions']}"/>
    <c:set var="siteVerbiage" value="${xmlQuestions.questionsMap['SiteVerbiage']}"/>
    <c:set var="wdkVerbiage" value="${xmlQuestions.questionsMap['WdkVerbiage']}"/>

    <c:if test="${siteVerbiage ne null}">
      <c:set var="siteAnswer" value="${siteVerbiage.fullAnswer.recordInstanceMap[id]}"/>
    </c:if>

    <c:if test="${wdkVerbiage ne null}">
      <c:set var="wdkAnswer" value="${wdkVerbiage.fullAnswer.recordInstanceMap[id]}"/>
    </c:if>

    <c:choose>
      <c:when test="${siteAnswer ne null}">
        <c:out value="${siteAnswer.attributesMap[attr]}" default="" escapeXml="false" />
      </c:when>
      <c:otherwise>
        <c:out value="${wdkAnswer.attributesMap[attr]}" default="" escapeXml="false" />
      </c:otherwise>
    </c:choose>

</c:if>
