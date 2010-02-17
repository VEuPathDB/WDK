<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="url" value="<c:url value='/processQuestion.do?questionFullName=InternalQuestions.GeneRecordClasses_GeneRecordClassBySnapshotBasket&GeneRecordClasses_GeneRecordClassDataset_type=basket&questionSubmit=Run+Step'/>" />

<table class="basket">
  <tr>
    <td>
      <input id="refresh-basket-button" type="button" value="Refresh" onClick="showBasket();"/>
    </td>
    <td>
      <input id="empty-basket-button" type="button" value="Empty Basket" onClick="updateBasket(this,'clear',0,0,'GeneRecordClasses.GeneRecordClass')"/></td>
    <td>
      <input id="make-strategy-from-basket-button" type="button" value="Copy into New Strategy" onClick="window.location='<c:url value='/processQuestion.do?questionFullName=InternalQuestions.GeneRecordClasses_GeneRecordClassBySnapshotBasket&GeneRecordClasses_GeneRecordClassDataset_type=basket&questionSubmit=Run+Step'/>'"/>
    </td>
  </tr>
</table>
