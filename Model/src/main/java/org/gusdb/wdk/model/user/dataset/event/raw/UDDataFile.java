package org.gusdb.wdk.model.user.dataset.event.raw;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UDDataFile
{
  private static final String KeyName = "name";
  private static final String KeyFile = "file";
  private static final String KeySize = "size";

  private String name;
  private String file;
  private long size;

  @JsonGetter(KeyName)
  public String getName() {
    return name;
  }

  @JsonSetter(KeyName)
  public UDDataFile setName(String name) {
    this.name = name;
    return this;
  }

  @JsonGetter(KeyFile)
  public String getFile() {
    return file;
  }

  @JsonSetter(KeyFile)
  public UDDataFile setFile(String file) {
    this.file = file;
    return this;
  }

  @JsonGetter(KeySize)
  public long getSize() {
    return size;
  }

  @JsonSetter(KeySize)
  public UDDataFile setSize(long size) {
    this.size = size;
    return this;
  }
}
