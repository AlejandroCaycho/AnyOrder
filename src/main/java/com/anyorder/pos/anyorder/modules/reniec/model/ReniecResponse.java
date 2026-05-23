package com.anyorder.pos.anyorder.modules.reniec.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReniecResponse {
    private boolean success;
    private String source;
    private ReniecData result;
}
