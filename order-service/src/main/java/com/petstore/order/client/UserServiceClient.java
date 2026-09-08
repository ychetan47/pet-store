package com.petstore.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstore.order.dto.AddressDto;
import com.petstore.order.exception.BadRequestException;
import com.petstore.order.exception.ResourceNotFoundException;
import com.petstore.order.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public UserServiceClient(RestClient.Builder restClientBuilder,
                             ObjectMapper objectMapper,
                             @Value("${services.user.url:http://localhost:8081}") String userServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(userServiceUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    public AddressDto getAddress(Long addressId, Long userId) {
        try {
            String raw = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/addresses/{id}")
                            .queryParam("userId", userId)
                            .build(addressId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        if (resp.getStatusCode().value() == 404) {
                            throw new ResourceNotFoundException("Address", "id", addressId);
                        }
                        throw new BadRequestException("User service rejected address lookup for id: " + addressId);
                    })
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode dataNode = root.get("data");
            if (dataNode == null || dataNode.isNull()) {
                throw new ResourceNotFoundException("Address", "id", addressId);
            }
            return objectMapper.treeToValue(dataNode, AddressDto.class);
        } catch (ResourceNotFoundException | BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to communicate with User Service for address id: {} and user id: {}", addressId, userId, ex);
            throw new ServiceUnavailableException("User Service", "Unable to retrieve delivery address details.");
        }
    }
}
