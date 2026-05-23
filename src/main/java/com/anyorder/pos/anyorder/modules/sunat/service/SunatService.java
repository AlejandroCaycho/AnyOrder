package com.anyorder.pos.anyorder.modules.sunat.service;

import com.anyorder.pos.anyorder.modules.sunat.model.SunatData;
import com.anyorder.pos.anyorder.modules.sunat.model.SunatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SunatService {

    private final RestTemplate restTemplate;
    private final com.anyorder.pos.anyorder.config.ExternalConfig externalConfig;

    public SunatService(RestTemplate restTemplate, com.anyorder.pos.anyorder.config.ExternalConfig externalConfig) {
        this.restTemplate = restTemplate;
        this.externalConfig = externalConfig;
    }

    public SunatData consultarRuc(String ruc) {
        if (ruc == null || ruc.length() != 11) {
            throw new IllegalArgumentException("RUC inválido. Debe tener 11 dígitos.");
        }

        String finalUrl = externalConfig.getApi().getSunat().getUrl() + "/" + ruc;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + externalConfig.getApi().getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.info("Consultando SUNAT para RUC: {}", ruc);
            ResponseEntity<SunatResponse> response = restTemplate.exchange(
                    finalUrl, HttpMethod.GET, entity, SunatResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getResult();
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
