import test from 'ava';
import {
  latest,
  synchronized,
  Mutex
} from '../../webapp/wdk/js/client/utils/PromiseUtils';

// helpers

function timeout(ms) {
  return new Promise(function(resolve, reject) {
    setTimeout(function() {
      resolve(ms);
    }, ms);
  });
}

test('latest', function(t) {
  let latestTimeout = latest(timeout);
  return Promise.race([
    latestTimeout(100).then(ms => t.is(ms, 400)),
    latestTimeout(200).then(ms => t.is(ms, 400)),
    latestTimeout(300).then(ms => t.is(ms, 400)),
    latestTimeout(400).then(ms => t.is(ms, 400))
  ]);
});

test('synchronized', function(t) {
  const synchronizedTimeout = synchronized(timeout);
  const called = [];
  return Promise.all([
    synchronizedTimeout(400).then(ms => called.push(ms)),
    synchronizedTimeout(300).then(ms => called.push(ms)),
    synchronizedTimeout(200).then(ms => called.push(ms)),
    synchronizedTimeout(100).then(ms => {
      called.push(ms);
      t.is(ms, 100);
      t.deepEqual(called, [400, 300, 200, 100]);
    })
  ]);
})

test('Mutex', function(t) {
  const mutex = new Mutex();
  const called = [];
  const error = new Error("Inside synchronize.");
  const mss = [400,300,200,100];

  mss.forEach((ms, index) => {
    mutex.synchronize(() => {
      t.is(index, called.length,
        "synchronize callback should be called only when previous callback resolved.")
      return timeout(ms).then(ms => {
        called.push(ms)
      });
    });
  })

  mutex.synchronize(() => { throw error; }).catch(err => {
    t.is(err, error, "Errors should be handled by consumer");
  });

  return mutex.synchronize(() => {}).then(() => {
    t.deepEqual(called, [400,300,200,100], "Promises should resolve in order added");
  });

});
