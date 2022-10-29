package com.dev.springboot.webflux.apirest.app.models.service;

import com.dev.springboot.webflux.apirest.app.models.dao.CategoryDao;
import com.dev.springboot.webflux.apirest.app.models.dao.ProductDao;
import com.dev.springboot.webflux.apirest.app.models.documents.Category;
import com.dev.springboot.webflux.apirest.app.models.documents.Product;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {


    private final ProductDao dao;


    private final CategoryDao categoryDao;

    public ProductServiceImpl(ProductDao dao, CategoryDao categoryDao) {
        this.dao = dao;
        this.categoryDao = categoryDao;
    }

    @Override
    public Flux<Product> findAll() {
        return dao.findAll();
    }

    @Override
    public Mono<Product> findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Mono<Product> save(Product product) {
        return dao.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {
        return dao.delete(product);
    }

    @Override
    public Flux<Product> findAllWithNameUpperCase() {
        return dao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        });
    }

    @Override
    public Flux<Product> findAllWithNameUpperCaseRepeat() {
        return findAllWithNameUpperCase().repeat(5000);
    }

    @Override
    public Flux<Category> findAllCategories() {
        return categoryDao.findAll();
    }

    @Override
    public Mono<Category> findCategoryId(String id) {
        return categoryDao.findById(id);
    }

    @Override
    public Mono<Category> saveCategory(Category category) {
        return categoryDao.save(category);
    }

    @Override
    public Mono<Product> findByName(String name) {
        return dao.findByName(name);
    }

    @Override
    public Mono<Category> findByCategoryName(String name) {
        return categoryDao.findByName(name);
    }


}
