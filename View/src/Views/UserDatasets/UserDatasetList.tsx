import React, { Component } from 'react';
import { escape } from 'lodash';
import { History } from 'history';
import { wrappable } from 'Utils/ComponentUtils';
import { UserDataset } from 'Utils/WdkModel';
import { bytesToHuman } from 'Utils/Converters';
import DataTable from 'Components/DataTable/DataTable';
import Loading from 'Components/Loading/Loading';
import { User } from "Utils/WdkUser";

type Props = {
  user: User;
  userDatasets: UserDataset[];
  history: History;
};

const getRowId = (row: { id: number }) => row.id;

const na = '<span style="color: lightgray">N/A</span>';

const columns = [
  { name: 'id', displayName: 'ID' },
  { name: 'name', displayName: 'Name' },
  { name: 'summary', displayName: 'Summary' },
  { name: 'type', displayName: 'Type' },
  { name: 'installed', displayName: 'Installed' },
  { name: 'owner', displayName: 'Shared By' },
  { name: 'shared', displayName: 'Shared With Others' },
  { name: 'created', displayName: 'Created' },
  { name: 'modified', displayName: 'Modified' },
  { name: 'size', displayName: 'Size' },
  { name: 'quota', displayName: 'Quota Usage %' }
];

const mapUserDatasets = (userDatasets: UserDataset[], history: History, user: User) =>
  userDatasets.length === 0 ? userDatasets : userDatasets.map(ud => ({
    id: `<a href="${history.createHref({ pathname: `/workspace/datasets/${ud.id}` })}">${ud.id}</a>`,
    name: escape(ud.meta.name),
    summary: escape(ud.meta.summary),
    type: `${ud.type.name} ${ud.type.version}`,
    installed: ud.isInstalled ? 'Yes' : 'No',
    owner: user.id === ud.ownerUserId ? na : ud.owner,
    shared: user.id === ud.ownerUserId ? (ud.sharedWith && ud.sharedWith.length ? 'Yes' : 'No') : na,
    created: new Date(ud.created).toLocaleDateString(),
    modified: new Date(ud.modified).toLocaleDateString(),
    size: bytesToHuman(ud.size),
    quota: ud.percentQuotaUsed == null ? '' : `${ud.percentQuotaUsed}%`
  }));

class UserDatasetList extends Component <Props> {
  constructor (props: Props) {
    super(props);
  }

  render () {
    const { userDatasets, history, user } = this.props;
    return (
      <div>
        <DataTable
          columns={columns}
          getRowId={getRowId}
          data={mapUserDatasets(userDatasets, history, user)}
        />
      </div>
    );
  }
};

export default wrappable(UserDatasetList);
