<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<!-- NOTE: We need to have all of the baskets here so counts can
     be display in the menu bar, but we don't want to load the
     results b/c there may be a custom results display for any
     record type -->

<wdk:basketMenu />
<wdk:basketControls />

<div class="Workspace">
<c:forEach items="${baskets}" var="basket">
  <div id="basket_${basket.displayType}" class="basket_panel" recordClass="${basket.type}">
  </div>
</c:forEach>
</div>
