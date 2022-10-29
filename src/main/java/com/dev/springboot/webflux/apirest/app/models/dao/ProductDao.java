package com.dev.springboot.webflux.apirest.app.models.dao;


import com.dev.springboot.webflux.apirest.app.models.documents.Product;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

    Mono<Product> findByName(String name);

    @Query("{'name': ?0}")
    Mono<Product> getName(String name);
}
