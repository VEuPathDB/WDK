!(function($) {
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

}(jQuery));
