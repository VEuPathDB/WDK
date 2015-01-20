import assert from 'assert';
import 'wdk/views/core/view';

describe('wdk.views.core', function() {
  var View = wdk.views.core.View;

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

      assert(myView.say() === 'mixin');
      assert(anotherView.say() === 'mixin');
    });

  });

});
