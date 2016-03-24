import React from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordActionLink from './RecordActionLink';
import RecordOverview from './RecordOverview';

let stubHandler = actionName => event => {
  event.preventDefault();
  alert('You clicked ' + actionName);
};

const loadingClassName = 'fa fa-circle-o-notch fa-spin';

let RecordHeading = props => {
  let { record, recordClass, basketEntry, user, updateBasketStatus, router } = props;
  let isInBasket = basketEntry && basketEntry.isInBasket;
  let isInFavorites = false;
  let favoriteStatusLoading = false;
  let actions = [
    {
      label: user.isGuest ? 'Login to manage basket'
           : isInBasket ? 'Remove from basket'
           : 'Add to basket',
      iconClassName: basketEntry && basketEntry.isLoading ? loadingClassName : 'fa fa-shopping-basket',
      onClick(event) {
        event.preventDefault();
        if (!user.isGuest) {
          updateBasketStatus(!isInBasket)
        }
      }
    },
    {
      label: user.isGuest ? 'Login to manage favorites'
           : isInFavorites ? 'Remove from favorite'
           : 'Add to favorite',
      iconClassName: favoriteStatusLoading ? loadingClassName : 'fa fa-lg fa-star',
      onClick(event) {
        event.preventDefault();
        if (!user.isGuest) {
          userActions.updateFavoriteStatus(record, !isInFavorites)
        }
      }
    },
    {
      label: 'Download ' + recordClass.displayName,
      iconClassName: 'fa fa-lg fa-download',
      onClick: () => {
        router.push('/record/' + recordClass.urlSegment + '/download/' +
                    record.id.map(pk => pk.value).join('/'));
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
      <h1 className="wdk-RecordHeading">{recordClass.displayName} {record.displayName}</h1>
      <RecordOverview record={record} recordClass={recordClass}/>
    </div>
  );
}

RecordHeading.propTypes = {
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired
}

export default wrappable(RecordHeading);
