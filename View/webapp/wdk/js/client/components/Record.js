import React from 'react';
import Sticky from './Sticky';
import RecordMainSection from './RecordMainSection';
import RecordHeading from './RecordHeading';
import RecordNavigationSection from './RecordNavigationSection';
import { wrappable } from '../utils/componentUtils';
import {
  formatAttributeValue
} from '../utils/stringUtils';


let Record = React.createClass({

  propTypes: {
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.object.isRequired,
    questions: React.PropTypes.array.isRequired,
    recordClasses: React.PropTypes.array.isRequired,
    recordActions: React.PropTypes.object.isRequired,
    hiddenCategories: React.PropTypes.array.isRequired,
    collapsedCategories: React.PropTypes.array.isRequired
  },

  handleVisibleChange({ category, isVisible }) {
    let { recordClass } = this.props;
    this.props.recordActions.toggleCategoryVisibility({
      recordClass,
      category,
      isVisible
    });
  },

  handleCollapsedChange({ category, isCollapsed }) {
    let { recordClass } = this.props;
    this.props.recordActions.toggleCategoryCollapsed({
      recordClass,
      category,
      isCollapsed
    });
  },

  render() {
    let { recordClass } = this.props;
    return (
      <div className="wdk-Record">
        <Sticky className="wdk-Record-sidebar">
          <div>
            <RecordNavigationSection
              {...this.props}
              categories={recordClass.attributeCategories}
              onVisibleChange={this.handleVisibleChange}
            />
            <p style={{ padding: '0 .6em' }}><a href="#top">Back to top</a></p>
          </div>
        </Sticky>
        <div className="wdk-Record-main">
          <RecordHeading {...this.props}/>
          <RecordMainSection
            {...this.props}
            categories={recordClass.attributeCategories}
          />
        </div>
      </div>
    );
  }
});

export default wrappable(Record);
