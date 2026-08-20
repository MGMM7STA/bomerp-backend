INSERT INTO BOM_CATALOGO.categoria (nombre, descripcion) VALUES ('Abarrotes', 'Productos de primera necesidad');
INSERT INTO BOM_CATALOGO.categoria (nombre, descripcion) VALUES ('Bebidas', 'Gaseosas, aguas y jugos');
INSERT INTO BOM_CATALOGO.producto (id_categoria, nombre, precio, stock)
  SELECT id_categoria, 'Arroz Costeno 5kg', 24.90, 50 FROM BOM_CATALOGO.categoria WHERE nombre='Abarrotes';
INSERT INTO BOM_CATALOGO.producto (id_categoria, nombre, precio, stock)
  SELECT id_categoria, 'Inca Kola 1.5L', 7.50, 120 FROM BOM_CATALOGO.categoria WHERE nombre='Bebidas';
COMMIT;
EXIT;
