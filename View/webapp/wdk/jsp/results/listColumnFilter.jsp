<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="summary" value="${requestScope.summary}" />
<c:set var="step" value="${requestScope.step}" />

<div class="list-filter">
  <div class="description">
    Please choose from the list to filter the result.
  </div>
  <c:forEach items="${summary.counts}" var="item">
    <div class="item-count">
      <input name="values" type="checkbox" value="${item.key}" />
      <span class="item">${item.key}</span>
      <span class="count">(${item.value})</span>
    </div>
  </c:forEach>
</div>
