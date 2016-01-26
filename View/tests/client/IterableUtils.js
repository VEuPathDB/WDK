import test from 'tape';
import * as i from '../../webapp/wdk/js/client/utils/IterableUtils';

let GeneratorFunction = Object.getPrototypeOf(function*(){}).constructor;

function integers() {
  return {
    *[Symbol.iterator]() {
      let n = 1;
      while (true) {
        yield n++;
      }
    }
  };
}

test('seq', t => {
  let s = i.seq([1,2,3]);

  // These can be removed when we use type annotations
  t.ok(s[Symbol.iterator], 'seq returns an iterable');
  t.ok(s.map()[Symbol.iterator], 'seq#map() returns an iterable');
  t.ok(s.filter()[Symbol.iterator], 'seq#filter() returns an iterable');
  t.ok(s.take()[Symbol.iterator], 'seq#take() returns an iterable');
  t.ok(s.takeWhile()[Symbol.iterator], 'seq#takeWhile() returns an iterable');
  t.ok(s.dropWhile()[Symbol.iterator], 'seq#dropWhile() returns an iterable');

  t.deepEqual(s.map(n => n * n).toArray(), [ 1, 4, 9 ]);

  let mapCallCount = 0;
  let things = i.seq(integers())
  .map(n => (mapCallCount++, n * n))
  .takeWhile(n => n < 30)
  .reduce((sum, n) => sum + n)

  t.equal(mapCallCount, 7);


  let s2 = i.seq(s).map(n => n * 2);
  t.deepEqual(s2.toArray(), [ 2, 4, 6 ], 'seqs can be composed');

  t.end();
});

test('map', function(t) {
  t.deepEqual(
    Array.from(i.map(c => c.name, [ { name: 'A' }, { name: 'B' }, { name: 'C' } ])),
    [ 'A', 'B', 'C' ],
    'map should apply the transform to each item in an iterable'
  );

  t.end();
});

test('filter', function(t) {
  t.deepEqual(
    Array.from(i.filter(n => n % 2 === 0, [1,2,3,4,5,6,7,8,9,10])),
    [2,4,6,8,10]
  );

  t.end();
});

test('take', function(t) {
  t.deepEqual(
    Array.from(i.take(10, [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15])),
    [1,2,3,4,5,6,7,8,9,10]
  );

  t.end();
});

test('takeWhile', function(t) {
  t.deepEqual(
    Array.from(i.takeWhile(n => n < 10, [1,2,3,4,5,6,7,8,9,10,11])),
    [1,2,3,4,5,6,7,8,9]
  );

  t.end();
});

test('dropWhile', function(t) {
  t.deepEqual(
    Array.from(i.dropWhile(n => n < 10, [1,2,3,4,5,6,7,8,9,10,11])),
    [10, 11]
  );

  t.end();
});

test('find', function(t) {
  t.equal(
    i.find(n => n > 10, [ 1,2,3,4,5,6,7,8,9,10,11 ]),
    11
  );

  t.end();
});

test('findLast', function(t) {
  t.equal(
    i.findLast(n => n > 10, [ 1,2,3,4,5,6,7,8,9,10,11,12 ]),
    12
  );

  t.end();
});

test('reduce', function(t) {
  t.equal(
    i.reduce((acc, n) => acc + n, 0, [1,2,3,4,5,6,7,8,9,10]),
    55
  );

  t.end();
});
