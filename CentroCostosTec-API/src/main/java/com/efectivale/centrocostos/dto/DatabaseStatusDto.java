package com.efectivale.centrocostos.dto;

public record DatabaseStatusDto(
        String base,
        String motor,
        String url,
        boolean conectada,
        String detalle
) {
}
