package me.whizvox.gameshelf;

import me.whizvox.gameshelf.game.GameRelation;
import me.whizvox.gameshelf.media.GenericMedia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GSWebMvcConfiguration implements WebMvcConfigurer {

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setDefaultTimeout(-1);
    configurer.setTaskExecutor(asyncTaskExecutor());
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(String.class, GenericMedia.class, GenericMedia::parse);
    registry.addConverter(String.class, GameRelation.class, GameRelation::parse);
  }

  @Bean
  public AsyncTaskExecutor asyncTaskExecutor() {
    return new SimpleAsyncTaskExecutor("async");
  }

}
