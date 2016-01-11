import test from 'tape';
import {
  latest
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
  t.plan(1); // only one assertion should be executed
  let latestTimeout = latest(timeout);
  latestTimeout(100).then(ms => t.equals(ms, 400));
  latestTimeout(200).then(ms => t.equals(ms, 400));
  latestTimeout(300).then(ms => t.equals(ms, 400));
  latestTimeout(400).then(ms => t.equals(ms, 400));
});
