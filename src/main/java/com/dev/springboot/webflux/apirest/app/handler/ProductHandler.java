package com.dev.springboot.webflux.apirest.app.handler;

import com.dev.springboot.webflux.apirest.app.models.documents.Category;
import com.dev.springboot.webflux.apirest.app.models.documents.Product;
import com.dev.springboot.webflux.apirest.app.models.service.ProductService;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

    private final ProductService service;
    private final Validator validator;
    @Value("${config.uploads.path}")
    private String path;

    public ProductHandler(ProductService service, Validator validator) {
        this.service = service;
        this.validator = validator;
    }

    public Mono<ServerResponse> createWithImage(ServerRequest request) {
        Mono<Product> productMono = request.multipartData()
            .map(multiPart -> {
                FormFieldPart name = (FormFieldPart) multiPart.toSingleValueMap().get("name");
                FormFieldPart price = (FormFieldPart) multiPart.toSingleValueMap().get("price");
                FormFieldPart categoryId = (FormFieldPart) multiPart.toSingleValueMap()
                    .get("category.id");
                FormFieldPart categoryName = (FormFieldPart) multiPart.toSingleValueMap()
                    .get("category.name");
                Category category = new Category(categoryName.value());
                category.setId(categoryId.value());
                return new Product(name.value(), Double.parseDouble(price.value()), category);
            });
        return request.multipartData()
            .map(multipart -> multipart.toSingleValueMap().get("file"))
            .cast(FilePart.class)
            .flatMap(filePart -> productMono
                .flatMap(p -> {
                    p.setFoto(UUID.randomUUID() + "-" + filePart.filename()
                        .replace(" ", "")
                        .replace(":", "")
                        .replace("\\", "")
                    );
                    p.setCreateAt(new Date());
                    return filePart.transferTo(new File(path + p.getFoto())).then(service.save(p));
                })).flatMap(product -> ServerResponse.created(URI.create("api/v2/products/".concat(
                    product.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(product)));
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.multipartData()
            .map(multipart -> multipart.toSingleValueMap().get("file"))
            .cast(FilePart.class)
            .flatMap(filePart -> service.findById(id)
                .flatMap(p -> {
                    p.setFoto(UUID.randomUUID() + "-" + filePart.filename()
                        .replace(" ", "")
                        .replace(":", "")
                        .replace("\\", "")
                    );
                    return filePart.transferTo(new File(path + p.getFoto())).then(service.save(p));
                })).flatMap(product -> ServerResponse.created(URI.create("api/v2/products/".concat(
                    product.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(product))
                .switchIfEmpty(ServerResponse.notFound().build()));
    }


    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(service.findAll(), Product.class);

    }

    public Mono<ServerResponse> detail(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.findById(id)
            .flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(p)))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        return productMono.flatMap(p -> {
            Errors errors = new BeanPropertyBindingResult(p, Product.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                    .map(fieldError -> "The field " + fieldError.getField() + " "
                        + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(
                        list -> ServerResponse.badRequest().body(BodyInserters.fromObject(list)));
            } else {
                if (p.getCreateAt() == null) {
                    p.setCreateAt(new Date());
                }
                return service.save(p).flatMap(pdb -> ServerResponse.created(
                        URI.create("/api/v2/products/".concat(pdb.getId())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromObject(pdb)));
            }
        });
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productMono = request.bodyToMono(Product.class);
        Mono<Product> productDb = service.findById(id);
        return productDb.zipWith(productMono, (db, req) -> {
                db.setName(req.getName());
                db.setCategory(req.getCategory());
                db.setPrice(req.getPrice());
                return db;
            }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.save(p), Product.class))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productDb = service.findById(id);
        return productDb.flatMap(
                product -> service.delete(product).then(ServerResponse.noContent().build()))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

}
