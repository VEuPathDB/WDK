import { PropTypes } from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordTable from './RecordTable';
import CollapsibleSection from './CollapsibleSection';

/** Record table section on record page */
function RecordTableSection(props) {
  let { table, record, recordClass, isCollapsed, onCollapsedChange } = props;
  let { name, displayName } = table;
  let value = record.tables[name];
  let numVisibleAttrs = table.attributes.filter(attr => attr.isDisplayable).length;
  let className = [ 'wdk-RecordTable', 'wdk-RecordTable__' + table.name ].join(' ');

  if (value == null)
    return null

  if (value.length === 0 || numVisibleAttrs === 0)
    return <em>No data available</em>;

  return (
    <CollapsibleSection
      id={name}
      className="wdk-RecordTableContainer"
      headerContent={displayName}
      isCollapsed={isCollapsed}
      onCollapsedChange={onCollapsedChange}
    >
      {value == null ? <p>Loading...</p>
        : <RecordTable className={className} value={value} table={table} record={record} recordClass={recordClass}/>}
      </CollapsibleSection>
  );
}

RecordTableSection.propTypes = {
  table: PropTypes.object.isRequired,
  record: PropTypes.object.isRequired,
  recordClass: PropTypes.object.isRequired,
  isCollapsed: PropTypes.bool.isRequired,
  onCollapsedChange: PropTypes.func.isRequired
};

export default wrappable(RecordTableSection);
