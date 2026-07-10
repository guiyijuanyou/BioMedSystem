package com.cqutcm.biomed.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SPA (Single Page Application) fallback configuration.
 * <p>
 * Forwards any request that doesn't match a controller or a real static file
 * to {@code index.html}, so Vue Router (history mode) can handle it client-side.
 * REST controllers under {@code /api/**} always take precedence.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        // If a real file exists (e.g. /assets/xxx.js), serve it directly.
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        // Otherwise fall back to index.html for Vue Router history mode.
                        Resource index = new ClassPathResource("/static/index.html");
                        if (index.exists() && index.isReadable()) {
                            return index;
                        }
                        return null;
                    }
                });
    }
}
