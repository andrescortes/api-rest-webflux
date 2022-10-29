package com.dev.springboot.webflux.apirest.app.models.service;


import com.dev.springboot.webflux.apirest.app.models.documents.Category;
import com.dev.springboot.webflux.apirest.app.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
	
	public Flux<Product> findAll();
	
	public Flux<Product> findAllWithNameUpperCase();
	
	public Flux<Product> findAllWithNameUpperCaseRepeat();
	
	public Mono<Product> findById(String id);
	
	public Mono<Product> save(Product product);
	
	public Mono<Void> delete(Product product);
	
	public Flux<Category> findAllCategories();
	
	public Mono<Category> findCategoryId(String id);
	
	public Mono<Category> saveCategory(Category category);
	Mono<Product> findByName(String name);
	Mono<Category> findByCategoryName(String name);

}
