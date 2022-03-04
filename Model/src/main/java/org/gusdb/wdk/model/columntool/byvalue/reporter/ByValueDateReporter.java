package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.gusdb.fgputil.ComparableLocalDateTime;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Range;
import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.AbstractDistribution.ValueSpec;
import org.gusdb.fgputil.distribution.DateBinDistribution;
import org.gusdb.fgputil.distribution.DateBinDistribution.DateBinSpec;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONException;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class ByValueDateReporter extends AbstractByValueReporter {

  private static final String PROP_BIN_SIZE = "binSize";
  private static final String PROP_BIN_UNITS = "binUnits";

  // this value ensures default units of:
  private static final int STARTER_BIN_MAX = 35;

  private static final String[] ALLOWED_UNIT_VALUES =
      DateBinDistribution.ALLOWED_UNITS.stream()
      .map(u -> u.name().toLowerCase()).collect(Collectors.toList())
      .toArray(new String[DateBinDistribution.ALLOWED_UNITS.size()]);

  public ByValueDateReporter() {
    super(List.of(AttributeFieldDataType.DATE));
  }

  @Override
  public SchemaBuilder getInputSchema() {
    UntypedSchema rs = Schema.draft4();
    return rs.asObject()
        .optionalProperty(DisplayRangeFactory.PROP_DISPLAY_RANGE_MIN, rs.asNumber())
        .optionalProperty(DisplayRangeFactory.PROP_DISPLAY_RANGE_MAX, rs.asNumber())
        .optionalProperty(PROP_BIN_SIZE, rs.asInteger().minimum(1))
        .optionalProperty(PROP_BIN_UNITS, rs.asString().enumValues(ALLOWED_UNIT_VALUES));
  }


  @Override
  protected String convertToStringValue(ResultSet rs, String valueColumn) throws SQLException {
    Date d = rs.getDate(valueColumn);
    return d == null ? null : FormatUtil.formatDateTime(d);
  }

  @Override
  protected AbstractDistribution createDistribution(JSONObject config) throws WdkModelException, ReporterConfigException {
    try {
      return new DateBinDistribution(this, ValueSpec.COUNT, determineBinSpec(config));
    }
    // important to catch these here and convert to avoid 500s
    catch (JSONException | IllegalArgumentException e) {
      throw new ReporterConfigException(e.getMessage());
    }
  }

  private DateBinSpec determineBinSpec(JSONObject config) throws ReporterConfigException {

    Range<ComparableLocalDateTime> displayRange = new DisplayRangeFactory(_appDb, _attributeField.getName(), _jointAttributeSql)
        .calculateDisplayRange(config,
            (json, key) -> dateOrNull(json, key),
            (rs, key) -> parseDateTime(convertToStringValue(rs, key)));

    // find bin size, defaulting to 1 and choose units
    Integer binSize = config.has(PROP_BIN_SIZE) ? config.getInt(PROP_BIN_SIZE) : 1;

    // find bin units, calculating if absent
    ChronoUnit binUnits = calculateBinUnits(binUnitsOrNull(config), binSize, displayRange);

    return new DateBinSpec() {
      @Override
      public String getDisplayRangeMin() {
        return FormatUtil.formatDateTime(displayRange.getBegin().get());
      }
      @Override
      public String getDisplayRangeMax() {
        return FormatUtil.formatDateTime(displayRange.getEnd().get());
      }
      @Override
      public ChronoUnit getBinUnits() {
        return binUnits;
      }
      @Override
      public int getBinSize() {
        return binSize;
      }
    };
  }

  private static ChronoUnit calculateBinUnits(ChronoUnit binUnits,
      Integer binSize, Range<ComparableLocalDateTime> displayRange) throws ReporterConfigException {

    long daysInRange =
        displayRange.getEnd().get().getLong(ChronoField.EPOCH_DAY) -
        displayRange.getBegin().get().getLong(ChronoField.EPOCH_DAY);

    if (binUnits != null) {
      // check for max bins
      long numBins = daysInRange / daysInUnit(binUnits) / binSize;
      if (numBins <= MAX_BIN_COUNT) {
        // valid value
        return binUnits;
      }
      throw new ReporterConfigException("Configured bin size and units would " +
          "result in more bins (" + numBins + ") than the maximum, " + MAX_BIN_COUNT);
    }

    // try each unit (ascending) until a desirable number of bins is found
    for (ChronoUnit unit : DateBinDistribution.ALLOWED_UNITS) {
      long numBins = daysInRange / daysInUnit(unit) / binSize;
      if (numBins < STARTER_BIN_MAX) {
        return unit;
      }
    }
    // default to years
    return ChronoUnit.YEARS;
  }

  private static long daysInUnit(ChronoUnit unit) {
    switch(unit) {
      case DAYS: return 1;
      case WEEKS: return 7;
      case MONTHS: return 30;
      case YEARS: return 365;
      default: throw new IllegalArgumentException("Invalid units");
    }
  }

  private static ChronoUnit binUnitsOrNull(JSONObject config) {
    if (!config.has(PROP_BIN_UNITS)) return null;
    String s = config.getString(PROP_BIN_UNITS);
    if (!Arrays.asList(ALLOWED_UNIT_VALUES).contains(s)) {
      throw new IllegalArgumentException(PROP_BIN_UNITS + " must be one of [ " + String.join(", ", ALLOWED_UNIT_VALUES) + " ]");
    }
    return ChronoUnit.valueOf(s.toUpperCase());
  }

  private static ComparableLocalDateTime dateOrNull(JSONObject config, String key) {
    try {
      if (!config.has(key)) return null;
      String stringValue = config.getString(key);
      return parseDateTime(stringValue);
    }
    catch (JSONException | IllegalArgumentException e) {
      throw new IllegalArgumentException(key + " must be a ISO-8601 formatted date-time");
    }
  }

  private static ComparableLocalDateTime parseDateTime(String dateTimeString) {
    return dateTimeString == null ? null : new ComparableLocalDateTime(FormatUtil.parseDateTime(dateTimeString));
  }

}
