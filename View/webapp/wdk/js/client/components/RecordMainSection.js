import React from 'react';
import RecordMainCategorySection from './RecordMainCategorySection';
import { wrappable } from '../utils/componentUtils';

let RecordMainSection = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

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
      recordActions
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map(category => {
          let categoryName = category.name;
          let attributes = this.props.attributes.filter(attr => attr.category === categoryName);
          let tables = this.props.tables.filter(table => table.category === categoryName);

          return (
            <RecordMainCategorySection
              key={String(category.name)}
              depth={depth}
              category={category}
              record={record}
              attributes={attributes}
              tables={tables}
              isCollapsed={collapsedCategories.includes(category.name)}
              collapsedTables={collapsedTables}
              onCategoryToggle={this.props.onCategoryToggle}
              onTableToggle={this.props.onTableToggle}
            >
              <RecordMainSection {...this.props} depth={depth + 1} categories={category.subCategories}/>
            </RecordMainCategorySection>
            );
        })}
      </div>
    );
  }

});

export default wrappable(RecordMainSection);
