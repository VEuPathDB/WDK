package org.gusdb.wdk.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.apache.log4j.Logger;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonSchemaProvider implements MessageBodyReader < Object >,
    MessageBodyWriter<Object> {

  private static final Logger LOG = Logger.getLogger(JsonSchemaProvider.class);

  private static final String ERR_NO_SCHEMA = "missing @InSchema annotation";

  // TODO: This should not be hardcoded
  private static final String ROOT_PATH = "/service/schema";

  private static final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
  private static final ObjectMapper mapper = new ObjectMapper();

  @Context
  ResourceInfo ri;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] anns,
      MediaType mediaType) {
    return mediaType.getSubtype().equals("json")
        && findOutAnnotation().isPresent();
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] anns,
      MediaType mediaType) {
    return mediaType.getSubtype().equals("json")
        && findInAnnotation().isPresent();
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
    final String schemaPath = findOutAnnotation()
        .map(OutSchema::value)
        .map(JsonSchemaProvider::cleanPath)
        .map(this::toFullPath)
        .orElseThrow(() -> new InternalServerErrorException(ERR_NO_SCHEMA));

    final JsonNode node;
    final JsonSchema schema;
    final ProcessingReport report;
    try {
      schema = factory.getJsonSchema(JsonLoader.fromResource(schemaPath));
      node = mapper.convertValue(o, JsonNode.class);
      report = schema.validate(node);
    } catch (ProcessingException e) {
      throw new InternalServerErrorException(e);
    }

    if(!report.isSuccess()) {
      LOG.error("Invalid endpoint return value: " + report.toString());
      throw new InternalServerErrorException();
    }

    mapper.writeValue(entityStream, node);
  }

  @Override
  public Object readFrom(Class<Object> cls, Type type, Annotation[] anns,
      MediaType media, MultivaluedMap<String,String> headers, InputStream stream)
      throws IOException, WebApplicationException {
    final String schemaPath = findInAnnotation()
        .map(InSchema::value)
        .map(JsonSchemaProvider::cleanPath)
        .map(this::toFullPath)
        .orElseThrow(() -> new InternalServerErrorException(ERR_NO_SCHEMA));

    final JsonNode node;
    final JsonSchema schema;
    final ProcessingReport report;
    try {
      schema = factory.getJsonSchema(JsonLoader.fromResource(schemaPath));
      node = mapper.readTree(stream);
      report = schema.validate(node);
    } catch (ProcessingException e) {
      throw new InternalServerErrorException(e);
    }

    if (!report.isSuccess()) {
      throw new BadRequestException(report.toString());
    }

    return  mapper.convertValue(node, cls);
  }

  private String toFullPath(final String path) {
    return new File(ROOT_PATH, path).getPath();
  }

  private static String cleanPath(final String path) {
    return path.replace('.', '/');
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
