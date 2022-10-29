package com.dev.springboot.webflux.apirest.app.models.dao;


import com.dev.springboot.webflux.apirest.app.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoryDao extends ReactiveMongoRepository<Category, String>{
    Mono<Category> findByName(String name);
}
