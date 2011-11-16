<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="wdkUser" value="${sessionScope.wdkUser}" />
<c:set var="baskets" value="${requestScope.baskets}" />
<c:set var="total" value= "${0}" />
<c:forEach items="${baskets}" var="item">
  <c:set var="total" value="${total + item.value}" />
</c:forEach>

<script> $(configureBasket); </script>

<div id="basket-control-panel">
  <wdk:basketControls />
</div>

<c:choose>
  <c:when test="${total > 0}">
    <div id="basket-menu" class="tabs">
      <ul>
        <c:set var="index" value="${0}" />
        <c:forEach items="${baskets}" var="item">
          <c:set var="recordClass" value="${item.key}" />
          <c:set var="count" value="${item.value}" />
          <c:if test="${count > 0}">
            <li id="${fn:replace(recordClass.fullName, '.', '_')}" 
                tab-index="${index}" recordClass="${recordClass.fullName}">
              <a title="Basket_View" 
                 href="<c:url value='/showBasket.do?recordClass=${recordClass.fullName}'/>"
                >${recordClass.displayName} (<span class="count">${item.value}</span>)</a>
            </li>
            <c:set var="index" value="${index + 1}" />
          </c:if>
        </c:forEach>
      </ul>

    </div>
  </c:when>
  <c:otherwise>
    <div style="font-size:120%;line-height:1.2em;text-indent:10em;padding:0.5em">You have no items in any of your baskets.</div>
  </c:otherwise>
</c:choose>
