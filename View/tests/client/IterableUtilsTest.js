import test from 'ava';
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

  t.is(mapCallCount, 6);
  t.is(result, 55);


  let s2 = i.Seq.from(s).map(n => n * 2);
  t.deepEqual(s2.toArray(), [ 2, 4, 6 ], 'Seqs can be composed');

  /*
  function* gen() {
    yield 1;
    yield 2;
    yield 3;
  }

  let seqOfGen = i.seq(gen());
  t.is(
    seqOfGen.first(),
    seqOfGen.first(),
    'seq should be reusable'
  );
  */
});

test('concat', function(t) {
  t.deepEqual(
    [...i.concat([1, 2, 3], [4, 5, 6])],
    [1, 2, 3, 4, 5, 6],
    'concat should append elements'
  );

});

test('map', function(t) {
  t.deepEqual(
    Array.from(i.map(c => c.name, [ { name: 'A' }, { name: 'B' }, { name: 'C' } ])),
    [ 'A', 'B', 'C' ],
    'map should apply the transform to each item in an iterable'
  );

});

test('flatMap', function(t) {
  t.deepEqual(
    Array.from(i.flatMap(c => c.name, [ { name: 'ABC' }, { name: 'DEF' }, { name: 'GHI' } ])),
    [ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' ],
    'flatMap should apply the transform to each item in an iterable and flatten the resulting iterables'
  );

});

test('uniq', function(t) {
  t.deepEqual(
    Array.from(i.uniq([ 1, 2, 3, 2, 1, 4, 5, 6 ])),
    [ 1, 2, 3, 4, 5, 6 ],
    'uniq should apply the transform to each item in an iterable'
  );

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
});

test('filter', function(t) {
  t.deepEqual(
    Array.from(i.filter(n => n % 2 === 0, [1,2,3,4,5,6,7,8,9,10])),
    [2,4,6,8,10]
  );

});

test('take', function(t) {
  t.deepEqual(
    Array.from(i.take(10, [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15])),
    [1,2,3,4,5,6,7,8,9,10]
  );

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

});

test('takeWhile', function(t) {
  t.deepEqual(
    Array.from(i.takeWhile(n => n < 10, [1,2,3,4,5,6,7,8,9,10,11])),
    [1,2,3,4,5,6,7,8,9]
  );

});

test('drop', function(t) {
  t.deepEqual(
    [ ...i.drop(1, [1,2,3,4,5,6,7,8,9,10,11]) ],
    [2,3,4,5,6,7,8,9,10, 11]
  );

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

});

test('dropWhile', function(t) {
  t.deepEqual(
    Array.from(i.dropWhile(n => n < 10, [1,2,3,4,5,6,7,8,9,10,11])),
    [10, 11]
  );

});

test('find', function(t) {
  t.is(
    i.find(n => n > 10, [ 1,2,3,4,5,6,7,8,9,10,11 ]),
    11
  );

});

test('findLast', function(t) {
  t.is(
    i.findLast(n => n > 10, [ 1,2,3,4,5,6,7,8,9,10,11,12 ]),
    12
  );

});

test('first', function(t) {
  t.is(
    i.first([ 1,2,3,4,5,6,7,8,9,10,11]),
    1
  );
});

test('last', function(t) {
  t.is(
    i.last([1,2,3,4,5,6,7,8,9,10]),
    10
  );
});

test('some', function(t) {
  t.is(
    i.some(n => n > 1, [0, 1, 2]),
    true,
    'some should return true if any members pass the supplied test'
  );
  t.is(
    i.some(n => n > 2, [0, 1, 2]),
    false,
    'some should return false if no member passes the supplied test'
  );
});

test('every', function(t) {
  t.is(
    i.every(n => n >= 0, [0, 1, 2]),
    true,
    'some should return true if any members pass the supplied test'
  );
  t.is(
    i.every(n => n < 2, [0, 1, 2]),
    false,
    'some should return false if no member passes the supplied test'
  );
});

test('reduce', function(t) {
  t.is(
    i.reduce((acc, n) => acc + n, 5, [1,2,3,4,5,6,7,8,9,10]),
    60,
    'reduce should use seed value when provided'
  );

  t.is(
    i.reduce((acc, n) => acc + n, [1,2,3,4,5,6,7,8,9,10]),
    55,
    'reduce should use first item if iterable when seed value is not provided'
  );


});
