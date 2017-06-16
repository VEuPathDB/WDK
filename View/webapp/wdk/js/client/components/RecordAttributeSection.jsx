import PropTypes from 'prop-types';
import { wrappable } from '../utils/componentUtils';
import RecordAttribute from './RecordAttribute';

/** Record attribute section container for record page */
function RecordAttributeSection(props) {
  let { attribute, record, recordClass } = props;
  let { name } = attribute;
  let value = record.attributes[name]
  if (value == null) return null;
  return (
    <div id={name}
      className={`wdk-RecordAttributeSectionItem wdk-RecordAttributeSectionItem__${name}`}>
      <div className="wdk-RecordAttributeName">
        <strong>{attribute.displayName}</strong>
      </div>
      <div className="wdk-RecordAttributeValue">
        <RecordAttribute
          attribute={attribute}
          record={record}
          recordClass={recordClass}
        />
      </div>
    </div>
  );
}

RecordAttributeSection.propTypes = {
  attribute: PropTypes.object.isRequired,
  record: PropTypes.object.isRequired,
  recordClass: PropTypes.object.isRequired,
  isCollapsed: PropTypes.bool.isRequired,
  onCollapsedChange: PropTypes.func.isRequired
};

export default wrappable(RecordAttributeSection);
