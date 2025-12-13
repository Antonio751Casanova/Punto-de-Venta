package controlador;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUtils {

	public static String leerCeldaComoString(Cell cell) {
        if (cell == null) {
			return "";
		}

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue()
                             .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                return String.format("%.2f", cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return leerCeldaComoString(cell.getCachedFormulaResultType());

            default:
                return "";
        }
    }

	// En ExcelUtils.java - Añade este método
	public static boolean isFileLocked(String filePath) {
	    try {
	        File file = new File(filePath);
	        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
	        FileLock lock = channel.tryLock();
	        if (lock != null) {
	            lock.release();
	            channel.close();
	            return false;
	        }
	        return true;
	    } catch (Exception e) {
	        return true;
	    }


	}

	public static boolean verificarFormatoCeldas(File excelFile) {
	    try (Workbook workbook = WorkbookFactory.create(excelFile)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Row header = sheet.getRow(0);

	        // Verificar que las columnas clave existen
	        if (!header.getCell(7).getStringCellValue().equalsIgnoreCase("Stock")) {
	            throw new Exception("La columna de Stock no está en la posición esperada (columna H)");
	        }

	        // Verificar formato de celdas
	        for (Row row : sheet) {
	            if (row.getRowNum() == 0) {
					continue;
				}

	            Cell stockCell = row.getCell(7);
	            if (stockCell != null && stockCell.getCellType() != CellType.NUMERIC) {
	                System.err.println("Advertencia: Celda de stock no numérica en fila " + row.getRowNum());
	                return false;
	            }
	        }
	        return true;
	    } catch (Exception e) {
	        System.err.println("Error al verificar formato: " + e.getMessage());
	        return false;
	    }
	}


	@SuppressWarnings("deprecation")
	public static void repararArchivoExcel() {
	    File original = new File("data/productos.xlsx");
	    File backup = new File("data/productos_backup_pre_reparacion.xlsx");

	    try {
	        // 1. Crear backup
	        Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);

	        // 2. Procesar archivo
	        Workbook workbook = WorkbookFactory.create(original);
	        Sheet sheet = workbook.getSheetAt(0);
	        boolean cambios = false;

	        for (Row row : sheet) {
	            if (row.getRowNum() == 0) {
					continue;
				}

	            Cell stockCell = row.getCell(7); // Columna H
	            if (stockCell != null && stockCell.getCellType() != CellType.NUMERIC) {
	                try {
	                    double valor = Double.parseDouble(stockCell.toString());
	                    stockCell.setCellValue(valor);
	                    stockCell.setCellType(CellType.NUMERIC);
	                    cambios = true;
	                    System.out.println("Reparada celda en fila " + row.getRowNum());
	                } catch (NumberFormatException e) {
	                    stockCell.setCellValue(0); // Valor por defecto
	                    cambios = true;
	                }
	            }
	        }

	        if (cambios) {
	            try (FileOutputStream fos = new FileOutputStream(original)) {
	                workbook.write(fos);
	                JOptionPane.showMessageDialog(null,
	                    "Archivo reparado automáticamente. Se creó backup en:\n" +
	                    backup.getAbsolutePath(),
	                    "Reparación exitosa", JOptionPane.INFORMATION_MESSAGE);
	            }
	        }
	        workbook.close();
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null,
	            "Error al reparar archivo: " + e.getMessage(),
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	// En ExcelUtils.java
	public static void repararArchivoExcelCompleto() {
	    try {
	        File original = new File("data/productos.xlsx");
	        File backup = new File("data/productos_backup_" + System.currentTimeMillis() + ".xlsx");

	        // 1. Crear backup
	        Files.copy(original.toPath(), backup.toPath());

	        // 2. Procesar archivo
	        Workbook workbook = WorkbookFactory.create(original);
	        Sheet sheet = workbook.getSheetAt(0);
	        boolean cambios = false;

	        for (Row row : sheet) {
	            if (row == null || row.getRowNum() == 0) {
					continue;
				}

	            // Reparar stock (columna H)
	            Cell stockCell = row.getCell(7);
	            if (stockCell != null) {
	                try {
	                    double valor = Double.parseDouble(stockCell.toString());
	                    if (stockCell.getCellType() != CellType.NUMERIC ||
	                        stockCell.getNumericCellValue() != valor) {
	                        stockCell.setCellValue(valor);
	                        cambios = true;
	                    }
	                } catch (NumberFormatException e) {
	                    stockCell.setCellValue(0); // Valor por defecto
	                    cambios = true;
	                }
	            } else {
	                row.createCell(7, CellType.NUMERIC).setCellValue(0);
	                cambios = true;
	            }
	        }

	        if (cambios) {
	            try (FileOutputStream fos = new FileOutputStream(original)) {
	                workbook.write(fos);
	                JOptionPane.showMessageDialog(null,
	                    "Archivo reparado con éxito.\nBackup creado en:\n" + backup.getAbsolutePath(),
	                    "Reparación Exitosa", JOptionPane.INFORMATION_MESSAGE);
	            }
	        }
	        workbook.close();
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null,
	            "Error durante reparación: " + e.getMessage(),
	            "Error Crítico", JOptionPane.ERROR_MESSAGE);
	    }
	}



	 // Método sobrecargado para manejar CellType directamente
    public static String leerCeldaComoString(CellType cellType) {
        switch (cellType) {
            case STRING: return "(valor fórmula - texto)";
            case NUMERIC: return "(valor fórmula - número)";
            case BOOLEAN: return "(valor fórmula - booleano)";
            case BLANK: return "";
            default: return "";
        }
    }

    public static LocalDate leerCeldaComoFecha(Cell cell, DateTimeFormatter formatter) {
        try {
            String fechaStr = leerCeldaComoString(cell);
            return LocalDate.parse(fechaStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isExcelAvailable(String ruta) {
        try (FileInputStream fis = new FileInputStream(ruta)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int leerCeldaComoInt(Cell cell) {
        return (int) leerCeldaComoDouble(cell);
    }

    public static double leerCeldaComoDouble(Cell cell) {
        try {
            return Double.parseDouble(leerCeldaComoString(cell));
        } catch (Exception e) {
            return 0.0;
        }
    }
}