package com.efectivale.centrocostos.dto;

import java.util.List;

public record ModuloCoberturaDto(
        String modulo,
        List<String> bases,
        String descripcion
) {
}
