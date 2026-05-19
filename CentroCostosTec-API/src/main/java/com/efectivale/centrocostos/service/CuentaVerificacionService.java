package com.efectivale.centrocostos.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CuentaVerificacionService {

    private final JdbcTemplate jdbc;
    private final JavaMailSender mailSender;

    @Value("${app.account-verification.base-url}")
    private String verificationBaseUrl;

    @Value("${app.account-verification.token-hours:24}")
    private long tokenHours;

    @Value("${app.mail.test-recipient:axel.adrian02@gmail.com}")
    private String testRecipient;

    public CuentaVerificacionService(@Qualifier("dbdespensaJdbc") JdbcTemplate jdbc,
                                     ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.jdbc = jdbc;
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    @Transactional
    public void registrarYEnviarVerificacion(Long usuarioId, String correo, String username, String corporativoId) {
        if (correo == null || correo.isBlank()) {
            log.warn("Usuario {} sin correo, no se pudo enviar verificacion", username);
            return;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiracion = LocalDateTime.now().plusHours(tokenHours);

        jdbc.update("UPDATE usuario_verificacion SET usado = TRUE, fecha_uso = NOW() WHERE usuarioid = ? AND usado = FALSE", usuarioId);
        jdbc.update(
            "INSERT INTO usuario_verificacion (usuarioid, correo, token, fecha_expiracion, usado, fecha_creacion) VALUES (?, ?, ?, ?, FALSE, NOW())",
            usuarioId,
            correo,
            token,
            expiracion
        );

        String link = verificationBaseUrl + "?token=" + token;
        enviarCorreo(correo, username, corporativoId, link);
    }

    @Transactional
    public Map<String, Object> verificarCuenta(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token de verificacion invalido");
        }

        Map<String, Object> data = jdbc.query(
            "SELECT uv.usuarioid, uv.correo, uv.usado, uv.fecha_expiracion, ui.usuariousr "
                + "FROM usuario_verificacion uv "
                + "JOIN usuario_interno ui ON ui.usuarioid = uv.usuarioid "
                + "WHERE uv.token = ?",
            rs -> rs.next() ? Map.of(
                "usuarioid", rs.getLong("usuarioid"),
                "correo", rs.getString("correo"),
                "usado", rs.getBoolean("usado"),
                "expira", rs.getTimestamp("fecha_expiracion").toLocalDateTime(),
                "username", rs.getString("usuariousr")
            ) : null,
            token
        );

        if (data == null) {
            throw new IllegalArgumentException("No se encontro una verificacion valida para el token enviado");
        }

        if ((Boolean) data.get("usado")) {
            throw new IllegalArgumentException("El token ya fue utilizado");
        }

        LocalDateTime expira = (LocalDateTime) data.get("expira");
        if (expira.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token de verificacion ha expirado");
        }

        Long usuarioId = (Long) data.get("usuarioid");
        jdbc.update("UPDATE usuario_interno SET email_verificado = TRUE, usuariofechamodificacion = NOW() WHERE usuarioid = ?", usuarioId);
        jdbc.update("UPDATE usuario_verificacion SET usado = TRUE, fecha_uso = NOW() WHERE token = ?", token);

        return Map.of(
            "verificado", true,
            "username", data.get("username"),
            "correo", data.get("correo")
        );
    }

    private void enviarCorreo(String destino, String username, String corporativoId, String link) {
        if (mailSender == null) {
            log.warn("JavaMailSender no configurado. Link de verificacion para {}: {}", username, link);
            return;
        }

        String destinatarioFinal = resolveTestRecipient(destino);
        if (!destinatarioFinal.equals(destino)) {
            log.warn("Modo pruebas activo: correo verificacion redirigido a {} (solicitado={})", destinatarioFinal, destino);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatarioFinal);
        message.setSubject("Verifica tu cuenta - " + corporativoId);
        message.setText(
            "Hola " + username + ",\n\n"
                + "Tu cuenta fue creada. Verificala para iniciar sesion:\n"
                + link + "\n\n"
                + "El enlace expira en " + tokenHours + " horas."
        );
        mailSender.send(message);
        log.info("Correo de verificacion enviado a {} (solicitado={})", destinatarioFinal, destino);
    }

    public void enviarCredencialesAcceso(String destino, String username, String passwordPlano, String corporativoId) {
        if (destino == null || destino.isBlank()) {
            log.warn("No se envio correo de credenciales para {} porque no tiene email", username);
            return;
        }

        if (mailSender == null) {
            log.warn("JavaMailSender no configurado. Credenciales {} -> corp={}, user={}, pass={}",
                destino, corporativoId, username, passwordPlano);
            return;
        }

        String destinatarioFinal = resolveTestRecipient(destino);
        if (!destinatarioFinal.equals(destino)) {
            log.warn("Modo pruebas activo: correo credenciales redirigido a {} (solicitado={})", destinatarioFinal, destino);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatarioFinal);
        message.setSubject("Credenciales de acceso - " + corporativoId);
        message.setText(
            "Hola " + username + ",\n\n"
                + "Tu cuenta de acceso ha sido creada.\n\n"
                + "Corporativo: " + corporativoId + "\n"
                + "Usuario: " + username + "\n"
                + "Contraseña temporal: " + passwordPlano + "\n\n"
                + "Puedes iniciar sesión directamente en la plataforma.\n"
                + "Por seguridad, cambia tu contraseña al ingresar."
        );
        mailSender.send(message);
        log.info("Correo de credenciales enviado a {} (solicitado={})", destinatarioFinal, destino);
    }

    private String resolveTestRecipient(String destino) {
        if (testRecipient == null || testRecipient.isBlank()) {
            return destino;
        }
        return testRecipient.trim();
    }
}
