package com.bianjp.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {
  private String title;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}