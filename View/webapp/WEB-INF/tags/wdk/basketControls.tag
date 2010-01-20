
<table class="basket">
  <tr>
    <td>
      <input id="refresh-basket-button" type="button" value="Refresh" onClick="showBasket();"/>
    </td>
    <td>
      <input id="empty-basket-button" type="button" value="Empty Basket" onClick="updateBasket(this,'clear',0,0,'GeneRecordClasses.GeneRecordClass')"/></td>
    <td>
      <input id="make-strategy-from-basket-button" type="button" value="Save as Strategy" onClick="window.location='${url}'"/>
    </td>
  </tr>
</table>
