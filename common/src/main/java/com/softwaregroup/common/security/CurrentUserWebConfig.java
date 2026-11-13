package com.softwaregroup.common.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CurrentUserWebConfig implements WebMvcConfigurer {
    private final CurrentUserArgumentResolver resolver;
    private final RoleAuthorizationInterceptor roleInterceptor;

    public CurrentUserWebConfig(CurrentUserArgumentResolver resolver,
                                RoleAuthorizationInterceptor roleInterceptor) {
        this.resolver = resolver;
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor);
    }
}
