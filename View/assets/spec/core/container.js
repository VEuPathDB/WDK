describe('wdk.core.container', function() {

  var BaseObject = wdk.core.BaseObject;
  var container = wdk.core.Container.create();

  describe('Container', function() {
    it('should register and return factories', function() {
      var Factory = BaseObject.extend();
      container.register('my-factory', Factory);

      var retrievedFactory = container.get('my-factory');
      var instance = retrievedFactory.create();

      expect(retrievedFactory).toEqual(Factory)
      expect(instance instanceof Factory).toBe(true);
    });

    it('should throw an error on re-register', function() {
      var err = null;
      try {
        container.register('my-factory', function NewFactory() {});
      } catch(e) {
        err = e;
      }
      expect(err instanceof TypeError).toBe(true);
    });

    it('should throw an error on undefined factory', function() {
      var err = null;
      try {
        container.register('new-factory');
      } catch(e) {
        err = e;
      }
      expect(err instanceof TypeError).toBe(true);
    });

  });
});
