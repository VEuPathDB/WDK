// Type definitions
interface Mapper<T, U> {
  (x: T): U;
}
interface FlatMapper<T, U> {
  (x: T): Iterable<U>;
}
interface Predicate<T> {
  (x: T): boolean;
}
interface Reducer<T, U> {
  (acc: U | T, x: T): U;
}
interface Collector<T, U> {
  from: (i: Iterable<T>) => U;
};

/**
 * Useful operators for Iterables.
 *
 * The module exports the function `seq` which creates a wrapper that exposes
 * a fluent interface for traversing and manipulating the underlying iterable
 * object using operators.
 *
 * The module also exports each operator as a function for use in isolation.
 *
 * All methods and functions return a new Iterable object. Iteration is lazy
 * and will only execute when a value is requested (toArray, reduce, etc).
 * The iteration will terminate as early as possible.
 *
 * For example, the following `seq` code will only iterate 3 times:
 *
 *    let array = [];
 *    for (let i = 1; i <= 1000; i++) {
 *      array.push(i);
 *    }
 *
 *    // 3 iterations
 *    seq(array)
 *      .map(n => n * n)
 *      .filter(n => n % 2 === 1)
 *      .takeUntil(n => n > 30)
 *      .toArray() // [ 1, 9, 25 ]
 *
 */

// XXX The for..of loop construct is not being used because babel adds a
// try-catch to the loop body, which deoptimizes the code path. See
// https://github.com/google/traceur-compiler/issues/1773.

/**
 * Wraps `iterable` in an object with collection operations.
 *
 * @param {Iterable<T>} iterable
 * @return {Seq<T>}
 */
export function seq<T>(iterable: Iterable<T>) {
  return new Seq(iterable);
}

/**
 * Underlying class used by `seq`.
 */
class Seq<T> {

  /**
   * @param {Iterable<T>} iterable
   */
  constructor(private iterable: Iterable<T>) { }

  static from<T>(iterable: Iterable<T>) {
    return new Seq(iterable);
  }

  [Symbol.iterator]() {
    return this.iterable[Symbol.iterator]();
  }

  concat(thatIterable: Iterable<T>) {
    let thisIterable = this.iterable;
    return new Seq({
      *[Symbol.iterator]() {
        yield* thisIterable;
        yield* thatIterable;
      }
    });
  }

  map<U>(fn: Mapper<T, U>) {
    return new Seq(map(fn, this));
  }

  flatMap<U>(fn: FlatMapper<T, U>) {
    return new Seq(flatMap(fn, this));
  }

  uniq() {
    return new Seq(uniq(this));
  }

  filter(fn: Predicate<T>) {
    return new Seq(filter(fn, this));
  }

  take(n: number) {
    return new Seq(take(n, this));
  }

  takeWhile(fn: Predicate<T>) {
    return new Seq(takeWhile(fn, this));
  }

  drop(n: number) {
    return new Seq(drop(n, this));
  }

  dropWhile(fn: Predicate<T>) {
    return new Seq(dropWhile(fn, this));
  }

  find(fn: Predicate<T>) {
    return find(fn, this);
  }

  findLast(fn: Predicate<T>) {
    return findLast(fn, this);
  }

  every(fn: Predicate<T>) {
    return every(fn, this);
  }

  some(fn: Predicate<T>) {
    return some(fn, this);
  }

  reduce<U>(fn: Reducer<T, U>, value?: U) {
    return value == null ? reduce(fn, this)
    : reduce(fn, value, this);
  }

  toArray() {
    return this.reduce((arr: T[], item: T) => (arr.push(item), arr), []);
  }

  into<U>(Collector: Collector<T, U>) {
    return Collector.from(this);
  }

  first() {
    return first(this);
  }

  last() {
    return last(this);
  }

  rest() {
    return rest(this);
  }

  forEach(fn: (t:T) => void) {
    for (let iter = this[Symbol.iterator]();;) {
      let { done, value } = iter.next();
      if (done) break;
      fn(value);
    }
    return this;
  }

}

