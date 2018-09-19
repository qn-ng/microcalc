package me.foly.microcalc.models;

import io.reactivex.Observable;
import io.vertx.core.json.JsonObject;
import me.foly.microcalc.Env;

import java.util.List;

public class NodeVal implements Node {

  private int value;

  public NodeVal(int value) {
    this.value = value;
  }

  @Override
  public NodeType getType() {
    return NodeType.VAL;
  }

  @Override
  public Observable<JsonObject> getValue(List<Pair<String, String>> headers) {
    return Observable.just(new JsonObject().put("result", value).put("service", Env.APP_SERVICE));
  }
}
