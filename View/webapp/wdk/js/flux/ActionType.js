/**
 * Actions used in Flux architecture.
 *
 * An Action is an object encoding some change that the application should
 * respond to. Currently, the application Dispatcher provides a method to
 * dispatch an action. When this method is called, the Dispatcher will invoke
 * all registered callback functions with the action.
 *
 * Each Action below is a factory that accepts an object, returning an instance
 * of an Immutable.js Record. The objects being passed to Action below are
 * template objects. The values of the properties are treated as defaults. When
 * the created factory is then called with an object, only the properties
 * defined in the template object will be assigned to the object returned by the
 * factory.
 *
 *
 * Example:
 *
 *    // Create a new Action factory
 *    const MyAction = Action({
 *      a: undefined
 *    });
 *
 *    // Create a new action using the factory we just created
 *    const myAction = MyAction({
 *      a: 1,
 *      b: 2
 *    });
 *
 *    // Read values from myAction
 *    myAction.a //=> 1
 *    myAction.b //=> undefined
 *
 * See http://facebook.github.io/immutable-js/docs/#/Record.
 */

import Action from './utils/Action';

export const AppLoading = Action({
  isLoading: false
});

export const AppError = Action({
  requestData: {},
  error: null
});

export const AnswerLoading = Action({
  isLoading: true
});

export const AnswerAdded = Action({
  requestData: {},
  answer: {}
});

export const AnswerMoveColumn = Action({
  columnName: '',
  newPosition: -1
});

export const AnswerChangeAttributes = Action({
  attributes: []
});

export const AnswerUpdateFilter = Action({
  questionName: null,
  terms: '',
  attributes: null,
  tables: null
});

export const QuestionsAdded = Action({
  questions: null
});

export const RecordClassesAdded = Action({
  recordClasses: null
});

export let RecordDetailsReceived = Action({
  meta: undefined,
  record: undefined
});
