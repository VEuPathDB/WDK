/**
 * Provides a simple wrapper around <input type="text"/>.  The only difference
 * is that the value passed to the onChange property is the new value inside the
 * textbox, not the event causing the change.  This component can be easily
 * modified to render a password input by passing a type="password" property.
 */

import React from 'react';
import { wrappable } from 'Utils/ComponentUtils';

type Props = React.InputHTMLAttributes<HTMLInputElement> & {
  onChange: (value: string) => void;
}

let TextBox = function(props: Props) {
  let onChange = function(event: React.ChangeEvent<HTMLInputElement>) {
    props.onChange(event.target.value);
  };
  return ( <input type="text" {...props} onChange={onChange}/> );
}

export default wrappable(TextBox);
