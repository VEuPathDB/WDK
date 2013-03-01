<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <!-- Remove to save bandwidth until we add OpenID back in -->
  <!--<imp:aboutOpenId/>-->

  <c:set var="reviseTitle">
    <imp:verbiage key="dialog.revise-search.title"/>
  </c:set>
  <div style="display:none;" id="wdk-dialog-revise-search" class="ui-dialog-fixed-width" title="${reviseTitle}">
    <imp:verbiage key="dialog.revise-search.content"/>
  </div>


  <c:set var="annotTitle">
    <imp:verbiage key="dialog.annot-change.title"/>
  </c:set>
  <div style="display:none;" id="wdk-dialog-annot-change" class="ui-dialog-fixed-width" title="${annotTitle}">
    <imp:verbiage key="dialog.annot-change.content"/>
  </div>


  <div style="display:none;" id="wdk-dialog-strat-desc">
    <div class="description"><jsp:text/></div>
    <div class="edit"><a href="#">Edit</a></div>
  </div>


  <c:set var="updateTitle">
    <imp:verbiage key="dialog.update-strat.title"/>
  </c:set>

  <div style="display:none;" id="wdk-dialog-update-strat" title="${udpateTitle}">
    <div class="ui-state-highlight ui-corner-all save_as_msg">
      <imp:verbiage key="dialog.update-strat.content"/>
    </div>
    <form id="wdk-update-strat" >
      <input type="hidden" name="strategy" value=""/>
      <dl>
        <dt class="name_label">Name:</dt>
        <dd class="name_input"><input type="text" name="name"/></dd>
        <dt class="desc_label">Description (optional):</dt>
        <dd class="desc_input">
          <textarea name="description" rows="10"><jsp:text/></textarea>
          <div class="char_note"><em>Note: There is a 4,000 character limit.</em></div>
        </dd>
      </dl>
      <div style="text-align: right"><input name="submit" type="submit" value="Save strategy"/></div>
    </form>
  </div>


  <c:set var="shareTitle">
    <imp:verbiage key="dialog.share-strat.title"/>
  </c:set>
  <div style="display:none;" id="wdk-dialog-share-strat" title="${shareTitle}">
    <div class="ui-state-highlight ui-corner-all share_msg">
      <imp:verbiage key="dialog.share-strat.content"/>
    </div>
    <div class="share_url"><jsp:text/></div>
  </div>


  <div id="wdk-dialog-login-form" title="Account login">
    <imp:loginForm showCancel="true"/>
  </div>

</jsp:root>
