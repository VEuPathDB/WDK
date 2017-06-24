import test from 'tape';
import * as i from '../../webapp/wdk/js/client/utils/IterableUtils';

/**
 * generate list of natural numbers
 */
function* nat(max = 10000) {
  let n = 1;
  while(n <= max) {
    yield n++;
  }
}

test('Seq', t => {
  let s = i.Seq.from([1,2,3]);

  t.deepEqual(s.map(n => n * n).toArray(), [ 1, 4, 9 ]);

  let mapCallCount = 0;
  let result = i.Seq.from(nat())
    .map(n => (mapCallCount++, n * n))
    .takeWhile(n => n < 30)
    .reduce((sum, n) => sum + n)

  t.equal(mapCallCount, 6);
  t.equal(result, 55);


  let s2 = i.Seq.from(s).map(n => n * 2);
  t.deepEqual(s2.toArray(), [ 2, 4, 6 ], 'Seqs can be composed');

  /*
  function* gen() {
    yield 1;
    yield 2;
    yield 3;
  }

  let seqOfGen = i.seq(gen());
  t.equal(
    seqOfGen.first(),
    seqOfGen.first(),
    'seq should be reusable'
  );
  */

  t.end();
});

test('concat', function(t) {
  t.deepEqual(
    [...i.concat([1, 2, 3], [4, 5, 6])],
    [1, 2, 3, 4, 5, 6],
    'concat should append elements'
  );

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

test('flatMap', function(t) {
  t.deepEqual(
    Array.from(i.flatMap(c => c.name, [ { name: 'ABC' }, { name: 'DEF' }, { name: 'GHI' } ])),
    [ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' ],
    'flatMap should apply the transform to each item in an iterable and flatten the resulting iterables'
  );

  t.end();
});

test('uniq', function(t) {
  t.deepEqual(
    Array.from(i.uniq([ 1, 2, 3, 2, 1, 4, 5, 6 ])),
    [ 1, 2, 3, 4, 5, 6 ],
    'uniq should apply the transform to each item in an iterable'
  );

  t.end();
});

test('uniqBy', function(t) {
  const source = [ { a: 1}, { a: 1} ];
  t.deepEqual(
    Array.from(i.uniqBy(n => n.a, source)),
    [ { a: 1} ]
  );
  t.notDeepEqual(
    Array.from(i.uniqBy(n => n, source)),
    [ { a: 1} ]
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

test('takeLast', function(t) {
  t.deepEqual(
    Array.from(i.takeLast(10, [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15])),
    [6,7,8,9,10,11,12,13,14,15]
  );

  t.deepEqual(
    Array.from(i.takeLast(-10, [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15])),
    [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]
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

test('drop', function(t) {
  t.deepEqual(
    [ ...i.drop(1, [1,2,3,4,5,6,7,8,9,10,11]) ],
    [2,3,4,5,6,7,8,9,10, 11]
  );

  t.end();
});

test('dropLast', function(t) {
  t.deepEqual(
    [ ...i.dropLast(1, [1,2,3,4,5,6,7,8,9,10,11]) ],
    [1,2,3,4,5,6,7,8,9,10]
  );

  t.deepEqual(
    [ ...i.dropLast(-1, [1,2,3,4,5,6,7,8,9,10,11]) ],
    []
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

test('first', function(t) {
  t.equal(
    i.first([ 1,2,3,4,5,6,7,8,9,10,11]),
    1
  );
  t.end();
});

test('last', function(t) {
  t.equal(
    i.last([1,2,3,4,5,6,7,8,9,10]),
    10
  );
  t.end();
});

test('some', function(t) {
  t.equal(
    i.some(n => n > 1, [0, 1, 2]),
    true,
    'some should return true if any members pass the supplied test'
  );
  t.equal(
    i.some(n => n > 2, [0, 1, 2]),
    false,
    'some should return false if no member passes the supplied test'
  );
  t.end();
});

test('every', function(t) {
  t.equal(
    i.every(n => n >= 0, [0, 1, 2]),
    true,
    'some should return true if any members pass the supplied test'
  );
  t.equal(
    i.every(n => n < 2, [0, 1, 2]),
    false,
    'some should return false if no member passes the supplied test'
  );
  t.end();
});

test('reduce', function(t) {
  t.equal(
    i.reduce((acc, n) => acc + n, 5, [1,2,3,4,5,6,7,8,9,10]),
    60,
    'reduce should use seed value when provided'
  );

  t.equal(
    i.reduce((acc, n) => acc + n, [1,2,3,4,5,6,7,8,9,10]),
    55,
    'reduce should use first item if iterable when seed value is not provided'
  );

  t.end();

});
