package com.tanhua.server.config;

import com.tanhua.server.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置拦截器：
 *         WebMvcConfigurer相当于 以前的xml配置中的<mvc:xxxx> 例如：<mvc:interceptors>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * <mvc:interceptors>
     *     <mvc:interceptor>
     *         .....
     *     </mvc:interceptor>
     * </mvc:interceptors>
     * @param registry
     */
    public void addInterceptors(InterceptorRegistry registry) {
        //相当于在springmvc.xml中配置了拦截器 <mvc:interceptor>
        //拦截路径
        registry.addInterceptor(tokenInterceptor)
                //放行路径
                .addPathPatterns("/**").excludePathPatterns("/user/login","/user/loginVerification");
    }
}
