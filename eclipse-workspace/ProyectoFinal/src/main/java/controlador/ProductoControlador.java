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
import java.util.List;

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

import modelo.Producto;


public class ProductoControlador {
	 private static boolean isWatching = false;
	 private static DefaultTableModel ultimoModelo;

	 public static void iniciarObservadorExcel(DefaultTableModel modeloTabla) {
		 	ultimoModelo = modeloTabla;
	        isWatching = false; // Persistencia en MySQL, no se requiere observador de archivos
	    }

	    // Detener el observador (opcional)
	    public static void detenerObservadorExcel() {
	        isWatching = false;
	    }


	    public static void cargarDatosExcel(DefaultTableModel modeloTabla) {
	        ultimoModelo = modeloTabla;
	        new SwingWorker<Void, Void>() {
	            @Override
	            protected Void doInBackground() {
	                try {
	                    List<Producto> productos = ExcelControlador.leerProductosDesdeExcel();
	                    SwingUtilities.invokeLater(() -> {
	                        modeloTabla.setRowCount(0);
	                        for (Producto p : productos) {
	                            modeloTabla.addRow(new Object[]{
	                                p.getId(),
	                                p.getNombre(),
	                                p.getMarca(),
	                                p.getPrecio(),
	                                p.getDescripcion(),
	                                p.getImagenPath(),
	                                p.getCategoria()
	                            });
	                        }
	                    });
	                } catch (Exception e) {
	                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
	                        null,
	                        "Error al cargar productos desde MySQL: " + e.getMessage(),
	                        "Error",
	                        JOptionPane.ERROR_MESSAGE));
	                }
	                return null;
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
	    if (ultimoModelo == null || fila >= ultimoModelo.getRowCount()) {
	    	return;
	    }
	    Object idObj = ultimoModelo.getValueAt(fila, 0);
	    if (idObj == null) {
	    	return;
	    }
	    int idProducto = Integer.parseInt(idObj.toString());
	    String columnaDB = null;
	    switch (columna) {
	        case 1:
	            columnaDB = "nombre";
	            break;
	        case 2:
	            columnaDB = "marca";
	            break;
	        case 3:
	            columnaDB = "precio";
	            break;
	        case 4:
	            columnaDB = "descripcion";
	            break;
	        case 5:
	            columnaDB = "imagen";
	            break;
	        case 6:
	            columnaDB = "categoria";
	            break;
	        default:
	            break;
	    }
	    if (columnaDB == null) {
	    	return;
	    }

	    try {
	        MySQLControlador.actualizarCampoProducto(idProducto, columnaDB, valor);
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null,
	                "Error al actualizar el producto en MySQL: " + e.getMessage(),
	                "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}



	public static void guardarCambiosExcel(DefaultTableModel modeloTabla, String rutaArchivo) {
	    ultimoModelo = modeloTabla;
	    try {
	        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
	            int id = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(i, 0)));
	            String nombre = safeString(modeloTabla.getValueAt(i, 1));
	            String marca = safeString(modeloTabla.getValueAt(i, 2));
	            double precio = modeloTabla.getValueAt(i, 3) instanceof Number
	                    ? ((Number) modeloTabla.getValueAt(i, 3)).doubleValue() : 0d;
	            String descripcion = safeString(modeloTabla.getValueAt(i, 4));
	            String imagen = safeString(modeloTabla.getValueAt(i, 5));
	            String categoria = safeString(modeloTabla.getValueAt(i, 6));

	            Producto producto = new Producto(
	                    id,
	                    nombre,
	                    marca,
	                    precio,
	                    descripcion,
	                    imagen,
	                    0,
	                    0,
	                    "",
	                    0,
	                    categoria
	            );
	            MySQLControlador.upsertProducto(producto);
	        }
	        JOptionPane.showMessageDialog(null,
	                "Productos guardados en MySQL correctamente.",
	                "Éxito",
	                JOptionPane.INFORMATION_MESSAGE);
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null,
	                "Error al guardar en MySQL: " + e.getMessage(),
	                "Error",
	                JOptionPane.ERROR_MESSAGE);
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
	    if (motivo == null || motivo.trim().isEmpty()) {
	        throw new IllegalArgumentException("El motivo no puede estar vacío");
	    }
	    try {
	        String nombre = buscarNombreProducto(idProducto);
	        MySQLControlador.registrarMovimientoInventario(idProducto, nombre, cantidadModificada, motivo);
	    } catch (Exception e) {
	        throw new RuntimeException("Error al actualizar stock en MySQL", e);
	    }
	}

	private static String buscarNombreProducto(int idProducto) {
	    if (ultimoModelo != null) {
	        for (int i = 0; i < ultimoModelo.getRowCount(); i++) {
	            Object idObj = ultimoModelo.getValueAt(i, 0);
	            if (idObj != null && Integer.parseInt(idObj.toString()) == idProducto) {
	                Object nombreObj = ultimoModelo.getValueAt(i, 1);
	                return nombreObj != null ? nombreObj.toString() : "";
	            }
	        }
	    }
	    try {
	        return ExcelControlador.leerProductosDesdeExcel().stream()
	                .filter(p -> p.getId() == idProducto)
	                .map(Producto::getNombre)
	                .findFirst()
	                .orElse("");
	    } catch (Exception e) {
	        return "";
	    }
	}

	private static String safeString(Object valor) {
	    return valor == null ? "" : valor.toString();
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

	    if (motivo == null || motivo.trim().isEmpty()) {
	        throw new IllegalArgumentException("El motivo no puede estar vacío");
	    }
	    if (cantidad <= 0) {
	        throw new IllegalArgumentException("La cantidad debe ser positiva");
	    }

	    int delta = "ENTRADA".equalsIgnoreCase(tipo) ? cantidad : -cantidad;
	    try {
	        return MySQLControlador.registrarMovimientoInventario(idProducto, nombreProducto, delta, motivo);
	    } catch (Exception e) {
	        throw new IllegalStateException("No se pudo registrar el movimiento en MySQL", e);
	    }
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
