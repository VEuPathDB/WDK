/**
 * Factory function that returns an Action factory. This function will set the
 * `type` of an action to it's factory function. This makes it possible to
 * write a switch statement on an action using it's constructor. The point of
 * this is so that we can define "types" for our actions without having to also
 * maintain constants used as identifiers.
 *
 * Example:
 *
 *    // Create a factory for MyAction, specifying that it has a property named
 *    // 'value'
 *    var MyAction = Action({
 *      value: undefined
 *    });
 *
 *    // Create an action whose property 'value' has the value 3.
 *    var myAction = MyAction({
 *      value: 3
 *    });
 *
 *    myAction.type === MyAction //=> true
 *
 *    switch(action.type) {
 *      case MyAction:
 *        // ...this is matched
 *      ...
 *    }
 *
 */
import { Record } from 'immutable';

export default function Action(...args) {
  const Action = Record(...args);
  Action.prototype.getType = function getType() {
    return Action;
  };
  return Action
}
