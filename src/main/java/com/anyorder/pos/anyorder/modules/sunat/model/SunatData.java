package com.anyorder.pos.anyorder.modules.sunat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SunatData {

    @JsonProperty("razon_social")
    private String businessName;

    @JsonProperty("numero_documento")
    private String documentNumber;

    @JsonProperty("estado")
    private String status;

    @JsonProperty("condicion")
    private String condition;

    @JsonProperty("direccion")
    private String address;

    @JsonProperty("distrito")
    private String district;

    @JsonProperty("provincia")
    private String province;

    @JsonProperty("departamento")
    private String department;

    @JsonProperty("tipo")
    private String companyType;

    @JsonProperty("actividad_economica")
    private String economicActivity;
}
