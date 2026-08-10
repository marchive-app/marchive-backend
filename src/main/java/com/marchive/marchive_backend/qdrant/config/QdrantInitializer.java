package com.marchive.marchive_backend.qdrant.config;

import com.marchive.marchive_backend.qdrant.service.QdrantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class QdrantInitializer implements CommandLineRunner {

    private final QdrantService qdrantService;

    public QdrantInitializer(QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    @Override
    public void run(String... args) {
        qdrantService.createCollection();
    }
}
