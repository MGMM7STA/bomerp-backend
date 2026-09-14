-- LP2 - S05 (Crea) - Semestre Saludable
-- Datos de prueba para evidenciar filtros combinados, orden y reporte agregado.
-- Ejecutar como SYSTEM dentro del contenedor bomerp-oracle.

SET SERVEROUTPUT ON
SET LINESIZE 200
SET PAGESIZE 60
SET FEEDBACK ON

DECLARE
  v_campania NUMBER;
  v_total    NUMBER;

  PROCEDURE sembrar(p_est NUMBER, p_dias NUMBER, p_estado VARCHAR2, p_veces NUMBER) IS
    v_id NUMBER;
  BEGIN
    INSERT INTO BOM_PARTICIPACION.REGISTROS
      (ID_ESTUDIANTE, ID_CAMPANIA, FECHA, ESTADO, TOTAL_EVIDENCIAS, PUNTAJE_TOTAL)
    VALUES
      (p_est, v_campania, SYSTIMESTAMP - NUMTODSINTERVAL(p_dias, 'DAY'),
       p_estado, p_veces, p_veces)
    RETURNING ID INTO v_id;

    INSERT INTO BOM_PARTICIPACION.DETALLE_EVIDENCIAS
      (ID_REGISTRO, ID_HABITO, NOMBRE_HABITO, DESCRIPCION, URL_IMAGEN,
       PUNTAJE_UNITARIO, VECES, SUBTOTAL)
    VALUES
      (v_id, 1 + MOD(p_est + p_dias, 3), 'Habito sembrado', 'Evidencia de prueba', NULL,
       1, p_veces, p_veces);
  END;

BEGIN
  SELECT ID INTO v_campania
  FROM   BOM_CAMPANIAS.CAMPANIAS
  WHERE  ESTADO = 'ACTIVA' AND ROWNUM = 1;

  DBMS_OUTPUT.PUT_LINE('Campania activa: ' || v_campania);

  sembrar(1, 1, 'VALIDADO',  2);
  sembrar(1, 2, 'VALIDADO',  1);
  sembrar(1, 3, 'ENVIADO',   2);
  sembrar(2, 1, 'ENVIADO',   1);
  sembrar(2, 2, 'VALIDADO',  2);
  sembrar(2, 4, 'RECHAZADO', 1);
  sembrar(3, 1, 'VALIDADO',  2);
  sembrar(3, 3, 'ENVIADO',   2);
  sembrar(3, 5, 'VALIDADO',  1);

  COMMIT;

  SELECT COUNT(*) INTO v_total FROM BOM_PARTICIPACION.REGISTROS;
  DBMS_OUTPUT.PUT_LINE('Registros totales en la tabla: ' || v_total);
END;
/

COLUMN ID                FORMAT 999
COLUMN ID_ESTUDIANTE     FORMAT 999        HEADING 'ESTUD'
COLUMN FECHA             FORMAT A18
COLUMN ESTADO            FORMAT A12
COLUMN TOTAL_EVIDENCIAS  FORMAT 999        HEADING 'EVID'
COLUMN PUNTAJE_TOTAL     FORMAT 99990.99   HEADING 'PUNTAJE'

SELECT ID,
       ID_ESTUDIANTE,
       TO_CHAR(FECHA,'DD/MM/YYYY HH24:MI') AS FECHA,
       ESTADO,
       TOTAL_EVIDENCIAS,
       PUNTAJE_TOTAL
FROM   BOM_PARTICIPACION.REGISTROS
ORDER  BY FECHA DESC;

EXIT
