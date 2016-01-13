import React from 'react';
import { Link } from 'react-router';
import { wrappable } from '../utils/componentUtils';

function RecordLink(props) {
  let params = {
    recordClass: props.recordClass.urlSegment,
    splat: props.record.id.map(p => p.value).join('/')
  };
  return (
    <Link to="record" params={params} {...props}>
      {props.children}
    </Link>
  );
}

RecordLink.propTypes = {
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired
};

export default wrappable(RecordLink);
