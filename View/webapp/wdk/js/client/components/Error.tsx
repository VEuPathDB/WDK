import * as React from 'react';
import { wrappable } from '../utils/componentUtils';

const Error: React.StatelessComponent<void> = (props) =>
  <div className="wdk-Error">
    <h1>Oops...</h1>
    {props.children || <p>Something went wrong.</p>}
  </div>

export default wrappable(Error);
