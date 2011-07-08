<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<!-- NOTE: We need to have all of the baskets here so counts can
     be displayed in the menu bar, but we don't want to load the
     results b/c there may be a custom results display for any
     record type -->

<c:set var="wdkUser" value="${sessionScope.wdkUser}" />

<c:choose>
  <c:when test="${wdkUser.basketCount > 0}">
    <wdk:basketMenu />
    <wdk:basketControls />

    <c:forEach items="${baskets}" var="basket">
      <c:set var="type" value="${fn:replace(basket.shortDisplayType, ' ', '_')}" />
      <div id="basket_${type}" class="basket_panel" recordClass="${basket.type}" displayName="${basket.displayType}">
        <div class="Workspace">
        </div>
      </div>
    </c:forEach>
  </c:when>
  <c:otherwise>
    <div style="font-size:120%;line-height:1.2em;text-indent:10em;padding:0.5em">You have no items in any of your baskets.</div>
  </c:otherwise>
</c:choose>
