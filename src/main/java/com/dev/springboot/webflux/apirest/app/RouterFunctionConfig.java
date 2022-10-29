package com.dev.springboot.webflux.apirest.app;

import com.dev.springboot.webflux.apirest.app.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterFunctionConfig {


    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return RouterFunctions
            .route(RequestPredicates.GET("/api/v2/products").or(RequestPredicates.GET("/api/v3/products")), request -> handler.list(request))
            .andRoute(RequestPredicates.GET("/api/v2/products/{id}"),request -> handler.detail(request))
            .andRoute(RequestPredicates.POST("/api/v2/products"),request -> handler.createProduct(request))
            .andRoute(RequestPredicates.PUT("/api/v2/products/{id}"),request -> handler.updateProduct(request))
            .andRoute(RequestPredicates.DELETE("/api/v2/products/{id}"),request -> handler.deleteProduct(request))
            .andRoute(RequestPredicates.POST("/api/v2/products/upload/{id}"),request -> handler.upload(request))
            .andRoute(RequestPredicates.POST("/api/v2/products/create"),request -> handler.createWithImage(request));
    }
}
