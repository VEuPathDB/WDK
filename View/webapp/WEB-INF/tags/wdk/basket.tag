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

<!-- the order of tabs is determined in apicommonmodel.xml -->
<c:choose>
  <c:when test="${total > 0}">
    <div style="border:none" id="basket-menu" class="tabs">
<!--
<p style="font-size:80%;font-style:italic;margin-bottom:7px"><b>Note on invalid IDs:</b> Changes that occur between database releases might invalidate some of the IDs in your Baskets. 
<br>We will map your old IDs to new IDs. Those IDs that could not be mapped to a new ID will not be included in your basket.</p>
-->
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
