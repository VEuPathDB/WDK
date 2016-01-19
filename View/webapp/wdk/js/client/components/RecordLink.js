import React from 'react';
import { Link } from 'react-router';
import { wrappable } from '../utils/componentUtils';

let idPartPropType = React.PropTypes.shape({
  name: React.PropTypes.string.isRequired,
  value: React.PropTypes.string.isRequired
});

function RecordLink(props) {
  let params = {
    recordClass: props.recordClass.urlSegment,
    splat: props.recordId.map(p => p.value).join('/')
  };
  return (
    <Link to="record" params={params} {...props}>
      {props.children}
    </Link>
  );
}

RecordLink.propTypes = {
  recordId: React.PropTypes.arrayOf(idPartPropType).isRequired,
  recordClass: React.PropTypes.object.isRequired
};

export default wrappable(RecordLink);