export function map<T, U>(fn: Mapper<T, U>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (done) break;
        yield fn(value);
      }
    }
  }
}

export function flatMap<T, U>(fn: FlatMapper<T, U>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (done) break;
        yield* fn(value);
      }
    }
  }
}

export function uniq<T>(iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      let values = new Set();
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (done) break;
        if (values.has(value) === false) {
          values.add(value);
          yield value;
        }
      }
    }
  }
}

export function filter<T>(fn: Predicate<T>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (done) break;
        if (fn(value)) yield value;
      }
    }
  }
}

export function take<T>(n: number, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      let count = 0;
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (!done && count++ < n) yield value;
        else break;
      }
    }
  }
}

/**
 * Keep items until test returns false.
 */
export function takeWhile<T>(fn: Predicate<T>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (done || fn(value) === false) break;
        yield value;
      }
    }
  }
}

export function drop<T>(n: number, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (done) break;
        if (n-- > 0) continue;
        yield value;
      }
    }
  }
}
/**
 * Ignore items until test returns false.
 */
export function dropWhile<T>(fn: Predicate<T>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      let take = false;
      for (let iter = iterable[Symbol.iterator]();;) {
        let { done, value } = iter.next();
        if (done) break;
        if (take === false) take = !fn(value);
        if (take === true) yield value;
      }
    }
  }
}


// Return values -- an item from iterable collection, or a reduction

/**
 * Find the first item that test returns true for.
 */
export function find<T>(test: Predicate<T>, iterable: Iterable<T>) {
  for (let iter = iterable[Symbol.iterator]();;) {
    let { done, value } = iter.next();
    if (done) break;
    if (test(value) === true) return value;
  }
  return undefined;
}

/**
 * Find the last item that the test returns true for.
 */
export function findLast<T>(test: Predicate<T>, iterable: Iterable<T>) {
  let last: T|void;
  for (let iter = iterable[Symbol.iterator]();;) {
    let { done, value } = iter.next();
    if (done) break;
    if (test(value)) last = value;
  }
  return last;
}

export function first<T>(iterable: Iterable<T>) {
  return iterable[Symbol.iterator]().next().value;
}

export function last<T>(iterable: Iterable<T>) {
  let last: T|void;
  for (let iter = iterable[Symbol.iterator]();;) {
    let { done, value } = iter.next();
    if (done) break;
    last = value;
  }
  return last;
}

export function rest<T>(iterable: Iterable<T>) {
  return drop(1, iterable);
}

export function every<T>(test: Predicate<T>, iterable: Iterable<T>): boolean {
  for (let iter = iterable[Symbol.iterator]();;) {
    let { done, value } = iter.next();
    if (done) break;
    if (test(value) === false) return false;
  }
  return true;
}

export function some<T>(test: Predicate<T>, iterable: Iterable<T>): boolean {
  for (let iter = iterable[Symbol.iterator]();;) {
    let { done, value } = iter.next();
    if (done) break;
    if (test(value) === true) return true;
  }
  return false;
}

/**
 * Reduce collection to a single value.
 */
export function reduce<T, U>(fn: Reducer<T, U>, iterable: Iterable<T>): U;
export function reduce<T, U>(fn: Reducer<T, U>, value: U, iterable: Iterable<T>): U;
export function reduce<T, U>(fn: any, value: any, iterable?: any) {
  let result: U|T;
  if (arguments.length === 2) {
    // No seed value, so we get the first value from iterable as the initial
    // value and get the rest of the iterable for the rest of the reduce
    // operation.
    iterable = rest(<Iterable<T>>value);
    result = first(<Iterable<T>>value);
  }
  else {
    result = <U>value;
  }
  for (let iter = iterable[Symbol.iterator]();;) {
    let { done, value } = iter.next();
    if (done) break;
    result = fn(result, value);
  }
  return result;
}
