package me.foly.microcalc.models;

import io.reactivex.Observable;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface Node {
  NodeType getType();

  Observable<JsonObject> getValue(List<Pair<String, String>> headers);
}
