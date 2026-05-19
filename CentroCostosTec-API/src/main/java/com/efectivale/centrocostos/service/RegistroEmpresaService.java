package com.efectivale.centrocostos.service;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efectivale.centrocostos.dto.RegistroEmpresaDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegistroEmpresaService {

    private final JdbcTemplate jdbc;
    private final JavaMailSender mailSender;
    private final TenantAuditService tenantAuditService;

    @Value("${app.mail.from:noreply@novahub.mx}")
    private String mailFrom;

    @Value("${app.mail.test-recipient:axel.adrian02@gmail.com}")
    private String testRecipient;

    public RegistroEmpresaService(@Qualifier("dbdespensaJdbc") JdbcTemplate jdbc,
                                  ObjectProvider<JavaMailSender> mailSenderProvider,
                                  TenantAuditService tenantAuditService) {
        this.jdbc = jdbc;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.tenantAuditService = tenantAuditService;
    }

    @Transactional
    public Map<String, String> registrarEmpresa(RegistroEmpresaDto dto) {
        try {

        // 1. Generar código único para la empresa (MAX 20 chars)
        String codigoBase = generarCodigo(dto.getRfc() != null ? dto.getRfc() : dto.getNombreEmpresa());
        String codigoEmpresa = garantizarCodigoUnico(codigoBase);

        // 2. Insertar empresa_operativa
        jdbc.update(
            "INSERT INTO empresa_operativa " +
            "(codigo_empresa, nombre_empresa, activa, color_primario, color_secundario, logo_url, " +
            " razon_social, rfc, email_contacto, telefono_contacto, sitio_web, " +
            " calle, numero_exterior, numero_interior, colonia, municipio, estado, pais, codigo_postal, fecha_modificacion) " +
            "VALUES (?, ?, TRUE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
            codigoEmpresa,
            dto.getNombreEmpresa(),
            dto.getColorPrimario(),
            dto.getColorSecundario(),
            dto.getLogoUrl(),
            dto.getRazonSocial() != null ? dto.getRazonSocial() : dto.getNombreEmpresa(),
            dto.getRfc(),
            dto.getEmailEmpresa(),
            dto.getTelefonoEmpresa(),
            dto.getSitioWeb(),
            dto.getCalle(),
            dto.getNumeroExterior(),
            dto.getNumeroInterior(),
            dto.getColonia(),
            dto.getMunicipio(),
            dto.getEstado(),
            dto.getPais(),
            dto.getCodigoPostal()
        );

        // 3. Insertar unidad_operativa principal con contexto operativo único por empresa
        try {
            // En algunos entornos gestionados el LOCK explícito puede estar restringido.
            jdbc.execute("LOCK TABLE unidad_operativa IN EXCLUSIVE MODE");
        } catch (DataAccessException ex) {
            log.warn("No se pudo aplicar LOCK TABLE unidad_operativa; se continua en modo compatible: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        }
        long siguienteClienteId = obtenerSiguienteClienteId();
        long siguienteConsignatarioId = obtenerSiguienteConsignatarioId();
        String codigoUnidad = codigoEmpresa + "-PRINC";
        jdbc.update(
            "INSERT INTO unidad_operativa (codigo_unidad, codigo_empresa, clienteid, consignatarioid, activa) " +
            "VALUES (?, ?, ?, ?, TRUE)",
            codigoUnidad,
            codigoEmpresa,
            siguienteClienteId,
            siguienteConsignatarioId
        );

        // 4. Generar username del admin
        String adminUsername = generarUsernameAdmin(codigoEmpresa);

        // 5. Hashear contraseña con MD5
        String passwordHash = AuthService.toMd5(dto.getAdminPassword());

        // 6. Insertar usuario_interno (perfilid=1 = ADMIN, email_verificado=TRUE porque él mismo definió la contraseña)
        // Usar RETURNING en PostgreSQL para obtener la clave generada
        Long usuarioId = jdbc.queryForObject(
            "INSERT INTO usuario_interno " +
            "(usuariousr, usuarionombre, usuariopwd, usuariocorreo, usuarioactivo, perfilid, " +
            " corporativoid, centroid, requiere_cambio_password, email_verificado, " +
            " usuariofechacreacion, usuariofechaexpirapwd) " +
            "VALUES (?, ?, ?, ?, TRUE, 1, ?, ?, FALSE, TRUE, NOW(), NOW() + INTERVAL '1 year') " +
            "RETURNING usuarioid",
            Long.class,
            adminUsername,
            dto.getAdminNombre(),
            passwordHash,
            dto.getAdminEmail(),
            codigoEmpresa,
            codigoUnidad
        );

        // 7. Insertar usuario_perfil
        if (usuarioId != null) {
            jdbc.update(
                "INSERT INTO usuario_perfil (usuarioid, fecha_modificacion) VALUES (?, NOW())",
                usuarioId
            );
        }

        // 8. Enviar correo con credenciales
        enviarCredenciales(dto.getAdminEmail(), dto.getAdminNombre(), codigoEmpresa, adminUsername, dto.getAdminPassword());

        log.info("Empresa registrada exitosamente: codigo={} usuario={}", codigoEmpresa, adminUsername);

        tenantAuditService.log(
            "EMPRESA",
            "REGISTRAR_EMPRESA",
            "Empresa registrada por autoservicio: " + codigoEmpresa + " usuarioAdmin=" + adminUsername,
            usuarioId,
            adminUsername,
            codigoEmpresa,
            siguienteClienteId,
            siguienteConsignatarioId
        );

        return Map.of(
            "codigoEmpresa", codigoEmpresa,
            "adminUsername", adminUsername,
            "mensaje", "Empresa registrada. Revisa tu correo con los datos de acceso."
        );
        } catch (DataAccessException ex) {
            String detalle = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            log.error("Fallo SQL en registrarEmpresa: {}", detalle, ex);
            throw ex;
        }
    }

    // -----------------------------------------------------------------------

    private String generarCodigo(String base) {
        // Tomar primeras letras significativas, sin espacios ni caracteres especiales, máx 10
        String limpio = base.toUpperCase()
            .replaceAll("[^A-Z0-9]", "")
            .replaceAll("\\s+", "");
        return limpio.length() > 10 ? limpio.substring(0, 10) : limpio;
    }

    private String garantizarCodigoUnico(String base) {
        String candidato = base;
        int sufijo = 1;
        while (existeCodigoEmpresa(candidato)) {
            candidato = (base.length() > 8 ? base.substring(0, 8) : base) + String.format("%02d", sufijo);
            sufijo++;
        }
        return candidato;
    }

    private boolean existeCodigoEmpresa(String codigo) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM empresa_operativa WHERE codigo_empresa = ?",
            Integer.class, codigo
        );
        return count != null && count > 0;
    }

    private long obtenerSiguienteClienteId() {
        Long max = jdbc.queryForObject(
            "SELECT COALESCE(MAX(clienteid), 0) FROM unidad_operativa",
            Long.class
        );
        return (max == null ? 0L : max) + 1L;
    }

    private long obtenerSiguienteConsignatarioId() {
        Long max = jdbc.queryForObject(
            "SELECT COALESCE(MAX(consignatarioid), 0) FROM unidad_operativa",
            Long.class
        );
        return (max == null ? 0L : max) + 1L;
    }

    private String generarUsernameAdmin(String codigoEmpresa) {
        String base = "admin." + codigoEmpresa.toLowerCase();
        // Truncar a 50 chars
        return base.length() > 48 ? base.substring(0, 48) : base;
    }

    private void enviarCredenciales(String email, String nombre, String codigoEmpresa,
                                     String username, String password) {
        String asunto = "Bienvenido a la Plataforma - Tus datos de acceso";
        String cuerpo = String.format(
            "Hola %s,\n\n" +
            "Tu empresa ha sido registrada exitosamente en la Plataforma Interna.\n\n" +
            "Tus datos de acceso son:\n" +
            "  Corporativo: %s\n" +
            "  Usuario:     %s\n" +
            "  Contraseña:  %s\n\n" +
            "Inicia sesión en: http://localhost:4200/login\n\n" +
            "Por tu seguridad, te recomendamos cambiar tu contraseña después del primer acceso.\n\n" +
            "Saludos,\nEquipo NovaHub",
            nombre, codigoEmpresa, username, password
        );

        if (mailSender != null) {
            try {
                String destinatarioFinal = resolveTestRecipient(email);
                if (!destinatarioFinal.equals(email)) {
                    log.warn("Modo pruebas activo: correo registro redirigido a {} (solicitado={})", destinatarioFinal, email);
                }
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(mailFrom);
                msg.setTo(destinatarioFinal);
                msg.setSubject(asunto);
                msg.setText(cuerpo);
                mailSender.send(msg);
                log.info("Correo de credenciales enviado a {} (solicitado={})", destinatarioFinal, email);
            } catch (MailException ex) {
                log.warn("No se pudo enviar el correo a {}: {}. Imprimiendo credenciales en log.", email, ex.getMessage());
                log.info("=== CREDENCIALES EMPRESA === Corporativo: {} | Usuario: {} | Email: {}", codigoEmpresa, username, email);
            }
        } else {
            log.info("=== CREDENCIALES EMPRESA (sin SMTP) === Corporativo: {} | Usuario: {} | Email: {}", codigoEmpresa, username, email);
        }
    }

    private String resolveTestRecipient(String emailSolicitado) {
        if (testRecipient == null || testRecipient.isBlank()) {
            return emailSolicitado;
        }
        return testRecipient.trim();
    }
}
