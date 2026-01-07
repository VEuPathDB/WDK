package org.gusdb.wdk.service.service;

import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.ParamBuilder;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.runtime.BuildStatus;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.WdkCacheSeeder;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.PageViewLogger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

@Path("system")
public class SystemService extends AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(SystemService.class);

  // this path is sometimes skipped by request filter logic
  public static final String PROMETHEUS_ENDPOINT_PATH = "system/metrics/prometheus";

  protected static final String CACHE_SEED_ENDPOINT = "seed-wdk-caches";

  @GET
  @Path("userdb/connections")
  @Produces(MediaType.TEXT_HTML)
  public String getUserDbInfo() {
    return getConnectionInfoResponse(getWdkModel().getUserDb());
  }

  @GET
  @Path("appdb/connections")
  @Produces(MediaType.TEXT_HTML)
  public String getAppDbInfo() {
    return getConnectionInfoResponse(getWdkModel().getAppDb());
  }

  private String getConnectionInfoResponse(DatabaseInstance db) {
    assertAdmin();
    return "<!DOCTYPE html><html><body><pre>"
      + db.getUnclosedConnectionInfo()
      + "</pre></body></html>";
  }

  @GET
  @Path("caches")
  @Produces(MediaType.APPLICATION_JSON)
  public String getInMemoryCacheSnapshot() {
    assertAdmin();
    JSONArray array = new JSONArray();

    for (var entry : CacheMgr.get().getAllCaches().entrySet()) {
      var cache    = entry.getValue();
      var size     = cache.getSize();
      var lastTrim = cache.getLastTrimDate();

      array.put(new JSONObject()
        .put("name", entry.getKey())
        .put("capacity", cache.getCapacity())
        .put("size", size)
        .put("pct-full", (100.0 * size / cache.getCapacity()) + "%")
        .put("trim-amount", cache.getNumToTrimOnCapacity())
        .put("last-trim", (lastTrim == null ? "never" : lastTrim.toString()))
        .put("time-since-last-trim", (lastTrim == null ? "N/A" :
          Timer.getDurationString(new Date().getTime() - lastTrim.getTime()))));
    }

    return array.toString(2);
  }

  @GET
  @Path(CACHE_SEED_ENDPOINT)
  @Produces(MediaType.APPLICATION_JSON)
  public Response seedWdkCaches() throws WdkModelException {
    assertAdmin();
    WdkCacheSeeder seeder = new WdkCacheSeeder(getWdkModel());
    String result = new JSONObject()
        .put("questionResults", seeder.cacheQuestions())
        .put("publicStratsResults", seeder.cachePublicStrategies())
        .toString(2);
    LOG.info("WDK Cache Seeding Complete with results: " + result);
    return Response.ok(result).build();
  }

  @GET
  @Path("/build-status")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getBuildStatus() {
    return Response.ok(BuildStatus.getLatestBuildStatus()).build();
  }

  @GET
  @Path("/metrics/prometheus")
  @Produces(MediaType.TEXT_PLAIN)
  public StreamingOutput getMetrics() {
    return output -> {
      try (var write = new OutputStreamWriter(output)) {
        TextFormat.write004(write, CollectorRegistry.defaultRegistry.metricFamilySamples());
      }
    };
  }

  @GET
  @Path("/metrics/count-page-view/{clientPath:.+}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response registerVisit(@PathParam("clientPath") String clientPath) {
    String userAgent = getHeaders().getOrDefault("User-Agent", List.of("<unknown>")).get(0);
    PageViewLogger.logPageView(getWdkModel().getProjectId(), getRequestingUser(), clientPath, userAgent);
    return Response.noContent().build();
  }

  @GET
  @Path("/metrics/searches")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionMetrics(
      @QueryParam("startDate") String startDate,
      @QueryParam("endDate") String endDate) throws WdkModelException {
    try {
      DateFormat format = FormatUtil.STANDARD_DATE_TEXT_FORMAT.get();
      if (startDate != null)
        startDate = format.format(format.parse(startDate));
      if (endDate != null)
        endDate = format.format(format.parse(endDate));
      boolean hasDate = (startDate != null || endDate != null);
      String lastRunTimeCondition = getDateRangeCondition("last_run_time", startDate, endDate);
      String creationDateCondition = getDateRangeCondition("create_time", startDate, endDate);
      String fullDateCondition = !hasDate ? "" :
        " and ( " + lastRunTimeCondition + " or " + creationDateCondition + " )";
      String sql =
          "select question_name, count(question_name) as cnt" +
          " from wdkuser.steps" +
          " where project_id = ?" +
          fullDateCondition +
          " group by question_name" +
          " order by cnt desc";
      ParamBuilder args = new ParamBuilder();
      new ListBuilder<String>()
          .add(getWdkModel().getProjectId())
          .addIf(Predicate.not(Objects::isNull), startDate)
          .addIf(Predicate.not(Objects::isNull), endDate)
          .addIf(Predicate.not(Objects::isNull), startDate)
          .addIf(Predicate.not(Objects::isNull), endDate)
          .toList()
          .stream()
          .forEach(args::addString);
      DataSource appDb = getWdkModel().getUserDb().getDataSource();
      JSONArray result = new SQLRunner(appDb, sql, "question-metrics").executeQuery(args, rs -> {
        JSONArray arr = new JSONArray();
        while (rs.next()) {
          arr.put(new JSONObject()
            .put("questionName", rs.getString("question_name"))
            .put("count", rs.getInt("cnt")));
        }
        return arr;
      });
      return Response.ok(result.toString(2)).build();
    }
    catch (ParseException e) {
      throw new BadRequestException("Date formats must be in the form YYYY-MM-DD");
    }
    catch (SQLRunnerException e) {
      throw WdkModelException.translateFrom(e);
    }
  }

  private String getDateRangeCondition(String columnName, String startDate, String endDate) {
    String startCondition = (startDate == null ? "" : columnName + " >= TO_DATE(?, 'YYYY-MM-DD')");
    String endCondition = (endDate == null ? "" : columnName + " <= TO_DATE(?, 'YYYY-MM-DD')");
    if (startDate != null && endDate != null) {
      return "( " + startCondition + " and " + endCondition + " )";
    }
    else if (startDate != null) {
      return startCondition;
    }
    else if (endDate != null) {
      return endCondition;
    }
    else {
      return null;
    }
  }
}
