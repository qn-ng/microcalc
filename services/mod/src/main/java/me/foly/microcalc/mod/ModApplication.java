package me.foly.microcalc.mod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ModApplication {
  public static void main(String[] args) {
    SpringApplication.run(ModApplication.class, args);
  }

  @Bean
  WebClient multClient(@Value("${mc.mult_endpoint}") String MULT_ENDPOINT) {
    return WebClient.create(MULT_ENDPOINT);
  }

  @Bean
  WebClient divClient(@Value("${mc.div_endpoint}") String DIV_ENDPOINT) {
    return WebClient.create(DIV_ENDPOINT);
  }

  @Bean
  WebClient subClient(@Value("${mc.sub_endpoint}") String SUB_ENDPOINT) {
    return WebClient.create(SUB_ENDPOINT);
  }
}
