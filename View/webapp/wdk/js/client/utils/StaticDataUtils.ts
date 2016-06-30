import { isPlainObject } from 'lodash';
export let StaticDataProps = {
  CONFIG: "config",
  ONTOLOGY: "ontology",
  QUESTIONS: "questions",
  RECORDCLASSES: "recordClasses",
  USER: "user",
  PREFERENCES: "preferences"
};

/**
 * Adds an isBroadcast property to the passed action with value true.  Only
 * object types are supported.
 *
 * @param {Object} action to be broadcast
 */
export function broadcast(action: Object) {
  if (action === null || !isPlainObject(action)) {
    throw "Parameter 'action' is null or not an object; only objects are supported by this function.";
  }
  return Object.assign(action, { isBroadcast: true });
}
