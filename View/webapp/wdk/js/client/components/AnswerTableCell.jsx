import React from 'react';
import RecordLink from './RecordLink';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let primaryKeyName = 'primary_key';

function AnswerTableCell(props) {
  if (props.value == null) {
    return null;
  }

  let { value, descriptor, record, recordClass } = props;

  if (descriptor.name === primaryKeyName) {
    return (
      <RecordLink
        recordId={record.id}
        recordClass={recordClass}
        className="wdk-AnswerTable-recordLink"
      >
        {renderAttributeValue(value, props)}
      </RecordLink>
    );
  }
  else {
    return renderAttributeValue(value, props);
  }
}

AnswerTableCell.propTypes = {
  // TODO Put reusable propTypes in a module
  value: React.PropTypes.string,
  descriptor: React.PropTypes.object.isRequired,
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired,
  width: React.PropTypes.number.isRequired
};

export default wrappable(AnswerTableCell);
