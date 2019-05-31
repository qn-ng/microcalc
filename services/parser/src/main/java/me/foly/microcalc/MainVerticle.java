package me.foly.microcalc;

import io.vertx.core.Launcher;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
  public static void main(final String[] args) {
    Launcher.executeCommand("run", MainVerticle.class.getName());
  }

  @Override
  public void start() throws Exception {
    CalcClient calcClient = new CalcClient(vertx);
    ParseEngine parseEngine = new ParseEngine(calcClient);
    HttpHandler httpHandler = new HttpHandler(parseEngine);

    Router apiRouter = Router.router(vertx);
    apiRouter.route().handler(httpHandler::extractHeaders);
    apiRouter.get("/status").handler(httpHandler::getStatus);
    apiRouter.post("/calculate").handler(BodyHandler.create()).handler(httpHandler::postCalculate);

    Router mainrouter = Router.router(vertx);
    mainrouter.mountSubRouter("/api/" + Env.APP_VERSION, apiRouter);
    vertx.createHttpServer().requestHandler(mainrouter).rxListen(8080).subscribe();
  }
}
