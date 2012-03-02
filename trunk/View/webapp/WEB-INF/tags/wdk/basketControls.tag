<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<table width="100%" id="basket-control">
  <tr>

<td style="padding:1px;vertical-align:middle" width="50%">
<table>
<tr>
    <td style="padding:1px">
      <input id="refresh-basket-button" type="button" value="Refresh" onClick="refreshBasket();"/>
    </td>
    <td>
      <input id="empty-basket-button" type="button" value="Empty basket" onClick="emptyBasket();"/></td>
    <td>
      <input id="make-strategy-from-basket-button" type="button" value="Save basket to a strategy" onClick="saveBasket();"/>
    </td>
    <td>
      <imp:customBasketControl />
    </td>
</td>
</tr>
</table>
</td>

<td width="50%">
<table align="right">
<tr>
    <td style="text-align:right;padding:3px 0 0;vertical-align:top">
	<span style="color:darkred;font-style:italic;margin-bottom:7px;margin-right:5px;position:relative;right:5px;"><b>Note on new releases:</b> Annotation changes might make IDs in your baskets outdated. <br>We will map your old IDs to new IDs. Unmapped IDs will not be kept.<br>To keep a copy of your current basket please download your IDs.</span>
    </td>
</tr>
</table>
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
