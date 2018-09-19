package me.foly.microcalc;

public class Env {
  public static final String ADD_HOST = envOrDefault("ADD_HOST", "localhost");
  public static final int ADD_PORT = Integer.valueOf(envOrDefault("ADD_PORT", "8080"));
  public static final String ADD_URI = envOrDefault("ADD_URI", "/api/v1/op/add");

  public static final String SUB_HOST = envOrDefault("SUB_HOST", "localhost");
  public static final int SUB_PORT = Integer.valueOf(envOrDefault("SUB_PORT", "8080"));
  public static final String SUB_URI = envOrDefault("SUB_URI", "/api/v1/op/sub");

  public static final String MULT_HOST = envOrDefault("MULT_HOST", "localhost");
  public static final int MULT_PORT = Integer.valueOf(envOrDefault("MULT_PORT", "8080"));
  public static final String MULT_URI = envOrDefault("MULT_URI", "/api/v1/op/mult");

  public static final String DIV_HOST = envOrDefault("DIV_HOST", "localhost");
  public static final int DIV_PORT = Integer.valueOf(envOrDefault("DIV_PORT", "8080"));
  public static final String DIV_URI = envOrDefault("DIV_URI", "/api/v1/op/div");

  public static final String NEG_HOST = envOrDefault("NEG_HOST", "localhost");
  public static final int NEG_PORT = Integer.valueOf(envOrDefault("NEG_PORT", "8080"));
  public static final String NEG_URI = envOrDefault("NEG_URI", "/api/v1/op/neg");

  public static final String POW_HOST = envOrDefault("POW_HOST", "localhost");
  public static final int POW_PORT = Integer.valueOf(envOrDefault("POW_PORT", "8080"));
  public static final String POW_URI = envOrDefault("POW_URI", "/api/v1/op/pow");

  public static final String MOD_HOST = envOrDefault("MOD_HOST", "localhost");
  public static final int MOD_PORT = Integer.valueOf(envOrDefault("MOD_PORT", "8080"));
  public static final String MOD_URI = envOrDefault("MOD_URI", "/api/v1/op/mod");

  public static final String APP_VERSION = "v1";
  public static final String APP_SERVICE = "name: parser, version: " + APP_VERSION;

  private static String envOrDefault(String name, String def) {
    String env = System.getenv(name);
    return env == null ? def : env;
  }
}
