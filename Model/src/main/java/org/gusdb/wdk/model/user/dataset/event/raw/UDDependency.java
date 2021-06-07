package org.gusdb.wdk.model.user.dataset.event.raw;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UDDependency
{
  private static final String KeyDisplayName = "resourceDisplayName";
  private static final String KeyIdentifier  = "resourceIdentifier";
  private static final String KeyVersion     = "resourceVersion";

  private String displayName;
  private String identifier;
  private String version;

  @JsonGetter(KeyDisplayName)
  public String getDisplayName() {
    return displayName;
  }

  @JsonSetter(KeyDisplayName)
  public UDDependency setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  @JsonGetter(KeyIdentifier)
  public String getIdentifier() {
    return identifier;
  }

  @JsonSetter(KeyIdentifier)
  public UDDependency setIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  @JsonGetter(KeyVersion)
  public String getVersion() {
    return version;
  }

  @JsonSetter(KeyVersion)
  public UDDependency setVersion(String version) {
    this.version = version;
    return this;
  }
}
