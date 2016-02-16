/**
 * Provides a simple wrapper around <input type="checkbox"/>.  Rather than using
 * the 'checked' property however, this component takes a 'value' property which
 * must be a boolean.  If true, the box is checked, else it is not.  The
 * onChange function passed to this component is passed the new value of the
 * checkbox (typically !previousValue), rather than a click event.
 */

import { wrappable } from '../utils/componentUtils';

let Checkbox = function(props) {
  let onChange = function(event) {
    props.onChange(!props.value);
  };
  return ( <input type="checkbox" {...props} checked={props.value} onChange={onChange}/> );
}

export default wrappable(Checkbox);
