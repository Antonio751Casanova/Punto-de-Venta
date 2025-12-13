package controlador;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import modelo.Movimiento;
import modelo.Producto;

public class ExcelControlador {
    // Ruta por defecto para el archivo Excel
    public static final String RUTA_DEFAULT = "data/productos.xlsx";

    // Método original que recibe la ruta como parámetro
    public static List<Producto> leerProductosDesdeExcel() throws IOException {

    	File excelFile = new File(RUTA_DEFAULT);
        if (!excelFile.exists() || excelFile.length() == 0) {
            throw new IOException("Archivo no existe o está vacío");
        }


        // Verificar integridad del ZIP (estructura Excel)
        try (ZipFile zipFile = new ZipFile(excelFile)) {
            if (zipFile.getEntry("xl/workbook.xml") == null) {
                throw new IOException("Archivo Excel corrupto: falta workbook.xml");
            }
        }

        List<Producto> productos = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(RUTA_DEFAULT);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
				 {
					continue; // Saltar cabecera
				}

                try {
                    // Verificar y obtener ID
                    int id = 0;
                    if (row.getCell(0) != null) {
                        id = (int) row.getCell(0).getNumericCellValue();
                    }

                    // Verificar y obtener nombre (campo obligatorio)
                    String nombre = "";
                    if (row.getCell(1) != null && row.getCell(1).getCellType() != CellType.BLANK) {
                        nombre = row.getCell(1).getStringCellValue().trim();
                    } else {
                        continue; // Saltar fila si no tiene nombre
                    }

                    // Marca (campo opcional)
                    String marca = "";
                    if (row.getCell(2) != null) {
                        marca = row.getCell(2).getStringCellValue().trim();
                    }

                    // Manejo seguro del precio
                    double precio = 0.0;
                    if (row.getCell(3) != null) {
                        Cell precioCell = row.getCell(3);
                        if (precioCell.getCellType() == CellType.NUMERIC) {
                            precio = precioCell.getNumericCellValue();
                        } else if (precioCell.getCellType() == CellType.STRING) {
                            try {
                                precio = Double.parseDouble(precioCell.getStringCellValue().trim());
                            } catch (NumberFormatException e) {
                                System.err.println("Formato de precio inválido en fila " + row.getRowNum());
                            }
                        }
                    }

                    // Descripción (campo opcional)
                    String descripcion = "";
                    if (row.getCell(4) != null) {
                        descripcion = row.getCell(4).getStringCellValue().trim();
                    }

                    // Imagen (campo opcional)
                    String imagenPath = "";
                    if (row.getCell(5) != null) {
                        imagenPath = row.getCell(5).getStringCellValue().trim();
                    }

                    // Nuevos campos: Stock (H), Stock Mínimo (I), Proveedor (J)
                    int stock = 0;
                    if (row.getCell(7) != null && row.getCell(7).getCellType() == CellType.NUMERIC) {
                        stock = (int) row.getCell(7).getNumericCellValue();
                    }

                    int stockMinimo = 0;
                    if (row.getCell(8) != null && row.getCell(8).getCellType() == CellType.NUMERIC) {
                        stockMinimo = (int) row.getCell(8).getNumericCellValue();
                    }

                    String proveedor = "";
                    if (row.getCell(9) != null && row.getCell(9).getCellType() == CellType.STRING) {
                        proveedor = row.getCell(9).getStringCellValue().trim();
                    }

                 // Dentro del bloque donde procesas cada fila:
                    int ventas = 0;
                    if (row.getCell(10) != null && row.getCell(10).getCellType() == CellType.NUMERIC) {
                        ventas = (int) row.getCell(10).getNumericCellValue();
                    }



                    productos.add(new Producto(id, nombre, marca, precio, descripcion,
                                            imagenPath, stock, stockMinimo, proveedor, ventas));

                } catch (Exception e) {
                    System.err.println("Error procesando fila " + row.getRowNum() + ": " + e.getMessage());
                    continue; // Continuar con la siguiente fila si hay error
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer archivo Excel: " + e.getMessage());
            e.printStackTrace();
        }
        return productos;
    }

    public static List<Movimiento> leerMovimientosDesdeExcel() throws IOException {
        List<Movimiento> movimientos = new ArrayList<>();
        Path movimientosPath = Paths.get("data/movimientos.xlsx");

        if (!Files.exists(movimientosPath)) {
            return movimientos;
        }

        // Preparar múltiples formatos de fecha para manejar diferentes casos
        SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US), // Ej: "Tue May 20 22:56:27 CST 2025"
            new SimpleDateFormat("dd/MM/yyyy HH:mm"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("dd-MM-yyyy HH:mm")
        };

