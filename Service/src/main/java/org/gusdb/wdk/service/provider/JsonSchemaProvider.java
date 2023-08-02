package org.gusdb.wdk.service.provider;

import static org.gusdb.fgputil.functional.Functions.mapException;
import static org.gusdb.fgputil.json.JsonUtil.Jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.fge.jsonschema.core.load.Dereferencing;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonSchemaProvider implements MessageBodyReader <Object>,
    MessageBodyWriter<Object> {

  private static final String SCHEMA_PATH = "resource:/schema";
  private static final JsonSchemaFactory SCHEMA_FAC;

  static {
    SCHEMA_FAC = JsonSchemaFactory.newBuilder()
      .setLoadingConfiguration(LoadingConfiguration.newBuilder()
        .dereferencing(Dereferencing.INLINE)
        .freeze())
      .freeze();
  }

  @Context
  private ResourceInfo ri;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] anns,
      MediaType mediaType) {
    return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] anns,
      MediaType mediaType) {
    return "json".equals(mediaType.getSubtype());
  }

  @Override
  public long getSize(Object o, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Object o, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    final JsonNode node = Jackson.convertValue(o, JsonNode.class);

    final String schema = findOutAnnotation()
        .map(OutSchema::value)
        .map(JsonSchemaProvider::cleanPath)
        .map(this::toSchemaFile)
        .orElse(null);

    if(schema != null) {
      final ProcessingReport report = validate(schema, node);
      if (!report.isSuccess()) {
        throw new InternalServerErrorException(report.toString());
      }
    }

    Jackson.writeValue(entityStream, node);
  }

  @Override
  public Object readFrom(Class<Object> cls, Type type, Annotation[] anns,
      MediaType media, MultivaluedMap<String,String> headers, InputStream stream)
      throws WebApplicationException {
    final JsonNode node = Functions.mapException(() ->
      Optional.ofNullable(Jackson.readTree(stream))
        .orElseGet(NullNode::getInstance),
      parseException -> new BadRequestException(parseException.getMessage()));

    final String schema = findInAnnotation()
        .map(InSchema::value)
        .map(JsonSchemaProvider::cleanPath)
        .map(this::toSchemaFile)
        .orElse(null);

    if (schema != null) {
      final ProcessingReport report = validate(schema, node);
      if (!report.isSuccess()) {
        throw new BadRequestException(report.toString());
      }
    }

    return Jackson.convertValue(node, cls);
  }

  private ProcessingReport validate(final String path, final JsonNode node)
      throws WebApplicationException {

    // try to find the JsonSchema for this path; error if unable
    final JsonSchema schema = mapException(
        () -> SCHEMA_FAC.getJsonSchema(path),
        e -> new InternalServerErrorException(e));
 
    // once schema is found, assume any error is due to request body misformat
    return mapException(
        () -> schema.validate(node),
        e -> new BadRequestException(e.getMessage(), e));
  }

  private String toSchemaFile(final String path) {
    return Paths.get(SCHEMA_PATH, path).toString();
  }

  private static String cleanPath(final String path) {
    return path.replace('.', '/') + ".json";
  }

  private Optional<InSchema> findInAnnotation() {
    return Arrays.stream(ri.getResourceMethod().getDeclaredAnnotations())
        .filter(InSchema.class::isInstance)
        .findFirst()
        .map(InSchema.class::cast);
  }

  private Optional<OutSchema> findOutAnnotation() {
    return Arrays.stream(ri.getResourceMethod().getDeclaredAnnotations())
        .filter(OutSchema.class::isInstance)
        .findFirst()
        .map(OutSchema.class::cast);
  }
}
