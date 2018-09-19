package me.foly.microcalc.models;

import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import me.foly.microcalc.CalcClient;

import java.util.List;

public class NodeBin implements Node {
  private CalcClient calcClient;
  private NodeType type;
  private Node left;
  private Node right;

  public NodeBin(NodeType type, Node left, Node right, CalcClient calcClient) {
    this.type = type;
    this.left = left;
    this.right = right;
    this.calcClient = calcClient;
  }

  @Override
  public NodeType getType() {
    return type;
  }

  @Override
  public Observable<JsonObject> getValue(List<Pair<String, String>> headers) {
    return Observable.combineLatest(left.getValue(headers), right.getValue(headers), (l, r) -> new JsonArray().add(l).add(r))
        .take(1)
        .switchMap(origins -> calcClient.bin(origins, type, headers));
  }
}
