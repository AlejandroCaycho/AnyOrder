package com.anyorder.pos.anyorder.modules.sunat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatResponse {
    private boolean success;
    private String source;
    private SunatData result;
}
