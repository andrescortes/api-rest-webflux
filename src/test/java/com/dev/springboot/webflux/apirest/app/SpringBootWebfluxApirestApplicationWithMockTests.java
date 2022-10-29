package com.dev.springboot.webflux.apirest.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.dev.springboot.webflux.apirest.app.models.documents.Category;
import com.dev.springboot.webflux.apirest.app.models.documents.Product;
import com.dev.springboot.webflux.apirest.app.models.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class SpringBootWebfluxApirestApplicationWithMockTests {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ProductService service;

    @Value("${config.base.endpoint}")
    private String url;

    @Test
    void listTest() {
        client.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Product.class)
            .consumeWith(listEntityExchangeResult -> {
                List<Product> products = listEntityExchangeResult.getResponseBody();
                products.forEach(product -> {
                    System.out.println("product = " + product.getName());
                });
                Assertions.assertTrue(products.size() > 0);
            });
        //.hasSize(14);

    }

    @Test
    void detailTest() {
        //to test app, must be sync not async, about of context Test
        Product product = service.findByName("TV Panasonic Pantalla LCD").block();

        client.get()
            .uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Product.class)
            .consumeWith(response -> {
                Product p = response.getResponseBody();
                Assertions.assertEquals("TV Panasonic Pantalla LCD", p.getName());
                Assertions.assertTrue(p.getId().length() > 0);
            })
            /*.expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.name").isEqualTo("TV Panasonic Pantalla LCD")*/
        ;
    }

    @Test
    void createProductTest() {
        Category category = service.findByCategoryName("Muebles").block();
        Product product = new Product("Mesa Comedor", 100.00, category);
        client.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(product), Product.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.product.id").isNotEmpty()
            .jsonPath("$.product.name").isEqualTo("Mesa Comedor")
            .jsonPath("$.product.category.name").isEqualTo("Muebles");

    }

    @Test
    void create2ProductTest() {
        Category category = service.findByCategoryName("Muebles").block();
        Product product = new Product("Mesa Comedor", 100.00, category);
        client.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(product), Product.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            //.expectBody(Product.class)
            .expectBody(new ParameterizedTypeReference<LinkedHashMap<String,Object>>() {
            })
            .consumeWith(response -> {
                Object o = response.getResponseBody().get("product");
                Product p = new ObjectMapper().convertValue(o, Product.class);
                Assertions.assertNotNull(p.getId());
                assertThat(p.getName()).isEqualTo("Mesa Comedor");
                assertThat(p.getCategory().getName()).isEqualTo("Muebles");

            });
    }

    @Test
    void updateProductTest() {
        Product product = service.findByName("Sony Notebook").block();
        Category category = service.findByCategoryName("Electrónico").block();
        Product productEdited = new Product("Asus Notebook", 700.00, category);
        client.put().uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(productEdited), Product.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.name").isEqualTo("Asus Notebook")
            .jsonPath("$.category.name").isEqualTo("Electrónico");
    }

    @Test
    void deleteProductTest() {
        Product product = service.findByName("Mica Cómoda 5 Cajones").block();

        client.delete()
            .uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
            .exchange()
            .expectStatus().isNoContent()
            .expectBody()
            .isEmpty();

        client.delete()
            .uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .isEmpty();
    }

}
