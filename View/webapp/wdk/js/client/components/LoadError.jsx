import { wrappable } from '../utils/componentUtils';

let message = "We're sorry.  An error has occured and this page cannot be loaded.  Please try again later.";

let LoadError = props => (
  <span style={{color: 'red' }}>{message}</span>
);

export default wrappable(LoadError);
