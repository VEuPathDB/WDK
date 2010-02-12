package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;

public class Favorite {

    private User user;
    private RecordClass recordClass;
    private List<RecordInstance> recordInstances;

    public Favorite(User user) {
        this.user = user;
        recordInstances = new ArrayList<RecordInstance>();
    }

    public User getUser() {
        return user;
    }

    public RecordClass getRecordClass() {
        return recordClass;
    }

    void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    public RecordInstance[] getRecordInstances() {
        RecordInstance[] array = new RecordInstance[recordInstances.size()];
        recordInstances.toArray(array);
        return array;
    }
    
    public int getCount() {
        return recordInstances.size();
    }

    void addRecordInstance(RecordInstance recordInstance)
            throws WdkModelException {
        // make sure the record instance is of the recordClass type
        String rcName = recordInstance.getRecordClass().getFullName();
        if (!recordClass.getFullName().equals(rcName))
            throw new WdkModelException("The type of the record instance ("
                    + rcName + ") doesn't match the type of the favorite: '"
                    + recordClass.getFullName() + "'");
        recordInstances.add(recordInstance);
    }
}
