package com.anyorder.pos.anyorder.modules.sunat.service;

import com.anyorder.pos.anyorder.modules.sunat.model.SunatData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SunatService {

    private final RestTemplate restTemplate;
    private final String url;
    private final String token;

    public SunatService(RestTemplate restTemplate, 
                         @Value("${external.api.sunat.url}") String url, 
                         @Value("${external.api.token}") String token) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.token = token;
    }

    public SunatData consultarRuc(String ruc) {
        if (ruc == null || ruc.length() != 11) {
            throw new IllegalArgumentException("RUC inválido. Debe tener 11 dígitos.");
        }

        String finalUrl = this.url + "/" + ruc;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.info("Consultando SUNAT para RUC: {}", ruc);
            ResponseEntity<SunatData> response = restTemplate.exchange(
                    finalUrl, HttpMethod.GET, entity, SunatData.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("SUNAT retornó estado: {}", response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Token de SUNAT inválido o expirado");
            throw new RuntimeException("Error de autenticación con la API de SUNAT");
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("RUC {} no encontrado en SUNAT", ruc);
            return null;
        } catch (Exception e) {
            log.error("Error al consultar SUNAT: {}", e.getMessage());
            throw new RuntimeException("Error en la consulta a SUNAT: " + e.getMessage());
        }
    }
}
