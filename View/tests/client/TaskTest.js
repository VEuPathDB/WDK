/**
 * Created by dfalke on 9/28/16.
 */
import _test from 'ava';
import { Task } from '../../webapp/wdk/js/client/utils/Task';
const noop = () => {};
const test = _test.cb

test('Task.of', (t) => {
  Task.of(20).run((v) => {
    t.is(v, 20, 'should fulfill with value passed to it.');
    t.end()
  });
});

test('Task.reject', (t) => {
  Task.reject('error').run(noop, (e) => {
    t.is(e, 'error', 'should reject with the value passed to it.');
    t.end();
  });
});

test('Task#map', (t) => {
  Task.of(20).map((v) => v * v).run((v) => {
    t.is(v, 400, 'should fulfill with mappend applied to parent Task\'s fulfillment value.');
    t.end();
  });
});

test('Task#mapRejected', (t) => {
  Task.reject(20).mapRejected((v) => v * v).run(noop, (v) => {
    t.is(v, 400, 'should reject with mappend applied to parent Task\'s rejection value.');
    t.end();
  });
});

test('Task#chain', (t) => {
  t.plan(2);

  Task.of(20).chain((v) => delayValue(v * v, 1000)).run((v) => {
    t.is(v, 400, 'should fulfill with the inner Task\'s fulfillment of the outer Task\'s fulfillent value.');
    t.end();
  });

  Task.of(20).chain(() => Task.reject('fail')).run(noop, (e) => {
    t.is(e, 'fail', 'should reject with the inner Task\'s rejection value')
  });
});

test('Task#chainRejected', (t) => {
  Task.reject(20).chainRejected((e) => Task.of(e * 20)).run((v) => {
    t.is(v, 400, 'should fulfill with the inner Task\'s fulfillment of the outer Task\'s rejection value.')
    t.end();
  });
});

test('Task.fromPromise', t => {
  let cancel = Task.fromPromise(Promise.resolve(1))
    .run(() => void t.fail('this should cancel'));
  cancel();

  Task.fromPromise(Promise.resolve(10))
    .run(v => t.is(v, 10, 'should resolve with Promise\'s resolve value'));

  setTimeout(() => t.end(), 1000);
});

test('Task.cancel', t => {
  t.plan(1);
  let task = new Task(function(resolve) {
    setTimeout(resolve, 100, 1);
  });

  // this assertion should never be executed since the Task is immediately
  // cancelled. If it does execute, the test will fail since we planned for
  // 1 assertion, but will have made 2 assertions when `t.end()` is called.
  let cancel = task.run(v => t.truthy(v));
  cancel();

  setTimeout(function() {
    t.truthy(true);
    t.end();
  }, 200);
});

function delayValue(value, delay) {
  return new Task(function(fulfill) {
    let id = setTimeout(fulfill, delay, value);
    return () => void clearTimeout(id);
  });
}
