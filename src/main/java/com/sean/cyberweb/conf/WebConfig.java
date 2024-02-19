package com.sean.cyberweb.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // PC控管圖片路徑
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/profileImg/**")
//                .addResourceLocations("file:/C:/Users/ML2/Desktop/Cyber%20Web/Img/avatar/");
//    }

    // MAC控管圖片路徑
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/profileImg/**")
                .addResourceLocations("file:/Users/ml2_oao/Desktop/CyberWeb/Img/");
    }
}
