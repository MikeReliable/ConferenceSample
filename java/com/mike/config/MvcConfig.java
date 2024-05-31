package com.mike.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String thesisUploadPath;

    @Value("${photo.upload.path}")
    private String photo;

    @Value("${doi.path}")
    private String thesisByDoi;

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/2021").setViewName("meso2021/index");
        registry.addViewController("/2021/En").setViewName("meso2021/eng");
        registry.addViewController("/2022").setViewName("meso2022/mainPage2022");
        registry.addViewController("/2022/En").setViewName("meso2022/mainPageEn2022");
        registry.addViewController("/2023").setViewName("meso2023/mainPage2023");
        registry.addViewController("/2023/En").setViewName("meso2023/mainPageEn2023");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/img/");
        registry.addResourceHandler("/photo/**")
                .addResourceLocations("file://" + photo + "/");
        registry.addResourceHandler("/thesis/**")
                .addResourceLocations("file://" + thesisUploadPath + "/");
        registry.addResourceHandler("/thesisByDoi/**")
                .addResourceLocations("file://" + thesisByDoi + "/");
    }
}
