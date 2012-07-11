// =============================================================================
// the common functions provided by wdk
// =============================================================================

// On all pages, check that cookies are enabled.
jQuery(document).ready(function() {
    if (window.wdk == undefined) window.wdk = new WDK();
    wdk.registerToggle();
    wdk.registerTable();
});

//some helper functions used by isolates results page (clustal) and view-JSON.js
function guestUser() {
  return $("#guestUser").attr("name");
}

function exportBaseURL() {
  return $("#exportBaseURL").attr("name");
}

function modelName() {
  return $("#modelName").attr("name");
}

function wdkUser() {
  this.id = $("#wdk-userinfo").attr("user-id");
  this.name = $("#wdk-userinfo").attr("name");
  this.country = $("#wdk-userinfo").attr("country");
  this.email = $("#wdk-userinfo").attr("email");
  this.isGuest = $("#wdk-userinfo").attr("isGuest");
}


var WDK = function() {

    this.initialize = function() {
        var testCookieName = 'wdkTestCookie';
        var testCookieValue = 'test';

        this.createCookie(testCookieName,testCookieValue,1);
        var test = this.readCookie(testCookieName);
        if (test == 'test') {
            this.eraseCookie(testCookieName);
        } else {
            jQuery.blockUI({message: "<div><h2>Cookies are disabled</h2><p>This site requires cookies.  Please enable them in your browser preferences.</p><input type='submit' value='OK' onclick='jQuery.unblockUI();' /></div>", css: {position : 'absolute', backgroundImage : 'none'}});
        }
    }

    // -------------------------------------------------------------------------
    // cookie handling methods
    // -------------------------------------------------------------------------
    this.createCookie = function(name,value,days) {
      if (days) {
        var date = new Date();
        date.setTime(date.getTime()+(days*24*60*60*1000));
        var expires = "; expires="+date.toGMTString();
      }
      else var expires = "";
      document.cookie = name+"="+value+expires+"; path=/";
    };

    this.readCookie = function(name) {
      var nameEQ = name + "=";
      var ca = document.cookie.split(';');
      for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
      }
      return null;
    };

    this.eraseCookie = function(name) {
      this.createCookie(name,"",-1);
    }

    // ------------------------------------------------------------------------
    // Event registration & handling code. The proper event will be invoked
    // during the page loading of the assign type. For example, question events
    // will be invoked on the loading of stand-alone question page, and the
    // loading of question page in the add/revise step popup box.
    // ------------------------------------------------------------------------
    this.questionEvents = new Array();
    this.resultEvents = new Array();
    this.recordEvents = new Array();

    this.registerQuestionEvent = function(handler) {
        this.questionEvents.push(handler);
    }

    this.registerResultEvent = function(handler) {
        this.resultEvents.push(handler);
    }

    this.registerRecordEvent = function(handler) {
        this.recordEvents.push(handler);
    }

    this.onloadQuestion = function() {
        for (var i= 0; i < this.questionEvents.length; i++) {
            var handler = this.questionEvents[i];
            handler();
        }
    }

    this.onloadResult = function() {
        for (var i= 0; i < this.resultEvents.length; i++) {
            var handler = this.resultEvents[i];
            handler();
        }
    }

    this.onloadRecord = function() {
        for (var i= 0; i < this.recordEvents.length; i++) {
            var handler = this.recordEvents[i];
            handler();
        }
    }

    this.findActiveWorkspace = function() {
        // check if the current page is result page
        var tabs = jQuery("#strategy_tabs");
        var section = "";
        if (tabs.length > 0) { // on result page
            // determine the default top level tab
            section = tabs.children("#selected").children("a").attr("id");
            if (section == "tab_basket") { // on basket tab
                section = jQuery("#basket #basket-menu > ul > li.ui-tabs-selected > a").attr("href");
                section = "#basket #basket-menu > " + section;
            } else { // on open strategies tab
                section = "#" + section.substring(4) + " .Workspace";
            }
        } else { // not on strategy page, just get the general workspace
           section = ".Workspace";
        }
        return jQuery(section);
    }

    this.findActiveView = function() {
        var workspace = this.findActiveWorkspace();
        // check if we have summary view or record view
        var views = workspace.find("#Summary_Views");
        if (views.length == 0) { // no sumamry views, get record views
            views = workspace.find("#Record_Views");
        }
        var section = views.children("ul").children("li.ui-tabs-selected").children("a").attr("href");
        return views.find(section);
    }

    this.initialize();
}



