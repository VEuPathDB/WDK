<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="tags" value="${requestScope.tags}" />
<c:set var="wordOrder" value="${requestScope.wordOrder}" />
<c:set var="countOrder" value="${requestScope.countOrder}" />

<script type="text/javascript" src="<c:url value='/wdk/js/wordCloud.js' />"></script>
<div id="word-cloud">
  <div id="tags">
    <c:forEach items="${tags}" var="tag">
      <span word="${tag.word}" count="${tag.count}" weight="${tag.weight}" score="${tag.score}">${tag.word}</span>
    </c:forEach>
  </div>
  <div id="word-order">
    <c:forEach items="${wordOrder}" var="word">
	  <span>${word}</span>
	</c:forEach>
  </div>
  <div id="count-order">
    <c:forEach items="${countOrder}" var="word">
	  <span>${word}</span>
	</c:forEach>
  </div>
  <div>
    # of words: <b>more</b> <span id="amount"> </span> <b>less</b>
  </div>
  <div>
    Sort by:
    <input type="radio" id="sort" value="word-order" />Alphabetic
    <input type="radio" id="sort" value="count-order" />Weight
  </div>
  <div id="layout"> </div>
</div>
