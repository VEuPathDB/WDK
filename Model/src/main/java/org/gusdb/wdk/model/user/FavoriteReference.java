package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.record.attribute.AttributeField;

/**
 * An object representation to the {@code <recordClass>/<favorite>} tag, it's
 * used to associate the note field of a {@link Favorite} in the favorite page to an
 * {@link AttributeField} in the {@link RecordClass}.
 * 
 * @author jerric
 * 
 */
public class FavoriteReference extends WdkModelBase {

  private String noteField;

  /**
   * @return the noteField
   */
  public String getNoteField() {
    return noteField;
  }

  /**
   * @param noteField
   *          the noteField to set
   */
  public void setNoteField(String noteField) {
    this.noteField = noteField;
  }
}
