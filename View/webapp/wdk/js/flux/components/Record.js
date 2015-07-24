import React from 'react';
import Sticky from './Sticky';
import RecordMainSection from './RecordMainSection';
import RecordHeading from './RecordHeading';
import RecordNavigationSection from './RecordNavigationSection';
import wrappable from '../utils/wrappable';
import {
  formatAttributeValue
} from '../utils/stringUtils';


let Record = React.createClass({

  propTypes: {
    meta: React.PropTypes.object.isRequired,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.object.isRequired,
    questions: React.PropTypes.array.isRequired,
    recordClasses: React.PropTypes.array.isRequired,
    recordActions: React.PropTypes.object.isRequired,
    hiddenCategories: React.PropTypes.array.isRequired
  },

  handleVisibleChange({ category, isVisible }) {
    let { recordClass } = this.props;
    this.props.recordActions.toggleCategory({
      recordClass,
      category,
      isVisible
    });
  },

  render() {
    return (
      <div className="wdk-Record">
        <RecordHeading {...this.props}/>
        <Sticky className="wdk-Record-sidebar">
          <RecordNavigationSection {...this.props} onVisibleChange={this.handleVisibleChange} />
        </Sticky>
        <RecordMainSection {...this.props} />
      </div>
    );
  }
});

export default wrappable(Record);
