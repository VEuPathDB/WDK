// =============================================================================
// the common functions provided by wdk
// =============================================================================

// On all pages, check that cookies are enabled.
jQuery(document).ready(function($) {
    
  // set up WDK global object
  if (window.wdk == undefined) window.wdk = new WDK();
  wdk.registerToggle();
  wdk.registerTable();

  // call all onload functions throughout the page
  Utilities.executeOnloadFunctions("body");

  // call all onload functions after ajax calls
  $("body").ajaxStop(function() {
    Utilities.executeOnloadFunctions("body");
    wdk.registerTable();
  });
  
  // convert all buttons to jQuery buttons
  $(".button").button();

});

//some helper functions used by isolates results page (clustal) and view-JSON.js
function guestUser() {
  return jQuery("#guestUser").attr("name");
}

function exportBaseURL() {
  return jQuery("#exportBaseURL").attr("name");
}

function modelName() {
  return jQuery("#modelName").attr("name");
}

function wdkUser() {
  this.id = jQuery("#wdk-userinfo").attr("user-id");
  this.name = jQuery("#wdk-userinfo").attr("name");
  this.country = jQuery("#wdk-userinfo").attr("country");
  this.email = jQuery("#wdk-userinfo").attr("email");
  this.isGuest = jQuery("#wdk-userinfo").attr("isGuest");
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
        var show = jQuery(this).attr("show");
        var active = (show == "true") ? 0 : false;
        jQuery(this).accordion({
            autoHeight: false,
            collapsible: true,
            active: active
        });
    });

    // register expand/collapse links
    // data-container is a selector for a container element
    // data-show is a boolean to show or hide toggles
    jQuery(".wdk-toggle-group").click(function(e) {
      var $this = jQuery(this);
      var container = $this.closest($this.data("container"));
      var $toggles = container.find(".wdk-toggle");

      if ($this.data("show")) {
        $toggles.each(function() {
          var $toggle = jQuery(this);
          if ($toggle.accordion("option", "active") !== 0) {
            $toggle.accordion("option", "active", 0);
          }
        });
      } else {
        $toggles.accordion("option", "active", false);
      }

      e.preventDefault();
    });
}

WDK.prototype.registerTable = function() {
    // register data tables on wdk table
    jQuery(".wdk-table.datatables").dataTable({
        "bJQueryUI": true
    });

    // also register other tables
    jQuery("table.wdk-data-table").not(".dataTable").wdkDataTable();
}


setUpNavDropDowns = function() {
  var timer;
  jQuery("#nav-top > li").hoverIntent({
    over: function() {
      var $this = jQuery(this);
      clearTimeout(timer);
      timer = setTimeout(function() {
        $this.children("ul").show("fade");
      }, 150);
    },
    out: function() {
      jQuery(this).children("ul").hide();
    },
    timeout: 500
  });
};

jQuery(document).ready(function(jQuery) {
  // instantiate dialogs
  var dialogOpts = {
    width: "auto",
    autoOpen: false,
    modal: true,
    resizable: false,
    beforeClose: function() {
      jQuery(this).find("form").each(function() {
        this.reset();
      });
    },
    open: function() {
      jQuery(".strategy-description.qtip").qtip("hide");
    }
  };
  jQuery("[id^='wdk-dialog-']").dialog(dialogOpts);
  
  // connect dialogs
  jQuery("body").on("click", "[class*='open-dialog-']", function(e) {
    e.preventDefault();
    var match = this.className.match(/\bopen-dialog-(\w+-\w+)\b/);
    if (match) {
      jQuery("#wdk-dialog-" + match[1]).dialog("open");
    }
  }).on("click", "[class*='close-dialog-']", function(e) {
    e.preventDefault();
    var match = this.className.match(/\bclose-dialog-(\w+-\w+)\b/);
    if (match) {
      jQuery("#wdk-dialog-" + match[1]).dialog("close");
    }
  });

  // connect window pop outs
  jQuery("body").on("click", "a[class^='open-window-']", function(e) {
    e.preventDefault();
    // regex below may be too stringent -- should allow for arbitrary identifier?
    var windowName,
        windowFeatures,
        match = this.className.match(/^open-window-(\w+-\w+)$/),
        windowUrl = this.href,
        windowWidth = 980,
        windowHeight = 620,
        windowLeft = screen.width/2 - windowWidth/2,
        windowTop = screen.height/2 - windowHeight/2,
        defaultFeatures = {
          location:   "no",
          menubar:    "no",
          resizable:  "yes",
          status:     "no",
          width:      windowWidth,
          height:     windowHeight,
          top:        windowTop,
          left:       windowLeft
        };

    // in the future, allow spefied data attributes to override features
    windowFeatures = $.map(defaultFeatures, function(v, k) { return k + "=" + v; }).join(",");
    if (match) {
      windowName = "wdk-window-" + match[1];
      window.open(windowUrl, windowName.replace(/-/g, "_"), windowFeatures).focus();
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

});

// this function is used by reporter pages
function makeSelection(state) {
    var form = document.downloadConfigForm;
    var cb = form.selectedFields;
    for (var i=0; i<cb.length; i++) {
        if (state == 1) cb[i].checked = 'checked';
        else if (state == 0) cb[i].checked = null;
        else if (state == -1) {
            cb[i].checked = ((cb[i].checked) ? '' : 'checked');
        }
    }
}

// custom dataTables plugin
jQuery.fn.wdkDataTable = function(opts) {
  return this.each(function() {
    var $this = $(this),
        sorting = $this.data("sorting"),
        dataTableOpts = {
          "sScrollY": "600px",
          "bScrollCollapse": true,
          "bPaginate": false,
          "bJQueryUI": true
        };

    if ($this.length === 0) return;

    if (sorting) {
      dataTableOpts["aoColumns"] = $.map(sorting, function(o) {
        return o ? [null] : {"bSortable" : false };
      });
    }

    // allow options to be passed like in the default dataTable function
    $this.dataTable($.extend(dataTableOpts, opts));
  });
};

