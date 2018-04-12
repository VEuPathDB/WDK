import React, { Component } from 'react';
import { wrappable } from 'Utils/ComponentUtils';
import { UserDatasetShare } from "Utils/WdkModel";

type Props = {
  shares: UserDatasetShare[];
};

const todo = () => alert('TODO')

const makeClassName = (element?: string, modifier?: string) =>
  'wdk-UserDataset' +
  (element ? `${element}` : ``) +
  (modifier ? `__${modifier}` : ``);

class UserDatasetSharing extends Component<Props> {
  render() {
    const { shares } = this.props;
    return (
      <div>
        <div className={makeClassName('AddShareContainer')}>
          <button onClick={todo} type="button" className={makeClassName('AddShare')}>Add Share</button>
        </div>
        <div className={makeClassName('AddShareNote')}>
          People you are sharing this data set with:
        </div>
        {shares.length && (
          <div>
            {shares.map(share =>
              <div key={share.user} className={makeClassName('ShareEntry')}>
                <span className={makeClassName('ShareEntryName')}>
                  {share.userDisplayName}
                </span>
                <span className={makeClassName('ShareEntryTime')}>
                  shared on {new Date(share.time).toLocaleDateString()}
                </span>
                <span className={makeClassName('ShareEntryRemove')}>
                  <button onClick={todo} type="button" className={makeClassName('RemoveShare')}>
                    Remove
                  </button>
                </span>
              </div>
            )}
          </div>
        )}
      </div>
    );
  }
}

export default wrappable(UserDatasetSharing);
