package org.gusdb.wdk.service;

import static org.gusdb.fgputil.FormatUtil.isLong;
import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.wdk.service.service.AbstractWdkService.getStreamingOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FileChunkInputStream;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.Range;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModelException;

public class FileRanges {

  private static final Logger LOG = Logger.getLogger(FileRanges.class);

  public static final String RANGE_HEADER = "Range";
  public static final String CONTENT_RANGE_HEADER = "Content-Range";

  private static final Long DEFAULT_RANGE_BEGIN = 0L;
  private static final String SIZE_UNITS = "bytes";
  private static final String RANGE_HEADER_VALUE_PREFIX = SIZE_UNITS + "=";

  public static class ByteRangeInformation extends TwoTuple<Boolean,Range<Long>> {
    public ByteRangeInformation(boolean rangeHeaderSubmitted, Range<Long> parsedRange) {
      super(rangeHeaderSubmitted, parsedRange);
    }
    public boolean isRangeHeaderSubmitted() { return getFirst(); }
    public Range<Long> getDesiredRange() { return getSecond(); }
  }

  public static ByteRangeInformation parseRangeHeaderValue(String rangeStr) {
    LOG.debug("Incoming range string: " + rangeStr);
    if (rangeStr == null || rangeStr.isBlank()) {
      return new ByteRangeInformation(false, new Range<>(DEFAULT_RANGE_BEGIN, null));
    }
    if (!rangeStr.startsWith(RANGE_HEADER_VALUE_PREFIX)) {
      throw new BadRequestException("Endpoint does not support non-byte range requests.");
    }
    String rangeStrValue = rangeStr.substring(RANGE_HEADER_VALUE_PREFIX.length());
    if (rangeStrValue.startsWith("-")) {
      throw new BadRequestException("Range must have a begin value.");
    }
    String[] tokens = rangeStrValue.split("-");
    if (tokens.length > 2) {
      throw new BadRequestException("Currently only a single range is supported");
    }
    LOG.debug("Received " + tokens.length + " tokens [" + join(tokens, ",") + "]");
    if (isLong(tokens[0]) && (tokens.length == 1 || isLong(tokens[1]))) {
      try {
        Range<Long> range = new Range<>(Long.parseLong(tokens[0]),
            tokens.length == 1 ? null : Long.parseLong(tokens[1]));
        if (range.getBegin() < 0) {
          throw new BadRequestException("Range cannot begin before byte 0.");
        }
        // range header is 0-based, inclusive on both ends
        range.setEndInclusive(true);
        return new ByteRangeInformation(true, range);
      }
      catch (IllegalArgumentException e) {
        throw new BadRequestException(e.getMessage());
      }
    }
    else {
      throw new BadRequestException("Range header must be of the form '" + RANGE_HEADER_VALUE_PREFIX + "<min>-[<max>]'.");
    }
  }

  public static Response getFileChunkResponse(Path filePath, ByteRangeInformation rangeInfo) throws WdkModelException {
    FileChunkInputStream fileIn = null;
    Range<Long> byteRange = rangeInfo.getDesiredRange();
    try {

      long fileSize = new File(filePath.toString()).length();
      if (fileSize == 0) {
        throw new WdkModelException("File " + filePath + " does not exist or is size 0.");
      }

      // if full file requested, simply return file
      if (byteRange.getBegin() == 0 && (!byteRange.hasEnd() || byteRange.getEnd() == fileSize - 1)) {
        return Response
            .ok(filePath.toFile())
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .status(rangeInfo.isRangeHeaderSubmitted() ? Status.PARTIAL_CONTENT : Status.OK)
            .header(CONTENT_RANGE_HEADER, SIZE_UNITS + " */" + fileSize)
            .build();
      }

      // resolve end and check that it is not greater than file length
      if (!byteRange.hasEnd() || byteRange.getEnd() > fileSize - 1) {
        byteRange.setEnd(fileSize - 1);
      }
      /* per HTTP spec, range end values greater than size of the file are fine
      else if (fileSize - 1 < byteRange.getEnd()) {
        throw new BadRequestException("End of range must be <= " + (fileSize - 1) + " for this resource.");
      }*/

      // create an input stream that will only read to the end of the specified range
      fileIn = new FileChunkInputStream(filePath, byteRange);
      return Response
          .ok(getStreamingOutput(fileIn))
          .type(MediaType.APPLICATION_OCTET_STREAM)
          .status(Status.PARTIAL_CONTENT)
          .header(CONTENT_RANGE_HEADER, SIZE_UNITS + " " +
              byteRange.getBegin() + "-" + byteRange.getEnd() + "/" + fileSize)
          .build();
    }
    catch(IOException e) {
      // close fileIn in error case; in success case, StreamingOutput will close the file
      IoUtil.closeQuietly(fileIn);
      LOG.error("Could not read requested file: " + filePath, e);
      throw new WdkModelException("Unable to read required resource.", e);
    }
  }
}
