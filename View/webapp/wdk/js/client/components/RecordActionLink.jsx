import React from 'react';
import classnames from 'classnames';
import { wrappable } from '../utils/componentUtils';

let clickHandler = props => e => {
  e.preventDefault();
  props.onClick(e, props.record);
}

let RecordActionLink = props => {
  let className = 'wdk-RecordActionLink ' + props.className;
  return (
    <a href="#" title={props.label} className={className} onClick={clickHandler(props)}>
      {props.showLabel ? props.label : ''} <i className={props.iconClassName}/>
    </a>
  );
}

RecordActionLink.propTypes = {
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired,
  className: React.PropTypes.string,
  iconClassName: React.PropTypes.string,
  onClick: React.PropTypes.func,
  label: React.PropTypes.string,
  showLabel: React.PropTypes.bool
}

RecordActionLink.defaultProps = {
  onClick: (e, record) => console.log('Record action clicked', e, record),
  className: '',
  label: 'Record action',
  iconClassName: 'fa fa-bolt',
  showLabel: true
}

export default wrappable(RecordActionLink);
