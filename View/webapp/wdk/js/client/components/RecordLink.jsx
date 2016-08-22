import React from 'react';
import Link from './Link';
import { wrappable } from '../utils/componentUtils';

let idPartPropType = React.PropTypes.shape({
  name: React.PropTypes.string.isRequired,
  value: React.PropTypes.string.isRequired
});

function RecordLink(props) {
  let { recordClass, recordId } = props;
  let pkValues = recordId.map(p => p.value).join('/');

  return (
    <Link to={`/record/${recordClass.urlSegment}/${pkValues}`}>
      {props.children}
    </Link>
  );
}

RecordLink.propTypes = {
  recordId: React.PropTypes.arrayOf(idPartPropType).isRequired,
  recordClass: React.PropTypes.object.isRequired
};

export default wrappable(RecordLink);
