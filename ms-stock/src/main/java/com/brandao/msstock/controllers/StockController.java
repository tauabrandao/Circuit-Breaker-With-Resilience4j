package com.brandao.msstock.controllers;

import com.brandao.msstock.dto.StockDTO;
import com.brandao.msstock.services.StockService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService service;

    private final Environment environment;

    private Logger logger = LoggerFactory.getLogger(StockController.class);

    public StockController(StockService service, Environment environment) {
        this.service = service;
        this.environment = environment;
    }

    @GetMapping("/price/{name}")
    //@Retry(name = "stock-price", fallbackMethod = "getMockStock")
//    @CircuitBreaker(name = "default", fallbackMethod = "getMockStock")
    @RateLimiter(name = "default")
    public ResponseEntity<Object> getStockPrice(@PathVariable String name) throws IOException {

        logger.info("The instance running on port %s was called to fetch the stock details"
                .formatted(environment.getProperty("local.server.port")));

        if(!serviceAvailable()){
            logger.info("unavailable service");
            throw new RuntimeException();
        }

        return ResponseEntity.ok(service.getStockPrice(name));
    }

    //boolean to simulate unavailable service
    public boolean serviceAvailable(){
        return new SecureRandom().nextInt(10) % 2 == 0;
    }

    public ResponseEntity<StockDTO> getMockStock(Exception ex){
        return ResponseEntity.ok(StockDTO.builder().company("Mock company").price(BigDecimal.ZERO).build());
    }
}
