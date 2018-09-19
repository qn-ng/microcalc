package me.foly.microcalc;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import me.foly.microcalc.models.Node;
import me.foly.microcalc.models.Pair;
import org.jparsec.error.ParserException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HttpHandler {
  private static final List<String> TRACING_HEADERS =
      Arrays.asList(
          "x-request-id",
          "x-b3-traceid",
          "x-b3-spanid",
          "x-b3-parentspanid",
          "x-b3-sampled",
          "x-b3-flags",
          "x-ot-span-context");
  private ParseEngine parseEngine;

  public HttpHandler(ParseEngine parseEngine) {
    this.parseEngine = parseEngine;
  }

  public void getStatus(RoutingContext ctx) {
    res(ctx, 200, "OK");
  }

  public void postCalculate(RoutingContext ctx) {
    JsonObject body = ctx.getBodyAsJson();
    if (body == null || !body.containsKey("input")) {
      error(ctx, 400, "Input required");
      return;
    }

    Node node;

    try {
      node = parseEngine.CALCULATOR.parse(ctx.getBodyAsJson().getString("input"));
    } catch (ParserException ex) {
      error(ctx, 400, ex.getMessage());
      return;
    }

    List<Pair<String, String>> headers = ctx.get("istio-headers");

    node.getValue(headers)
        .subscribe(
            result -> res(ctx, 200, result),
            err -> {
              err.printStackTrace();
              error(ctx, 500, "Something wrong...");
            });
  }

  public void extractHeaders(RoutingContext ctx) {
    HttpServerRequest req = ctx.request();
    List<Pair<String, String>> headers =
        TRACING_HEADERS
            .stream()
            .map(header -> new Pair<>(header, req.getHeader(header)))
            .filter(p -> p.v != null && !p.v.isEmpty())
            .collect(Collectors.toList());
    ctx.put("istio-headers", headers);
    ctx.next();
  }

  private void error(RoutingContext ctx, int code, String msg) {
    JsonObject content = new JsonObject();
    content.put("error", msg);
    res(ctx, code, content);
  }

  private <T> void res(RoutingContext ctx, int code, T data) {
    JsonObject content = new JsonObject();
    content.put("data", data);
    res(ctx, code, content);
  }

  private void res(RoutingContext ctx, int code, JsonObject content) {
    HttpServerResponse response = ctx.response();
    response.setStatusCode(code);
    response.putHeader("Content-Type", "application/json");
    response.end(Json.encodePrettily(content));
  }

}
