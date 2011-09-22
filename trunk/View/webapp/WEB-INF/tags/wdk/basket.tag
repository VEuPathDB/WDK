<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="wdkUser" value="${sessionScope.wdkUser}" />
<c:set var="baskets" value="${requestScope.baskets}" />
<c:set var="total" value= "${0}" />
<c:forEach items="${baskets}" var="item">
  <c:set var="total" value="${total + item.value}" />
</c:forEach>
   
<script>
$(function() {
    $( "#basket-menu.tabs" ).tabs({
			ajaxOptions: {
                success(data, textStatus, jqXHR) {
                    $("#basket .Workspace").html(data);
                },
				error: function( xhr, status, index, anchor ) {
					$("#basket .Workspace").html( "Couldn't load this tab. Please try later" );
				}
			}
		});
});
</script>

<div="basekt">
<c:choose>
  <c:when test="${total > 0}">
    <div id="basket-menu" class="tabs">
      <ul>
        <c:forEach items="${basket}" var="item">
          <c:set var="recordClass" value="${item.key}" />
          <li>
            <a href="<c:url value='/showBasket.do?recordClass=${recordClass.fullName}'/>">${recordClass.displayName} (${item.value})</a>
          </li>
        </c:forEach>
      <ul>	  
    </div>

    <wdk:basketControls />

    <div id="Workspace"> </div>
 
  </c:when>
  <c:otherwise>
    <div style="font-size:120%;line-height:1.2em;text-indent:10em;padding:0.5em">You have no items in any of your baskets.</div>
  </c:otherwise>
</c:choose>
</div>
