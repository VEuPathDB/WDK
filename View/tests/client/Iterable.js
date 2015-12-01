import test from 'tape';
import * as i from '../../webapp/wdk/js/client/utils/Iterable';

let GeneratorFunction = Object.getPrototypeOf(function*(){}).constructor;

test('compose', function(t) {
  t.plan(2);

  let square = i.map(n => n * n);
  let halve = i.map(n => n / 2);
  let whole = i.filter(n => Math.floor(n) === n);

  let xf1 = i.compose(halve, square, square);
  t.deepEqual(
    Array.from(xf1([1, 2, 3, 4, 5])),
    [ .5, 8, 40.5, 128, 312.5 ],
    'compose should apply functions from right to left'
  );

  let xf2 = i.compose(whole, halve, square, square);
  t.deepEqual(
    Array.from(xf2([1,2,3,4,5])),
    [ 8, 128 ]
  );
});

test('map', function(t) {
  t.plan(2);

  let names = i.map(c => c.name);

  t.ok(
    names instanceof GeneratorFunction,
    'map should return a generator function'
  );

  t.deepEqual(
    Array.from(names([ { name: 'A' }, { name: 'B' }, { name: 'C' } ])),
    [ 'A', 'B', 'C' ],
    'map should apply the transform to each item in an iterable'
  );

});

test('filter', function(t) {
  t.plan(1);

  t.deepEqual(
    Array.from(i.filter(n => n % 2 === 0)([1,2,3,4,5,6,7,8,9,10])),
    [2,4,6,8,10]
  );
});

test('take', function(t) {
  t.plan(1);

  t.deepEqual(
    Array.from(i.take(10)([1,2,3,4,5,6,7,8,9,10,11,12,13,14,15])),
    [1,2,3,4,5,6,7,8,9,10]
  );
});

test('takeWhile', function(t) {
  t.plan(1);

  t.deepEqual(
    Array.from(i.takeWhile(n => n < 10)([1,2,3,4,5,6,7,8,9,10,11])),
    [1,2,3,4,5,6,7,8,9]
  );
});

test('dropWhile', function(t) {
  t.plan(1);

  t.deepEqual(
    Array.from(i.dropWhile(n => n < 10)([1,2,3,4,5,6,7,8,9,10,11])),
    [10, 11]
  );
});

test('find', function(t) {
  t.plan(1);

  t.equal(
    i.find(n => n > 10, [ 1,2,3,4,5,6,7,8,9,10,11 ]),
    11
  );
});

test('findLast', function(t) {
  t.plan(1);

  t.equal(
    i.findLast(n => n > 10, [ 1,2,3,4,5,6,7,8,9,10,11,12 ]),
    12
  );
});

test('reduce', function(t) {
  t.plan(1);

  t.equal(
    i.reduce((acc, n) => acc + n, 0, [1,2,3,4,5,6,7,8,9,10]),
    55
  );
});
