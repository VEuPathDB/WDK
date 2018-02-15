import React from 'react';
import PropTypes from 'prop-types';
import { isRange } from '../../../utils/FilterServiceUtils';
import Loading from '../../Loading';
import EmptyField from './EmptyField';
import MembershipField from './MembershipField';
import NumberField from './NumberField';
import DateField from './DateField';

/**
 * Main interactive filtering interface for a particular field.
 */
export default function FieldFilter(props) {
  let FieldDetail = getFieldDetailComponent(props.field);
  let fieldDetailProps = {
    dataCount: props.dataCount,
    filteredDataCount: props.filteredDataCount,
    fieldSummary: props.fieldSummary,
    displayName: props.displayName,
    field: props.field,
    filter: props.filter,
    fieldState: props.fieldState,
    onChange: props.onChange,
    onSort: props.onMemberSort,
    onSearch: props.onMemberSearch,
    onRangeScaleChange: props.onRangeScaleChange,
    selectByDefault: props.selectByDefault
  };
  let className = 'field-detail';
  if (props.useFullWidth) className += ' ' + className + '__fullWidth';
  if (props.addTopPadding) className += ' ' + className + '__topPadding';

  return (
    <div className={className}>
      {!fieldDetailProps.field ? (
        <EmptyField displayName={fieldDetailProps.displayName}/>
      ) : (
        <div>
          <h3>
            {fieldDetailProps.field.display + ' '}
          </h3>
          {fieldDetailProps.field.description && (
            <div className="field-description">{fieldDetailProps.field.description}</div>
          )}
          {fieldDetailProps.fieldState && fieldDetailProps.fieldState.errorMessage ? (
            <div style={{ color: 'darkred' }}>{props.fieldState.errorMessage}</div>
          ) : (fieldDetailProps.fieldSummary == null || fieldDetailProps.dataCount == null) ? (
            <Loading />
          ) : (
            <FieldDetail {...fieldDetailProps} />
          )}
        </div>
      )}
    </div>
  );
}

const FieldSummary = PropTypes.shape({
  valueCounts: PropTypes.array.isRequired,
  internalsCount: PropTypes.number.isRequired,
  internalsFilteredCount: PropTypes.number.isRequired
});

const MultiFieldSummary = PropTypes.arrayOf(PropTypes.shape({
  field: PropTypes.object.isRequired,
  filter: PropTypes.object.isRequired,
  summary: FieldSummary.isRequired
}));

FieldFilter.propTypes = {
  displayName: PropTypes.string,
  dataCount: PropTypes.number,
  filteredDataCount: PropTypes.number,
  filter: PropTypes.object,
  field: PropTypes.object,
  fieldState: PropTypes.object,
  fieldSummary: PropTypes.oneOfType([FieldSummary, MultiFieldSummary]),

  onChange: PropTypes.func,
  onMemberSort: PropTypes.func,
  onMemberSearch: PropTypes.func,
  onRangeScaleChange: PropTypes.func,
  useFullWidth: PropTypes.bool,

  addTopPadding: PropTypes.bool,
  selectByDefault: PropTypes.bool.isRequired
};

FieldFilter.defaultProps = {
  displayName: 'Items'
}

/**
 * Finds the component for a field.
 *
 * @param {Field} field
 */
function getFieldDetailComponent(field) {
  return field == null ? null
    : isRange(field) == false ? MembershipField
    : field.type == 'string' ? MembershipField
    : field.type == 'number' ? NumberField
    : field.type == 'date' ? DateField
    : null;
}
