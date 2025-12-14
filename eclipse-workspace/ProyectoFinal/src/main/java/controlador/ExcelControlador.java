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
        try {
            return MySQLControlador.obtenerProductos();
        } catch (Exception e) {
            throw new IOException("Error al leer productos desde MySQL", e);
        }
    }

    public static List<Movimiento> leerMovimientosDesdeExcel() throws IOException {
        try {
            return MySQLControlador.obtenerMovimientos();
        } catch (Exception e) {
            throw new IOException("Error al leer movimientos desde MySQL", e);
        }
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
        try {
            MySQLControlador.actualizarVentas(idProducto, nuevasVentas);
        } catch (Exception e) {
            System.err.println("Error al actualizar ventas en MySQL: " + e.getMessage());
        }
    }



    public static boolean actualizarStockMinimoEnExcel(int idProducto, int nuevoMinimo) {
        try {
            return MySQLControlador.actualizarStockMinimo(idProducto, nuevoMinimo);
        } catch (Exception e) {
            System.err.println("Error al actualizar stock mínimo en MySQL: " + e.getMessage());
            return false;
        }
    }



	// En ExcelController.java
    public static boolean registrarMovimientoInventario(int id, String nombre, int cantidad, String motivo, int stockActual) {
	    try {
	    	return MySQLControlador.registrarMovimientoInventario(id, nombre, cantidad, motivo);
	    } catch (Exception e) {
	        System.err.println("Error al registrar movimiento en MySQL: " + e.getMessage());
	        return false;
	    }
	}

}
