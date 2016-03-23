import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import RecordMainCategorySection from './RecordMainCategorySection';
import { wrappable } from '../utils/componentUtils';
import { getId, getLabel } from '../utils/CategoryUtils';

let RecordMainSection = React.createClass({

  mixins: [ PureRenderMixin ],

  getDefaultProps() {
    return {
      depth: 1
    };
  },

  render() {
    let {
      depth,
      record,
      categories,
      collapsedSections,
      recordActions,
      parentEnumeration
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map((category, index) => {
          let categoryName = getLabel(category);
          let categoryId = getId(category);
          let enumeration = parentEnumeration == null
            ? index + 1
            : parentEnumeration + '.' + (index + 1);

          return (
            <RecordMainCategorySection
              key={categoryName}
              depth={depth}
              category={category}
              record={record}
              recordClass={this.props.recordClass}
              isCollapsed={collapsedSections.includes(categoryId)}
              onSectionToggle={this.props.onSectionToggle}
              onTableToggle={this.props.onTableToggle}
              enumeration={enumeration}
            >
            <RecordMainSection
              {...this.props}
              depth={depth + 1}
              categories={category.children}
              parentEnumeration={enumeration}
            />
            </RecordMainCategorySection>
            );
        })}
      </div>
    );
  }

});

export default wrappable(RecordMainSection);
