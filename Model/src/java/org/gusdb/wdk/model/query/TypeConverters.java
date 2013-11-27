package org.gusdb.wdk.model.query;

import java.sql.Date;
import java.sql.Timestamp;

public class TypeConverters {

  public interface TypeConverter {
    public Object convert(String s);
  }

  private TypeConverters() {}
  
  public static TypeConverter STRING_CONVERTER = new TypeConverter(){
    @Override
    public Object convert(String s) { return s; }
  };

  public static TypeConverter INTEGER_CONVERTER = new TypeConverter(){
    @Override
    public Object convert(String s) { return Integer.parseInt(s); }
  };

  public static TypeConverter FLOAT_CONVERTER = new TypeConverter(){
    @Override
    public Object convert(String s) { return Float.parseFloat(s); }
  };

  public static TypeConverter BOOLEAN_CONVERTER = new TypeConverter(){
    @Override
    public Object convert(String s) { return Boolean.parseBoolean(s); }
  };

  public static TypeConverter TIMESTAMP_CONVERTER = new TypeConverter(){
    @Override
    public Object convert(String s) {
      return new Timestamp(Date.valueOf(s).getTime());
    }
  };
}
