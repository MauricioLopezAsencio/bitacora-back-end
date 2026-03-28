package com.spring.security.jwt.repository;

import com.spring.security.jwt.dto.CategoriaStatsDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.dto.PrestamoActivoDto;
import com.spring.security.jwt.exception.NegocioException;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IProductResository;
import org.springframework.beans.factory.annotation.Autowired;
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
        String SQL = "SELECT * FROM cat_herramientas WHERE estatus = true";
        return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
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
                    (CURRENT_DATE - eh.fecha) AS dias_prestado
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
                    .alerta(PrestamoActivoDto.calcularAlerta(dias))
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
}
