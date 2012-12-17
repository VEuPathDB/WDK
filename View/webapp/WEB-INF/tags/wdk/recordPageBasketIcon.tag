<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.servletsuite.com/servlets/wraptag" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set value="${requestScope.wdkRecord}" var="wdkRecord"/>
<c:set value="${requestScope.action}" var="frontAction" />
<c:set value="${wdkRecord.primaryKey.values}" var="vals"/>
<c:set value="${vals['source_id']}" var="id"/>
<c:set value="${vals['project_id']}" var="pid"/>
<c:set value="basket${fn:replace(id,'.','_')}" var="basketId"/>
<c:set value="favorite${fn:replace(id,'.','_')}" var="favoriteId"/>
<c:set value="20" var="imagesize"/>

<c:if test="${frontAction != null}">
  <script type="text/javascript">
    jQuery(document).ready(function() {
        jQuery("#${frontAction}").click();
    });
  </script>
</c:if>
<style type="text/css">
	img {vertical-align:bottom;}
</style>


<span class="wdk-record" recordClass="${wdkRecord.recordClass.fullName}">
    <c:choose>
        <c:when test="${wdkUser.guest}">
          <c:if test="${wdkRecord.recordClass.useBasket}">
          <a class="basket" href="javascript:void(0)" onclick="wdk.user.login();"> Add to Basket
            <img src="<c:url value='/wdk/images/basket_gray.png'/>" width='${imagesize}' value="0" title="Please log in to access the basket."/>
          </a>
          &nbsp;&nbsp;&nbsp;&nbsp;
          </c:if>

          <a class="favorite" href="javascript:void(0)" onclick="wdk.user.login();">Add to Favorites
            <img src="<c:url value='/wdk/images/favorite_gray.gif'/>" width='${imagesize}' value="0" title="Please log in to access the favorites."/>
          </a>
         </c:when>

        <c:otherwise>
            <c:set var="image" value="${wdkRecord.inBasket ? 'color' : 'gray'}" />
            <c:set var="action" value="${wdkRecord.inBasket ? 'Remove from' : 'Add to'}" />
            <c:set var="imagevalue" value="${wdkRecord.inBasket ? '1' : '0'}"/>
            <c:set var="imagetitle" value="${wdkRecord.inBasket ? 'Click to remove this item from the basket.' : 'Click to add this item to the basket.'}"/>

<%--This block must remain together--%>
          <c:if test="${wdkRecord.recordClass.useBasket}">
            <a href="javascript:void(0)" onclick="jQuery(this).next().click();" id="basketrp">${action} Basket</a>
	    <a id="${basketId}" class="basket" href="javascript:void(0)" 
		onClick="wdk.basket.updateBasket(this, 'recordPage', '${id}', '${pid}', '${wdkRecord.recordClass.fullName}')">
            <img src="<c:url value='/wdk/images/basket_${image}.png' />" width='${imagesize}' value="${imagevalue}" title="${imagetitle}"/>
            </a>
            &nbsp;&nbsp;&nbsp;&nbsp;
          </c:if>
<%--End of Block --%>

            <c:set var="favorite" value="${wdkRecord.inFavorite}" />
            <c:set var="image" value="${favorite ? 'color' : 'gray'}" />
            <c:set var="action" value="${favorite ? 'remove' : 'add'}"/>
            <c:set var="actionWritten" value="${favorite ? 'Remove from' : 'Add to'}"/>
            <c:set var="imagetitle" value="Click to ${favorite ? 'Remove this item from' : 'Add this item to'} Favorites."/>
 <%-- This block must remain together --%>
           <a href="javascript:void(0)" onclick="jQuery(this).next().click()" id="favoritesrp">${actionWritten} Favorites</a> 
	    <img id="${favoriteId}" class="clickable" src="<c:url value='/wdk/images/favorite_${image}.gif'/>"  width='${imagesize}' 
                 title="${imagetitle}" onClick="wdk.favorite.updateFavorite(this, '${action}')" />
<%-- End block--%>
        </c:otherwise>
    </c:choose>

    <span class="primaryKey" >
        <c:forEach items="${vals}" var="key">
            <span key="${key.key}">${key.value}</span>
        </c:forEach>
    </span>
</span>
		
