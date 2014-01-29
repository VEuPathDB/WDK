describe('wdk.core.c_properties', function() {

  var create = wdk.core.c_properties.create;
  var extend = wdk.core.c_properties.extend;

  describe('properties attached to contructors', function() {

    it('should preserve inheritance', function() {

      var Foo = function() {};
      Foo.create = create;
      Foo.extend = extend;

      var Bar = Foo.extend();

      var foo = Foo.create();
      var bar = Bar.create();

      expect(foo instanceof Foo).toBe(true);
      expect(bar instanceof Bar).toBe(true);
      expect(bar instanceof Foo).toBe(true);

    });

  });

});
