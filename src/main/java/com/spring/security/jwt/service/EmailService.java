package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.PrestamoActivoDto;
import com.spring.security.jwt.model.CorreoNotificacionModel;
import com.spring.security.jwt.repository.CorreoNotificacionRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de correo HTML.
 * Diseño híbrido: estructura de cuadrícula (AWS-inspired) con la paleta oscura
 * original de la aplicación. Tono amigable y readable.
 * Compatible con Outlook, Gmail y Apple Mail (solo inline styles, sin CSS externo).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final CorreoNotificacionRepository correoRepository;

    @Value("${spring.mail.username}")
    private String remitente;

    // ── Paleta original de la aplicación ──────────────────────────────────────
    private static final String C_BODY    = "#07091e"; // fondo profundo
    private static final String C_CARD    = "#111840"; // card principal
    private static final String C_INNER   = "#0d1435"; // secciones internas
    private static final String C_BORDER  = "#1e2a6a"; // bordes sutiles
    private static final String C_ACCENT  = "#5577ff"; // azul acento
    private static final String C_TEXT    = "#dde8ff"; // texto principal
    private static final String C_MUTED   = "#6a7db0"; // texto secundario
    private static final String C_CAPTION = "#8899cc"; // etiquetas
    // Acentos semánticos
    private static final String C_BLUE    = "#4d7ef7";
    private static final String C_GREEN   = "#34d399";
    private static final String C_ORANGE  = "#fb923c";
    private static final String C_RED     = "#f87171";
    // Badges turno
    private static final String C_MAT_BG     = "#2a1f00";
    private static final String C_MAT_TEXT   = "#fde68a";
    private static final String C_MAT_BORDER = "#7a5a00";
    private static final String C_VESP_BG    = "#2a1200";
    private static final String C_VESP_TEXT  = "#fed7aa";
    private static final String C_VESP_BORDER= "#9a3412";
    private static final String C_NOC_BG     = "#180f38";
    private static final String C_NOC_TEXT   = "#d8b4fe";
    private static final String C_NOC_BORDER = "#5b2db0";
    // Badges estado
    private static final String C_ON_BG     = "#052314";
    private static final String C_ON_TEXT   = "#6ee7b7";
    private static final String C_ON_BORDER = "#065f46";
    private static final String C_OFF_BG    = "#2d0707";
    private static final String C_OFF_TEXT  = "#fca5a5";
    private static final String C_OFF_BORDER= "#7f1d1d";

    // ── Public API ──────────────────────────────────────────────────────────────

    public void enviarRecordatorioPrestamo(String nombreEmpleado, String nombreHerramienta,
                                           String turno, LocalDate fecha,
                                           int totalUnidades, int disponibles, int prestadas) {
        List<CorreoNotificacionModel> destinatarios = correoRepository.findByBoActivoTrueAndBoBitacoraTrue();
        String asunto = "Prestamo registrado: " + nombreHerramienta;
        for (CorreoNotificacionModel dest : destinatarios) {
            String html = buildHtmlPrestamo(dest.getDsNombre(), nombreEmpleado, nombreHerramienta,
                    turno, fecha, totalUnidades, disponibles, prestadas);
            enviar(asunto, html, dest.getDsCorreo(), nombreEmpleado, nombreHerramienta, turno);
        }
    }

    public void enviarRecordatorioFinTurno(String nombreEmpleado, String nombreHerramienta,
                                           String turno, LocalDate fecha,
                                           DashboardDto dashboard) {
        List<CorreoNotificacionModel> destinatarios = correoRepository.findByBoActivoTrueAndBoRecordatoriosTrue();
        String asunto = "Accion requerida \u2013 " + nombreHerramienta + " pendiente de devolucion";
        for (CorreoNotificacionModel dest : destinatarios) {
            String html = buildHtmlFinTurno(dest.getDsNombre(), nombreEmpleado, nombreHerramienta,
                    turno, fecha, dashboard);
            enviar(asunto, html, dest.getDsCorreo(), nombreEmpleado, nombreHerramienta, turno);
        }
    }

    // ── Send ───────────────────────────────────────────────────────────────────

    private void enviar(String asunto, String html, String correo, String emp, String herr, String turno) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(correo);
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Correo enviado a={} empleado={} herramienta={} turno={}", correo, emp, herr, turno);
        } catch (MessagingException | MailException e) {
            log.error("Error al enviar correo a={} empleado={} herramienta={}: {}", correo, emp, herr, e.getMessage());
        }
    }

    // ── HTML Composers ─────────────────────────────────────────────────────────

    private String buildHtmlPrestamo(String destNombre, String emp, String herr, String turno,
                                      LocalDate fecha, int total, int disponibles, int prestadas) {
        StringBuilder sb = new StringBuilder();
        abrirFrame(sb);
        header(sb,
            "&#128230; Prestamo Registrado",
            "Hola, " + destNombre + ". Se ha registrado un nuevo prestamo en el sistema.",
            false);
        kpis(sb, total, disponibles, prestadas);
        infoCard(sb, emp, herr, turno, fecha);
        footer(sb, "Este mensaje confirma que el prestamo fue registrado correctamente en el Sistema BT.");
        cerrarFrame(sb);
        return sb.toString();
    }

    private String buildHtmlFinTurno(String destNombre, String emp, String herr, String turno,
                                      LocalDate fecha, DashboardDto dashboard) {
        int total     = dashboard == null ? 0 : dashboard.getTotalUnidades();
        int disp      = dashboard == null ? 0 : dashboard.getTotalDisponibles();
        int prestadas = dashboard == null ? 0 : dashboard.getTotalPrestadas();
        List<PrestamoActivoDto> activos = dashboard == null ? List.of() : dashboard.getPrestamosActivos();

        StringBuilder sb = new StringBuilder();
        abrirFrame(sb);
        header(sb,
            "&#9203; Accion Requerida",
            destNombre + ", hay una herramienta pendiente de devolucion en los proximos 30 minutos.",
            true);
        alertaBanner(sb, emp, herr, turno);
        kpis(sb, total, disp, prestadas);
        tablaActivos(sb, activos);
        footer(sb, "Recuerda: devolver la herramienta a tiempo ayuda a que todos puedan usarla. ¡Gracias!");
        cerrarFrame(sb);
        return sb.toString();
    }

    // ── Frame ──────────────────────────────────────────────────────────────────

    private void abrirFrame(StringBuilder sb) {
        sb.append("<!DOCTYPE html><html lang=\"es\"><head>")
          .append("<meta charset=\"UTF-8\">")
          .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
          .append("<title>Sistema BT</title></head>")
          .append("<body style=\"margin:0;padding:0;background-color:").append(C_BODY)
          .append(";font-family:Arial,Helvetica,sans-serif;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"")
          .append(" style=\"background-color:").append(C_BODY).append(";\">")
          .append("<tr><td align=\"center\" style=\"padding:36px 16px;\">")
          // Card central — borde sutil, esquinas redondeadas
          .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"")
          .append(" style=\"max-width:600px;width:100%;background-color:").append(C_CARD)
          .append(";border:1px solid ").append(C_BORDER)
          .append(";border-radius:12px;overflow:hidden;\">")
          .append("<tr><td>");
    }

    private void cerrarFrame(StringBuilder sb) {
        sb.append("</td></tr></table></td></tr></table></body></html>");
    }

    // ── Secciones ──────────────────────────────────────────────────────────────

    /**
     * Header: barra de acento 4 px, fondo oscuro, logo + título + subtítulo amigable.
     */
    private void header(StringBuilder sb, String titulo, String subtitulo, boolean esAlerta) {
        String acento = esAlerta ? C_RED : C_ACCENT;
        String bgSub  = esAlerta ? "#120a0a" : "#0c1238";

        sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          // Barra de acento superior
          .append("<tr><td style=\"background-color:").append(acento)
          .append(";height:4px;font-size:0;\"> </td></tr>")
          // Logo bar — centrado
          .append("<tr><td style=\"background-color:").append(C_INNER)
          .append(";padding:16px 28px;text-align:center;\">")
          .append("<span style=\"font-size:13px;font-weight:bold;color:").append(C_ACCENT)
          .append(";letter-spacing:3px;text-transform:uppercase;\">&#11835; Sistema BT &#11835;</span>")
          .append("</td></tr>")
          // Título y subtítulo — centrados
          .append("<tr><td style=\"background-color:").append(bgSub)
          .append(";padding:24px 28px 28px;text-align:center;\">")
          .append("<p style=\"margin:0 0 10px;font-size:23px;font-weight:bold;color:")
          .append(C_TEXT).append(";letter-spacing:-0.3px;\">").append(titulo).append("</p>")
          .append("<p style=\"margin:0;font-size:13px;color:").append(C_MUTED).append(";\">")
          .append(subtitulo).append("</p>")
          .append("</td></tr>")
          // Divisor de acento
          .append("<tr><td style=\"background-color:").append(acento)
          .append(";height:1px;font-size:0;\"> </td></tr>")
          .append("</table>");
    }

    /**
     * Banner de alerta — borde izquierdo rojo, fondo oscuro rojizo.
     * Tono directo pero amigable.
     */
    private void alertaBanner(StringBuilder sb, String emp, String herr, String turno) {
        String icono = "NOCTURNO".equalsIgnoreCase(turno)   ? "&#127769;"
                     : "VESPERTINO".equalsIgnoreCase(turno) ? "&#127749;&#65039;"
                     : "&#9728;&#65039;";

        sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"padding:16px 28px 8px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>")
          .append("<td style=\"border-left:4px solid ").append(C_RED)
          .append(";background-color:#1f0c0c;border-radius:0 8px 8px 0;padding:14px 18px;\">")
          .append("<p style=\"margin:0 0 4px;font-size:12px;font-weight:bold;color:")
          .append(C_RED).append(";text-transform:uppercase;letter-spacing:1px;\">")
          .append("Herramienta pendiente de devolucion</p>")
          .append("<p style=\"margin:0;font-size:13px;color:").append(C_TEXT).append(";\">")
          .append("El turno ").append(icono).append(" <strong style=\"color:#ffffff;\">")
          .append(turno).append("</strong> de <strong style=\"color:#ffffff;\">").append(emp)
          .append("</strong> termina pronto y la herramienta <strong style=\"color:#ffffff;\">")
          .append(herr).append("</strong> aun no fue devuelta.")
          .append("</p>")
          .append("</td></tr></table>")
          .append("</td></tr></table>");
    }

    /**
     * Tres KPI cards en fila — estructura AWS: borde superior de color,
     * cuerpo con fondo oscuro, número grande y etiqueta.
     */
    private void kpis(StringBuilder sb, int total, int disponibles, int prestadas) {
        sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"padding:20px 28px 8px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        kpiCard(sb, C_BLUE,   "&#128230;", total,       "Total");
        sb.append("<td width=\"10\" style=\"font-size:0;\"> </td>");
        kpiCard(sb, C_GREEN,  "&#9989;",   disponibles, "Disponibles");
        sb.append("<td width=\"10\" style=\"font-size:0;\"> </td>");
        kpiCard(sb, C_ORANGE, "&#10145;",  prestadas,   "Prestadas");

        sb.append("</tr></table></td></tr></table>");
    }

    private void kpiCard(StringBuilder sb, String acento, String icono, int valor, String etiqueta) {
        sb.append("<td style=\"width:33%;vertical-align:top;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          // Borde superior de color
          .append("<tr><td style=\"background-color:").append(acento)
          .append(";height:3px;border-radius:6px 6px 0 0;font-size:0;\"> </td></tr>")
          // Cuerpo de la card
          .append("<tr><td style=\"background-color:").append(C_INNER)
          .append(";border:1px solid ").append(C_BORDER)
          .append(";border-top:none;border-radius:0 0 6px 6px;padding:16px 12px;text-align:center;\">")
          .append("<p style=\"margin:0 0 4px;font-size:22px;line-height:1;\">").append(icono).append("</p>")
          .append("<p style=\"margin:6px 0 2px;font-size:28px;font-weight:bold;color:")
          .append(acento).append(";line-height:1;\">").append(valor).append("</p>")
          .append("<p style=\"margin:0;font-size:10px;color:").append(C_CAPTION)
          .append(";text-transform:uppercase;letter-spacing:1px;\">").append(etiqueta).append("</p>")
          .append("</td></tr>")
          .append("</table></td>");
    }

    /**
     * Panel de detalle del préstamo — header de sección con icono,
     * filas alternas de datos con borde sutil.
     */
    private void infoCard(StringBuilder sb, String emp, String herr, String turno, LocalDate fecha) {
        sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"padding:12px 28px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"")
          .append(" style=\"border:1px solid ").append(C_BORDER).append(";border-radius:8px;overflow:hidden;\">")
          // Header de sección
          .append("<tr><td style=\"background-color:").append(C_INNER)
          .append(";padding:10px 18px;border-bottom:1px solid ").append(C_BORDER).append(";\">")
          .append("<span style=\"font-size:11px;font-weight:bold;color:").append(C_ACCENT)
          .append(";text-transform:uppercase;letter-spacing:2px;\">&#128203; Detalle del Prestamo</span>")
          .append("</td></tr>")
          // Filas
          .append("<tr><td>")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");

        filaDetalle(sb, "Empleado",    txt(emp,              C_TEXT,    "14px", true),  false);
        filaDetalle(sb, "Herramienta", txt(herr,             C_TEXT,    "14px", true),  true);
        filaDetalle(sb, "Turno",       badgeTurno(turno),                               false);
        filaDetalle(sb, "Fecha",       txt(fecha.toString(), C_CAPTION, "13px", false), true);

        sb.append("</table></td></tr></table></td></tr></table>");
    }

    /**
     * Tabla de préstamos activos — header de sección, encabezados de columna,
     * filas alternas, punto semáforo al final de cada fila.
     */
    private void tablaActivos(StringBuilder sb, List<PrestamoActivoDto> activos) {
        sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"padding:8px 28px 16px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"")
          .append(" style=\"border:1px solid ").append(C_BORDER).append(";border-radius:8px;overflow:hidden;\">")
          // Header de sección
          .append("<tr><td style=\"background-color:").append(C_INNER)
          .append(";padding:10px 18px;border-bottom:1px solid ").append(C_BORDER).append(";\">")
          .append("<span style=\"font-size:11px;font-weight:bold;color:").append(C_ACCENT)
          .append(";text-transform:uppercase;letter-spacing:2px;\">")
          .append("&#128336; Prestamos Activos &nbsp;")
          .append("<span style=\"background-color:#1e2a6a;color:").append(C_ACCENT)
          .append(";font-size:11px;padding:1px 8px;border-radius:10px;\">")
          .append(activos.size()).append("</span>")
          .append("</span></td></tr>")
          // Encabezados de columna
          .append("<tr style=\"background-color:").append(C_INNER).append(";\">")
          .append(thCol("",      "left",   "Empleado"))
          .append(thCol("",      "left",   "Herramienta"))
          .append(thCol("90px",  "center", "Turno"))
          .append(thCol("100px", "center", "Estado"))
          .append(thCol("42px",  "center", "Dias"))
          .append(thCol("22px",  "center", ""))
          .append("</tr>");

        if (activos.isEmpty()) {
            sb.append("<tr><td colspan=\"6\" style=\"padding:22px;text-align:center;color:")
              .append(C_MUTED).append(";font-size:13px;background-color:").append(C_CARD).append(";\">")
              .append("&#10003; No hay prestamos pendientes en este momento.</td></tr>");
        } else {
            boolean zebra = false;
            for (PrestamoActivoDto p : activos) {
                boolean vencida = !p.isTurnoActivo();
                String  bgFila  = vencida ? "#160505" : (zebra ? C_INNER : C_CARD);
                String  bIzq    = vencida ? "border-left:3px solid " + C_RED + ";" : "border-left:3px solid transparent;";
                String  colSem  = colorSemaforo(p.getAlerta());

                sb.append("<tr style=\"background-color:").append(bgFila).append(";\">")
                  .append("<td style=\"").append(bIzq).append(tdStyle("left")).append("color:").append(C_TEXT).append(";font-size:13px;\">")
                  .append(p.getNombreEmpleado()).append("</td>")
                  .append(tdCol(p.getNombreHerramienta(), C_CAPTION, "13px", "left"))
                  .append("<td style=\"").append(tdStyle("center")).append("\">").append(badgeTurno(p.getTurno())).append("</td>")
                  .append("<td style=\"").append(tdStyle("center")).append("\">").append(badgeEstado(p.isTurnoActivo())).append("</td>")
                  .append(tdCol(p.getDiasPrestado() + " d", C_CAPTION, "12px", "center"))
                  .append("<td style=\"").append(tdStyle("center")).append("\">")
                  .append("<span style=\"color:").append(colSem).append(";font-size:16px;\">&#9679;</span></td>")
                  .append("</tr>");
                zebra = !zebra;
            }
        }

        sb.append("</table></td></tr></table>");
    }

    /**
     * Footer — divisor sutil, nota amigable, barra de acento inferior.
     */
    private void footer(StringBuilder sb, String nota) {
        sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"padding:20px 28px;border-top:1px solid ").append(C_BORDER).append(";\">")
          .append("<p style=\"margin:0 0 6px;font-size:13px;color:").append(C_CAPTION).append(";\">")
          .append(nota).append("</p>")
          .append("<p style=\"margin:0;font-size:11px;color:").append(C_MUTED).append(";\">")
          .append("Generado automaticamente &mdash; Sistema BT &bull; No responder a este correo.")
          .append("</p></td></tr>")
          // Barra inferior de acento
          .append("<tr><td style=\"background-color:").append(C_ACCENT)
          .append(";height:3px;font-size:0;\"> </td></tr>")
          .append("</table>");
    }

    // ── Micro-helpers ───────────────────────────────────────────────────────────

    private void filaDetalle(StringBuilder sb, String label, String valueHtml, boolean altRow) {
        String bg = altRow ? C_INNER : C_CARD;
        sb.append("<tr style=\"background-color:").append(bg).append(";\">")
          .append("<td style=\"color:").append(C_MUTED)
          .append(";font-size:12px;width:130px;padding:10px 18px;vertical-align:middle;")
          .append("border-bottom:1px solid ").append(C_BORDER).append(";\">")
          .append(label).append("</td>")
          .append("<td style=\"padding:10px 18px;vertical-align:middle;")
          .append("border-bottom:1px solid ").append(C_BORDER).append(";\">")
          .append(valueHtml).append("</td>")
          .append("</tr>");
    }

    private String txt(String valor, String color, String size, boolean bold) {
        String w = bold ? "font-weight:bold;" : "";
        return "<span style=\"color:" + color + ";font-size:" + size + ";" + w + "\">" + valor + "</span>";
    }

    private String thCol(String width, String align, String texto) {
        String w = width.isBlank() ? "" : "width:" + width + ";";
        return "<th style=\"" + w + "padding:8px 12px;font-size:10px;color:" + C_ACCENT
                + ";text-align:" + align + ";border-bottom:1px solid " + C_BORDER
                + ";letter-spacing:1px;text-transform:uppercase;font-weight:bold;\">" + texto + "</th>";
    }

    private String tdStyle(String align) {
        return "padding:9px 12px;border-bottom:1px solid " + C_BORDER + ";text-align:" + align + ";";
    }

    private String tdCol(String valor, String color, String size, String align) {
        return "<td style=\"" + tdStyle(align) + "color:" + color + ";font-size:" + size + ";\">"
                + valor + "</td>";
    }

    // ── Badges ─────────────────────────────────────────────────────────────────

    private String badgeTurno(String turno) {
        if ("NOCTURNO".equalsIgnoreCase(turno)) {
            return pill("&#127769; Nocturno",  C_NOC_BG,  C_NOC_TEXT,  C_NOC_BORDER);
        }
        if ("VESPERTINO".equalsIgnoreCase(turno)) {
            return pill("&#127749; Vespertino", C_VESP_BG, C_VESP_TEXT, C_VESP_BORDER);
        }
        return pill("&#9728; Matutino", C_MAT_BG, C_MAT_TEXT, C_MAT_BORDER);
    }

    private String badgeEstado(boolean turnoActivo) {
        if (turnoActivo) {
            return pill("&#9654; En curso",   C_ON_BG,  C_ON_TEXT,  C_ON_BORDER);
        }
        return pill("&#9711; Finalizado", C_OFF_BG, C_OFF_TEXT, C_OFF_BORDER);
    }

    private String pill(String texto, String bg, String color, String borde) {
        return "<span style=\"display:inline-block;background-color:" + bg
                + ";color:" + color + ";border:1px solid " + borde
                + ";padding:3px 10px;border-radius:6px;"
                + "font-size:11px;font-weight:bold;white-space:nowrap;\">"
                + texto + "</span>";
    }

    private String colorSemaforo(String alerta) {
        return switch (alerta) {
            case "AMARILLO" -> C_ORANGE;
            case "ROJO"     -> C_RED;
            default         -> C_GREEN;
        };
    }
}
