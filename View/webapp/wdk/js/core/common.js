/* global wdkConfig */
/* jshint evil:true */

wdk.util.namespace("window.wdk", function(ns, $) {
  "use strict";

  // =============================================================================
  // the common functions provided by wdk
  // =============================================================================

  function exportBaseURL() {
    //return $("#exportBaseURL").attr("name");
    return wdkConfig.exportBaseUrl;
  }

  function modelName() {
    //return $("#modelName").attr("name");
    return wdkConfig.modelName;
  }

  function cookieTest() {
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
  function createCookie(name,value,days) {
    var expires;
    if (days) {
      var date = new Date();
      date.setTime(date.getTime()+(days*24*60*60*1000));
      expires = "; expires="+date.toGMTString();
    }
    else expires = "";
    document.cookie = name+"="+value+expires+"; path=/";
  }

  function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
      var c = ca[i];
      while (c.charAt(0)==' ') c = c.substring(1,c.length);
      if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
  }

  function eraseCookie(name) {
    createCookie(name,"",-1);
  }

  // ------------------------------------------------------------------------
  // Event registration & handling code. The proper event will be invoked
  // during the page loading of the assign type. For example, question events
  // will be invoked on the loading of stand-alone question page, and the
  // loading of question page in the add/revise step popup box.
  // ------------------------------------------------------------------------
  var questionEvents = [];
  /*
   The next two variables are not used:
  var resultEvents = [];
  var recordEvents = [];
  */

 /**
  * @deprecated See wdk.on
  */
  function registerQuestionEvent(handler) {
    questionEvents.push(handler);
  }

  /*
   The next two functions are not used:
  function registerResultEvent(handler) {
    resultEvents.push(handler);
  }

  function registerRecordEvent(handler) {
    recordEvents.push(handler);
  }
  */

 /**
  * @deprecated See wdk.trigger
  */
  function onloadQuestion() {
    for (var i= 0; i < questionEvents.length; i++) {
      var handler = questionEvents[i];
      handler();
    }
  }

  /*
   The next two functions are not used:
  function onloadResult() {
    for (var i= 0; i < resultEvents.length; i++) {
      var handler = resultEvents[i];
      handler();
    }
  }

  function onloadRecord() {
    for (var i= 0; i < recordEvents.length; i++) {
      var handler = recordEvents[i];
      handler();
    }
  }
  */

  function findActiveWorkspace() {
    // check if the current page is result page
    var tabs = $("#strategy_tabs");
    var section = "";
    if (tabs.length > 0) { // on result page
      // determine the default top level tab
      section = tabs.children("#selected").children("a").attr("id");
      if (section == "tab_basket") { // on basket tab
        section = "#" + $("#basket #basket-menu > ul > li.ui-tabs-active").attr("aria-controls");
      } else { // on open strategies tab
        section = "#" + section.substring(4) + " .Workspace";
      }
    } else { // not on strategy page, just get the general workspace
      section = ".Workspace";
    }
    return $(section);
  }

  function findActiveView() {
    var workspace = findActiveWorkspace();
    // check if we have summary view or record view
    var views = workspace.find("#Summary_Views");
    if (views.length === 0) { // no sumamry views, get record views
      views = workspace.find("#Record_Views");
    }
    var section = views.children("ul").children("li.ui-tabs-active").attr("aria-controls");
    return views.find(document.getElementById(section));
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
      if(bool && cb[i].checked === false) cb[i].click();
      if(!bool && cb[i].checked === true) cb[i].click();
    }
  }

  // returns whether or not user is logged in
  function isUserLoggedIn() {
    //return ($('#loginStatus').attr('loggedIn') == "true");
    return !wdkConfig.wdkUser.IsGuest;
  }

  /**
   * Returns the complete path to the webapp dir
   * and appends the given url
   */
  function webappUrl(path) {
    return resolveUrl(wdkConfig.webappUrl, path);
  }

  /**
   * Returns the complete path to the assets dir
   * and appends the given url
   */
  function assetsUrl(path) {
    return resolveUrl(wdkConfig.assetsUrl, path);
  }

  /**
   * Resolves `path` relative to `root`
   */
  function resolveUrl(root, path) {
    path = path || '';
    path = (path.indexOf('/') === 0) ? path : '/' + path;
    return root + path;
  }


  // TODO: mixin
  function registerToggle($el) {
    // register toggles
    $el.find(".wdk-toggle").not('[__rendered]')
      .each(function(index, node) {
        $(node).simpleToggle().attr('__rendered', true);
      });

    // register expand/collapse links
    // data-container is a selector for a container element
    // data-show is a boolean to show or hide toggles
    // data-animated overrides the built-in animation
    $el.find(".wdk-toggle-group").not('[__rendered]')
      .each(function(index, node) {
        $(node)
          .click(function(e) {
            var $this = $(this);
            var container = $this.closest($this.data("container"));
            var $toggles = container.find(".wdk-toggle");

            $toggles.simpleToggle("toggle", $this.data("show"));

            e.preventDefault();
          })
          .attr('__rendered', true);
      });
  }

  // TODO: mixin
  function registerCollapsible($el) {
    $el.find(".collapsible").each(function() {
      var $this = $(this);

      if ($this.attr("rendered")) {
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
        $arrowSpan.addClass("ui-icon wdk-icon-plus");
      } else {
        $content.show();
        $arrowSpan.addClass("ui-icon wdk-icon-minus");
      }

      $trigger.on("click", function(e) {
        e.preventDefault();
        $this.toggleClass("collapsed", $content.css("display") === "block");
        $content.slideToggle();
        $arrowSpan.toggleClass("wdk-icon-plus", $this.hasClass("collapsed"));
        $arrowSpan.toggleClass("wdk-icon-minus", !$this.hasClass("collapsed"));
      });

      $this.attr("rendered", true);

    });
  }

  // TODO: view
  function registerTable($el) {
    // register data tables on wdk table
    $el.find(".wdk-table.datatables").not('.dataTable').dataTable({
      "bJQueryUI": true
    });

    // also register other tables
    $el.find("table.wdk-data-table").not(".dataTable").wdkDataTable();
  }

  // TODO: mixin
  function registerTooltips($el) {
    $el.find(".wdk-tooltip").not('[data-hasqtip]').wdkTooltip();
  }

  // TODO: mixin
  function registerSnippet($el) {
    $el.find(".snippet").each(function(idx, node) {
      var $node = $(node),
          defaultHeight = $node.height(); // jshint ignore:line

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

      // var $ellipsis = $("<div><b>&hellip;</b></div>")
      // .css("padding-left", "2.4em")
      // .attr("title", "Click to expand");

      // Wrap inner content so we can hide its overflow
      $node.wrapInner($("<div class='orig'/>").height(height)
          .css("overflow", "hidden"));

      //cris   $ellipsis.appendTo($node);

      var $toggle1 = $("<div><a href='#'><span " +
          "class='ui-icon ui-icon-arrowthickstop-1-s'></span><b style='font-size:120%'>...</b></a></div>")
          .appendTo($node)
          .addClass("snippet-toggle");

      var $toggle2 = $toggle1.clone()
          .prependTo($node)
          .css("float", "right");
      $toggle2.html("");

      $node.on("click", ".snippet-toggle a", function(e) {
            e.preventDefault();
            if ($node.data("shown")) {
              //hide
              $node.find(".orig:first").animate({
                height: height
              },
              {
                easing: "easeOutQuint"
              });
              $toggle1.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-s'></span><b style='font-size:120%'>...</b></a>");
              $toggle2.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show all datasets ...</a>");
              $toggle2.html("");

             //cris    $ellipsis.show();
              $node.data("shown", false);
            } else {
              //show
              $node.find(".orig:first").animate({
                height: $node.find("div > div").height()
              },
              {
                easing: "easeOutQuint"
              });
              $toggle1.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-n'></span>Show less</a>");
              $toggle2.html("<a href='#'><span " +
                "class='ui-icon ui-icon-arrowthickstop-1-n'></span>Show less</a>");
              //$toggle2.html(""); //dont show

              //cris    $ellipsis.hide();
              $node.data("shown", true);
            }
          });

      $node.data("rendered", true);
    });
  }

  // TODO: mixin
  function registerTruncate($el) {
    var SHOW_CHARS = 120;

    $el.find(".truncate").each(function(idx, node) {
      var $node = $(node);

      if ($node.data("rendered")) {
        return;
      }
      //text contains original text with '\n'
      var text = $node.text().trim();

      if (text.length <= SHOW_CHARS) {
        $node.data("rendered", true);
        return;
      }

      // this might be for IE7 so overflow: hidden works?
      $node.css("position", "relative");

      var $teaser = $("<div/>")
      .addClass("teaser")
      .html(text.slice(0, SHOW_CHARS) + "<b>&hellip;</b>");
 /* cris: 2 problems: IE8 and italics in organism is lost on teaser
       .html(text).css({
        width: "90%",
        "white-space": "nowrap",
        overflow: "hidden",
        "text-overflow": "ellipsis"
      });
*/
      // on bottom
      var $toggle1 = $("<div/>")
      .addClass("truncate-toggle")
      .attr("href", "#")
      .html("<a href='#'><span " +
          "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show full description ...</a>");

      // on right
      var $toggle2 = $toggle1.clone()
      .css({
        "float": "right"
      });
      $toggle1.html(""); //dont show

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
              "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show full description ...</a>");
            $toggle2.html("<a href='#'><span " +
              "class='ui-icon ui-icon-arrowthickstop-1-s'></span>Show full description ...</a>");
            $toggle1.html("");
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
          //$toggle2.html("");
          $node.data("showing", true);
        }
      });

      $node.data("rendered", true);
    });
  }

  // TODO: mixin
  function registerEditable($el) {
    // all elements with className wdk-editable, eg:
    //   <span class="wdk-editable"
    //       data-change="someFunction">edit me</span>

    $el.find(".wdk-editable").each(function(idx, element) {
      if ($(element).data("rendered")) return;

      var save = $(element).data("save");

      // JSHint cannot parse this line, so we need to ignore it
      /* jshint ignore:start */
      if (typeof save === "string") {
        try {
          save = (0, eval)("(" + save + ")");
        } catch (e) {
          if (console && console.log) {
            console.log(e);
          }
        }
      }
      /* jshint ignore:end */

      $(element).editable({
        save: typeof save === "function" ? save : function(){return true;}
      });

      $(element).data("rendered", true);
    });
  }

  function registerButton($el) {
    $el.find('.button')
      .not('[__rendered]')
        .button()
        .attr('__rendered', true);
  }

  // TODO: view
  function setUpNavDropDowns() {
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
  }

  // TODO: view - use href to find dialog
  function setUpDialogs() {
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
  }

  // TODO: view
  function setUpPopups() {
    // connect window pop outs
    $("body").on("click", "a[class='new-window']", function(e) {
      e.preventDefault();
      // regex below may be too stringent -- should allow for arbitrary identifier?
      var windowFeatures;
      var windowName = $(this).data("name") || "wdk_window";
      var windowUrl = this.href;
      var windowWidth = 1050;
      var windowHeight = 740;
      var windowLeft = screen.width/2 - windowWidth/2;
      var windowTop = screen.height/2 - windowHeight/2;
      var defaultFeatures = {
        location:    "no",
        menubar:     "no",
        toolbar:     "no",
        personalbar: "no",
        resizable:   "yes",
        scrollbars:  "yes",
        status:      "yes",
        width:       windowWidth,
        height:      windowHeight,
        top:         windowTop,
        left:        windowLeft
      };

      // in the future, allow spefied data attributes to override features
      windowFeatures = $.map(defaultFeatures, function(v, k) { return k + "=" + v; }).join(",");
      window.open(windowUrl, windowName.replace(/-/g, "_"), windowFeatures).focus();
    });
  }

  // TODO: replace with views
  function invokeControllers($el) {
    // TODO - Add data-action attribute
    // controller is a misnomer here. see issue #14107
    $el.find("[data-controller]").not('[__invoked]')
      .each(function invokeController(idx, element) {
        var $element = $(element);
        var $attrs = $element.data();
        var controller = $attrs.controller;

        try {
          // TODO - Replace with a container. This way we can do some validation,
          // such as prevent collisions, and inject dependencies. It will also
          // be quicker to do a dictionary lookup.
          wdk.util.executeFunctionByName(controller, window, window, $element, $attrs);
        } catch (e) {
          console.error(e);
        } finally {
          $element.attr('__invoked', true);
        }
      });
  }

  // TODO Convert global refs to event names
  function triggerControllerEvents($el) {
    $el.find("[data-controller]").not('[__triggered]')
      .each(function(index, el) {
        var $el = $(el);
        $el.trigger($el.data('controller'));
        $el.attr('__triggered', true);
      });
  }

  function resolveAssetsUrls($el) {
    $el.find('[data-assets-src]:not([src])').each(function() {
      $(this).attr('src', assetsUrl($(this).data('assets-src')));
    });
  }

  // when a portion (or all) of the DOM is loaded...
  function load($el) {
    $el = $el || $('body');

    console.log('load', $el[0]);

    resolveAssetsUrls($el);
    wdk.components.ajaxElement.triggerElements($el);
    registerTable($el);
    registerTooltips($el);
    registerToggle($el);
    registerCollapsible($el);
    registerSnippet($el);
    registerTruncate($el);
    registerEditable($el);
    registerButton($el);
    wdk.util.executeOnloadFunctions($el);
    triggerControllerEvents($el);
    invokeControllers($el);
  }

  _.extend(ns, {
    VERSION: wdkConfig.version,
    MODEL_NAME: wdkConfig.modelName,
    $: $,
    load: load,
    cookieTest: cookieTest,
    setUpDialogs: setUpDialogs,
    setUpPopups: setUpPopups,
    exportBaseURL: exportBaseURL,
    modelName: modelName,
    readCookie: readCookie,
    createCookie: createCookie,
    registerQuestionEvent: registerQuestionEvent,
    onloadQuestion: onloadQuestion, // anything using this?
    findActiveWorkspace: findActiveWorkspace,
    findActiveView: findActiveView,
    chooseAll: chooseAll,
    webappUrl: webappUrl,
    assetsUrl: assetsUrl,
    isUserLoggedIn: isUserLoggedIn,
    checkFields: checkFields,
    uncheckFields: uncheckFields,
    setUpNavDropDowns: setUpNavDropDowns
  });

});
