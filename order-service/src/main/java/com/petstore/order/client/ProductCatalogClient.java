package com.petstore.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.order.dto.ProductDto;
import com.petstore.order.dto.StockReservationRequest;
import com.petstore.order.dto.StockReservationResponse;
import com.petstore.order.exception.BadRequestException;
import com.petstore.order.exception.InsufficientStockException;
import com.petstore.order.exception.ResourceNotFoundException;
import com.petstore.order.exception.ServiceUnavailableException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductCatalogClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ProductCatalogClient(RestClient.Builder restClientBuilder,
                                ObjectMapper objectMapper,
                                @Value("${services.catalog.url:http://localhost:8082}") String catalogServiceUrl,
                                @Value("${services.catalog.connect-timeout-ms:3000}") int connectTimeout,
                                @Value("${services.catalog.read-timeout-ms:5000}") int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        this.restClient = restClientBuilder
                .baseUrl(catalogServiceUrl)
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getProductFallback")
    @Retry(name = "catalogService")
    @Bulkhead(name = "catalogService")
    public ProductDto getProduct(Long productId) {
        try {
            String raw = restClient.get()
                    .uri("/internal/products/{id}", productId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        if (resp.getStatusCode().value() == 404) {
                            throw new ResourceNotFoundException("Product", "id", productId);
                        }
                        throw new BadRequestException("Catalog service rejected product lookup for id: " + productId);
                    })
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode dataNode = root.get("data");
            if (dataNode == null || dataNode.isNull()) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }
            return objectMapper.treeToValue(dataNode, ProductDto.class);
        } catch (ResourceNotFoundException | BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to communicate with Catalog Service for product id: {}", productId, ex);
            throw new ServiceUnavailableException("Catalog Service", "Unable to retrieve product details at this time.");
        }
    }

    public ProductDto getProductFallback(Long productId, Throwable t) {
        if (t instanceof ResourceNotFoundException) {
            throw (ResourceNotFoundException) t;
        }
        if (t instanceof BadRequestException) {
            throw (BadRequestException) t;
        }
        logger.warn("Resilience4j fallback triggered for getProduct(id={}): {}", productId, t.getMessage());
        throw new ServiceUnavailableException("Catalog Service", "Catalog service is temporarily unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "reserveStockFallback")
    @Retry(name = "catalogService")
    @Bulkhead(name = "catalogService")
    public StockReservationResponse reserveStock(Long productId, int quantity) {
        try {
            String raw = restClient.post()
                    .uri("/internal/products/{id}/reserve-stock", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new StockReservationRequest(quantity))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        try {
                            String errBody = new String(resp.getBody().readAllBytes());
                            JsonNode errNode = objectMapper.readTree(errBody);
                            String code = errNode.path("code").asText();
                            String message = errNode.path("message").asText();
                            if ("INSUFFICIENT_STOCK".equals(code)) {
                                throw new InsufficientStockException(productId, quantity, 0);
                            }
                            throw new BadRequestException(message != null ? message : "Unable to reserve inventory");
                        } catch (InsufficientStockException | BadRequestException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new BadRequestException("Inventory reservation failed for product id: " + productId);
                        }
                    })
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode dataNode = root.get("data");
            return objectMapper.treeToValue(dataNode, StockReservationResponse.class);
        } catch (InsufficientStockException | BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to communicate with Catalog Service during stock reservation for product {}", productId, ex);
            throw new ServiceUnavailableException("Catalog Service", "Inventory reservation service is temporarily unavailable.");
        }
    }

    public StockReservationResponse reserveStockFallback(Long productId, int quantity, Throwable t) {
        if (t instanceof InsufficientStockException) {
            throw (InsufficientStockException) t;
        }
        if (t instanceof BadRequestException) {
            throw (BadRequestException) t;
        }
        logger.warn("Resilience4j fallback triggered for reserveStock(id={}, qty={}): {}", productId, quantity, t.getMessage());
        throw new ServiceUnavailableException("Catalog Service", "Inventory reservation service is temporarily unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "catalogService")
    @Retry(name = "catalogService")
    public void releaseStock(Long productId, int quantity) {
        try {
            restClient.post()
                    .uri("/internal/products/{id}/release-stock", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new StockReservationRequest(quantity))
                    .retrieve()
                    .toBodilessEntity();
            logger.info("Released stock for product {} (qty: {})", productId, quantity);
        } catch (Exception ex) {
            logger.error("Failed to release stock for product id {}. Manual inventory audit may be required.", productId, ex);
        }
    }
}
