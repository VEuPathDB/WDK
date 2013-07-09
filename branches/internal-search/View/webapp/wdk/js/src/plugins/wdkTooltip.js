(function($) {
  "use strict";

  // A wrapper around the qtip2 http://craigsworks.com/projects/qtip2/
  // library to provide a data api.
  //
  // API (data-{property}):
  //  my : String corner of tooltip relative to position
  //  at : String corner of element relative to position
  //  content : String selector containing content
  //
  // opts are sent to qtip. Data opts win

  $.fn.wdkTooltip = function(opts) {
    return this.each(function(idx, node) {
      var settings = $.extend(true, {}, $.fn.wdkTooltip.defaults, opts);
      var $node = $(node);
      var $data = $node.data();
      var dataOpts = {};

      if ($data.at) {
        dataOpts.position = dataOpts.position || {};
        dataOpts.position.at = $data.at;
      }
      if ($data.my) {
        dataOpts.position = dataOpts.position || {};
        dataOpts.position.my = $data.my;
      }
      if ($data.content) {
        dataOpts.content = {
          text: $node.find($data.content)
        };
      }

      var dataSettings = $.extend(true, settings, dataOpts);

      $node.qtip(dataSettings);
    });
  };

  $.fn.wdkTooltip.defaults = {
    position: {
      my: "top center",
      at: "bottom center",
      viewport: $(window)
    },
    hide: {
     fixed: true
    }
  };

})(jQuery);
