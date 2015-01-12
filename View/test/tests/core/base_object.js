import assert from 'assert';
import 'wdk/core/base_class';

describe('wdk.core.base_class', function() {
  var BaseClass = wdk.core.BaseClass;

  describe('BaseClass', function() {

    describe('extend', function() {
      it('should preserve inheritance', function() {

        var Foo = BaseClass.extend();
        var Bar = Foo.extend();

        var foo = Foo.create();
        var bar = Bar.create();

        assert(foo instanceof Foo);
        assert(bar instanceof Bar);
        assert(bar instanceof Foo);

      });

      it('should allow a single prototype object to be specified', function() {
        var A = BaseClass.extend({
          say: function() { return 'first arg' }
        });
        var a = A.create();
        assert(a.say() === 'first arg');
      });

      it('should allow a variable number of prototype objects to be specified', function() {
        var A = BaseClass.extend({
          say: function() { return 'first arg' }
        }, {
          tell: function() { return 'second arg' }
        });

        var a = A.create();

        assert(a.say() === 'first arg');
        assert(a.tell() === 'second arg');
      });

    });

    describe('create', function() {
      it('should call a user-defined constructor', function() {
        var A = BaseClass.extend({
          constructor: function(name) {
            this.name = name;
          }
        });

        var a = A.create('Dave');

        assert(a.name === 'Dave');
      });

      it('should apply late mixins', function() {
        var A = BaseClass.extend();
        var Mixin = {
          say: function() {
            return 'I am from a mixin';
          }
        };
        var a = A.create({
          mixins: [Mixin]
        });

        var B = BaseClass.extend({
          constructor: function() {
            this.constructor.applyMixin.call(this, Mixin)
          }
        });
        var b = B.create();

        assert(a.say() === 'I am from a mixin');
        assert(b.say() === 'I am from a mixin');
      });
    });

    describe('reopenClass', function() {
      var A, B;
      beforeEach(function() {
        A = BaseClass.extend();
        B = A.extend();

        A.reopenClass({
          say: function() {
            return 'I am A';
          }
        });
      });

      it('should allow static properties to be added', function() {
        assert(A.say() === 'I am A');
      });

      it('should not pollute the prototype chain', function() {
        assert(typeof B.say === 'undefined');
      });

    });

  });
});
