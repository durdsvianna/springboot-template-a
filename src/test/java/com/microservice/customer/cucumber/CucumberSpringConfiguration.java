package com.microservice.customer.cucumber;

import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.client.CustomerApiClient;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "api.base-url=https://xyz.net/api/v1/enterprise"
})
public class CucumberSpringConfiguration {

    // Mock WebClient to avoid calling the real external API
    @MockBean
    WebClient webClient;
    
    // Mock API clients
    @MockBean
    CustomerApiClient customerApiClient;
    
    @MockBean
    AddressApiClient addressApiClient;
}