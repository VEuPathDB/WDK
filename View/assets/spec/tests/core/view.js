describe('wdk.core.view', function() {
  var View = wdk.core.View;

  describe('View', function() {

    it('should call didInitialize', function(done) {
      View.extend({
        didInitialize: done
      }).create();
    });

    it('should apply mixins', function() {
      var MyMixin = {
        say: function() {
          return 'mixin';
        }
      };

      var MyView = View.extend({
        mixins: [ MyMixin ]
      });

      var myView = MyView.create();

      var AnotherView = View.extend({
        initialize: function() {
          this.applyMixin(MyMixin);
        }
      });

      var anotherView = AnotherView.create();

      expect(myView.say()).to.equal('mixin');
      expect(anotherView.say()).to.equal('mixin');
    });

  });

});
