import { Component, PropTypes } from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordMainSection from './RecordMainSection';
import Sticky from './Sticky';


class Record extends Component {

  render() {
    return (
      <div className="wdk-Record">
        <RecordMainSection {...this.props} categories={this.props.categoryTree.children} />
      </div>
    );
  }
}

export default wrappable(Record);
