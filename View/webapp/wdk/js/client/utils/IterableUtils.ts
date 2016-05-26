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
interface Collector<T> {
  from: (i: Iterable<T>) => Collector<T>;
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

  [Symbol.iterator]() {
    return this.iterable[Symbol.iterator]();
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

  reduce<U>(fn: Reducer<T, U>, value?: U) {
    return value == null ? reduce(fn, this)
    : reduce(fn, value, this);
  }

  toArray() {
    return this.reduce((arr: T[], item: T) => (arr.push(item), arr), []);
  }

  into(Collector: Collector<T>) {
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
    for (let x of this) {
      fn(x);
    }
    return this;
  }

}

export function map<T, U>(fn: Mapper<T, U>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        yield fn(x);
      }
    }
  }
}

export function flatMap<T, U>(fn: FlatMapper<T, U>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        yield* fn(x);
      }
    }
  }
}

export function uniq<T>(iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      let values = new Set();
      for (let x of iterable) {
        if (values.has(x) === false) {
          values.add(x);
          yield x;
        }
      }
    }
  }
}

export function filter<T>(fn: Predicate<T>, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        if (fn(x)) yield x;
      }
    }
  }
}

export function take<T>(n: number, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      let count = 0;
      for (let x of iterable) {
        if (count++ < n) yield x;
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
      for (let x of iterable) {
        if (fn(x) === false) break;
        yield x;
      }
    }
  }
}

export function drop<T>(n: number, iterable: Iterable<T>) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        if (n-- > 0) continue;
        yield x;
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
      for (let item of iterable) {
        if (take === false) take = !fn(item);
        if (take === true) yield item;
      }
    }
  }
}


// Return values -- an item from iterable collection, or a reduction

/**
 * Find the first item that test returns true for.
 */
export function find<T>(test: Predicate<T>, iter: Iterable<T>) {
  for (let item of iter) {
    if (test(item) === true) return item;
  }
  return undefined;
}

/**
 * Find the last item that the test returns true for.
 */
export function findLast<T>(test: Predicate<T>, iter: Iterable<T>) {
  let last: T;
  for (let item of iter) {
    if (test(item)) last = item;
  }
  return last;
}

export function first<T>(iterable: Iterable<T>) {
  return iterable[Symbol.iterator]().next().value;
}

export function last<T>(iterable: Iterable<T>) {
  let last: T;
  for (last of iterable) { }
  return last;
}

export function rest<T>(iterable: Iterable<T>) {
  return drop(1, iterable);
}

/**
 * Reduce collection to a single value.
 */
export function reduce<T, U>(fn: Reducer<T, U>, iterable: Iterable<T>): U;
export function reduce<T, U>(fn: Reducer<T, U>, value: U, iterable: Iterable<T>): U;
export function reduce<T, U>(fn: Reducer<T, U>, value: U | Iterable<T>, iterable?: Iterable<T>) {
  let seed: U|T;
  if (arguments.length === 2) {
    iterable = rest(<Iterable<T>>value);
    seed = first(<Iterable<T>>value);
  }
  else {
    seed = <U>value;
  }
  for (let item of iterable) {
    seed = fn(seed, item);
  }
  return value;
}
