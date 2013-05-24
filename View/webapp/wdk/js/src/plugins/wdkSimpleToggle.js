(function($) {
  "use strict";

  // Very basic toggle functionality designed
  // to be performant with large toggle groups
  //
  // Showing and hiding will be CSS driven
  //
  //
  // show:
  //  * add wdk-toggle-show className
  //
  // hide:
  //  * remove wdk-toggle-show className
  //
  // toggle:
  //  * toggle wdk-toggle-show className
  //
  //
  // clicking on wdk-toggle-name will toggle

  $.widget("wdk.simpleToggle", {

    options: {
      // show by default?
      show: false
    },

    _create: function() {

      var widget = this;

      // add shown/hidden icons
      $("<span/>").addClass("wdk-toggle-shown-icon ui-icon ui-icon-triangle-1-s")
        .prependTo(widget.element.find(".wdk-toggle-name"));

      $("<span/>").addClass("wdk-toggle-hidden-icon ui-icon ui-icon-triangle-1-e")
        .prependTo(widget.element.find(".wdk-toggle-name"));

      widget.element.find(".wdk-toggle-name")
        .attr("title", "Click to show or hide")
        // attach event
        .on("click", function(e) {
          e.preventDefault();
          widget.toggle.call(widget);
        })

      if (widget.options.show) {
        widget.show.call(widget);
      }

    },

    show: function() {
      this.element.addClass("wdk-toggle-show");
    },

    hide: function() {
      this.element.removeClass("wdk-toggle-show");
    },

    toggle: function(show) {
      this.element.toggleClass("wdk-toggle-show", show);
    }

  });

})(jQuery);
