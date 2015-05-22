import { Dispatcher } from 'flux';
/**
 * See {@link http://facebook.github.io/flux/docs/dispatcher.html#content} for
 * full API.
 */

function createDispatcher() {
  return new Dispatcher();
}

export default {
  createDispatcher
};
