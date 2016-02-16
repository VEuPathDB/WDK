/**
 * Provides a simple wrapper around <input type="text"/>.  The only difference
 * is that the value passed to the onChange property is the new value inside the
 * textbox, not the event causing the change.  This component can be easily
 * modified to render a password input by passing a type="password" property.
 */

import { wrappable } from '../utils/componentUtils';

let TextBox = function(props) {
  let onChange = function(event) {
    props.onChange(event.target.value);
  };
  return ( <input type="text" {...props} onChange={onChange}/> );
}

export default wrappable(TextBox);
