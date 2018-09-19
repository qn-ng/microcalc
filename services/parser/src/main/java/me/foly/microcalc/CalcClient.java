package me.foly.microcalc;

import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.WebClient;
import me.foly.microcalc.models.NodeType;
import me.foly.microcalc.models.Pair;

import java.util.List;

import static me.foly.microcalc.Env.*;

public class CalcClient {
  private static Logger logger = LoggerFactory.getLogger(CalcClient.class);

  private final WebClient wc;

  public CalcClient(Vertx vertx) {
    this.wc = WebClient.create(vertx);
  }

  public Observable<JsonObject> bin(
      JsonArray origins, NodeType type, List<Pair<String, String>> headers) {
    String host, uri;
    int port;
    switch (type) {
      case OP_PLUS:
        host = ADD_HOST;
        port = ADD_PORT;
        uri = ADD_URI;
        break;
      case OP_MINUS:
        host = SUB_HOST;
        port = SUB_PORT;
        uri = SUB_URI;
        break;
      case OP_MULT:
        host = MULT_HOST;
        port = MULT_PORT;
        uri = MULT_URI;
        break;
      case OP_DIV:
        host = DIV_HOST;
        port = DIV_PORT;
        uri = DIV_URI;
        break;
      case OP_POW:
        host = POW_HOST;
        port = POW_PORT;
        uri = POW_URI;
        break;
      case OP_MOD:
        host = MOD_HOST;
        port = MOD_PORT;
        uri = MOD_URI;
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return calc(
        new JsonObject()
            .put(
                "operands",
                new JsonArray()
                    .add(origins.getJsonObject(0).getInteger("result"))
                    .add(origins.getJsonObject(1).getInteger("result"))),
        host,
        port,
        uri,
        origins,
        headers);
  }

  public Observable<JsonObject> un(
      JsonArray origins, NodeType type, List<Pair<String, String>> headers) {
    String host, uri;
    int port;
    switch (type) {
      case OP_NEG:
        host = NEG_HOST;
        port = NEG_PORT;
        uri = NEG_URI;
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return calc(
        new JsonObject()
            .put("operands", new JsonArray().add(origins.getJsonObject(0).getInteger("result"))),
        host,
        port,
        uri,
        origins,
        headers);
  }

  private Observable<JsonObject> calc(
      JsonObject operands,
      String host,
      int port,
      String uri,
      JsonArray origins,
      List<Pair<String, String>> headers) {
    HttpRequest<Buffer> client = wc.post(port, host, uri);
    headers.forEach(p -> client.putHeader(p.k, p.v));

    return client
        .rxSendJsonObject(operands)
        .map(
            response -> {
              JsonObject body = response.bodyAsJsonObject();
              if (response.statusCode() == 400 || body.containsKey("error"))
                throw new IllegalArgumentException("Invalid Input");
              else if (response.statusCode() != 200) throw new Exception(response.bodyAsString());
              return body.put("origins", origins);
            })
        .toObservable();
  }
}
