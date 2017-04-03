import * as React from 'react';
import { wrappable } from '../utils/componentUtils';

const iconClassNames = {
  warning: 'fa fa-warning',
  info: 'fa fa-info',
  help: 'fa fa-question'
}

type Props = {
  type: keyof typeof iconClassNames;
  className?: string;
};

const Icon: React.StatelessComponent<Props> = ({ type, className }) =>
  <i className={makeClassName(type, className)}/>

export default wrappable(Icon);

function makeClassName(type: Props['type'], className = 'wdk-Icon') {
  return `${iconClassNames[type]} ${className} ${className}__${type}`;
}
