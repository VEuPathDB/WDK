/**
 * Created by dfalke on 9/28/16.
 */
import test from 'tape';
import { Task } from '../../webapp/wdk/js/client/utils/Task';

test('Task', t => {
  t.plan(7);

  Task.of(20).run({
    onFulfill: v => {
      t.equal(v, 20, 'Task.of should fulfill with value passed to it.');
    }
  });

  Task.reject('error').run({
    onRejected: e => {
      t.equal(e, 'error', 'Task.reject should reject with the value passed to it.');
    }
  });

  Task.of(20).map(v => v * v).run({
    onFulfill: v => {
      t.equal(v, 400, 'task.map should fulfill with mappend applied to parent Task\'s fulfillment value.');
    }
  });

  Task.reject(20).mapRejected(v => v * v).run({
    onRejected: v => {
      t.equal(v, 400, 'task.mapRejected should reject with mappend applied to parent Task\'s rejection value.');
    }
  });

  Task.of(20).chain(v => delayValue(v * v, 1000)).run({
    onFulfill: v => {
      t.equal(v, 400, 'task.chain should fulfill with the inner Task\'s fulfillment of the outer Task\'s fulfillent value.');
    }
  });

  Task.of(20).chain(() => Task.reject('fail')).run({
    onRejected: e => {
      t.equal(e, 'fail', 'task.chain should reject with the inner Task\'s rejection value')
    }
  });
  
  Task.reject(20).chainRejected(e => Task.of(e * 20)).run({
    onFulfill: v => {
      t.equal(v, 400, 'task.chainRejected should fulfill with the inner Task\'s fulfillment of the outer Task\'s rejection value.')
    }
  });
  
});

function delayValue(value, delay) {
  return new Task(function(fulfill) {
    let id = setTimeout(fulfill, delay, value);
    return () => void clearTimeout(id);
  });
}
