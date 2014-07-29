(function($) {
  'use strict';

  // Override jQueryUI tabs defaults
  //
  // We add two pieces of functionality:
  // 1. Spinner
  // 2. Cache content (when successfully loaded)

  var uiLoad = $.ui.tabs.prototype.load;

  /**
   * Overridden jQuery.ui.tabs.prototype.load method
   *
   * Original method signature was load(index, event).
   *
   * We add the following:
   *    load(index, event, options)
   *    load(index, options)
   *
   * Available options are:
   *    - skipCache {Boolean} : Force tab to load, even
   *      with widget.options.cache = true.
   */
  $.ui.tabs.prototype.load = function(index, event, options) {
    if (!(event instanceof $.Event)) {
      options = event;
      event = undefined;
    }

    if (options) {
      // assume an options object as been passed

      // force cache to be skipped
      var origCache = this.options.cache;
      if (options.skipCache) this.options.cache = false;

      // call original load method
      uiLoad.call(this, index, event);

      // reset widget cache option
      if (options.skipCache) this.options.cache = origCache;
    } else {
      uiLoad.call(this, index, event);
    }
  };

  $.extend($.ui.tabs.prototype.options, {
    cache: true,
    beforeLoad: beforeLoad
  });

  function beforeLoad(event, ui) {
    /* jshint validthis:true */
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

}(jQuery));
