CREATE UNIQUE INDEX IF NOT EXISTS uq_notificacion_aviso_t45_cita_tipo
    ON notificacion.notificacion (cita_id, tipo)
    WHERE tipo = 'AVISO_T45';
