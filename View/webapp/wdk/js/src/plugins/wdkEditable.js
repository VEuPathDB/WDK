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


  $.widget("wdk.editable", {
    options: {
      // event handler
      save: function() {},
      // event handler
      show: function() {},
      // event handler
      hide: function() {},
      help: "ENTER to save; ESC to cancel"
    },

    // initialization code
    _create: function() {

      var widget = this;

      // input box
      widget.$input = $("<input/>")
      .attr("type", "text");

      // attach handlers
      widget.element.on("editableshow", widget.options.show);
      widget.element.on("editablehide", widget.options.hide);
      widget.element.on("editablesave", widget.options.save);

      widget.element.click(function() {
        widget.show.call(widget);
      });
    },

    // show input box
    show: function() {
      var widget = this;

      if (widget.element.children('input').length == 0) {
        // cache original value
        widget.value = widget.element.text();

        widget.$input.val(widget.value);

        widget.element.html(widget.$input);

        widget.$input.select();

        // TODO - replace blur with click outside of element
        //    this will prevent accidental closing
        widget.$input.on("blur keyup", function(e) {
          if (e.type === "blur" || e.which === 13) {
            // ENTER pressed - save
            widget.save.call(widget);
          } else if (e.which === 27) {
            // ESC pressed - cancel
            widget.hide.call(widget);
          }
        });
        widget.element.trigger("editableshow", [widget]);
      }
    },

    // hide input box
    hide: function() {
      var widget = this;
      widget.element.text(widget.value);
      widget.element.trigger("editablehide", [widget]);
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
