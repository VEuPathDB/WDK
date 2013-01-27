wdk.util.namespace("window.wdk", function(ns, $) {
  "use strict";

  //$.noConflict();

  // =============================================================================
  // the common functions provided by wdk
  // =============================================================================

  function exportBaseURL() {
    return $("#exportBaseURL").attr("name");
  }

  function modelName() {
    return $("#modelName").attr("name");
  }

  var cookieTest = function() {
      var testCookieName = 'wdkTestCookie';
      var testCookieValue = 'test';

      createCookie(testCookieName, testCookieValue, 1);
      var test = readCookie(testCookieName);
      if (test == 'test') {
          eraseCookie(testCookieName);
      } else {
          $.blockUI({message: "<div><h2>Cookies are disabled</h2><p>This site requires cookies.  Please enable them in your browser preferences.</p><input type='submit' value='OK' onclick='jQuery.unblockUI();' /></div>", css: {position : 'absolute', backgroundImage : 'none'}});
      }
  }

  // -------------------------------------------------------------------------
  // cookie handling methods
  // -------------------------------------------------------------------------
  var createCookie = function(name,value,days) {
     var expires;
     if (days) {
      var date = new Date();
      date.setTime(date.getTime()+(days*24*60*60*1000));
      expires = "; expires="+date.toGMTString();
    }
    else expires = "";
    document.cookie = name+"="+value+expires+"; path=/";
  };

  var readCookie = function(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
      var c = ca[i];
      while (c.charAt(0)==' ') c = c.substring(1,c.length);
      if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
  };

  var eraseCookie = function(name) {
    createCookie(name,"",-1);
  }

  // ------------------------------------------------------------------------
  // Event registration & handling code. The proper event will be invoked
  // during the page loading of the assign type. For example, question events
  // will be invoked on the loading of stand-alone question page, and the
  // loading of question page in the add/revise step popup box.
  // ------------------------------------------------------------------------
  var questionEvents = new Array();
  var resultEvents = new Array();
  var recordEvents = new Array();

  var registerQuestionEvent = function(handler) {
      questionEvents.push(handler);
  }

  var registerResultEvent = function(handler) {
      resultEvents.push(handler);
  }

  var registerRecordEvent = function(handler) {
      recordEvents.push(handler);
  }

  var onloadQuestion = function() {
      for (var i= 0; i < questionEvents.length; i++) {
          var handler = questionEvents[i];
          handler();
      }
  };

  var onloadResult = function() {
      for (var i= 0; i < resultEvents.length; i++) {
          var handler = resultEvents[i];
          handler();
      }
  }

  var onloadRecord = function() {
      for (var i= 0; i < recordEvents.length; i++) {
          var handler = recordEvents[i];
          handler();
      }
  }

  var findActiveWorkspace = function() {
      // check if the current page is result page
      var tabs = $("#strategy_tabs");
      var section = "";
      if (tabs.length > 0) { // on result page
          // determine the default top level tab
          section = tabs.children("#selected").children("a").attr("id");
          if (section == "tab_basket") { // on basket tab
              section = $("#basket #basket-menu > ul > li.ui-tabs-selected > a").attr("href");
              section = "#basket #basket-menu > " + section;
          } else { // on open strategies tab
              section = "#" + section.substring(4) + " .Workspace";
          }
      } else { // not on strategy page, just get the general workspace
         section = ".Workspace";
      }
      return $(section);
  }

  var findActiveView = function() {
      var workspace = findActiveWorkspace();
      // check if we have summary view or record view
      var views = workspace.find("#Summary_Views");
      if (views.length == 0) { // no sumamry views, get record views
          views = workspace.find("#Record_Views");
      }
      var section = views.children("ul").children("li.ui-tabs-selected").children("a").attr("href");
      return views.find(section);
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
      wdk.api.multiSelectAll(bool, form, node);
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
  return ($('#loginStatus').attr('loggedIn') == "true");
}

function getWebAppUrl() {
  return $("#wdk-web-app-url").attr("value");
}

// function getWebAppUrl() {
//   var scripts = document.getElementsByTagName('script');
//   var scriptPath;
//   for (var i = 0; i < scripts.length; i++) {
//       var script = scripts[i];
//       scriptPath =
//         ((script.getAttribute.length !== undefined) ?
//             script.src : script.getAttribute('src', -1));
//       if (scriptPath.indexOf("wdkCommon.js") != -1) {
//         break;
//       }
//   }
//   var suffixLen = new String("wdk/js/wdkCommon.js").length;
//   scriptPath = scriptPath.substring(0, scriptPath.length - suffixLen);
//   return scriptPath;
// }


  var registerToggle = function() {
    // register toggles
    $(".wdk-toggle").each(function() {
      // check if the section should be displayed by default
      var show = $(this).attr("show");
      var active = (show == "true") ? 0 : false;
      $(this).accordion({
        autoHeight: false,
        collapsible: true,
        active: active
      });
    });

    // register expand/collapse links
    // data-container is a selector for a container element
    // data-show is a boolean to show or hide toggles
    $(".wdk-toggle-group").click(function(e) {
      var $this = $(this);
      var container = $this.closest($this.data("container"));
      var $toggles = container.find(".wdk-toggle");

      if ($this.data("show")) {
        $toggles.each(function() {
          var $toggle = $(this);
          if ($toggle.accordion("option", "active") !== 0) {
            $toggle.accordion("option", "active", 0);
          }
        });
      } else {
        $toggles.accordion("option", "active", false);
      }

      e.preventDefault();
    });

  };

  var registerCollapsible = function() {
    $(".collapsible").each(function() {
      var $this = $(this);

      if ($this.data("rendered")) {
        return;
      }

      var $trigger = $this.children().first().addClass("collapsible-title");
      var $content = $trigger.next().addClass("collapsible-content");
      var $arrowSpan = $("<span></span>").prependTo($trigger);

      if (!$trigger.attr("title")) {
        $trigger.attr("title", "Click to expand or collapse");
      }

      if ($content.css("display") === "none") {
        $content.hide();
        $this.addClass("collapsed");
        $arrowSpan.addClass("ui-icon ui-icon-triangle-1-e");
      } else {
        $content.show();
        $arrowSpan.addClass("ui-icon ui-icon-triangle-1-s");
      }

      $trigger.on("click", function(e) {
        e.preventDefault();
        $this.toggleClass("collapsed", $content.css("display") === "block");
        $content.slideToggle();
        $arrowSpan.toggleClass("ui-icon-triangle-1-e", $this.hasClass("collapsed"));
        $arrowSpan.toggleClass("ui-icon-triangle-1-s", !$this.hasClass("collapsed"));
      });

      $this.data("rendered", true);
    });
  }

  var registerTable = function() {
    // register data tables on wdk table
    $(".wdk-table.datatables").dataTable({
        "bJQueryUI": true
    });

    // also register other tables
    $("table.wdk-data-table").not(".dataTable").wdkDataTable();
  };

  var registerTooltips = function() {
    // register elements with fancy tooltips
    $(".wdk-tooltip").not(".qtip").qtip({
      position: {
        my: "top center",
        at: "bottom center"
      }
    });
  };

  var registerSnippet = function() {
    $(".snippet").each(function(idx, node) {
      var $node = $(node),
          defaultHeight = $node.height();

      if ($node.data("rendered")) {
        return;
      }

      // We want to show at minimum two lines (i.e., leave 26px unobstructed).
      var showHeight = Math.max($node.data("snippet-show"), 26) || 26;

      // Add the height of the gradient to the showHeight
      var height = showHeight + 8;

      if (height >= $node.height()) {
        $node.data("rendered", true);
        return;
      }

      var $ellipsis = $("<div><b>&hellip;</b></div>")
      .css("padding-left", "0.4em")
      .attr("title", "Click \"Show more\" to expand");

      // Wrap inner content so we can hide its overflow
      $node.wrapInner($("<div class='orig'/>").height(height)
          .css("overflow", "hidden"));

      $ellipsis.appendTo($node);

      var $toggle1 = $("<div><a href='#'><span " +
          "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show more</a></div>")
          .appendTo($node)
          .addClass("snippet-toggle");

      var $toggle2 = $toggle1.clone()
          .prependTo($node)
          .css("float", "right");

      $node.on("click", ".snippet-toggle a", function(e) {
            e.preventDefault();
            if ($node.data("shown")) {
              //hide
              $node.find(".orig:first").animate({
                height: height
              },
              {
                easing: "easeOutQuint"
              })
              $toggle1.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show more</a>");
              $toggle2.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show more</a>");
              $ellipsis.show();
              $node.data("shown", false);
            } else {
              //show
              $node.find(".orig:first").animate({
                height: $node.find("div > div").height()
              },
              {
                easing: "easeOutQuint"
              })
              $toggle1.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-n'></span>Show less</a>");
              $toggle2.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-n'></span>Show less</a>");
              $ellipsis.hide();
              $node.data("shown", true);
            }
          });

      $node.data("rendered", true);
    });
  };

  var registerTruncate = function() {
    var SHOW_CHARS = 120;

    $(".truncate").each(function(idx, node) {
      var $node = $(node);

      if ($node.data("rendered")) {
        return;
      }

      var text = $node.text().trim();

      if (text.length <= SHOW_CHARS) {
        $node.data("rendered", true);
        return;
      }

      $node.css("position", "relative");

      var $teaser = $("<div/>")
      .addClass("teaser")
      //.html(text.slice(0, SHOW_CHARS) + "<b>&hellip;</b>");
      .html(text).css({
        width: "90%",
        "white-space": "nowrap",
        overflow: "hidden",
        "text-overflow": "ellipsis"
      });

      // on bottom
      var $toggle1 = $("<div/>")
      .addClass("truncate-toggle")
      .attr("href", "#")
      .html("<a href='#'><span " +
          "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show more</a>");

      // on right
      var $toggle2 = $toggle1.clone()
      .css({
        "float": "right"
      })


      // hide original content and append teaser before it
      var $orig = $node.wrapInner("<div class='orig'/>")
      .find(".orig:first")
      .after($toggle1)
      .before($toggle2)
      .before($teaser)
      .hide();

      var height = $orig.height();
      $orig.height(13);

      $node.on("click", ".truncate-toggle", function(e) {
        e.preventDefault();
        if ($node.data("showing")) {
          // hide
          $orig.animate({
            height: 13
          },function() {
            $orig.hide();
            $teaser.show();
            $toggle1.html("<a href='#'><span " +
              "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show more</a>");
            $toggle2.html("<a href='#'><span " +
              "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show more</a>");
          });
          $node.data("showing", false);
        } else {
          // show
          $teaser.hide();
          $orig.show().animate({
            height: height
          }, function() {
            $orig.height("auto");
          });
          $toggle1.html("<a href='#'><span " +
            "class='ui-icon ui-icon-arrowthickstop-1-n'></span>Show less</a>");
          $toggle2.html("<a href='#'><span " +
            "class='ui-icon ui-icon-arrowthickstop-1-n'></span>Show less</a>");
          $node.data("showing", true);
        }
      });

      $node.data("rendered", true);
    });
  };

  var setUpNavDropDowns = function() {
    var timer;
    $("#nav-top > li").hoverIntent({
      over: function() {
        var $this = $(this);
        clearTimeout(timer);
        timer = setTimeout(function() {
          $this.children("ul").show("fade");
        }, 150);
      },
      out: function() {
        $(this).children("ul").hide();
      },
      timeout: 500
    });
  };

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

  var setUpDialogs = function () {
    var dialogOpts = {
      width: "auto",
      autoOpen: false,
      modal: true,
      resizable: false
    };

    $("[id^='wdk-dialog-']").dialog(dialogOpts);

    // connect dialogs
    $("body").on("click", "[class*='open-dialog-']", function(e) {
      e.preventDefault();
      var match = this.className.match(/\bopen-dialog-(\w+-\w+)\b/);
      if (match) {
        $("#wdk-dialog-" + match[1]).dialog("open");
      }
    }).on("click", "[class*='close-dialog-']", function(e) {
      e.preventDefault();
      var match = this.className.match(/\bclose-dialog-(\w+-\w+)\b/);
      if (match) {
        $("#wdk-dialog-" + match[1]).dialog("close");
      }
    });
  };

  var setUpPopups = function() {
    // connect window pop outs
    $("body").on("click", "a[class^='open-window-']", function(e) {
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
  };

  // when a portion (or all) of the DOM is loaded...
  function load() {
    wdk.util.executeOnloadFunctions("body");
    registerTable();
    registerTooltips();
    registerToggle();
    registerCollapsible();
    registerSnippet();
    registerTruncate();
    $(".button").button();
  }

  // On all pages, check that cookies are enabled.
  function init() {
    cookieTest();
    setUpDialogs();
    setUpPopups();
    load();
    $("body").ajaxSuccess(load);
  }


  ns.init = init;
  ns.exportBaseURL = exportBaseURL;
  ns.modelName = modelName;
  ns.readCookie = readCookie;
  ns.createCookie = createCookie;
  ns.registerQuestionEvent = registerQuestionEvent;
  ns.onloadQuestion = onloadQuestion; // anything using this?
  ns.findActiveWorkspace = findActiveWorkspace;
  ns.findActiveView = findActiveView;
  ns.chooseAll = chooseAll;
  ns.getWebAppUrl = getWebAppUrl;
  ns.isUserLoggedIn = isUserLoggedIn;
  ns.checkFields = checkFields;
  ns.uncheckFields = uncheckFields;
  ns.setUpNavDropDowns = setUpNavDropDowns;
  ns.makeSelection = makeSelection;

});
