wdk.util.namespace("wdk.plugin", function(ns, $) {
  "use strict";

  // The "editable" plugin provides inline-edit functionality
  //  
  // It can be used in the following way:
  //
  //   $("selector").editable({
  //     change: function(event, widget) {
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
  //   $("selector").editable("edit")


  $.widget("wdk.editable", {
    options: {
      change: function() {}
    },

    // initialization code
    _create: function() {

      var widget = this;

      this.element.click(function() {
        widget.edit.call(widget);
      });
    },

    // public function - create input box
    edit: function() {
      var widget = this,
          $this = widget.element;

      if ($this.children('input').length == 0) {
        var $inputbox;

        // cache original value
        widget.cachedText = $this.text();

        $inputbox = $("<input/>")
        .attr("type", "text")
        .val(widget.cachedText);

        $this.html($inputbox);

        $inputbox.select();

        $inputbox.on("blur keyup", function(e) {
          if (e.type === "blur" || e.which === 13) {
            var value = $inputbox.val();
            $this.text(value);
            if (typeof widget.options.change === "function") {
              // TODO - determine what the object context should be
              // currently it is the input element (which is probably correct)
              // TODO - make this return a promise?
              widget.options.change.call(this, e, widget);
            }
          } else if (e.which === 27) {
            // ESC entered - cancel
            $this.text(widget.cachedText);
          }
        });
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
