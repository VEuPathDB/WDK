describe('wdk.core.runloop', function() {

  var runloop;

  beforeEach(function() {
    runloop = wdk.core.RunLoop.create();
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
        runloop.defer(deferred);
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
          runloop.defer(function() {
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
        runloop.defer(function() {
          runloop.defer(function() {
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

    it('should defer for at least the specified period of time', function() {
      var timeout = 200;
      var done = false;
      var doneEarly = false;

      runs(function() {
        setTimeout(function() {
          doneEarly = done;
        }, timeout - 1);

        runloop.defer(function() {
          done = true;
        }, timeout);
      });

      waitsFor(function() { return done || doneEarly; }, timeout * 2);

      runs(function() {
        expect(doneEarly).toBe(false);
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
        runloop.deferOnce(incrementCounter);
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

    it('should defer for at least the specified period of time', function() {
      var timeout = 200;
      var done = false;
      var doneEarly = false;

      runs(function() {
        setTimeout(function() {
          doneEarly = done;
        }, timeout - 1);

        runloop.deferOnce(function() {
          done = true;
        }, timeout);
      });

      waitsFor(function() { return done; }, timeout * 2);

      runs(function() {
        expect(doneEarly).toBe(false);
      });

    });
  });

});
