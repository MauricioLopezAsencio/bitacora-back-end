package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.EmpleadoHerramientaDTO;
import com.spring.security.jwt.model.EmpleadoHerramientaModel;
import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.model.TurnoConfigModel;
import com.spring.security.jwt.repository.EmpleadoRepository;
import com.spring.security.jwt.repository.HerramientaRepository;
import com.spring.security.jwt.repository.ParametroSistemaRepository;
import com.spring.security.jwt.repository.TurnoConfigRepository;
import com.spring.security.jwt.repository.impl.EmpleadoHerramientaRepository;
import com.spring.security.jwt.exception.NegocioException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmpleadoHerramientaService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private HerramientaRepository herramientaRepository;

    @Autowired
    private EmpleadoHerramientaRepository empleadoHerramientaRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private TurnoConfigRepository turnoConfigRepository;

    @Autowired
    private ParametroSistemaRepository parametroSistemaRepository;

    @Value("${app.turno.duracion-horas:12}")
    private long duracionTurnoHoras;

    // Método para asignar una herramienta a un empleado
    @Transactional
    public EmpleadoHerramientaModel asignarHerramientaAEmpleado(EmpleadoHerramientaDTO dto) {
        EmpleadoModel empleado = empleadoRepository.findById(dto.getEmpleadoId())
                .orElseThrow(() -> new NegocioException("Empleado no encontrado con id: " + dto.getEmpleadoId()));

        HerramientaModel herramienta = herramientaRepository.findById(dto.getHerramientaId())
                .orElseThrow(() -> new NegocioException("Herramienta no encontrada con id: " + dto.getHerramientaId()));

        if (herramienta.getCantidadDisponible() == null || herramienta.getCantidadDisponible() <= 0) {
            throw new NegocioException("No hay unidades disponibles de '" + herramienta.getNombre() + "' para préstamo");
        }

        String turno = normalizarTurno(dto.getTurno());

        EmpleadoHerramientaModel empleadoHerramienta = new EmpleadoHerramientaModel();
        empleadoHerramienta.setEmpleado(empleado);
        empleadoHerramienta.setHerramienta(herramienta);
        empleadoHerramienta.setFecha(LocalDate.now());
        empleadoHerramienta.setEstatus(false);
        empleadoHerramienta.setTurno(turno);
        empleadoHerramienta.setFcAsignacion(LocalDateTime.now());

        EmpleadoHerramientaModel guardado = empleadoHerramientaRepository.save(empleadoHerramienta);
        herramientaRepository.decrementarDisponible(herramienta.getId());

        final Long   asignacionId = guardado.getId();
        final String empNombre   = empleado.getNombre();
        final String herrNombre  = herramienta.getNombre();
        final String turnoFinal  = turno;
        final LocalDate fechaFinal = guardado.getFecha();

        log.info("Prestamo registrado asignacionId={} empleado={} herramienta={} turno={} fecha={}",
                asignacionId, empNombre, herrNombre, turnoFinal, fechaFinal);

        // KPI de esta herramienta (post-decremento estimado) para el correo inmediato
        int totalH      = herramienta.getCantidadTotal() != null ? herramienta.getCantidadTotal() : 0;
        int dispH       = Math.max(0, (herramienta.getCantidadDisponible() != null
                            ? herramienta.getCantidadDisponible() : 0) - 1);
        int prestadasH  = totalH - dispH;

        // Email 1: confirmación inmediata al registrar el préstamo
        emailService.enviarRecordatorioPrestamo(empNombre, herrNombre, turnoFinal, fechaFinal,
                totalH, dispH, prestadasH);

        // Email 2: recordatorio N minutos antes de que termine el turno (configurable en BD)
        // Solo se envía si la herramienta sigue sin devolverse; usa dashboard global en ese momento
        int minutosAntes = parametroSistemaRepository.findById(1L)
                .map(p -> p.getMinutosRecordatorio())
                .orElse(30);
        Instant finTurno = calcularFinTurno(turno, guardado.getFecha(), guardado.getFcAsignacion())
                .minusSeconds((long) minutosAntes * 60);
        log.info("Recordatorio programado asignacionId={} finTurno={} minutosAntes={}",
                asignacionId, finTurno, minutosAntes);
        taskScheduler.schedule(() ->
            empleadoHerramientaRepository.findById(asignacionId).ifPresent(asignacion -> {
                if (!asignacion.isEstatus()) {
                    log.info("Enviando recordatorio finTurno asignacionId={} empleado={} herramienta={}",
                            asignacionId, empNombre, herrNombre);
                    DashboardDto dashboard = iProductService.getDashboard();
                    emailService.enviarRecordatorioFinTurno(empNombre, herrNombre, turnoFinal, fechaFinal, dashboard);
                } else {
                    log.info("Recordatorio omitido, herramienta ya devuelta asignacionId={}", asignacionId);
                }
            }),
            finTurno
        );

        return guardado;
    }

    @PostConstruct
    public void recuperarRecordatoriosPendientes() {
        int minutosAntes = parametroSistemaRepository.findById(1L)
                .map(p -> p.getMinutosRecordatorio())
                .orElse(30);
        List<EmpleadoHerramientaModel> activas = empleadoHerramientaRepository
                .findActivasPendientesRecordatorio();
        for (EmpleadoHerramientaModel a : activas) {
            Instant finTurno = calcularFinTurno(a.getTurno(), a.getFecha(), a.getFcAsignacion())
                    .minusSeconds((long) minutosAntes * 60);
            if (finTurno.isAfter(Instant.now())) {
                final Long asignacionId  = a.getId();
                final String empNombre   = a.getEmpleado().getNombre();
                final String herrNombre  = a.getHerramienta().getNombre();
                final String turno       = a.getTurno();
                final LocalDate fecha    = a.getFecha();
                taskScheduler.schedule(() ->
                    empleadoHerramientaRepository.findById(asignacionId).ifPresent(asig -> {
                        if (!asig.isEstatus()) {
                            DashboardDto dashboard = iProductService.getDashboard();
                            emailService.enviarRecordatorioFinTurno(empNombre, herrNombre, turno, fecha, dashboard);
                        }
                    }), finTurno);
                log.info("Recordatorio re-agendado asignacion={} finTurno={}", asignacionId, finTurno);
            }
        }
    }

    // Método para obtener todas las relaciones de empleado y herramienta
    public List<BitacoraDto> findAll() {

        List<EmpleadoHerramientaModel> empleadoHerramientaModels = empleadoHerramientaRepository.findAll();

        // Obtener todos los empleados en un solo llamado
        List<EmpleadoModel> empleados = empleadoRepository.findAll();
        // Obtener todas las herramientas en un solo llamado
        List<HerramientaModel> herramientas = herramientaRepository.findAll();

        // Crear un mapa de empleados y herramientas para facilitar el acceso
        Map<Long, String> empleadoMap = empleados.stream()
                .collect(Collectors.toMap(
                        EmpleadoModel::getId,
                        empleado -> empleado.getNombre()
                ));

        Map<Long, String> herramientaMap = herramientas.stream()
                .collect(Collectors.toMap(HerramientaModel::getId, HerramientaModel::getNombre));

        List<BitacoraDto> response = new ArrayList<>();

        // Iterar sobre las relaciones de empleado y herramienta
        for (EmpleadoHerramientaModel ehModel : empleadoHerramientaModels) {
            // Asegúrate de obtener el ID del empleado y de la herramienta
            Long empleadoId = ehModel.getEmpleado().getId(); // Cambiado para obtener el ID
            Long herramientaId = ehModel.getHerramienta().getId(); // Cambiado para obtener el ID

            String nombreEmpleado = empleadoMap.get(empleadoId); // Usar el ID del empleado
            String nombreHerramienta = herramientaMap.get(herramientaId); // Usar el ID de la herramienta

            // Crear un nuevo BitacoraDto y añadirlo a la respuesta
            BitacoraDto bitacoraDto = new BitacoraDto();
            bitacoraDto.setId(ehModel.getId());
            bitacoraDto.setNombreEmpleado(nombreEmpleado);
            bitacoraDto.setNombreHerramienta(nombreHerramienta);
            bitacoraDto.setEstatus(ehModel.isEstatus());
            bitacoraDto.setFecha(ehModel.getFecha());
            bitacoraDto.setTurno(ehModel.getTurno());

            response.add(bitacoraDto);
        }

        return response;
    }

    // ── Helpers de turno ──────────────────────────────────────────────────────

    /**
     * Acepta MATUTINO, VESPERTINO o NOCTURNO (insensible a mayúsculas).
     * Cualquier otro valor se trata como MATUTINO por defecto.
     */
    private String normalizarTurno(String turno) {
        if (turno == null) return "MATUTINO";
        return switch (turno.toUpperCase()) {
            case "VESPERTINO" -> "VESPERTINO";
            case "NOCTURNO"   -> "NOCTURNO";
            default           -> "MATUTINO";
        };
    }

    /**
     * Devuelve el {@link Instant} exacto en que finaliza el turno.
     * Los horarios se leen desde {@code ct_turno_config}; si no existe la fila,
     * se usan los valores por defecto (MATUTINO 14h / VESPERTINO 22h / NOCTURNO 06h).
     * <p>
     * Para NOCTURNO (fin &lt; inicio, cruza medianoche): si la asignación ocurrió
     * antes de {@code hora_fin}, el turno ya comenzó "ayer" y termina HOY;
     * si ocurrió después, termina MAÑANA.
     * </p>
     * Si el fin calculado ya pasó, se usa {@code asignacion + duracionTurnoHoras} como respaldo.
     */
    private Instant calcularFinTurno(String turno, LocalDate fecha, LocalDateTime asignacion) {
        TurnoConfigModel config = turnoConfigRepository.findByCvTurno(turno.toUpperCase())
                .orElse(null);
        int horaFin = resolverHoraFin(turno, config);

        LocalDateTime fin;
        if ("NOCTURNO".equalsIgnoreCase(turno)) {
            LocalTime horaAsignacion = asignacion.toLocalTime();
            if (horaAsignacion.isBefore(LocalTime.of(horaFin, 0))) {
                fin = fecha.atTime(LocalTime.of(horaFin, 0));
            } else {
                fin = fecha.plusDays(1).atTime(LocalTime.of(horaFin, 0));
            }
        } else {
            fin = fecha.atTime(LocalTime.of(horaFin, 0));
        }

        Instant resultado = fin.atZone(ZoneId.systemDefault()).toInstant();
        if (resultado.isBefore(Instant.now())) {
            return asignacion.plusHours(duracionTurnoHoras)
                    .atZone(ZoneId.systemDefault()).toInstant();
        }
        return resultado;
    }

    /** Hora de fin desde config BD; si no existe, devuelve el valor por defecto del turno. */
    private int resolverHoraFin(String turno, TurnoConfigModel config) {
        if (config != null) return config.getHoraFin();
        return switch (turno.toUpperCase()) {
            case "VESPERTINO" -> 22;
            case "NOCTURNO"   -> 6;
            default           -> 14; // MATUTINO
        };
    }

    @Transactional
    public EmpleadoHerramientaModel actualizarEstatus(BitacoraDto dto) {
        if (!dto.isEstatus()) {
            throw new NegocioException(
                "No se puede desactivar una asignación. Si el empleado tomó la herramienta nuevamente, registre una nueva asignación."
            );
        }

        EmpleadoHerramientaModel empleadoHerramienta = empleadoHerramientaRepository.findById(dto.getId())
                .orElseThrow(() -> new NegocioException("Relación empleado-herramienta no encontrada con id: " + dto.getId()));

        if (empleadoHerramienta.isEstatus()) {
            throw new NegocioException(
                "Esta asignación ya fue devuelta. Si el empleado tomó la herramienta nuevamente, registre una nueva asignación."
            );
        }

        empleadoHerramienta.setEstatus(true);
        EmpleadoHerramientaModel devuelta = empleadoHerramientaRepository.save(empleadoHerramienta);
        herramientaRepository.incrementarDisponible(empleadoHerramienta.getHerramienta().getId());
        log.info("Herramienta devuelta asignacionId={} herramienta={} empleado={}",
                devuelta.getId(),
                devuelta.getHerramienta().getNombre(),
                devuelta.getEmpleado().getNombre());
        return devuelta;
    }

}
