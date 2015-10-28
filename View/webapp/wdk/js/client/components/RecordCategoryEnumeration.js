import {Component, PropTypes} from 'react';
import { wrappable } from '../utils/componentUtils';

class RecordCategoryEnumeration extends Component {
  render() {
    return (
      <span className="wdk-RecordCategoryEnumeration">
        {this.props.enumeration}
      </span>
    );
  }
}

RecordCategoryEnumeration.propTypes = {
  enumeration: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]).isRequired
};


export default wrappable(RecordCategoryEnumeration);
