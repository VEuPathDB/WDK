import assert from 'assert';
import 'wdk/core/runloop';

describe('wdk.core.runloop', function() {

  var runloop;

  beforeEach(function() {
    runloop = wdk.core.RunLoop.create();
  });

  describe('defer', function() {

    it('should execute after non-deferred functions', function(done) {
      var values = [];

      function undeferred() {
        values.push('undeferred');
      }

      function deferred() {
        values.push('deferred');

        assert(values[0] === 'undeferred');
        assert(values[1] === 'deferred');
        done();
      }

      runloop.defer(deferred);
      undeferred();

    });

    it('should execute for every call', function(done) {
      var RUNS = 10;
      var callCount = 0;

      for (var i = 1; i <= RUNS; i++) (function(i) {
        runloop.defer(function() {
          callCount++;
          if (i === RUNS) {
            assert(callCount === RUNS);
            done();
          }
        });
      }(i));

    });

    it('should nest properly', function(done) {
      var order = [];
        runloop.defer(function() {
          runloop.defer(function() {
            order.push('inner');
            assert(order.indexOf('outter') === 0);
            assert(order.indexOf('middle') === 1);
            assert(order.indexOf('inner') === 2);
            done();
          });
          order.push('middle');
        });
        order.push('outter');

    });

    it('should defer for at least the specified period of time', function(done) {
      var timeout = 200;
      var deferComplete = false;

        setTimeout(function() {
          assert(!deferComplete);
          done()
        }, timeout - 1);

        runloop.defer(function() {
          deferComplete = true;
        }, timeout);


    });
  });

  describe('deferOnce', function() {
    it('should only execute once in a given event loop', function(done) {
      var RUNS = 10;
      var callCount = 0;

      function incrementCounter() {
        callCount++;
      }

      function update() {
        runloop.deferOnce(incrementCounter);
      }

      for (var i = 1; i <= RUNS; i++) {
        update();
      }

      setTimeout(function() {
        assert(callCount === 1);
        done();
      }, 300);

    });

    it('should defer for at least the specified period of time', function(done) {
      var timeout = 200;
      var deferOnceDone = false;

      setTimeout(function() {
        assert(!deferOnceDone);
        done();
      }, timeout - 1);

      runloop.deferOnce(function() {
        deferOnceDone = true;
      }, timeout);

    });
  });

});
