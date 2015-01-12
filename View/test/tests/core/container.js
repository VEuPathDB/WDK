import assert from 'assert';
import 'wdk/core/container';
import 'wdk/core/base_class';

describe('wdk.core.container', function() {

  var BaseClass = wdk.core.BaseClass;
  var container = wdk.core.Container.create();

  describe('Container', function() {
    it('should register and return factories', function() {
      var Factory = BaseClass.extend();
      container.register('my-factory', Factory);

      var retrievedFactory = container.get('my-factory');
      var instance = retrievedFactory.create();

      assert(retrievedFactory === Factory)
      assert(instance instanceof Factory === true);
    });

    it('should throw an error on re-register', function() {
      var err = null;
      try {
        container.register('my-factory', function NewFactory() {});
      } catch(e) {
        err = e;
      }
      assert(err instanceof TypeError);
    });

    it('should throw an error on undefined factory', function() {
      var err = null;
      try {
        container.register('new-factory');
      } catch(e) {
        err = e;
      }
      assert(err instanceof TypeError);
    });

  });
});
