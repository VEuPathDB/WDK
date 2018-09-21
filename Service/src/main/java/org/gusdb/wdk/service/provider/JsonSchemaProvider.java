package org.gusdb.wdk.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;

import javax.ws.rs.*;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonSchemaProvider implements MessageBodyReader <Object>,
    MessageBodyWriter<Object> {

  private static final String SCHEMA_PATH = "resource:/schema/service";
  private static final JsonSchemaFactory SCHEMA_FAC = JsonSchemaFactory.byDefault();
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .registerModule(new JsonOrgModule());

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
    return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
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
    final JsonNode node = (o instanceof JsonNode)
        ? (JsonNode) o
        : MAPPER.convertValue(o, JsonNode.class);

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

    MAPPER.writeValue(entityStream, node);
  }

  @Override
  public Object readFrom(Class<Object> cls, Type type, Annotation[] anns,
      MediaType media, MultivaluedMap<String,String> headers, InputStream stream)
      throws IOException, WebApplicationException {
    final JsonNode node = MAPPER.readTree(stream);

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

    return  MAPPER.convertValue(node, cls);
  }

  private ProcessingReport validate(final String path, final JsonNode node)
      throws WebApplicationException {
    final JsonSchema schema;

    try {
      schema = SCHEMA_FAC.getJsonSchema(path);
      return schema.validate(node);
    } catch (ProcessingException e) {
      throw new InternalServerErrorException(e);
    }
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
