<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">
	
	<div id="wdk-dialog-about-openid" style="display:none" title="What is OpenID?">
	  <div class="popup-dialog">
		  <h2>What is OpenID?</h2>
		  <p>
		    <a target="_blank" href="http://openid.net">OpenID</a> is a decentralized way to identify yourself on the
		    web.  Once you have an OpenID, you can log in to any site that supports it, using only one username and
		    password- no more remembering passwords for every site you have an account on.  In addition, once you are
		    logged in to one site with your OpenID, you need only specify your OpenID to other sites and you won't
		    have to type in your password again!  OpenID is not very widespread yet, but it is growing, and its
		    technology underlies all those "Login with Google" links you see on various sites.
		  </p>
		  <h2>What's the advantage to using OpenID on <a target="_blank" href="http://eupathdb.org">EuPathDB</a> sites?</h2>
		  <p>
		    The only difference between a EuPathDB registration with OpenID and without is that you don't have to
		    remember a EuPathDB-specific password when you visit our sites.  Just use your OpenID and we can log
		    you in without a password. 
		  </p>
		  <h2>How do I get an OpenID?</h2>
		  <p>
		    You probably already have at least one OpenID, since sites like Google, Yahoo, and AOL provide them along
		    with their accounts.  We recommend getting an OpenID from an independent site (e.g. <a target="_blank"
		    href="https://www.myopenid.com">MyOpenID</a>).  You can learn how to find or create your OpenID <a
		    target="_blank" href="http://openid.net/get-an-openid">here</a>.
		  </p>
		  <h2>How can I learn more?</h2>
		  <p>
		    There is an excellent site <a target="_blank" href="http://openidexplained.com">here</a> that can teach
		    you the practicality of having an OpenID, and you can read about the development of the technology and
		    OpenID standards at the <a target="_blank" href="http://openid.net">OpenID Foundation</a>.  Of course you
		    can always read about it on <a target="_blank" href="http://en.wikipedia.org/wiki/Openid">Wikipedia</a>,
		    or <a target="_blank" href="https://www.google.com/#q=openid">Google it</a> like you would anything else.
		  </p>
	  </div>
	</div>


  <div style="display:none;" id="wdk-dialog-IE-warning" class="ui-dialog-fixed-width" title="Notice for IE users">
    <h2>Notice for IE users</h2>
    <p>For the best experience, we recommend that you 
      <a href="http://www.google.com/chromeframe" target="_blank">
        download a new Chrome plugin</a> that will make your browser work with our website. 
      <ul class="cirbulletlist">
        <li>It is very easy and fast</li> 
        <li>You do not need to be an administrator.</li>
        <li>You might need to clean the cache and restart IE to make it work</li>
      </ul>
    </p>

    <p>Even better, when possible, we recommend that you download one of the 
      <span style="font-size:120%;font-weight:bold">supported browsers</span>,
      listed below:
      <ul id='supported-browsers'>
        <li class='browser-link'><a href='http://www.mozilla.com/firefox/'>Firefox</a></li>
        <li class='browser-link'><a href='http://www.apple.com/safari/'>Safari</a></li>
        <li class='browser-link'><a href='http://www.google.com/chrome/'>Chrome</a></li>
      </ul>
    </p>
    <p>If you are unable or not allowed to upgrade your browser, please <a href='mailto:help@eupathdb.org'>let us know</a>.</p>
    <p>To continue using this site with your current browser, click 'Ignore.'</p>
    <input type='submit' value='Ignore' onclick="jQuery.cookie('api-unsupported',true,{path:'/'});jQuery.unblockUI();"/>
  </div>


  <div style="display:none;" id="wdk-dialog-revise-search" class="ui-dialog-fixed-width" title="Redesigned Searches">
    <ul class="cirbulletlist">
      <li>Searches are sometimes 'redesigned' if database revisions lead to new parameters and/or new parameter choices. 
        <br/><br/><br/>
      </li>
      <li>When parameters have been modified and we cannot easily map your old
        choices into the new search, the search will be covered with a
        <span style="font-size:140%;color:darkred;font-family:sans-serif">X</span>.
        It means it needs to be revised.
        <br/><br/><br/>
      </li>
      <li>Please open strategies marked with
        <img style="vertical-align:bottom" src="wdk/images/invalidIcon.png" width="12"/>
        and click each search that needs revision.
        <br/><br/><br/>
      </li>
      <!-- maybe too much info
      <li>In some rare cases, the search name you had in your history,
        does not exist in the new release and cannot be mapped to a new search. Your only choice will be to delete the search from the strategy.
      </li>
      --> 
    </ul>
  </div>


  <div style="display:none;" id="wdk-dialog-annot-change" class="ui-dialog-fixed-width" title="Annotation Changes">
    <ul class="cirbulletlist">
      <li>genome annotations are constantly updated to reflect new biological information concerning the sequences.  <br/><br/>
      </li>
      <li>when annotations are updated during a new release of our websites, some ids may change or be retired.</li>
    </ul>
  </div>


  <div style="display:none;" id="wdk-dialog-strat-desc">
    <div class="description"><jsp:text/></div>
    <div class="edit"><a href="#">Edit</a></div>
  </div>


  <div style="display:none;" id="wdk-dialog-update-strat" title="Update Strategy">
      <div class="save_as_msg">
        <p class="important">Important!</p>
        <ul>
            <li>You are saving/sharing this strategy, not the IDs in your result.</li>
            <li>Results might change with subsequent releases of the site if the underlying data has changed.</li>
            <li>To keep a copy of your current result, please <a class="download" href="#">download your IDs</a>.</li>
        </ul>
      </div>
      <form id="wdk-update-strat">
          <input type="hidden" name="strategy" value=""/>
          <dl>
              <dt class="name_label">Name:</dt>
              <dd class="name_input"><input type="text" name="name"/><jsp:text/></dd>
              <dt class="desc_label">Description (optional):</dt>
              <dd class="desc_input">
                  <textarea name="description" rows="10"><jsp:text/></textarea>
                  <div class="char_note"><em>Note: There is a 4,000 character limit.</em></div>
              </dd>
          </dl>
          <div style="text-align: right"><input name="submit" type="submit" value="Save strategy"/></div>
      </form>
  </div>


  <div style="display:none;" id="wdk-dialog-share-strat" title="Share Your Strategy Using The URL Below">
      <div class="share_msg">
        <p class="important">Important!</p>
        <ul>
            <li>You are saving/sharing this strategy, not the IDs in your result.</li>
            <li>Results might change with subsequent releases of the site if the underlying data has changed.</li>
            <li>To keep a copy of your current result, please <a class="download" href="#">download your IDs</a>.</li>
        </ul>
      </div>
      <div class="share_url"><jsp:text/></div>
  </div>

</jsp:root>
