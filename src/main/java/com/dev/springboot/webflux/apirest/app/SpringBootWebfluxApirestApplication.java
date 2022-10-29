package com.dev.springboot.webflux.apirest.app;


import com.dev.springboot.webflux.apirest.app.models.documents.Product;
import com.dev.springboot.webflux.apirest.app.models.documents.Category;
import com.dev.springboot.webflux.apirest.app.models.service.ProductService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {


    private final ProductService service;


    private final ReactiveMongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(
        SpringBootWebfluxApirestApplication.class);

    public SpringBootWebfluxApirestApplication(ProductService service,
        ReactiveMongoTemplate mongoTemplate) {
        this.service = service;
        this.mongoTemplate = mongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
    }

    @Override
    public void run(String... args) {
        mongoTemplate.dropCollection("products").subscribe();
        mongoTemplate.dropCollection("categories").subscribe();

        Category electronico = new Category("Electrónico");
        Category deporte = new Category("Deporte");
        Category computacion = new Category("Computación");
        Category muebles = new Category("Muebles");

        Flux.just(electronico, deporte, computacion, muebles)
            .flatMap(service::saveCategory)
            .doOnNext(c -> log.info("Categoria creada: " + c.getName() + ", Id: " + c.getId())).thenMany(
                Flux.just(new Product("TV Panasonic Pantalla LCD", 456.89, electronico),
                        new Product("Sony Camara HD Digital", 177.89, electronico),
                        new Product("Apple iPod", 46.89, electronico),
                        new Product("Sony Notebook", 846.89, computacion),
                        new Product("Hewlett Packard Multifuncional", 200.89, computacion),
                        new Product("Bianchi Bicicleta", 70.89, deporte),
                        new Product("HP Notebook Omen 17", 2500.89, computacion),
                        new Product("Mica Cómoda 5 Cajones", 150.89, muebles),
                        new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico)
                    )
                    .flatMap(product -> {
                        product.setCreateAt(new Date());
                        return service.save(product);
                    })
            )
            .subscribe(product -> log.info("Insert: " + product.getId() + " " + product.getName()));

    }


}
