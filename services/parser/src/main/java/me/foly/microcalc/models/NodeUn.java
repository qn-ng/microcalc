package me.foly.microcalc.models;

import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.foly.microcalc.CalcClient;

import java.util.List;

public class NodeUn implements Node {
  private CalcClient calcClient;
  private NodeType type;
  private Node node;

  public NodeUn(NodeType type, Node node, CalcClient calcClient) {
    this.type = type;
    this.node = node;
    this.calcClient = calcClient;
  }

  @Override
  public NodeType getType() {
    return type;
  }

  @Override
  public Observable<JsonObject> getValue(List<Pair<String, String>> headers) {
    return node.getValue(headers)
        .switchMap(v -> calcClient.un(new JsonArray().add(v), type, headers));
  }
}
