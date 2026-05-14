package com.anyorder.pos.anyorder.modules.reniec.service;

import com.anyorder.pos.anyorder.modules.reniec.model.ReniecData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ReniecService {

    private final RestTemplate restTemplate;
    private final String url;
    private final String token;

    public ReniecService(RestTemplate restTemplate, 
                         @Value("${external.api.reniec.url}") String url, 
                         @Value("${external.api.token}") String token) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.token = token;
    }

    public ReniecData consultarDni(String dni) {
        if (dni == null || dni.length() != 8) {
            throw new IllegalArgumentException("DNI inválido. Debe tener 8 dígitos.");
        }

        String finalUrl = this.url + "/" + dni;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.info("Consultando RENIEC para DNI: {}", dni);
            ResponseEntity<ReniecData> response = restTemplate.exchange(
                    finalUrl, HttpMethod.GET, entity, ReniecData.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("RENIEC retornó estado: {}", response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Token de RENIEC inválido o expirado");
            throw new RuntimeException("Error de autenticación con la API de RENIEC");
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("DNI {} no encontrado en RENIEC", dni);
            return null;
        } catch (Exception e) {
            log.error("Error al consultar RENIEC: {}", e.getMessage());
            throw new RuntimeException("Error en la consulta a RENIEC: " + e.getMessage());
        }
    }
}
