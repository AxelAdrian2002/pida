package com.efectivale.centrocostos.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.efectivale.centrocostos.dto.DatabaseStatusDto;
import com.efectivale.centrocostos.dto.ModuloCoberturaDto;

@Service
public class DatabaseCoverageService {

    private final JdbcTemplate dbdespensaJdbc;
    private final JdbcTemplate pddespensaJdbc;
    private final JdbcTemplate megadbpedidoJdbc;
    private final JdbcTemplate dbemisJdbc;

    @Value("${spring.datasource.url}")
    private String postgresUrl;

    public DatabaseCoverageService(
            @Qualifier("dbdespensaJdbc") JdbcTemplate dbdespensaJdbc,
            @Qualifier("pddespensaJdbc") JdbcTemplate pddespensaJdbc,
            @Qualifier("megadbpedidoJdbc") JdbcTemplate megadbpedidoJdbc,
            @Qualifier("dbemisJdbc") JdbcTemplate dbemisJdbc) {
        this.dbdespensaJdbc = dbdespensaJdbc;
        this.pddespensaJdbc = pddespensaJdbc;
        this.megadbpedidoJdbc = megadbpedidoJdbc;
        this.dbemisJdbc = dbemisJdbc;
    }

    public List<DatabaseStatusDto> obtenerEstatusBases() {
        return List.of(
            validarPostgres("dbdespensa", "PostgreSQL", postgresUrl, dbdespensaJdbc),
                validarPostgres("pddespensa", "PostgreSQL", postgresUrl, pddespensaJdbc),
                validarPostgres("megadbpedido", "PostgreSQL", postgresUrl, megadbpedidoJdbc),
                validarPostgres("dbemis", "PostgreSQL", postgresUrl, dbemisJdbc)
        );
    }

    public List<ModuloCoberturaDto> obtenerCoberturaModulos() {
        return List.of(
                new ModuloCoberturaDto(
                        "Pedidos",
                        List.of("pddespensa", "megadbpedido"),
                        "Alta y gestion de pedidos (dispersion, stock y tarjeta), prefactura y autorizacion"
                ),
                new ModuloCoberturaDto(
                        "Tarjetas",
                        List.of("dbdespensa", "pddespensa"),
                        "Consulta, activacion/cancelacion y bitacora operativa"
                ),
                new ModuloCoberturaDto(
                        "Grupos",
                        List.of("dbdespensa", "pddespensa"),
                        "Registro de grupos y asignacion de empleados"
                ),
                new ModuloCoberturaDto(
                        "Empleados",
                        List.of("dbdespensa", "pddespensa", "dbemis"),
                        "Actualizacion, sincronizacion operativa y validaciones corporativas"
                ),
                new ModuloCoberturaDto(
                        "Autenticacion",
                        List.of("dbdespensa", "dbemis"),
                        "Credenciales y validaciones de cliente/plataforma"
                )
        );
    }

    private DatabaseStatusDto validarPostgres(String base, String motor, String url, JdbcTemplate jdbc) {
        try {
            jdbc.queryForObject("SELECT CURRENT_TIMESTAMP", Object.class);
            return new DatabaseStatusDto(base, motor, url, true, "Conexion OK");
        } catch (Exception ex) {
            return new DatabaseStatusDto(base, motor, url, false, limpiarMensaje(ex));
        }
    }

    private String limpiarMensaje(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            return "Error de conexion";
        }
        return msg.length() > 180 ? msg.substring(0, 180) + "..." : msg;
    }
}
