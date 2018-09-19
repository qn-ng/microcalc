package me.foly.microcalc.mod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@RestController
public class MainController {
  private static final List<String> TRACING_HEADERS =
      Arrays.asList(
          "x-request-id",
          "x-b3-traceid",
          "x-b3-spanid",
          "x-b3-parentspanid",
          "x-b3-sampled",
          "x-b3-flags",
          "x-ot-span-context");

  private static final String APP_VERSION = "v1";
  private static final String APP_BASEPATH = "/api/" + APP_VERSION;
  private static final String APP_SERVICE = "name: mod, version: " + APP_VERSION;

  private static final StatusResponse okResponse = new StatusResponse("OK");
  private static final ErrorResponse errorResponse = new ErrorResponse("Invalid Input");

  @Autowired private WebClient multClient;
  @Autowired private WebClient divClient;
  @Autowired private WebClient subClient;

  @GetMapping(APP_BASEPATH + "/status")
  public StatusResponse getStatus() {
    return okResponse;
  }

  @PostMapping(APP_BASEPATH + "/mod")
  public Mono postMod(
      @Valid @RequestBody CalcRequest calcRequest, @RequestHeader HttpHeaders headers) {
    final int op1 = calcRequest.getOperands().get(0);
    final int op2 = calcRequest.getOperands().get(1);
    if (op2 == 0) throw new BadOperandsException();

    final List<ServiceResponse> origins = new ArrayList<>();

    return div(op1, op2, headers)
        .flatMap(
            res -> {
              origins.add(res);
              return mult(res.getResult(), op2, headers);
            })
        .flatMap(
            res -> {
              origins.add(res);
              return sub(op1, res.getResult(), headers);
            })
        .flatMap(
            res -> {
              origins.add(res);
              return Mono.just(
                  new ServiceResponse(
                      res.getResult(), calcRequest.getOperands(), APP_SERVICE, origins));
            });
  }

  private Mono<ServiceResponse> div(int op1, int op2, HttpHeaders headers) {
    return client(divClient, op1, op2, headers);
  }

  private Mono<ServiceResponse> mult(int op1, int op2, HttpHeaders headers) {
    return client(multClient, op1, op2, headers);
  }

  private Mono<ServiceResponse> sub(int op1, int op2, HttpHeaders headers) {
    return client(subClient, op1, op2, headers);
  }

  private Mono<ServiceResponse> client(WebClient wc, int op1, int op2, HttpHeaders headers) {
    WebClient.RequestBodyUriSpec client = wc.post();

    TRACING_HEADERS
        .stream()
        .map(header -> new Pair<>(header, headers.get(header)))
        .filter(p -> p.v != null && p.v.size() > 0)
        .forEach(p -> client.header(p.k, p.v.toArray(new String[0])));

    return client
        .body(fromObject(new CalcRequest(Arrays.asList(op1, op2))))
        .exchange()
        .flatMap(res -> res.bodyToMono(ServiceResponse.class));
  }

  @ExceptionHandler({BadOperandsException.class, WebExchangeBindException.class})
  ResponseEntity handleBadOperands() {
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class ServiceResponse {
    private int result;
    private List<Integer> operands;
    private String service;
    private List<ServiceResponse> origins;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class CalcRequest {
    @NotNull
    @Size(min = 2, max = 2)
    public List<Integer> operands;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public static class BadOperandsException extends RuntimeException {}

  @Data
  @NoArgsConstructor
  static class StatusResponse {
    private String data;

    public StatusResponse(String data) {
      this.data = data;
    }
  }

  @Data
  @NoArgsConstructor
  static class ErrorResponse {
    private String error;

    public ErrorResponse(String error) {
      this.error = error;
    }
  }

  @AllArgsConstructor
  private class Pair<K, V> {
    K k;
    V v;
  }
}
