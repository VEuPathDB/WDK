describe('wdk.core.runloop', function() {

  var runtime;
  beforeEach(function() {
    runtime = wdk.core.RunLoop.create();
  });

  describe('defer', function() {

    it('should execute after non-deferred functions', function() {
      var values = [];
      var done = false;

      function undeferred() {
        values.push('undeferred');
      }

      function deferred() {
        values.push('deferred');
        done = true;
      }

      runs(function() {
        runtime.defer(deferred);
        undeferred();
      });

      waitsFor(function() { return done; }, 200);

      runs(function() {
        expect(values[0]).toBe('undeferred');
        expect(values[1]).toBe('deferred');
      });

    });

    it('should execute for every call', function() {
      var RUNS = 10;
      var callCount = 0;
      var done = false;

      runs(function() {
        for (var i = 1; i <= RUNS; i++) (function(i) {
          runtime.defer(function() {
            callCount++;
            done = (i === RUNS);
          });
        }(i));
      });

      waitsFor(function() { return done; }, 200);

      runs(function() {
        expect(callCount).toBe(RUNS);
      });

    });

    it('should nest properly', function() {
      var done = false;
      var order = [];
      runs(function() {
        runtime.defer(function() {
          runtime.defer(function() {
            order.push('inner');
            done = true;
          });
          order.push('middle');
        });
        order.push('outter');
      });

      waitsFor(function() { return done; }, 200);

      runs(function() {
        expect(order.indexOf('outter')).toBe(0);
        expect(order.indexOf('middle')).toBe(1);
        expect(order.indexOf('inner')).toBe(2);
      });

    });
  });

  describe('deferOnce', function() {
    it('should only execute once in a given event loop', function() {
      var RUNS = 10;
      var callCount = 0;
      var done = false;

      function incrementCounter() {
        callCount++;
      }

      function update() {
        runtime.deferOnce(incrementCounter);
      }

      runs(function() {
        for (var i = 1; i <= RUNS; i++) (function(i) {
          update();
          setTimeout(function() {
            done = (i === RUNS);
          }, 100);
        }(i));
      });

      waitsFor(function() { return done; }, 200);

      runs(function() {
        expect(callCount).toBe(1);
      });
    });
  });

});
