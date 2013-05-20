wdk.util.namespace("wdk.plugin", function(ns, $) {
  "use strict";

  // The "editable" plugin provides inline-edit functionality
  //  
  // It can be used in the following way:
  //
  //   $("selector").editable({
  //     save: function(event, widget) {
  //       var input = this;
  //       var user_text = this.value;
  //       // do some stuff...
  //     }
  //   })
  //
  //
  // The widget has one public function: edit.
  // It can be used in the following way to trigger the input box:
  //
  //   $("selector").editable("show")

  var triggers = ['click', 'hover', 'focus'];

  // incremented for each widget created
  var widgetId = 1;

  function attachTrigger(widget, trigger) {

  }

  $.widget("wdk.editable", {

    options: {
      // event handler
      save: function() {},

      // event handler
      show: function() {},

      // event handler
      hide: function() {},

      // events trigger - can be click | hover | focus | manual
      //   * multiple events can be passed as a space separated list
      //   * manual will turn off all triggers
      trigger: "click",

      // select text on show
      selectOnShow: true,

      // help text
      help: "ENTER to save; ESC to cancel"

    },

    // initialization code
    _create: function() {

      var widget = this;

      widget._id = widgetId++;
      widget._ns = ".editable_" + widget._id;
      // input box
      widget.$input = $("<input/>")
      .attr("type", "text");

      // attach triggers
      widget._attachTrigger(widget.options.trigger);

      // attach handlers
      widget.element.on("editableshow", widget.options.show);
      widget.element.on("editablehide", widget.options.hide);
      widget.element.on("editablesave", widget.options.save);

    },

    _setOption: function(key, value) {
      if (key === "trigger") {
        this.options.trigger = value;
        this._attachTrigger(value);
      }
    },

    _attachTrigger: function(trigger) {
      var widget = this;

      widget.element.off(widget._ns);
      $("body").off(widget._ns);

      // no trigger
      if (trigger === "manual") return;

      // namespace triggers
      widget._nsTriggers = $.map(this.options.trigger.split(/\s+/), function(a) {
        return a + widget._ns;
      }).join(" ");

      widget.element.on(widget._nsTriggers, function() {
        widget.show.call(widget);
      });

    },

    // show input box
    show: function() {
      var widget = this;
      $("body").off(widget._ns);
      // hide if trigger is outside of widget
      setTimeout(function() {
        $("body").on(widget._nsTriggers, function(e) {
          if (!widget.element.is(e.target) &&
              widget.element.has(e.target).length === 0) {
            widget.hide.call(widget);
          }
        });
      }, 0);

      // already shown
      if (widget.element.has('input').length !== 0) {
        widget.$input.select();
        return;
      }

      var e = $.Event("editableshow");

      widget.element.trigger(e, [widget]);

      if (!e.isDefaultPrevented()) {
        // cache original value
        widget.value = widget.element.text();

        widget.$input.val(widget.value);

        widget.element.html(widget.$input);

        widget.$input.select();

        widget.element.addClass(widget.widgetBaseClass + "-show");

        // save on ENTER, hide on ESC
        widget.$input.on("keyup", function(e) {
          if (e.which === 13) {
            // ENTER pressed - save
            widget.save.call(widget);
          } else if (e.which === 27) {
            // ESC pressed - cancel
            widget.hide.call(widget);
          }
        });
      }
    },

    // hide input box
    hide: function() {
      var widget = this;

      // already hidden
      if (widget.element.has("input").length === 0) return;

      var e = $.Event("editablehide");
      widget.element.trigger(e, [widget]);
      if (!e.isDefaultPrevented()) {
        widget.element.text(widget.value);
        widget.element.removeClass(widget.widgetBaseClass + "-show");
      }
    },

    // save text in input box to DOM element
    save: function() {
      var widget = this;
      widget.oldValue = widget.value;
      widget.value = widget.$input.val();
      widget.element.text(widget.value);
      if (widget.value !== widget.oldValue) {
        widget.element.trigger("editablesave", [widget]);
      }
    }

  });

  /***********************************
  Results page: edit strategy name
  by Ben Thomas, modified
  http://www.unleashed-technologies.com/
  **************************************/
  // $.fn.wdkEditable = function(opts) {
  //   return this.each(function() {

  //     var $this = $(this),
  //         dataOpts = $this.data();

  //     $this.click(function() {
  //       // cache original value
  //       var text = $this.text();
  //       if ($this.children('input').length == 0) {

  //         var $inputbox = $("<input/>")
  //         .attr("type", "text")
  //         .val(text);

  //         $(this).html($inputbox);
  //         $inputbox.focus();
  //         $inputbox.blur(function() {
  //           var value = $inputbox.val();
  //           $this.text(value);
  //         });
  //       }
  //     });
  //   });
  // };

});
