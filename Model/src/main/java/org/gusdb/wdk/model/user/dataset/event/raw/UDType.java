package org.gusdb.wdk.model.user.dataset.event.raw;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UDType
{
  private static final String KeyName    = "name";
  private static final String KeyVersion = "version";

  private String name;
  private String version;

  @JsonGetter(KeyName)
  public String getName() {
    return name;
  }

  @JsonSetter(KeyName)
  public UDType setName(String name) {
    this.name = name;
    return this;
  }

  @JsonGetter(KeyVersion)
  public String getVersion() {
    return version;
  }

  @JsonSetter(KeyVersion)
  public UDType setVersion(String version) {
    this.version = version;
    return this;
  }
}
