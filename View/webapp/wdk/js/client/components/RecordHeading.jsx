import React from 'react';
import ReactRouter from 'react-router';
import { wrappable } from '../utils/componentUtils';
import RecordActionLink from './RecordActionLink';
import RecordOverview from './RecordOverview';

let stubHandler = actionName => event => {
  event.preventDefault();
  alert('You clicked ' + actionName);
};

const loadingClassName = 'fa fa-circle-o-notch fa-spin';

let RecordHeading = props => {
  let { record, recordClass, user, basket, userActions, router } = props;
  let actions = [
    {
      label: user.isGuest ? 'Login to manage basket'
           : basket.inBasket ? 'Remove from basket'
           : 'Add to basket',
      iconClassName: basket.isLoading ? loadingClassName : 'fa fa-shopping-basket',
      onClick(event) {
        event.preventDefault();
        if (!user.isGuest) {
          userActions.updateBasketStatus(record.recordClassName, record.id, !basket.inBasket)
        }
      }
    },
    {
      label: 'Add to favorites',
      iconClassName: 'fa fa-lg fa-star',
      onClick: stubHandler('favorites')
    },
    {
      label: 'Download ' + recordClass.displayName,
      iconClassName: 'fa fa-lg fa-download',
      onClick: () => {
        router.transitionTo(wdk.webappUrl('app') + '/record/' + recordClass.urlSegment +
            '/download/' + record.id.map(pk => pk.value).join('/'));
      }
    }
  ];
  return (
    <div>
      <ul className="wdk-RecordActions">
        {actions.map((action, index) => {
          return (
            <li key={index} className="wdk-RecordActionItem">
              <RecordActionLink {...props} {...action}/>
            </li>
          );
        })}
      </ul>
      <h1 className="wdk-RecordHeading">{record.displayName}</h1>
      <RecordOverview record={record} recordClass={recordClass}/>
    </div>
  );
}

RecordHeading.propTypes = {
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired
}

export default wrappable(RecordHeading);
