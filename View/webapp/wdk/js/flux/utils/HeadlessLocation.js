/**
 * HeadlessLocation is an implentation of a React Router Location class.
 * See https://github.com/rackt/react-router/blob/master/docs/api/misc/Location.md
 *
 * It is meant to be used when it is undesirable to reflect the state of the
 * Router in the URL bar; for instance, when embedding an application on an
 * existing web site.
 */

import LocationActions from 'react-router/modules/actions/LocationActions';

class HeadlessLocation {

  constructor(initialPath) {
    this._paths = [];
    this._changeListeners = [];
    this._actionType = null;

    if (initialPath) {
      this._paths.push(initialPath);
    }
  }

  handlePathChange() {
    var change = {
      path: this._paths[this._paths.length - 1],
      type: this._actionType || LocationActions.POP
    };

    this._changeListeners.forEach(function(changeListener) {
      changeListener(change);
    });

    this._actionType = null;
  }

  addChangeListener(listener) {
    this._changeListeners.push(listener);
  }

  removeChangeListener(listener) {
    this._changeListeners = this._changeListeners.filter(function(changeListener) {
      return changeListener !== listener;
    });
  }

  push(path) {
    this._actionType = LocationActions.PUSH;
    this._paths.push(path);
    this.handlePathChange();
  }

  replace(path) {
    this._actionType = LocationActions.REPLACE;
    this._paths[this._paths.length - 1] = path;
    this.handlePathChange();
  }

  pop() {
    this._actionType = LocationActions.POP;
    this._paths.pop();
    this.handlePathChange();
  }

  getCurrentPath() {
    return this._paths[this._paths.length - 1];
  }

  toString() {
    return '<HeadlessLocation>';
  }

}

export default HeadlessLocation;
