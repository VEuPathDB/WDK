import { wrappable } from '../utils/componentUtils';

let Image = props => (
  <img {...props} src={wdk.assetsUrl(props.src)}/>
);

export default wrappable(Image);
