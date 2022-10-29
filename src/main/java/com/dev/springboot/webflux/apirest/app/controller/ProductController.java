package com.dev.springboot.webflux.apirest.app.controller;

import com.dev.springboot.webflux.apirest.app.models.documents.Product;
import com.dev.springboot.webflux.apirest.app.models.service.ProductService;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    @Value("${config.uploads.path}")
    private String path;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping("/v2")
    public Mono<ResponseEntity<Product>> createProductWithImage(Product product, @RequestPart
    FilePart file) {
        if (product.getCreateAt() == null) {
            product.setCreateAt(new Date());
        }
        product.setFoto(UUID.randomUUID() + "-" + file.filename()
            .replace(" ", "")
            .replace(":", "")
            .replace("\\", ""));
        return file.transferTo(new File(path + product.getFoto())).then(service.save(product))
            .map(p -> ResponseEntity.created(URI.create("/api/products/".concat(product.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Product>> upload(@PathVariable final String id, @RequestPart
    FilePart file) {
        return service.findById(id).flatMap(p -> {
                p.setFoto(UUID.randomUUID() + "-" + file.filename()
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("\\", ""));
                return file.transferTo(new File(path + p.getFoto())).then(service.save(p));
            }).map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> listProducts() {
        return Mono.just(
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> detail(@PathVariable final String id) {
        return service.findById(id).map(p -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(p))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createProduct(
        @Valid @RequestBody Mono<Product> monoProduct) {

        Map<String, Object> response = new HashMap<>();

        return monoProduct.flatMap(product -> {
            if (product.getCreateAt() == null) {
                product.setCreateAt(new Date());
            }
            return service.save(product)
                .map(p -> {
                    response.put("product", p);
                    response.put("message", "Product was created success");
                    response.put("timestamp", new Date());

                    return ResponseEntity.created(URI.create("/api/products/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
                });
        }).onErrorResume(t -> {
            return Mono.just(t).cast(WebExchangeBindException.class)
                .flatMap(e -> Mono.just(e.getFieldErrors())
                    .flatMapMany(errors -> Flux.fromIterable(errors))
                    .map(fieldError -> "The field " + fieldError.getField() + " "
                        + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(list -> {
                        response.put("errors", list);
                        response.put("timestamp", new Date());
                        response.put("status", HttpStatus.BAD_REQUEST.value());
                        return Mono.just(ResponseEntity.badRequest().body(response));
                    })
                );
        });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable final String id,
        @RequestBody final Product product) {
        return service.findById(id).flatMap(p -> {
                p.setName(product.getName());
                p.setPrice(product.getPrice());
                p.setCategory(product.getCategory());
                return service.save(p);
            }).map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable final String id) {
        return service.findById(id).flatMap(product -> service.delete(product)
            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))).defaultIfEmpty(
            new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
