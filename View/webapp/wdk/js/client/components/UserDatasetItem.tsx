import React from 'react';
import { wrappable } from '../utils/componentUtils';
import { UserDataset } from '../utils/WdkModel';

type Props = {
  userDataset: UserDataset
};

const UserDatasetItem = (props: Props) =>
  <div>
    <h1>{props.userDataset.meta.name}</h1>
    {/* TODO For development - remove before release */}
    <div style={{ marginTop: '1em' }}>
      <pre>{JSON.stringify(props.userDataset, null, 4)}</pre>
    </div>
  </div>

export default wrappable(UserDatasetItem);