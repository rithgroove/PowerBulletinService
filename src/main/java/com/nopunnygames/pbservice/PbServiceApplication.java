package com.nopunnygames.pbservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot entry point for the Power Bulletin canonical data service.
 */
@EnableJpaAuditing
@SpringBootApplication
public class PbServiceApplication {
    /**
     * Creates the application instance.
     */
    public PbServiceApplication() {
    }

    /**
     * Starts pb-service.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PbServiceApplication.class, args);
    }
}
