import PropTypes from 'prop-types';
import { wrappable } from '../utils/componentUtils';
import CollapsibleSection from './CollapsibleSection';
import RecordAttribute from './RecordAttribute';

/** Record attribute section container for record page */
function RecordAttributeSection(props) {
  let value = props.record.attributes[props.attribute.name];
  if (value == null) return null;
  if (value.length < 150) return (
    <InlineRecordAttributeSection {...props} />
  )
  else return (
    <BlockRecordAttributeSection {...props} />
  )
}

RecordAttributeSection.propTypes = {
  attribute: PropTypes.object.isRequired,
  record: PropTypes.object.isRequired,
  recordClass: PropTypes.object.isRequired,
  isCollapsed: PropTypes.bool.isRequired,
  onCollapsedChange: PropTypes.func.isRequired
};

export default wrappable(RecordAttributeSection);

/** Display attribute name and value on a single line */
function InlineRecordAttributeSection(props) {
  let { attribute, record, recordClass } = props;
  let { name } = attribute;
  return (
    <div id={name}
      className={`wdk-RecordAttributeSectionItem wdk-RecordAttributeSectionItem__${name}`}>
      <div className="wdk-RecordAttributeName">
        {attribute.displayName}
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

/** Display attribute name and value in a collapsible section */
function BlockRecordAttributeSection(props) {
  const { attribute, record, recordClass, isCollapsed, onCollapsedChange } = props;
  const { displayName, name } = attribute;
  return (
    <CollapsibleSection
      id={name}
      className={`wdk-RecordAttributeSectionItem`}
      headerContent={displayName}
      isCollapsed={isCollapsed}
      onCollapsedChange={onCollapsedChange}
    >
      <RecordAttribute
        attribute={attribute}
        record={record}
        recordClass={recordClass}
      />
    </CollapsibleSection>
  )
}