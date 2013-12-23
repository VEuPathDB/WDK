/**
 * Bootstrap the wdk app. This runs on DOMReady and initializes views, etc.
 */

(function bootstrap($) {
  'use strict';

  // conditionally load some scripts
  Modernizr.load([
    {
      test: !!window.JSON,
      nope: wdk.assetsUrl('/wdk/lib/json3.min.js'),
      complete: $.bind(window, wdk.load)
    },
    {
      // IE 8 needs some canvas help
      test: Modernizr.canvas,
      nope: wdk.assetsUrl('/wdk/lib/excanvas.min.js'),
      both: [
        '/wdk/lib/jquery.flot-0.8.1.min.js',
        '/wdk/lib/jquery.flot.categories-0.8.1.min.js'
      ].map(wdk.assetsUrl)
    }
  ]);

  // Override BlockUI CSS and message
  $.blockUI.defaults.overlayCSS.opacity = 0.2;
  $.blockUI.defaults.message = '<span class="h2center">Please wait...</span>';
  // allow rules in external CSS to be effective
  $.blockUI.defaults.css = {};

  // Override JSTree themes directory
  $.jstree._themes = wdk.assetsUrl('/wdk/lib/jstree/themes/');

  // Override jQueryUI tabs defaults
  //
  // We add two pieces of functionality:
  // 1. Spinner
  // 2. Cache content (when successfully loaded)
  $.extend($.ui.tabs.prototype.options, {
    cache: true,
    beforeLoad: beforeLoad,
    load: wdk.load
  });

  function beforeLoad(event, ui) {
    var $this = $(this);
    if (ui.tab.data("loaded") && $this.tabs("option", "cache")) {
      event.preventDefault();
      return;
    }

    ui.tab.find("span:last").append('<img style="margin-left:4px; ' +
      'position: relative; top:2px;" src="' +
      wdk.assetsUrl('/wdk/images/filterLoading.gif') + '"/>');

    ui.jqXHR
      .done(function() {
        ui.tab.data("loaded", true);
      })

      .fail(function(jqXHR, textStatus, errorThrown) {
        if (errorThrown != "abort") {
          ui.panel.html('<p style="padding:1em;">Unable to load tab content: ' +
            '<i>' + errorThrown + '</i></p>');
        }
      })

      .always(function() {
        ui.tab.find("img").remove();
      });
  }

  // This is wrong:
  //   1. This gets called before tab data is loaded (as of jQuery UI 1.9?)
  //   2. This gets called for _all_ ajax requests, which is unecessary.
  $(document).ajaxSuccess(function(event, xhr, ajaxOptions) {
    xhr.done(wdk.load);
  });

  $(wdk.cookieTest);
  $(wdk.setUpDialogs);
  $(wdk.setUpPopups);
  //$(wdk.load);

}(jQuery));
