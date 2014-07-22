describe("wdk.addStepPopup", function() {

  describe("validateOperations", function() {

    it( "is a function", function() {
      expect(wdk.addStepPopup.validateOperations instanceof Function).to.equal(true);
    });

    it( "executes inline onsubmit", function(done) {
      var $form;

      function inlineSubmit() {
        done();
      }

      // create form
      $form = $("<form/>")
      // add spy function to form data
      .data("inline-submit", inlineSubmit)
      // attach submit handler
      .submit(wdk.addStepPopup.validateOperations)
      // submit
      .submit();
    });

  });

});