function uncheckFields(notFirst) {
    var form = document.downloadConfigForm;
    var cb = form.selectedFields;
    if (notFirst) {
        for (var i=1; i<cb.length; i++) {
            if (cb[i].disabled) continue;
            cb[i].checked = null;
        }
    } else {
        cb[0].checked = null;
    }
}

function checkFields(all) {
    var form = document.downloadConfigForm;
    var cb = form.selectedFields;
    cb[0].checked = (all > 0 ? null : 'checked');
    for (var i=1; i<cb.length; i++) {
        if (cb[i].disabled) continue;
        cb[i].checked = (all > 0 ? 'checked' : null);
    }
}

function chooseAll(bool, form, node) {
    if (form[node].type == 'select-multiple') {
      multiSelectAll(bool, form, node);
    } else {
      checkAll(bool, form, node);
    }
}

function checkAll(bool, form, node) {
    var cb = form[node];//document.getElementsByName(node);
    for (var i=0; i<cb.length; i++) {
         if (cb[i].disabled) continue;
   if(bool && cb[i].checked == false) cb[i].click();
         if(!bool && cb[i].checked == true) cb[i].click();
    }
}

// returns whether or not user is logged in
function isUserLoggedIn() {
  return (jQuery('#loginStatus').attr('loggedIn') == "true");
}

function getWebAppUrl() {
  var scripts = document.getElementsByTagName('script');
  var scriptPath;
  for (var i = 0; i < scripts.length; i++) {
      var script = scripts[i];
      scriptPath =
        ((script.getAttribute.length !== undefined) ?
            script.src : script.getAttribute('src', -1));
      if (scriptPath.indexOf("wdkCommon.js") != -1) {
        break;
      }
  }
  var suffixLen = new String("wdk/js/wdkCommon.js").length;
  scriptPath = scriptPath.substring(0, scriptPath.length - suffixLen);
  return scriptPath;
}


WDK.prototype.registerToggle = function() {
    // register toggles
    jQuery(".wdk-toggle").each(function() {
        // check if the section should be displayed by default
        var show = $(this).attr("show");
        var active = (show == "true") ? 0 : false;
        $(this).accordion({
            autoHeight: false,
            collapsible: true,
            active: active
        });
    });
}

WDK.prototype.registerTable = function() {
    // register data tables on wdk table
    jQuery(".wdk-table.datatables").dataTable({
        "bJQueryUI": true
    });
}


jQuery(function() {
  // instantiate dialogs
  var dialogOpts = {
    width: "auto",
    autoOpen: false,
    modal: true,
    resizable: false
  };
  jQuery("[id^='wdk-dialog-']").dialog(dialogOpts);
  
  // connect dialogs
  jQuery("body").on("click", "[class^='open-dialog-']", function(e) {
    e.preventDefault();
    var match = this.className.match(/^open-dialog-(\w+-\w+)$/);
    if (match) {
      jQuery("#wdk-dialog-" + match[1]).dialog("open");
    }
  });

  // connect update strat dialog to qtip edit link 
  jQuery("body").on("click", ".qtip .open-dialog-update-strat", function(e) {
    e.preventDefault();
    var qt = jQuery(this).parents(".qtip").qtip("api"),
        strat_id = qt.get("position.target").parents("tr").attr("id").substr(6);
    //qt.hide();
    showUpdateDialog(strat_id, false, true);
  });

  // hide qtips when dialogs are open
  jQuery(".ui-dialog").on('dialogopen', function() {
    jQuery(".strategy-description.qtip").qtip("hide");
  });

});
