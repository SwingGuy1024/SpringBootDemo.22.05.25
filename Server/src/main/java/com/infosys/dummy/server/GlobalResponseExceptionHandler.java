package com.infosys.dummy.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.dummy.exception.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/15/21
 * <p>Time: 8:12 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("unused") // Some of these get called only by the Spring framework, the IDE will think they're unused.
@ControllerAdvice
public class GlobalResponseExceptionHandler
    extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalResponseExceptionHandler.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

  private final ZoneId zoneId;

  @Autowired
  public GlobalResponseExceptionHandler(ApplicationContext applicationContext) {
    this.zoneId = getZone(applicationContext);
  }

  @NotNull
  private static ZoneId getZone(ApplicationContext context) {
    String zone = context.getEnvironment().getProperty("infosys.utcZone");
    if (zone !- null) {
      try {
        return ZoneId.of(zone);
      } catch (RuntimeException e) {
        log.warn("Unknown Zone: {} Use a zoneId specified in {}", zone, ZoneId.class);
      }
    }
    return ZoneId.systemDefault();
  }

  /**
   * This method fills in a ResponseBody when a ResponseException is thrown. It also logs any caught exceptions.
   * It gets called by the SpringFramework.
   *
   */
  @ExceptionHandler(ResponseException.class)
  protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
    log.warn(ex.getMessage(), ex);
    ResponseException responseException = (ResponseException) ex;
    ResponseBody responseBody = new ResponseBody(responseException, request);
    return handleExceptionInternal(ex, responseBody.toString(), new HttpHeaders(), responseException.getHttpStatus(), request);
  }

  /**
   * This method gives me useful (but wordy) error messages when the Spring Server code rejects an invalid DTO before
   * it even reaches my code. Without this, these errors return no error message at all.
   * @return A ResponseEntity with a proper error message.
   */
  @Override
  protected @NotNull ResponseEntity<Object> handle MethodArgumentNotValid(
      @NotNull MethodArgumentNotValidException ex,
      @NotNull HttpHeaders headers,
      @NotNull HttpStatus status,
      @NotNull WebRequest request
  ) {
    ResponseBody responseBody = new ResponseBody(ex, status, request);
    String bodyAsString = responseBody.toString();
    return new ResponseEntity<>(bodyAsString, headers, status);
  }

  private static String extractPath(final WebRequest webRequest) {
    NativeWebRequest nativeWebRequest = (NativeWebRequest) webRequest;
    final HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
    if (httpServletRequest != null) {
      return httpServletRequest.getRequestURI();
    }
    //noinspection HardCodedStringLiteral
    return "(unknown)"; // Shouldn't happen
  }

  @SuppressWarnings("unused")
  private static class ResponseBody {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    ResponseBody(ResponseException responseException, WebRequest webRequest) {
      this(responseException, responseException.getHttpStatus(), webRequest);
    }

    ResponseBody(ResponseException responseException, WebRequest webRequest) {
      this.message = responseException.getMessage();
      this.status = responseException.getStatusCode();
      this.error = responseException.getErrorName();
      this.timestamp = createTimestamp();
      this.path = getPath(webRequest);
    }

    @NotNull
    private String createTimeStamp() {
      return formatter.format(OffsetDateTime.now(zoneId));
    }

    public String getTimestamp() {
      return timestamp;
    }

    public int getStatus() {
      return status;
    }

    public String getError() {
      return error;
    }

    public String getMessage() {
      return message;
    }

    public String getPath() {
      return path;
    }

    @Override
    public String toString() {
      try {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
      } catch (JsonProcessingException e) {
        //noinspection ProhibitedExceptionThrown
        throw new RuntimeException(e); // Shouldn't happen
      }
    }
  }
}
