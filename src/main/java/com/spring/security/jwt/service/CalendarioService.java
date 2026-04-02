package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.CalendarioEventoDto;
import com.spring.security.jwt.dto.microsoft.GraphEvent;
import com.spring.security.jwt.dto.microsoft.GraphEventsResponse;
import com.spring.security.jwt.dto.microsoft.GraphRecurrence;
import com.spring.security.jwt.dto.microsoft.GraphRecurrencePattern;
import com.spring.security.jwt.dto.microsoft.GraphRecurrenceRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CalendarioService implements ICalendarioService {

    private static final String GRAPH_EVENTS_URL =
            "https://graph.microsoft.com/v1.0/me/events?$select=subject,start,end,type,recurrence,attendees";

    private static final String DOMINIO_INTERNO = "@casystem.com.mx";

    private static final ZoneId ZONA_MEXICO = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_OCURRENCIAS = 500;

    private final RestTemplate restTemplate;
    private final FeriadosMexicoService feriadosService;

    public CalendarioService(RestTemplate restTemplate, FeriadosMexicoService feriadosService) {
        this.restTemplate = restTemplate;
        this.feriadosService = feriadosService;
    }

    @Override
    public List<CalendarioEventoDto> obtenerEventos(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GraphEventsResponse> response = restTemplate.exchange(
                    GRAPH_EVENTS_URL, HttpMethod.GET, entity, GraphEventsResponse.class);

            List<GraphEvent> eventos = Optional.ofNullable(response.getBody())
                    .map(GraphEventsResponse::getValue)
                    .orElse(Collections.emptyList());

            log.info("Eventos obtenidos de Microsoft Graph count={}", eventos.size());

            return eventos.stream()
                    .flatMap(event -> "seriesMaster".equalsIgnoreCase(event.getType())
                            ? expandirSerieMaestra(event).stream()
                            : Stream.of(toDto(event)))
                    .toList();

        } catch (HttpClientErrorException ex) {
            log.error("Error al consultar Microsoft Graph status={} body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }

    // ─── Conversión simple ───────────────────────────────────────────────────

    private CalendarioEventoDto toDto(GraphEvent event) {
        String startDate = null;
        String endDate = null;
        if (event.getRecurrence() != null && event.getRecurrence().getRange() != null) {
            startDate = event.getRecurrence().getRange().getStartDate();
            endDate   = event.getRecurrence().getRange().getEndDate();
        }
        return CalendarioEventoDto.builder()
                .subject(event.getSubject())
                .start(convertirAMexico(event.getStart().getDateTime(), event.getStart().getTimeZone()))
                .end(convertirAMexico(event.getEnd().getDateTime(), event.getEnd().getTimeZone()))
                .type(event.getType())
                .startDate(startDate)
                .endDate(endDate)
                .modalidad(resolverModalidad(event))
                .build();
    }

    // ─── Expansión de seriesMaster ───────────────────────────────────────────

    private List<CalendarioEventoDto> expandirSerieMaestra(GraphEvent event) {
        GraphRecurrence recurrence = event.getRecurrence();
        if (recurrence == null || recurrence.getPattern() == null || recurrence.getRange() == null) {
            log.warn("seriesMaster sin recurrencia completa subject={}", event.getSubject());
            return List.of(toDto(event));
        }

        GraphRecurrencePattern pattern = recurrence.getPattern();
        GraphRecurrenceRange range     = recurrence.getRange();

        LocalDate startDate = LocalDate.parse(range.getStartDate());
        LocalDate endDate   = resolverFechaFin(range, startDate);
        Integer maxOcurrencias = "numbered".equalsIgnoreCase(range.getType())
                ? range.getNumberOfOccurrences() : null;

        LocalTime startTime    = extraerHora(event.getStart().getDateTime());
        LocalTime endTime      = extraerHora(event.getEnd().getDateTime());
        long duracionMinutos   = ChronoUnit.MINUTES.between(startTime, endTime);
        if (duracionMinutos < 0) duracionMinutos += 24 * 60;

        ZoneId sourceZone = resolverZona(event.getStart().getTimeZone());

        LocalDate hoy = LocalDate.now(ZONA_MEXICO);

        List<LocalDate> fechas = generarOcurrencias(pattern, startDate, endDate, maxOcurrencias)
                .stream()
                .filter(fecha -> !fecha.isAfter(hoy))
                .filter(fecha -> !feriadosService.esFeriado(fecha))
                .toList();

        log.info("seriesMaster expandido subject={} ocurrencias hoy={}", event.getSubject(), fechas.size());

        final long durFinal = duracionMinutos;
        final String rangeStart  = range.getStartDate();
        final String rangeEnd    = range.getEndDate();
        final String modalidad   = resolverModalidad(event);

        return fechas.stream()
                .map(fecha -> {
                    ZonedDateTime startZdt = ZonedDateTime.of(fecha, startTime, sourceZone)
                            .withZoneSameInstant(ZONA_MEXICO);
                    ZonedDateTime endZdt   = startZdt.plusMinutes(durFinal);
                    return CalendarioEventoDto.builder()
                            .subject(event.getSubject())
                            .start(startZdt.format(FORMATTER))
                            .end(endZdt.format(FORMATTER))
                            .type("occurrence")
                            .startDate(rangeStart)
                            .endDate(rangeEnd)
                            .modalidad(modalidad)
                            .build();
                })
                .toList();
    }

    // ─── Generación de fechas por patrón ────────────────────────────────────

    private List<LocalDate> generarOcurrencias(GraphRecurrencePattern pattern,
                                                LocalDate startDate, LocalDate endDate,
                                                Integer maxOcurrencias) {
        List<LocalDate> dates = new ArrayList<>();
        String type     = pattern.getType() != null ? pattern.getType().toLowerCase() : "daily";
        int interval    = Math.max(1, pattern.getInterval());
        int limite      = maxOcurrencias != null ? maxOcurrencias : MAX_OCURRENCIAS;

        switch (type) {
            case "daily" -> {
                LocalDate cur = startDate;
                while (!cur.isAfter(endDate) && dates.size() < limite) {
                    dates.add(cur);
                    cur = cur.plusDays(interval);
                }
            }
            case "weekly" -> {
                if (pattern.getDaysOfWeek() == null || pattern.getDaysOfWeek().isEmpty()) break;

                Set<DayOfWeek> targetDays = pattern.getDaysOfWeek().stream()
                        .map(d -> DayOfWeek.valueOf(d.toUpperCase()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                // Arranca desde el lunes de la semana que contiene startDate
                LocalDate semana = startDate.with(DayOfWeek.MONDAY);

                while (!semana.isAfter(endDate) && dates.size() < limite) {
                    for (DayOfWeek dow : targetDays) {
                        if (dates.size() >= limite) break;
                        LocalDate candidate = semana.with(TemporalAdjusters.nextOrSame(dow));
                        if (!candidate.isBefore(startDate) && !candidate.isAfter(endDate)) {
                            dates.add(candidate);
                        }
                    }
                    semana = semana.plusWeeks(interval);
                }
                Collections.sort(dates);
            }
            case "absolutemonthly" -> {
                int dia = pattern.getDayOfMonth();
                LocalDate cur = startDate.withDayOfMonth(Math.min(dia, startDate.lengthOfMonth()));
                if (cur.isBefore(startDate)) cur = cur.plusMonths(interval);
                while (!cur.isAfter(endDate) && dates.size() < limite) {
                    dates.add(cur);
                    LocalDate next = cur.plusMonths(interval);
                    cur = next.withDayOfMonth(Math.min(dia, next.lengthOfMonth()));
                }
            }
            default -> log.warn("Patrón de recurrencia '{}' no soportado", type);
        }
        return dates;
    }

    // ─── Modalidad ──────────────────────────────────────────────────────────

    private String resolverModalidad(GraphEvent event) {
        if (event.getAttendees() == null || event.getAttendees().isEmpty()) {
            return "interna";
        }
        boolean todosInternos = event.getAttendees().stream()
                .filter(a -> a.getEmailAddress() != null && a.getEmailAddress().getAddress() != null)
                .allMatch(a -> a.getEmailAddress().getAddress()
                        .toLowerCase().endsWith(DOMINIO_INTERNO));
        return todosInternos ? "interna" : "externa";
    }

    // ─── Utilidades ─────────────────────────────────────────────────────────

    private LocalDate resolverFechaFin(GraphRecurrenceRange range, LocalDate startDate) {
        if ("endDate".equalsIgnoreCase(range.getType()) && range.getEndDate() != null) {
            return LocalDate.parse(range.getEndDate());
        }
        if ("numbered".equalsIgnoreCase(range.getType())) {
            return startDate.plusYears(5);
        }
        return startDate.plusYears(1); // noEnd: ventana de 1 año
    }

    private LocalTime extraerHora(String dateTime) {
        String truncated = dateTime.length() > 19 ? dateTime.substring(0, 19) : dateTime;
        return LocalDateTime.parse(truncated, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalTime();
    }

    private String convertirAMexico(String dateTime, String sourceTimeZone) {
        ZoneId origen    = resolverZona(sourceTimeZone);
        String truncated = dateTime.length() > 19 ? dateTime.substring(0, 19) : dateTime;
        LocalDateTime local       = LocalDateTime.parse(truncated, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        ZonedDateTime enMexico    = ZonedDateTime.of(local, origen).withZoneSameInstant(ZONA_MEXICO);
        return enMexico.format(FORMATTER);
    }

    private ZoneId resolverZona(String timeZone) {
        try {
            return ZoneId.of(timeZone);
        } catch (Exception ex) {
            log.warn("Zona horaria no reconocida '{}', se usa UTC", timeZone);
            return ZoneId.of("UTC");
        }
    }
}
