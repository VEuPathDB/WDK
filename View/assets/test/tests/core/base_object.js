describe('wdk.core.base_object', function() {
  var BaseObject = wdk.core.BaseObject;

  describe('BaseObject', function() {

    describe('extend', function() {
      it('should preserve inheritance', function() {

        var Foo = BaseObject.extend();
        var Bar = Foo.extend();

        var foo = Foo.create();
        var bar = Bar.create();

        expect(foo instanceof Foo).to.equal(true);
        expect(bar instanceof Bar).to.equal(true);
        expect(bar instanceof Foo).to.equal(true);

      });

      it('should allow a single prototype object to be specified', function() {
        var A = BaseObject.extend({
          say: function() { return 'first arg' }
        });
        var a = A.create();
        expect(a.say()).to.equal('first arg');
      });

      it('should allow a variable number of prototype objects to be specified', function() {
        var A = BaseObject.extend({
          say: function() { return 'first arg' }
        }, {
          tell: function() { return 'second arg' }
        });

        var a = A.create();

        expect(a.say()).to.equal('first arg');
        expect(a.tell()).to.equal('second arg');
      });

    });

    describe('create', function() {
      it('should call a user-defined constructor', function() {
        var A = BaseObject.extend({
          constructor: function(name) {
            this.name = name;
          }
        });

        var a = A.create('Dave');

        expect(a.name).to.equal('Dave');
      });

      it('should apply late mixins', function() {
        var A = BaseObject.extend();
        var Mixin = {
          say: function() {
            return 'I am from a mixin';
          }
        };
        var a = A.create({
          mixins: [Mixin]
        });

        var B = BaseObject.extend({
          constructor: function() {
            this.constructor.applyMixin.call(this, Mixin)
          }
        });
        var b = B.create();

        expect(a.say()).to.equal('I am from a mixin');
        expect(b.say()).to.equal('I am from a mixin');
      });
    });

    describe('reopenClass', function() {
      var A, B;
      beforeEach(function() {
        A = BaseObject.extend();
        B = A.extend();

        A.reopenClass({
          say: function() {
            return 'I am A';
          }
        });
      });

      it('should allow static properties to be added', function() {
        expect(A.say()).to.equal('I am A');
      });

      it('should not pollute the prototype chain', function() {
        expect(typeof B.say).to.equal('undefined');
      });

    });

  });
});
