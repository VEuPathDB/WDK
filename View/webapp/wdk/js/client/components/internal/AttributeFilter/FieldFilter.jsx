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
    distinctKnownCount: props.distinctKnownCount,
    filteredDistinctKnownCount: props.filteredDistinctKnownCount,
    displayName: props.displayName,
    field: props.field,
    distribution: props.distribution,
    filter: props.filter,
    fieldState: props.fieldState,
    onChange: props.onChange,
    onSort: props.onMemberSort,
    onRangeScaleChange: props.onRangeScaleChange,
    selectByDefault: props.selectByDefault
  };
  let className = 'field-detail';
  if (props.useFullWidth) className += ' ' + className + '__fullWidth';
  if (props.addTopPadding) className += ' ' + className + '__topPadding';

  return (
    <div className={className}>
      {!props.field ? (
        <EmptyField displayName={props.displayName}/>
      ) : (
        <div>
          <h3>
            {props.field.display + ' '}
          </h3>
          {props.field.description && (
            <div className="field-description">{props.field.description}</div>
          )}
          {(!props.distribution || !props.dataCount) ? (
            <Loading />
          ) : (
            <FieldDetail {...fieldDetailProps} />
          )}
        </div>
      )}
    </div>
  );
}

FieldFilter.propTypes = {
  displayName: PropTypes.string,
  dataCount: PropTypes.number,
  filteredDataCount: PropTypes.number,
  distinctKnownCount: PropTypes.number,
  filteredDistinctKnownCount: PropTypes.number,
  field: PropTypes.object,
  fieldState: PropTypes.object,
  filter: PropTypes.object,
  distribution: PropTypes.array,
  onChange: PropTypes.func,
  onMemberSort: PropTypes.func,
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
