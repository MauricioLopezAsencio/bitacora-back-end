package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.PrestamoActivoDto;
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
 * Todos los colores son HEX sólido para compatibilidad con Outlook, Gmail y Apple Mail.
 * El efecto "vidrio/transparente" se logra usando tonos intermedios entre
 * el fondo (#07091e) y colores más claros, con bordes de acento brillantes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.destinatario}")
    private String destinatario;

    @Value("${spring.mail.username}")
    private String remitente;

    // ── Paleta ─────────────────────────────────────────────────────────────────
    private static final String C_BODY        = "#07091e"; // fondo profundo
    private static final String C_GLASS       = "#111840"; // card "vidrio" sobre C_BODY
    private static final String C_GLASS_INNER = "#0d1435"; // interior de card (más oscuro)
    private static final String C_GLOW        = "#3355e0"; // borde glow azul
    private static final String C_ACCENT      = "#5577ff"; // acento azul brillante
    private static final String C_TEXT        = "#dde8ff"; // texto principal
    private static final String C_MUTED       = "#6a7db0"; // texto secundario
    private static final String C_CAPTION     = "#8899cc"; // etiquetas pequeñas
    // Acentos por tipo
    private static final String C_BLUE_ACC    = "#4d7ef7";
    private static final String C_GREEN_ACC   = "#34d399";
    private static final String C_ORANGE_ACC  = "#fb923c";
    private static final String C_RED_ACC     = "#f87171";
    // Badges
    private static final String C_DIA_BG      = "#2a1f00";
    private static final String C_DIA_TEXT    = "#fde68a";
    private static final String C_DIA_BORDER  = "#7a5a00";
    private static final String C_NOCHE_BG    = "#180f38";
    private static final String C_NOCHE_TEXT  = "#d8b4fe";
    private static final String C_NOCHE_BORDER= "#5b2db0";
    private static final String C_ON_BG       = "#052314";
    private static final String C_ON_TEXT     = "#6ee7b7";
    private static final String C_ON_BORDER   = "#065f46";
    private static final String C_OFF_BG      = "#2d0707";
    private static final String C_OFF_TEXT    = "#fca5a5";
    private static final String C_OFF_BORDER  = "#7f1d1d";

    // ── Public API ──────────────────────────────────────────────────────────────

    public void enviarRecordatorioPrestamo(String nombreEmpleado, String nombreHerramienta,
                                           String turno, LocalDate fecha,
                                           int totalUnidades, int disponibles, int prestadas) {
        enviar("Nuevo prestamo registrado - " + nombreHerramienta,
                buildHtmlPrestamo(nombreEmpleado, nombreHerramienta, turno, fecha,
                        totalUnidades, disponibles, prestadas),
                nombreEmpleado, nombreHerramienta, turno);
    }

    public void enviarRecordatorioFinTurno(String nombreEmpleado, String nombreHerramienta,
                                           String turno, LocalDate fecha,
                                           DashboardDto dashboard) {
        enviar("Recupera tu herramienta - " + nombreHerramienta + " | Turno termina en 30 min",
                buildHtmlFinTurno(nombreEmpleado, nombreHerramienta, turno, fecha, dashboard),
                nombreEmpleado, nombreHerramienta, turno);
    }

    // ── Send ───────────────────────────────────────────────────────────────────

    private void enviar(String asunto, String html, String emp, String herr, String turno) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Correo enviado empleado={} herramienta={} turno={}", emp, herr, turno);
        } catch (MessagingException | MailException e) {
            log.error("Error al enviar correo empleado={} herramienta={}: {}", emp, herr, e.getMessage());
        }
    }

    // ── HTML Composers ─────────────────────────────────────────────────────────

    private String buildHtmlPrestamo(String emp, String herr, String turno, LocalDate fecha,
                                      int total, int disponibles, int prestadas) {
        StringBuilder sb = new StringBuilder();
        abrirFrame(sb);
        encabezado(sb, "&#128230; Nuevo Prestamo", "Herramienta asignada correctamente", false);
        kpis(sb, total, disponibles, prestadas);
        infoCard(sb, emp, herr, turno, fecha);
        pie(sb, "Este correo confirma el registro del prestamo en el sistema BT.");
        cerrarFrame(sb);
        return sb.toString();
    }

    private String buildHtmlFinTurno(String emp, String herr, String turno, LocalDate fecha,
                                      DashboardDto dashboard) {
        int total     = dashboard == null ? 0 : dashboard.getTotalUnidades();
        int disp      = dashboard == null ? 0 : dashboard.getTotalDisponibles();
        int prestadas = dashboard == null ? 0 : dashboard.getTotalPrestadas();
        List<PrestamoActivoDto> activos = dashboard == null ? List.of() : dashboard.getPrestamosActivos();

        StringBuilder sb = new StringBuilder();
        abrirFrame(sb);
        encabezado(sb, "&#9201; Recupera tu Herramienta", "Tu turno termina en 30 minutos", true);
        alertaBanner(sb, emp, herr, turno);
        kpis(sb, total, disp, prestadas);
        tablaActivos(sb, activos);
        pie(sb, "Quedan 30 minutos para que finalice tu turno. Por favor devuelve la herramienta a tiempo.");
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
          .append("<tr><td align=\"center\" style=\"padding:40px 16px;\">")
          // Contenedor con borde glow (tabla exterior = glow, interior = card)
          .append("<table width=\"622\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"")
          .append(" style=\"max-width:622px;width:100%;\">")
          .append("<tr><td style=\"background-color:").append(C_GLOW)
          .append(";border-radius:16px;padding:1px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"")
          .append(" style=\"background-color:").append(C_BODY).append(";border-radius:15px;\">");
    }

    private void cerrarFrame(StringBuilder sb) {
        sb.append("</table></td></tr></table></td></tr></table></body></html>");
    }

    // ── Secciones ──────────────────────────────────────────────────────────────

    private void encabezado(StringBuilder sb, String titulo, String subtitulo, boolean esAlerta) {
        String acentoTop  = esAlerta ? C_RED_ACC    : C_ACCENT;
        String acentoSub  = esAlerta ? "#ff8080"    : C_CAPTION;
        String bg         = esAlerta ? "#1a0808"    : "#0c1238";

        // Barra superior de color (3px)
        sb.append("<tr><td style=\"background-color:").append(acentoTop)
          .append(";border-radius:15px 15px 0 0;padding:3px 0;font-size:0;\"> </td></tr>");

        // Contenido del encabezado
        sb.append("<tr><td style=\"background-color:").append(bg)
          .append(";padding:32px 40px 28px;text-align:center;\">")
          // Logo / sistema
          .append("<p style=\"margin:0 0 2px;font-size:10px;color:").append(acentoTop)
          .append(";letter-spacing:5px;text-transform:uppercase;font-weight:bold;\">")
          .append("&#11835; SISTEMA BT &#11835;</p>")
          // Título
          .append("<p style=\"margin:10px 0 6px;font-size:24px;font-weight:bold;color:")
          .append(C_TEXT).append(";letter-spacing:-0.5px;\">").append(titulo).append("</p>")
          // Subtítulo
          .append("<p style=\"margin:0;font-size:13px;color:").append(acentoSub)
          .append(";\">").append(subtitulo).append("</p>")
          .append("</td></tr>");

        // Línea divisora de acento
        sb.append("<tr><td style=\"padding:0 32px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>")
          .append("<td style=\"border-top:1px solid ").append(acentoTop)
          .append(";font-size:0;\"> </td></tr></table>")
          .append("</td></tr>");
    }

    private void alertaBanner(StringBuilder sb, String emp, String herr, String turno) {
        String icono = "NOCHE".equalsIgnoreCase(turno) ? "&#127769;" : "&#9728;&#65039;";
        sb.append("<tr><td style=\"padding:4px 24px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>")
          .append("<td style=\"background-color:#2a0505;border:1px solid ").append(C_RED_ACC)
          .append(";border-radius:10px;padding:14px 20px;text-align:center;\">")
          .append("<p style=\"margin:0;font-size:13px;color:").append(C_OFF_TEXT).append(";\">")
          .append("El turno ").append(icono).append(" <strong style=\"color:#ffffff;\">")
          .append(turno).append("</strong> de <strong style=\"color:#ffffff;\">")
          .append(emp).append("</strong> termina en 30 minutos. La herramienta <strong style=\"color:#ffffff;\">")
          .append(herr).append("</strong> aun no ha sido devuelta.</p>")
          .append("</td></tr></table></td></tr>")
          .append(spacer(8));
    }

    private void kpis(StringBuilder sb, int total, int disponibles, int prestadas) {
        sb.append("<tr><td style=\"padding:20px 24px 8px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        kpiCard(sb, "&#128230;", C_BLUE_ACC,   total,       "Total Unidades",  false);
        sb.append("<td width=\"8\" style=\"font-size:0;\"> </td>");
        kpiCard(sb, "&#9989;",   C_GREEN_ACC,  disponibles, "Disponibles",     false);
        sb.append("<td width=\"8\" style=\"font-size:0;\"> </td>");
        kpiCard(sb, "&#10145;",  C_ORANGE_ACC, prestadas,   "Prestadas",       false);

        sb.append("</tr></table></td></tr>");
    }

    private void kpiCard(StringBuilder sb, String icono, String acento,
                          int valor, String etiqueta, boolean ignorado) {
        sb.append("<td style=\"width:33%;vertical-align:top;\">")
          // Glow externo del card
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>")
          .append("<td style=\"background-color:").append(acento)
          .append(";border-radius:12px;padding:1px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          // Barra superior de color
          .append("<tr><td style=\"background-color:").append(acento)
          .append(";border-radius:11px 11px 0 0;padding:4px 0;font-size:0;\"> </td></tr>")
          // Contenido de la card
          .append("<tr><td style=\"background-color:").append(C_GLASS)
          .append(";border-radius:0 0 11px 11px;padding:20px 12px;text-align:center;\">")
          .append("<p style=\"margin:0;font-size:26px;line-height:1;\">").append(icono).append("</p>")
          .append("<p style=\"margin:10px 0 4px;font-size:32px;font-weight:bold;color:")
          .append(C_TEXT).append(";line-height:1;\">").append(valor).append("</p>")
          .append("<p style=\"margin:0;font-size:10px;color:").append(C_CAPTION)
          .append(";letter-spacing:1.5px;text-transform:uppercase;\">").append(etiqueta).append("</p>")
          .append("</td></tr>")
          .append("</table></td></tr></table></td>");
    }

    private void infoCard(StringBuilder sb, String emp, String herr, String turno, LocalDate fecha) {
        sb.append("<tr><td style=\"padding:12px 24px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>")
          // Card con borde izquierdo acento azul + glow
          .append("<td style=\"background-color:").append(C_GLASS)
          .append(";border-radius:12px;border-left:4px solid ").append(C_ACCENT)
          .append(";padding:22px 24px;\">")
          // Sección header
          .append("<p style=\"margin:0 0 16px;font-size:10px;color:").append(C_ACCENT)
          .append(";letter-spacing:4px;text-transform:uppercase;font-weight:bold;\">")
          .append("Detalle del Prestamo</p>")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");

        filaInfo(sb, "Empleado",    txt(emp,           C_TEXT,    "14px", true));
        filaInfo(sb, "Herramienta", txt(herr,          C_TEXT,    "14px", true));
        filaInfo(sb, "Turno",       badgeTurno(turno));
        filaInfo(sb, "Fecha",       txt(fecha.toString(), C_TEXT, "14px", false));

        sb.append("</table></td></tr></table></td></tr>");
    }

    private void tablaActivos(StringBuilder sb, List<PrestamoActivoDto> activos) {
        sb.append("<tr><td style=\"padding:12px 24px 4px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>")
          .append("<td style=\"background-color:").append(C_GLASS)
          .append(";border-radius:12px;overflow:hidden;\">")
          // Header de la sección
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
          .append("<tr><td style=\"background-color:").append(C_GLASS_INNER)
          .append(";padding:16px 22px;border-bottom:1px solid ").append(C_GLOW).append(";\">")
          .append("<p style=\"margin:0;font-size:10px;color:").append(C_ACCENT)
          .append(";letter-spacing:4px;text-transform:uppercase;font-weight:bold;\">")
          .append("&#128336; Prestamos Activos")
          .append("</p></td></tr>");

        // Encabezado de columnas
        sb.append("<tr style=\"background-color:").append(C_GLASS_INNER).append(";\">")
          .append(thCol("30px",  "center", "&bull;"))
          .append(thCol("",      "left",   "Empleado"))
          .append(thCol("",      "left",   "Herramienta"))
          .append(thCol("85px",  "center", "Turno"))
          .append(thCol("120px", "center", "Estado"))
          .append(thCol("50px",  "center", "Dias"))
          .append("</tr>");

        if (activos.isEmpty()) {
            sb.append("<tr><td colspan=\"6\" style=\"padding:24px;text-align:center;")
              .append("background-color:").append(C_GLASS)
              .append(";color:").append(C_MUTED).append(";font-size:13px;\">")
              .append("No hay prestamos activos en este momento.</td></tr>");
        } else {
            boolean zebra = false;
            for (PrestamoActivoDto p : activos) {
                boolean critica  = !p.isTurnoActivo() && "VERDE".equals(p.getAlerta());
                String  bgFila   = critica ? "#200808" : (zebra ? C_GLASS_INNER : C_GLASS);
                String  bIzq     = critica
                        ? "border-left:3px solid " + C_RED_ACC + ";"
                        : "border-left:3px solid transparent;";
                String  colSem   = colorSemaforo(p.getAlerta());

                sb.append("<tr style=\"background-color:").append(bgFila).append(";\">")
                  .append("<td style=\"").append(bIzq)
                  .append("padding:11px 8px;border-bottom:1px solid #181e50;text-align:center;\">")
                  .append("<span style=\"color:").append(colSem).append(";font-size:14px;\">")
                  .append("&#9679;</span></td>")
                  .append(tdCol(p.getNombreEmpleado(),    C_TEXT,    "13px", "left"))
                  .append(tdCol(p.getNombreHerramienta(), C_CAPTION, "13px", "left"))
                  .append("<td style=\"padding:11px 8px;border-bottom:1px solid #181e50;text-align:center;\">")
                  .append(badgeTurno(p.getTurno())).append("</td>")
                  .append("<td style=\"padding:11px 8px;border-bottom:1px solid #181e50;text-align:center;\">")
                  .append(badgeEstado(p.isTurnoActivo())).append("</td>")
                  .append(tdCol(p.getDiasPrestado() + " d", C_CAPTION, "12px", "center"))
                  .append("</tr>");
                zebra = !zebra;
            }
        }

        sb.append("</table></td></tr></table></td></tr>");
    }

    private void pie(StringBuilder sb, String nota) {
        sb.append(spacer(16))
          .append("<tr><td style=\"padding:0 24px 28px;\">")
          .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>")
          .append("<td style=\"border-top:1px solid #1e2660;padding-top:20px;text-align:center;\">")
          .append("<p style=\"margin:0 0 6px;color:").append(C_MUTED).append(";font-size:12px;\">")
          .append(nota).append("</p>")
          .append("<p style=\"margin:0;color:#334070;font-size:11px;\">")
          .append("Generado automaticamente por el Sistema BT</p>")
          .append("</td></tr></table></td></tr>");
    }

    // ── Micro-helpers ───────────────────────────────────────────────────────────

    private String spacer(int px) {
        return "<tr><td style=\"height:" + px + "px;font-size:0;\"> </td></tr>";
    }

    private void filaInfo(StringBuilder sb, String label, String valueHtml) {
        sb.append("<tr>")
          .append("<td style=\"color:").append(C_MUTED)
          .append(";font-size:12px;width:120px;padding:7px 0;vertical-align:middle;\">")
          .append(label).append("</td>")
          .append("<td style=\"padding:7px 0;vertical-align:middle;\">")
          .append(valueHtml).append("</td>")
          .append("</tr>");
    }

    private String txt(String valor, String color, String size, boolean bold) {
        String w = bold ? "font-weight:bold;" : "";
        return "<span style=\"color:" + color + ";font-size:" + size + ";" + w + "\">"
                + valor + "</span>";
    }

    private String thCol(String width, String align, String texto) {
        String w = width.isBlank() ? "" : "width:" + width + ";";
        return "<th style=\"" + w + "padding:10px 8px;font-size:10px;color:" + C_ACCENT
                + ";text-align:" + align + ";letter-spacing:1.5px;"
                + "text-transform:uppercase;font-weight:bold;\">" + texto + "</th>";
    }

    private String tdCol(String valor, String color, String size, String align) {
        return "<td style=\"padding:11px 8px;border-bottom:1px solid #181e50;"
                + "color:" + color + ";font-size:" + size + ";text-align:" + align + ";\">"
                + valor + "</td>";
    }

    // ── Badges ─────────────────────────────────────────────────────────────────

    private String badgeTurno(String turno) {
        if ("NOCHE".equalsIgnoreCase(turno)) {
            return pill("&#127769; NOCHE", C_NOCHE_BG, C_NOCHE_TEXT, C_NOCHE_BORDER);
        }
        return pill("&#9728;&#65039; DIA", C_DIA_BG, C_DIA_TEXT, C_DIA_BORDER);
    }

    private String badgeEstado(boolean turnoActivo) {
        if (turnoActivo) {
            return pill("&#9654; En curso",   C_ON_BG,  C_ON_TEXT,  C_ON_BORDER);
        }
        return pill("&#9888; Finalizado", C_OFF_BG, C_OFF_TEXT, C_OFF_BORDER);
    }

    /** Pill badge con bordes muy redondeados — parece translúcido sobre cualquier fondo */
    private String pill(String texto, String bg, String color, String borde) {
        return "<span style=\"display:inline-block;background-color:" + bg
                + ";color:" + color
                + ";border:1px solid " + borde
                + ";padding:4px 11px;border-radius:50px;"
                + "font-size:11px;font-weight:bold;white-space:nowrap;\">"
                + texto + "</span>";
    }

    private String colorSemaforo(String alerta) {
        return switch (alerta) {
            case "AMARILLO" -> "#fbbf24";
            case "ROJO"     -> C_RED_ACC;
            default         -> C_GREEN_ACC;
        };
    }
}
