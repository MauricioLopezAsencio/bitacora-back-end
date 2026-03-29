package com.spring.security.jwt.repository;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.CategoriaStatsDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.dto.HerramientaResponseDto;
import com.spring.security.jwt.dto.HerramientaUpdateRequest;
import com.spring.security.jwt.dto.PrestamoActivoDto;
import com.spring.security.jwt.exception.NegocioException;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IProductResository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductRepository implements IProductResository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<HerramientaModel> findAll() {
       String SQL = "SELECT * FROM cat_herramientas";
       return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
    }

    @Override
    public List<HerramientaModel> findAllActivo() {
        String SQL = "SELECT * FROM cat_herramientas WHERE estatus = true AND cantidad_disponible > 0";
        return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
    }


    @Override
    public Optional<HerramientaModel> findById(Long id) {
        String sql = "SELECT * FROM cat_herramientas WHERE id = ?";
        try {
            HerramientaModel h = jdbcTemplate.queryForObject(sql, new Object[]{id},
                    BeanPropertyRowMapper.newInstance(HerramientaModel.class));
            return Optional.ofNullable(h);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public HerramientaResponseDto update(Long id, HerramientaUpdateRequest request) {
        // Valida que exista
        HerramientaModel actual = findById(id)
                .orElseThrow(() -> new NegocioException("No se encontró la herramienta con id: " + id));

        // Calcula la nueva cantidad disponible conservando los préstamos activos
        int prestadas = actual.getCantidadTotal() - actual.getCantidadDisponible();
        int nuevaDisponible = Math.max(0, request.getCantidadTotal() - prestadas);

        String sql = """
                UPDATE cat_herramientas
                SET nombre = ?, categoria = ?, cantidad_total = ?, cantidad_disponible = ?, estatus = ?
                WHERE id = ?
                """;
        int rows = jdbcTemplate.update(sql,
                request.getNombre(),
                request.getCategoria(),
                request.getCantidadTotal(),
                nuevaDisponible,
                request.isEstatus(),
                id);

        if (rows == 0) {
            throw new NegocioException("No se pudo actualizar la herramienta con id: " + id);
        }

        return HerramientaResponseDto.builder()
                .id(id)
                .nombre(request.getNombre())
                .categoria(request.getCategoria())
                .cantidadTotal(request.getCantidadTotal())
                .cantidadDisponible(nuevaDisponible)
                .estatus(request.isEstatus())
                .build();
    }

    @Override
    public void delete(Long id) {
        // Valida que no tenga préstamos activos antes de eliminar
        String sqlCheck = "SELECT COUNT(*) FROM empleado_herramienta WHERE herramienta_id = ? AND estatus = false";
        Integer prestamosActivos = jdbcTemplate.queryForObject(sqlCheck, Integer.class, id);
        if (prestamosActivos != null && prestamosActivos > 0) {
            throw new NegocioException(
                    "No se puede eliminar la herramienta porque tiene " + prestamosActivos + " préstamo(s) activo(s).");
        }

        String sql = "DELETE FROM cat_herramientas WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        if (rows == 0) {
            throw new NegocioException("No se encontró la herramienta con id: " + id);
        }
    }

    @Override
    public HerramientaDto save(HerramientaDto entity) {
        int total = entity.getCantidadTotal() != null && entity.getCantidadTotal() > 0
                ? entity.getCantidadTotal() : 1;
        String sql = "INSERT INTO cat_herramientas (nombre, categoria, estatus, cantidad_total, cantidad_disponible) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getNombre());
            ps.setString(2, entity.getCategoria());
            ps.setBoolean(3, entity.isEstatus());
            ps.setInt(4, total);
            ps.setInt(5, total);
            return ps;
        }, keyHolder);

        return entity;
    }

    @Override
    public boolean toggleEstatus(Long id) {
        String sql = "UPDATE cat_herramientas SET estatus = NOT estatus WHERE id = ? RETURNING estatus";
        Boolean nuevoEstatus = jdbcTemplate.queryForObject(sql, Boolean.class, id);
        if (nuevoEstatus == null) {
            throw new NegocioException("No se encontró ninguna herramienta con el id: " + id);
        }
        return nuevoEstatus;
    }

    @Override
    public DashboardDto getDashboard() {
        String sqlResumen = """
                SELECT
                    COUNT(*) AS total_tipos,
                    COALESCE(SUM(cantidad_total), 0) AS total_unidades,
                    COALESCE(SUM(cantidad_total - cantidad_disponible), 0) AS total_prestadas,
                    COALESCE(SUM(cantidad_disponible), 0) AS total_disponibles
                FROM cat_herramientas
                WHERE estatus = true
                """;

        String sqlCategorias = """
                SELECT
                    categoria,
                    COALESCE(SUM(cantidad_total), 0) AS total_unidades,
                    COALESCE(SUM(cantidad_total - cantidad_disponible), 0) AS prestadas,
                    COALESCE(SUM(cantidad_disponible), 0) AS disponibles
                FROM cat_herramientas
                WHERE estatus = true
                GROUP BY categoria
                ORDER BY categoria
                """;

        String sqlPrestamos = """
                SELECT
                    eh.id,
                    e.nombre  AS nombre_empleado,
                    h.nombre  AS nombre_herramienta,
                    eh.fecha,
                    eh.turno,
                    ((NOW() AT TIME ZONE 'America/Mexico_City')::date - eh.fecha) AS dias_prestado
                FROM empleado_herramienta eh
                JOIN cat_empleados    e ON e.id = eh.empleado_id
                JOIN cat_herramientas h ON h.id = eh.herramienta_id
                WHERE eh.estatus = false
                ORDER BY dias_prestado DESC
                """;

        Map<String, Object> resumen = jdbcTemplate.queryForMap(sqlResumen);

        List<CategoriaStatsDto> porCategoria = jdbcTemplate.query(sqlCategorias, (rs, rowNum) ->
                CategoriaStatsDto.builder()
                        .categoria(rs.getString("categoria"))
                        .totalUnidades(rs.getInt("total_unidades"))
                        .prestadas(rs.getInt("prestadas"))
                        .disponibles(rs.getInt("disponibles"))
                        .build()
        );

        List<PrestamoActivoDto> prestamosActivos = jdbcTemplate.query(sqlPrestamos, (rs, rowNum) -> {
            int       dias   = rs.getInt("dias_prestado");
            LocalDate fecha  = rs.getObject("fecha", LocalDate.class);
            String    turno  = rs.getString("turno");
            return PrestamoActivoDto.builder()
                    .id(rs.getLong("id"))
                    .nombreEmpleado(rs.getString("nombre_empleado"))
                    .nombreHerramienta(rs.getString("nombre_herramienta"))
                    .fecha(fecha)
                    .turno(turno)
                    .turnoActivo(PrestamoActivoDto.calcularTurnoActivo(turno, fecha))
                    .diasPrestado(dias)
                    .alerta(PrestamoActivoDto.calcularAlerta(dias, turno, fecha))
                    .build();
        });

        return DashboardDto.builder()
                .totalTipos(((Number) resumen.get("total_tipos")).intValue())
                .totalUnidades(((Number) resumen.get("total_unidades")).intValue())
                .totalPrestadas(((Number) resumen.get("total_prestadas")).intValue())
                .totalDisponibles(((Number) resumen.get("total_disponibles")).intValue())
                .porCategoria(porCategoria)
                .prestamosActivos(prestamosActivos)
                .build();
    }

    @Override
    public List<BitacoraDto> getBitacora() {
        String sql = """
                SELECT
                    eh.id,
                    e.nombre  AS nombre_empleado,
                    h.nombre  AS nombre_herramienta,
                    eh.estatus,
                    eh.fecha,
                    eh.turno
                FROM empleado_herramienta eh
                JOIN cat_empleados    e ON e.id = eh.empleado_id
                JOIN cat_herramientas h ON h.id = eh.herramienta_id
                ORDER BY eh.id DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BitacoraDto dto = new BitacoraDto();
            dto.setId(rs.getLong("id"));
            dto.setNombreEmpleado(rs.getString("nombre_empleado"));
            dto.setNombreHerramienta(rs.getString("nombre_herramienta"));
            dto.setEstatus(rs.getBoolean("estatus"));
            dto.setFecha(rs.getObject("fecha", LocalDate.class));
            dto.setTurno(rs.getString("turno"));
            return dto;
        });
    }
}