        try (FileInputStream fis = new FileInputStream(movimientosPath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
				 {
					continue; // Saltar cabecera
				}

                try {
                    // 1. Manejo de la fecha
                    Date fecha = null;
                    Cell fechaCell = row.getCell(0);

                    if (fechaCell == null) {
                        throw new Exception("Celda de fecha vacía");
                    }

                    // Intentar obtener como fecha de Excel primero
                    try {
                        fecha = fechaCell.getDateCellValue();
                    } catch (Exception e) {
                        // Si falla, intentar parsear como string
                        String fechaStr = fechaCell.getStringCellValue().trim();

                        // Probar todos los formatos hasta que uno funcione
                        for (SimpleDateFormat df : dateFormats) {
                            try {
                                fecha = df.parse(fechaStr);
                                break;
                            } catch (Exception ex) {
                                // Continuar con el siguiente formato
                            }
                        }

                        if (fecha == null) {
                            throw new Exception("Formato de fecha no reconocido: " + fechaStr);
                        }
                    }

                    // 2. Resto de campos (manejo seguro)
                    int idProducto = safeGetNumericValue(row.getCell(1));
                    String nombreProducto = safeGetStringValue(row.getCell(2));
                    String tipo = safeGetStringValue(row.getCell(3));
                    int cantidad = safeGetNumericValue(row.getCell(4));
                    int stockResultante = safeGetNumericValue(row.getCell(5));
                    String motivo = safeGetStringValue(row.getCell(6));

                    movimientos.add(new Movimiento(
                        fecha, idProducto, nombreProducto,
                        tipo, cantidad, stockResultante, motivo
                    ));
                } catch (Exception e) {
                    System.err.println("Error al leer movimiento en fila " + (row.getRowNum()+1) + ": " + e.getMessage());
                    // Continuar con la siguiente fila
                }
            }
        } catch (Exception e) {
            throw new IOException("Error al leer archivo de movimientos: " + e.getMessage(), e);
        }

        return movimientos;
    }

    // Métodos auxiliares para manejo seguro de celdas
    private static int safeGetNumericValue(Cell cell) throws Exception {
        if (cell == null) {
            throw new Exception("Celda numérica vacía");
        }

        try {
            return (int) cell.getNumericCellValue();
        } catch (Exception e) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (Exception ex) {
                throw new Exception("Valor no es un número válido");
            }
        }
    }

    private static String safeGetStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }


	public static void actualizarVentasEnExcel(int idProducto, int nuevasVentas) {
        String ruta = "data/productos.xlsx";
        try (FileInputStream fis = new FileInputStream(ruta);
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(ruta)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
				 {
					continue; // Saltar cabecera
				}

                if ((int) row.getCell(0).getNumericCellValue() == idProducto) {
                    // Columna 10 para ventas (ajusta según tu estructura)
                    row.getCell(10).setCellValue(nuevasVentas);
                    break;
                }
            }

            workbook.write(fos);
        } catch (Exception e) {
            System.err.println("Error al actualizar ventas: " + e.getMessage());
        }
    }



    public static boolean actualizarStockMinimoEnExcel(int idProducto, int nuevoMinimo) {
        try (Workbook workbook = WorkbookFactory.create(new File(RUTA_DEFAULT));
             FileOutputStream fos = new FileOutputStream(RUTA_DEFAULT)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
					continue;
				}
                if ((int) row.getCell(0).getNumericCellValue() == idProducto) {
                    row.getCell(8).setCellValue(nuevoMinimo); // Columna Stock_Minimo
                    workbook.write(fos);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error al actualizar stock mínimo: " + e.getMessage());
            return false;
        }
    }



	// En ExcelController.java
	public static boolean registrarMovimientoInventario(int id, String nombre, int cantidad, String motivo, int stockActual) {
	    try {
	        // 1. Registrar en movimientos.xlsx
	    	ProductoControlador.registrarMovimientoEnExcel(id, nombre, cantidad > 0 ? "ENTRADA" : "SALIDA",
	            Math.abs(cantidad), motivo, stockActual);

	        // 2. Actualizar productos.xlsx
	    	ProductoControlador.actualizarStockEnExcel(id, cantidad, motivo);
	        return true;
	    } catch (Exception e) {
	        System.err.println("Error al registrar movimiento: " + e.getMessage());
	        return false;
	    }
	}

}

