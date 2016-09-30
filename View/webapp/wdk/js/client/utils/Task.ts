import { noop } from 'lodash';

type Operation<T, E> = (fulfill: (t: T) => void, reject: (e: E) => void) => () => void;

/**
 * A Task is a container for an operation.
 *
 * The result of the operation can be accessed using operators such as map. The
 * Task can be executed using the run method.
 */
export class Task<T, E> {

  _operation: Operation<T, E>;

  static of<T>(t: T) {
    return new Task<T, void>(fulfill => void fulfill(t));
  }

  static reject<E>(e: E) {
    return new Task<void, E>((fulfill, reject) => void reject(e));
  }

  constructor(operation: Operation<T, E>) {
    this._operation = operation;
  }

  run(onFulfill: (t: T) => void = noop, onRejected: (e: E) => void = noop) {
    return this._operation(onFulfill, onRejected);
  }

  map<U>(func: (t: T) => U) {
    return new Task<U, E>((fulfill, reject) => {
      return this.run((value) => void fulfill(func(value)), reject);
    });
  }

  mapRejected<F>(func: (e: E) => F) {
    return new Task<T, F>((fulfill, reject) => {
      return this.run(fulfill, (e) => void reject(func(e)));
    })
  }

  chain<U>(func: (t: T) => Task<U, E>) {
    return new Task<U, E>((fulfill, reject) => {
      let innerCancel = () => {};
      let outerCancel = this.run(
        (value) => {
          innerCancel = func(value).run( fulfill, reject)
        },
        reject
      );
      return () => { outerCancel(); innerCancel(); };
    });
  }

  chainRejected<U, F>(func: (e: E) => Task<U, F>) {
    return new Task<T|U, F>((fulfill, reject) => {
      let innerCancel = () => {};
      let outerCancel = this.run(
        fulfill,
        (error) => {
          innerCancel = func(error).run(fulfill, reject)
        }
      );
      return () => { outerCancel(); innerCancel();}
    })
  }

}
