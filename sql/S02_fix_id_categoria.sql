-- S02 fix (20/08/2026) - hallazgo al probar el CRUD completo (paso 3.7)
--
-- SINTOMA: POST /api/v1/productos devolvia 500
--   ORA-01400: no se puede realizar una insercion NULL en
--   ("BOM_CATALOGO"."PRODUCTO"."ID_CATEGORIA")
--
-- CAUSA: la entidad Producto no mapea ID_CATEGORIA, asi que Hibernate
-- genera el INSERT sin esa columna. La tabla creada por S01_02_tablas.sql
-- la define NOT NULL y sin DEFAULT, entonces Oracle rechaza la fila.
-- En la PC de la universidad no fallaba porque alli la tabla venia de
-- S02_tablas_ref.sql, que si trae DEFAULT 1.
--
-- DECISION: DEFAULT 1 en vez de permitir NULL (que fue lo que hizo el
-- docente en su version de BD2). Un producto sin categoria no significa
-- nada en un catalogo; el default garantiza que toda fila quede asociada
-- a una categoria existente aunque el cliente no la envie.
--
-- Ejecutar como SYSTEM:
--   Get-Content sql\S02_fix_id_categoria.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1

ALTER TABLE BOM_CATALOGO.PRODUCTO MODIFY (ID_CATEGORIA DEFAULT 1);

EXIT;