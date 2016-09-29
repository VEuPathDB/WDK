/**
 * A Task is a container for an operation.
 *
 * The result of the operation can be accessed using operators such as map. The
 * Task can be executed using the run method.
 */

type Operation<T, E> = (fulfill: (t: T) => void, reject: (e: E) => void) => () => void;

type RunHandler<T, E> = {
  onFulfill?: (t: T) => void;
  onRejected?: (e: E) => void;
};

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

  run(handler: RunHandler<T, E>) {
    return this._operation(handler.onFulfill, handler.onRejected);
  }

  map<U>(func: (t: T) => U) {
    return new Task<U, E>((fulfill, reject) => {
      return this.run({
        onFulfill: (value: T) => { fulfill(func(value)) },
        onRejected: reject
      });
    });
  }

  mapRejected<F>(func: (e: E) => F) {
    return new Task<T, F>((fulfill, reject) => {
      return this.run({
        onFulfill: fulfill,
        onRejected: (e: E) => { reject(func(e)) }
      })
    })
  }

  chain<U>(func: (t: T) => Task<U, E>) {
    return new Task<U, E>((fulfill, reject) => {
      let innerCancel = () => {};
      let outerCancel = this.run({
        onFulfill: (value: T) => {
          innerCancel = func(value).run({
            onFulfill: fulfill,
            onRejected: reject
          })
        },
        onRejected: reject
      });
      return () => { outerCancel(); innerCancel(); };
    });
  }

  chainRejected<U, F>(func: (e: E) => Task<U, F>) {
    return new Task<T|U, F>((fulfill, reject) => {
      let innerCancel = () => {};
      let outerCancel = this.run({
        onFulfill: fulfill,
        onRejected: (error: E) => {
          innerCancel = func(error).run({
            onFulfill: fulfill,
            onRejected: reject
          })
        }
      });
      return () => { outerCancel(); innerCancel();}
    })
  }

}
