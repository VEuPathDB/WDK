describe("wdk.addStepPopup", function() {

  describe("validateOperations", function() {

    it( "is a function", function() {
      expect(wdk.addStepPopup.validateOperations instanceof Function).toBe(true);
    });

    it( "executes inline onsubmit", function() {
      var $form, inlineSubmit;

      // create a function with a spy
      inlineSubmit = jasmine.createSpy("inline-submit");

      // create form
      $form = $("<form/>")
      // add spy function to form data
      .data("inline-submit", inlineSubmit)
      // attach submit handler
      .submit(wdk.addStepPopup.validateOperations)
      // submit
      .submit();

      expect(inlineSubmit).toHaveBeenCalled();
      expect(inlineSubmit.calls.length).toEqual(1);
    });

  });

});
