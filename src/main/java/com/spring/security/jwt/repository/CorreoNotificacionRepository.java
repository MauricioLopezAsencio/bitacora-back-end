package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.CorreoNotificacionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorreoNotificacionRepository extends JpaRepository<CorreoNotificacionModel, Long> {

    List<CorreoNotificacionModel> findByBoActivoTrueAndBoRecordatoriosTrue();

    List<CorreoNotificacionModel> findByBoActivoTrueAndBoBitacoraTrue();

    boolean existsByDsCorreo(String dsCorreo);

    boolean existsByDsCorreoAndIdNot(String dsCorreo, Long id);
}
