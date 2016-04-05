import React from 'react';
import {Link} from 'react-router';
import {wrappable} from '../utils/componentUtils';

let isExternal = url => /^https?:\/\//.test(url);

let RecordActionLink = props => {
  let className = 'wdk-RecordActionLink ' + props.className;
  let LinkComponent = props.external ? 'a' : Link;
  let linkProps = {
    [props.external ? 'href' : 'to']: props.href,
    title: props.label,
    className: className,
    onClick: props.onClick
  };
  return (
    <LinkComponent {...linkProps}>
      {props.showLabel ? props.label : ''} <i className={props.iconClassName}/>
    </LinkComponent>
  );
}

RecordActionLink.propTypes = {
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired,
  className: React.PropTypes.string,
  iconClassName: React.PropTypes.string,
  onClick: React.PropTypes.func,
  href: React.PropTypes.string,
  label: React.PropTypes.string,
  showLabel: React.PropTypes.bool
}

RecordActionLink.defaultProps = {
  href: '#',
  external: false,
  className: '',
  label: 'Record action',
  iconClassName: 'fa fa-bolt',
  showLabel: true
}

export default wrappable(RecordActionLink);
