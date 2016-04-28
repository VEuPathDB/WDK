import { wrappable } from '../utils/componentUtils';

let PrimaryKeySpan = props => {
  return ( <span>{props.primaryKeyString}</span> );
};

export default wrappable(PrimaryKeySpan);
