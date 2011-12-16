<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>

<table width="100%" id="basket-control">
  <tr>
    <td>
      <input id="refresh-basket-button" type="button" value="Refresh" onClick="refreshBasket();"/>
    </td>
    <td>
      <input id="empty-basket-button" type="button" value="Empty basket" onClick="emptyBasket();"/></td>
    <td>
      <input id="make-strategy-from-basket-button" type="button" value="Save basket to a strategy" onClick="saveBasket();"/>
    </td>
    <td>
      <site:customBasketControl />
    </td>
    <td style="text-align:right;padding:3px 0 0;vertical-align:top">
	<span style="font-size:80%;font-style:italic;margin-bottom:7px"><b>Note on invalid IDs:</b> Changes that occur between database releases might invalidate some of the IDs in your Baskets. <br>We will map your old IDs to new IDs. Unmapped old IDs will not be included in your basket.</span>
    </td>
  </tr>
</table>

<div id="basketConfirmation" style="display:none">
  <form action="javascript:void(0);">
    <h3>Are you sure you want to empty the <span id="basketName"></span> basket?</h3>
    <input type="submit" value="Yes" onclick="jQuery.unblockUI();return true;" />
    <input type="submit" value="No" onclick="jQuery.unblockUI();return false;" />
  </form>
</div>
