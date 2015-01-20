import assert from 'assert';
import { create, extend } from 'wdk/core/c_properties';

describe('wdk.core.c_properties', function() {

  describe('properties attached to contructors', function() {

    it('should preserve inheritance', function() {

      var Foo = function() {};
      Foo.create = create;
      Foo.extend = extend;

      var Bar = Foo.extend();

      var foo = Foo.create();
      var bar = Bar.create();

      assert(foo instanceof Foo);
      assert(bar instanceof Bar);
      assert(bar instanceof Foo);

    });

  });

});
