import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import RecordMainCategorySection from './RecordMainCategorySection';
import { wrappable } from '../utils/componentUtils';

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
      collapsedCategories,
      collapsedTables,
      recordActions,
      parentEnumeration
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map((category, index) => {
          let categoryName = category.name;
          let attributes = this.props.attributes.filter(attr => attr.category == categoryName);
          let tables = this.props.tables.filter(table => table.category == categoryName);
          let enumeration = parentEnumeration == null
            ? index + 1
            : parentEnumeration + '.' + (index + 1);

          return (
            <RecordMainCategorySection
              key={String(category.name)}
              depth={depth}
              category={category}
              record={record}
              recordClass={this.props.recordClass}
              attributes={attributes}
              tables={tables}
              isCollapsed={collapsedCategories.includes(category.name)}
              collapsedTables={collapsedTables}
              onCategoryToggle={this.props.onCategoryToggle}
              onTableToggle={this.props.onTableToggle}
              enumeration={enumeration}
            >
            <RecordMainSection
              {...this.props}
              depth={depth + 1}
              categories={category.categories}
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
