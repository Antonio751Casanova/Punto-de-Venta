package controlador;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class ProductoControlador {
	 private static boolean isWatching = false;

	 public static void iniciarObservadorExcel(DefaultTableModel modeloTabla) {
	        if (isWatching)
			 {
				return; // Evitar múltiples instancias
			}

	        new Thread(() -> {
	            try {
	                Path path = Paths.get("data/productos.xlsx");
	                WatchService watchService = FileSystems.getDefault().newWatchService();
	                path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

	                isWatching = true;
	                while (isWatching) {
	                    WatchKey key = watchService.take();
	                    for (WatchEvent<?> event : key.pollEvents()) {
	                        if (event.context().toString().equals("productos.xlsx")) {
	                            // Esperar 1 segundo para evitar múltiples eventos
	                            Thread.sleep(1000);
	                            // Recargar datos en la tabla
	                            cargarDatosExcel(modeloTabla);
	                            break;
	                        }
	                    }
	                    key.reset();
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }).start();
	    }

	    // Detener el observador (opcional)
	    public static void detenerObservadorExcel() {
	        isWatching = false;
	    }


	    public static void cargarDatosExcel(DefaultTableModel modeloTabla) {
	        File file = new File("data/productos.xlsx");

	        // Verificación robusta del archivo
	        if (!file.exists()) {
	            JOptionPane.showMessageDialog(null,
	                "El archivo productos.xlsx no existe en la carpeta data/",
	                "Error",
	                JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        if (file.length() == 0) {
	            JOptionPane.showMessageDialog(null,
	                "El archivo Excel está vacío",
	                "Error",
	                JOptionPane.WARNING_MESSAGE);
	            return;
	        }

	        new SwingWorker<Void, Void>() {
	            @Override
	            protected Void doInBackground() throws Exception {
	                try (FileInputStream fis = new FileInputStream(file);
	                     Workbook workbook = WorkbookFactory.create(fis)) {

	                    Sheet sheet = workbook.getSheetAt(0);
	                    SwingUtilities.invokeLater(() -> modeloTabla.setRowCount(0));

	                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
	                        Row row = sheet.getRow(i);
	                        if (row != null) {
	                            Object[] fila = new Object[7]; // Ajusta según tus columnas

	                            for (int j = 0; j < fila.length; j++) {
	                                Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

	                                // Manejo especial para columna de imagen (índice 5)
	                                if (j == 5) {
	                                    fila[j] = cell.getStringCellValue().trim(); // Guardar solo el nombre del archivo
	                                }
	                                // Manejo para ID (columna 0)
	                                else if (j == 0) {
	                                    fila[j] = (int) cell.getNumericCellValue();
	                                }
	                                // Manejo para Precio (columna 3)
	                                else if (j == 3) {
	                                    fila[j] = cell.getNumericCellValue();
	                                }
	                                // Para el resto de columnas
	                                else {
	                                    fila[j] = cell.getStringCellValue().trim();
	                                }
	                            }



	                            final Object[] filaFinal = fila;
	                            SwingUtilities.invokeLater(() -> modeloTabla.addRow(filaFinal));
	                        }


	                    }
	                }
	                return null;
	            }

	            @Override
	            protected void done() {
	                try {
	                    get(); // Para capturar excepciones

	                } catch (Exception e) {
	                    JOptionPane.showMessageDialog(null,
	                        "Error al cargar: " + e.getMessage(),
	                        "Error",
	                        JOptionPane.ERROR_MESSAGE);
	                }
	            }
	        }.execute();
	    }

	    @SuppressWarnings("unused")
		private static boolean existeImagen(String nombreImagen) {
	        if (nombreImagen == null || nombreImagen.trim().isEmpty()) {
	            return false;
	        }
	        return new File("imagenes/" + nombreImagen).exists();
	    }

	public static void actualizarCeldaEnExcel(int fila, int columna, Object valor, String rutaArchivo) {
	    try (FileInputStream fis = new FileInputStream(rutaArchivo);
	         Workbook workbook = new XSSFWorkbook(fis);
	         FileOutputStream fos = new FileOutputStream(rutaArchivo)) {

	        Sheet sheet = workbook.getSheetAt(0);
	        Row row = sheet.getRow(fila + 1); // +1 para saltar la cabecera

	        if (row == null) {
	            row = sheet.createRow(fila + 1);
	        }

	        Cell cell = row.getCell(columna);
	        if (cell == null) {
	            cell = row.createCell(columna);
	        }

	        // Manejar diferentes tipos de datos
	        if (valor instanceof Integer) {
	            cell.setCellValue((Integer) valor);
	        } else if (valor instanceof Double) {
	            cell.setCellValue((Double) valor);
	        } else {
	            cell.setCellValue(valor.toString());
	        }

	        workbook.write(fos);
	    } catch (Exception e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Error al actualizar Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}



	public static void guardarCambiosExcel(DefaultTableModel modeloTabla, String rutaArchivo) {
	    // 1. Crear backup con timestamp
	    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File backupDir = new File("data/backups");
	    if (!backupDir.exists()) {
	        backupDir.mkdirs();
	    }

	    try {
	        Files.copy(Paths.get(rutaArchivo), Paths.get("data/backups/productos_" + timestamp + ".xlsx"));
	    } catch (IOException e) {
	        System.err.println("Error al crear backup: " + e.getMessage());
	    }

	    // 2. Verificar estructura de directorios
	    File directorio = new File("data");
	    if (!directorio.exists() && !directorio.mkdirs()) {
	        JOptionPane.showMessageDialog(null,
	            "No se pudo crear el directorio 'data'",
	            "Error",
	            JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    // 3. Preparar archivos temporales
	    File archivoOriginal = new File(rutaArchivo);
	    File tempFile = new File(rutaArchivo + ".tmp");
	    File backupFile = new File(rutaArchivo + ".backup_" + timestamp);

	    detenerObservadorExcel();

	    try {
	        // 4. Crear respaldo adicional
	        if (archivoOriginal.exists()) {
	            Files.copy(archivoOriginal.toPath(), backupFile.toPath());
	        }

	        // 5. Escribir en archivo temporal
	        try (Workbook workbook = new XSSFWorkbook();
	             FileOutputStream fos = new FileOutputStream(tempFile)) {

	            Sheet sheet = workbook.createSheet("Productos");

	            // Estilos
	            CellStyle integerStyle = workbook.createCellStyle();
	            integerStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));

	            CellStyle headerStyle = workbook.createCellStyle();
	            Font headerFont = workbook.createFont();
	            headerFont.setBold(true);
	            headerStyle.setFont(headerFont);

	            // Escribir cabeceras
	            Row headerRow = sheet.createRow(0);
	            String[] headers = {"ID", "Nombre", "Marca", "Precio", "Descripción", "Imagen",
	                              "Categoría", "Stock", "Stock_Minimo", "Proveedor", "Ventas"};

	            for (int i = 0; i < headers.length; i++) {
	                Cell cell = headerRow.createCell(i);
	                cell.setCellValue(headers[i]);
	                cell.setCellStyle(headerStyle);


	            }

	            // 6. Leer archivo original si existe para preservar datos
	            Workbook originalWorkbook = null;
	            Sheet originalSheet = null;
	            boolean archivoExiste = archivoOriginal.exists();

	            if (archivoExiste) {
	                try {
	                    originalWorkbook = WorkbookFactory.create(archivoOriginal);
	                    originalSheet = originalWorkbook.getSheetAt(0);
	                } catch (Exception e) {
	                    System.err.println("Advertencia: No se pudo leer archivo original: " + e.getMessage());
	                }
	            }

	            // 7. Escribir datos
	            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
	                Row row = sheet.createRow(i + 1);



	                // Escribir columnas visibles (0-6) desde el modelo
	                for (int j = 0; j < 7; j++) {
	                    Object value = modeloTabla.getValueAt(i, j);
	                    Cell cell = row.createCell(j);

	                    if (j == 0) { // Columna ID
	                        cell.setCellStyle(integerStyle);
	                        if (value != null) {
	                            try {
	                                int idValue = (value instanceof Number) ?
	                                    ((Number)value).intValue() : Integer.parseInt(value.toString());
	                                cell.setCellValue(idValue);
	                            } catch (Exception e) {
	                                cell.setCellValue(0);
	                            }
	                        } else {
	                            cell.setCellValue(0);
	                        }
	                    } else {
	                        setCellValueSafely(cell, value);
	                    }
	                }

	                // Manejar columnas ocultas (7-10): Stock, Stock_Minimo, Proveedor, Ventas
	                if (archivoExiste && originalSheet != null && originalSheet.getLastRowNum() >= i + 1) {
	                    // Para filas existentes, copiar valores originales
	                    Row originalRow = originalSheet.getRow(i + 1);
	                    if (originalRow != null) {
	                        for (int k = 7; k <= 10; k++) {
	                            Cell originalCell = originalRow.getCell(k);
	                            if (originalCell != null) {
	                                Cell newCell = row.createCell(k);
	                                copyCellValue(originalCell, newCell);
	                            }
	                        }
	                    }
	                } else {
	                    // Para nuevas filas, establecer valores por defecto
	                    row.createCell(7).setCellValue(0); // Stock inicial
	                    row.createCell(8).setCellValue(5); // Stock_Minimo (valor por defecto)

	                    // Obtener proveedor del modelo si está disponible (columna 9)
	                    if (modeloTabla.getColumnCount() > 9 && modeloTabla.getValueAt(i, 9) != null) {
	                        row.createCell(9).setCellValue(modeloTabla.getValueAt(i, 9).toString());
	                    } else {
	                        row.createCell(9).setCellValue(""); // Proveedor vacío por defecto
	                    }

	                    row.createCell(10).setCellValue(0); // Ventas iniciales
	                }
	            }

	            // Cerrar workbook original si estaba abierto
	            if (originalWorkbook != null) {
	                originalWorkbook.close();
	            }

	            // Autoajustar columnas
	            for (int i = 0; i < headers.length; i++) {
	                sheet.autoSizeColumn(i);
	            }

	            workbook.write(fos);
	        }

	        // 8. Reemplazar archivo original
	        if (archivoOriginal.exists()) {
	            Files.delete(archivoOriginal.toPath());
	        }
	        Files.move(tempFile.toPath(), archivoOriginal.toPath(), StandardCopyOption.REPLACE_EXISTING);

	        JOptionPane.showMessageDialog(null,
	            "Datos guardados con éxito!\nRespaldo: productos_" + timestamp + ".xlsx",
	            "Éxito",
	            JOptionPane.INFORMATION_MESSAGE);

	    } catch (Exception e) {
	        // 9. Restaurar desde respaldo si hubo error
	        try {
	            if (backupFile.exists()) {
	                Files.move(backupFile.toPath(), new File(rutaArchivo).toPath(),
	                         StandardCopyOption.REPLACE_EXISTING);
	            }
	        } catch (IOException ex) {
	            System.err.println("Error al restaurar backup: " + ex.getMessage());
	        }

	        JOptionPane.showMessageDialog(null,
	            "Error al guardar. Se restauró la versión anterior.\nDetalle: " + e.getMessage(),
	            "Error",
	            JOptionPane.ERROR_MESSAGE);
	    } finally {
	        // 10. Limpieza y reinicio
	        if (tempFile.exists()) {
	            tempFile.delete();
	        }
	        iniciarObservadorExcel(modeloTabla);
	    }
	}

	public static void verificarEstructuraExcel(String rutaArchivo) {
	    File archivo = new File(rutaArchivo);
	    if (!archivo.exists()) {
	        try (Workbook workbook = new XSSFWorkbook();
	             FileOutputStream fos = new FileOutputStream(archivo)) {

	            Sheet sheet = workbook.createSheet("Productos");
	            Row headerRow = sheet.createRow(0);

	            String[] headers = {"ID", "Nombre", "Marca", "Precio", "Descripción", "Imagen",
	                              "Categoría", "Stock", "Stock_Minimo", "Proveedor", "Ventas"};

	            for (int i = 0; i < headers.length; i++) {
	                headerRow.createCell(i).setCellValue(headers[i]);
	            }

	            workbook.write(fos);
	        } catch (Exception e) {
	            System.err.println("Error al crear estructura inicial: " + e.getMessage());
	        }
	    }
	}


	// Métodos auxiliares (se mantienen igual que en la versión anterior)
	private static void copyCellValue(Cell source, Cell target) {
	    if (source == null || target == null) {
			return;
		}

	    switch (source.getCellType()) {
	        case NUMERIC:
	            target.setCellValue(source.getNumericCellValue());
	            break;
	        case STRING:
	            target.setCellValue(source.getStringCellValue());
	            break;
	        case BOOLEAN:
	            target.setCellValue(source.getBooleanCellValue());
	            break;
	        case FORMULA:
	            target.setCellValue(source.getCellFormula());
	            break;
	        default:
	            target.setCellValue("");
	    }
	}

	private static void setCellValueSafely(Cell cell, Object value) {
	    if (value == null) {
	        cell.setCellValue("");
	    } else if (value instanceof Integer) {
	        cell.setCellValue((Integer) value);
	    } else if (value instanceof Double) {
	        cell.setCellValue((Double) value);
	    } else if (value instanceof Number) {
	        cell.setCellValue(((Number) value).doubleValue());
	    } else {
	        cell.setCellValue(value.toString());
	    }
	}





	// En controlador/ProductoController.java
	public static void actualizarStockEnExcel(int idProducto, int cantidadModificada, String motivo) {
	    // Validar parámetros
	    if (motivo == null || motivo.trim().isEmpty()) {
	        throw new IllegalArgumentException("El motivo no puede estar vacío");
	    }

	    try {
	        // 1. Abrir archivo Excel
	        File excelFile = new File("data/productos.xlsx");
	        if (!excelFile.exists()) {
	            throw new FileNotFoundException("Archivo de productos no encontrado");
	        }

	        try (FileInputStream fis = new FileInputStream(excelFile);
	             Workbook workbook = WorkbookFactory.create(fis)) {

	            Sheet sheet = workbook.getSheetAt(0);
	            boolean productoEncontrado = false;

	            // 2. Buscar producto por ID
	            for (Row row : sheet) {
	                if (row.getRowNum() == 0)
					 {
						continue; // Saltar cabecera
					}

	                if ((int) row.getCell(0).getNumericCellValue() == idProducto) {
	                    productoEncontrado = true;
	                    int stockActual = (int) row.getCell(7).getNumericCellValue(); // Columna Stock

	                    // 3. Actualizar stock (forma más robusta)
	                    int nuevoStock = stockActual + cantidadModificada; // cantidadModificada puede ser + o -

	                    // Validar que el stock no sea negativo
	                    if (nuevoStock < 0) {
	                        throw new IllegalStateException(
	                            String.format("Stock no puede ser negativo (Producto ID: %d, Stock actual: %d, Cambio: %d)",
	                                idProducto, stockActual, cantidadModificada));
	                    }

	                    row.getCell(7).setCellValue(nuevoStock);

	                    // 4. Registrar movimiento (opcional)
	                    Cell motivoCell = row.getCell(8); // Asumiendo columna 8 para motivo
	                    if (motivoCell == null) {
							motivoCell = row.createCell(8);
						}
	                    motivoCell.setCellValue(motivo + " - " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

	                    break;
	                }
	            }

	            if (!productoEncontrado) {
	                throw new IllegalArgumentException("Producto con ID " + idProducto + " no encontrado");
	            }

	            // 5. Guardar cambios
	            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
	                workbook.write(fos);
	            }
	        }
	    } catch (FileNotFoundException e) {
	        JOptionPane.showMessageDialog(null,
	            "Archivo de productos no encontrado en: data/productos.xlsx",
	            "Error", JOptionPane.ERROR_MESSAGE);
	        throw new RuntimeException("Archivo no encontrado", e);
	    } catch (IllegalStateException | IllegalArgumentException e) {
	        JOptionPane.showMessageDialog(null,
	            e.getMessage(),
	            "Error de validación", JOptionPane.ERROR_MESSAGE);
	        throw e; // Relanzar para manejo superior
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null,
	            "Error técnico al actualizar stock: " + e.getMessage(),
	            "Error", JOptionPane.ERROR_MESSAGE);
	        throw new RuntimeException("Error al actualizar stock", e);
	    }
	}

	// En ProductoController.java
	private static void createBackup(String sourcePath) throws IOException {
	    String backupPath = "data/backups/" + Instant.now().toString().replace(":", "-") + "_productos.xlsx";
	    Files.copy(Paths.get(sourcePath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
	}

	DefaultTableModel modeloTabla = new DefaultTableModel() {
	    @Override
	    public Class<?> getColumnClass(int columnIndex) {
	        if (columnIndex == 3)
			 {
				return Double.class; // Columna "Precio" es numérica
			}
	        return String.class;
	    }


	};

	public static synchronized boolean registrarMovimientoEnExcel(int idProducto, String nombreProducto,
	        String tipo, int cantidad, String motivo, int stockActual) {

	    // 1. Validaciones iniciales
	    if (motivo == null || motivo.trim().isEmpty()) {
	        throw new IllegalArgumentException("El motivo no puede estar vacío");
	    }
	    if (cantidad <= 0) {
	        throw new IllegalArgumentException("La cantidad debe ser positiva");
	    }

	    // 2. Configuración de rutas
	    Path productosPath = Paths.get("data/productos.xlsx");
	    Path movimientosPath = Paths.get("data/movimientos.xlsx");
	    Path tempMovimientosPath = Paths.get("data/movimientos_temp_" + System.nanoTime() + ".xlsx");
	    Path backupMovimientosPath = Paths.get("data/backups/movimientos_backup_" +
	        new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) + ".xlsx");

	    // 3. Detener observador de archivos
	    ExcelLector.detenerObservador();
	    boolean exito = false;
	    Workbook movimientosWB = null;

	    try {
	        // 4. Crear backup del archivo de movimientos
	        if (Files.exists(movimientosPath)) {
	            Files.copy(movimientosPath, backupMovimientosPath, StandardCopyOption.REPLACE_EXISTING);
	        }

	        // 5. Cargar o crear workbook de movimientos
	        if (!Files.exists(movimientosPath) || Files.size(movimientosPath) == 0) {
	            movimientosWB = new XSSFWorkbook();
	        } else {
	            try (InputStream is = Files.newInputStream(movimientosPath)) {
	                movimientosWB = WorkbookFactory.create(is);
	            } catch (Exception e) {
	                System.err.println("Archivo corrupto, creando nuevo...");
	                movimientosWB = new XSSFWorkbook();
	            }
	        }

	        // 6. Configurar hoja de movimientos
	        Sheet movimientosSheet = movimientosWB.getSheet("Movimientos");
	        if (movimientosSheet == null) {
	            movimientosSheet = movimientosWB.createSheet("Movimientos");
	            String[] headers = {"Fecha", "ID Producto", "Producto", "Tipo",
	                              "Cantidad", "Stock Resultante", "Motivo"};
	            Row headerRow = movimientosSheet.createRow(0);
	            for (int i = 0; i < headers.length; i++) {
	                headerRow.createCell(i).setCellValue(headers[i]);
	            }
	        }

	        // 7. Registrar movimiento
	        Row newRow = movimientosSheet.createRow(movimientosSheet.getLastRowNum() + 1);
	        int nuevoStock = tipo.equals("ENTRADA") ? stockActual + cantidad : stockActual - cantidad;

	        newRow.createCell(0).setCellValue(new Date().toString());
	        newRow.createCell(1).setCellValue(idProducto);
	        newRow.createCell(2).setCellValue(nombreProducto);
	        newRow.createCell(3).setCellValue(tipo);
	        newRow.createCell(4).setCellValue(cantidad);
	        newRow.createCell(5).setCellValue(nuevoStock);
	        newRow.createCell(6).setCellValue(motivo);

	        // 8. Guardar en archivo temporal
	        try (OutputStream os = Files.newOutputStream(tempMovimientosPath)) {
	            movimientosWB.write(os);
	        }

	        // 9. Reemplazo atómico del archivo
	        exito = reemplazarArchivoAtomico(tempMovimientosPath, movimientosPath);

	        // 10. Actualizar stock en productos.xlsx
	        if (exito) {
	            exito = actualizarStockProducto(idProducto, tipo.equals("ENTRADA") ? cantidad : -cantidad);
	        }

	    } catch (Exception e) {
	        System.err.println("Error al registrar movimiento: " + e.getMessage());
	        e.printStackTrace();
	        // Restaurar backup si es necesario
	        if (Files.exists(backupMovimientosPath)) {
	            try {
	                Files.copy(backupMovimientosPath, movimientosPath, StandardCopyOption.REPLACE_EXISTING);
	            } catch (IOException ex) {
	                System.err.println("Error al restaurar backup: " + ex.getMessage());
	            }
	        }
	    } finally {
	        try {
	            if (movimientosWB != null) {
					movimientosWB.close();
				}
	            Files.deleteIfExists(tempMovimientosPath);
	        } catch (IOException e) {
	            System.err.println("Error al limpiar recursos: " + e.getMessage());
	        }
	        ExcelLector.reiniciarObservador();
	    }

	    if (!exito) {
	        throw new IllegalStateException("No se pudo registrar el movimiento");
	    }
	    return exito;
	}

	// Métodos auxiliares
	private static boolean reemplazarArchivoAtomico(Path origen, Path destino) {
	    try {
	        // Intento de movimiento atómico (mejor para Windows)
	        try {
	            Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	            return true;
	        } catch (AtomicMoveNotSupportedException e) {
	            // Fallback para sistemas sin soporte atómico
	            Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
	            return true;
	        }
	    } catch (IOException e) {
	        System.err.println("Error en reemplazo atómico: " + e.getMessage());
	        return false;
	    }
	}

	private static boolean actualizarStockProducto(int idProducto, int cantidadModificada) {
	    Path productosPath = Paths.get("data/productos.xlsx");
	    Path tempPath = Paths.get("data/productos_temp_" + System.nanoTime() + ".xlsx");
	    Path backupPath = Paths.get("data/backups/productos_backup_" +
	        new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) + ".xlsx");

	    try {
	        // Crear backup
	        Files.copy(productosPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

	        // Procesar archivo
	        try (Workbook workbook = WorkbookFactory.create(productosPath.toFile());
	             OutputStream os = Files.newOutputStream(tempPath)) {

	            Sheet sheet = workbook.getSheetAt(0);
	            boolean encontrado = false;

	            for (Row row : sheet) {
	                if (row.getRowNum() == 0) {
						continue;
					}

	                Cell idCell = row.getCell(0);
	                if (idCell != null && (int)idCell.getNumericCellValue() == idProducto) {
	                    Cell stockCell = row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    stockCell.setCellValue(stockCell.getNumericCellValue() + cantidadModificada);
	                    encontrado = true;
	                    break;
	                }
	            }

	            if (!encontrado) {
					return false;
				}
	            workbook.write(os);
	        }

	        // Reemplazo atómico
	        return reemplazarArchivoAtomico(tempPath, productosPath);

	    } catch (Exception e) {
	        System.err.println("Error al actualizar stock: " + e.getMessage());
	        // Restaurar backup si falla
	        if (Files.exists(backupPath)) {
	            try {
	                Files.copy(backupPath, productosPath, StandardCopyOption.REPLACE_EXISTING);
	            } catch (IOException ex) {
	                System.err.println("Error al restaurar backup: " + ex.getMessage());
	            }
	        }
	        return false;
	    } finally {
	        try {
	            Files.deleteIfExists(tempPath);
	        } catch (IOException e) {
	            System.err.println("Error al eliminar temporal: " + e.getMessage());
	        }
	    }
	}

	private static boolean reemplazarArchivoConReintentos(File origen, File destino, int maxReintentos) {
	    int intentos = 0;
	    while (intentos < maxReintentos) {
	        try {
	            // Pequeña pausa entre intentos
	            Thread.sleep(200 * intentos);
	            Files.move(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
	            return true;
	        } catch (Exception e) {
	            intentos++;
	            System.err.println("Intento " + intentos + " fallido: " + e.getMessage());
	        }
	    }
	    return false;
	}

	private static boolean actualizarStockProductoSeguro(int idProducto, int cantidadModificada) {
	    File productosFile = new File("data/productos.xlsx");
	    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
	    File tempFile = new File("data/productos_temp_" + timestamp + ".xlsx");
	    File backupFile = new File("data/backups/productos_backup_" + timestamp + ".xlsx");

	    try (Workbook workbook = WorkbookFactory.create(productosFile);
	         FileOutputStream fos = new FileOutputStream(tempFile)) {

	        // 1. Crear backup
	        Files.copy(productosFile.toPath(), backupFile.toPath());

	        // 2. Actualizar stock
	        Sheet sheet = workbook.getSheetAt(0);
	        boolean encontrado = false;
	        for (Row row : sheet) {
	            if (row.getRowNum() == 0) {
					continue;
				}
	            Cell idCell = row.getCell(0);
	            if (idCell != null && (int) idCell.getNumericCellValue() == idProducto) {
	                Cell stockCell = row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                stockCell.setCellValue(stockCell.getNumericCellValue() + cantidadModificada);
	                encontrado = true;
	                break;
	            }
	        }
	        if (!encontrado) {
				return false;
			}

	        // 3. Guardar cambios
	        workbook.write(fos);
	        return reemplazarArchivoConReintentos(tempFile, productosFile, 3);

	    } catch (Exception e) {
	        System.err.println("Error al actualizar stock: " + e.getMessage());
	        try {
	            // Restaurar desde backup si falla
	            if (backupFile.exists()) {
	                Files.copy(backupFile.toPath(), productosFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	            }
	        } catch (IOException ex) {
	            System.err.println("Error al restaurar backup: " + ex.getMessage());
	        }
	        return false;
	    } finally {
	        if (tempFile.exists()) {
				tempFile.delete();
			}
	    }
	}


}