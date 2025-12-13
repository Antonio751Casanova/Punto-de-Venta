package controlador;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import modelo.CorteCaja;

public class CorteCajaControlador {
    private static final String EXCEL_PATH = "data/corte_caja.xlsx";

    public static Map<String, Double> calcularVentasAutomaticas() {
        return PDFRecibo.calcularTotalesDelDia(LocalDate.now());
    }

    public static void guardarCorteAutomatico(double montoInicial, String observaciones) throws IOException {
        Map<String, Double> ventas = calcularVentasAutomaticas();

        CorteCaja corte = new CorteCaja(
            montoInicial,
            ventas.get("EFECTIVO"),
            ventas.get("TARJETA"),
            0.0, // Retiros (ajustar manualmente si es necesario)
            observaciones
        );

        guardarCorte(corte);
    }

    public static void guardarCorte(CorteCaja corte) throws IOException {
        File file = new File(EXCEL_PATH);
        Workbook workbook;
        Sheet sheet;

        // Crear archivo si no existe
        if (!file.exists()) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Cortes");
            crearCabeceras(sheet);
        } else {
            // Validar integridad del archivo existente
            try {
                workbook = WorkbookFactory.create(file);
                sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    sheet = workbook.createSheet("Cortes");
                    crearCabeceras(sheet);
                }
            } catch (Exception e) {
                // Si el archivo está corrupto, crear uno nuevo
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Cortes");
                crearCabeceras(sheet);
            }
        }

        // Buscar si ya existe un corte para esta fecha
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaHoraStr = corte.getFechaHoraFormateada();
        LocalDate fechaCorte = LocalDate.parse(fechaHoraStr.split(" ")[0],
            DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
				continue;
			}

            Cell fechaCell = row.getCell(0);
            if (fechaCell != null) {
                try {
                    String fechaExcelStr = leerCeldaExcel(fechaCell);
                    LocalDate fechaExcel = LocalDate.parse(fechaExcelStr.split(" ")[0],
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    if (fechaExcel.equals(fechaCorte)) {
                        throw new IOException("Ya existe un corte registrado para esta fecha");
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        // Agregar nuevo registro
        int lastRow = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRow + 1);

        newRow.createCell(0).setCellValue(corte.getFechaHoraFormateada());
        newRow.createCell(1).setCellValue(corte.getMontoInicial());
        newRow.createCell(2).setCellValue(corte.getVentasEfectivo());
        newRow.createCell(3).setCellValue(corte.getVentasTarjeta());
        newRow.createCell(4).setCellValue(corte.getRetiros());
        newRow.createCell(5).setCellValue(corte.getMontoFinal());
        newRow.createCell(6).setCellValue(corte.getObservaciones());

        // Guardar cambios
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        } finally {
            workbook.close();
        }


    }


    public static String leerCeldaExcel(Cell cell) {
        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();
        CellType cellType = cell.getCellType();

        // Manejo especial para fórmulas
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                // Manejo de fechas y números
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue()
                           .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                return NumberToTextConverter.toText(cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case BLANK:
                return "";

            default:
                return formatter.formatCellValue(cell);
        }
    }


    public static boolean existeCorteParaHoy() throws IOException {
        File file = new File(EXCEL_PATH);

        // 1. Si el archivo no existe o está vacío, no hay cortes
        if (!file.exists() || file.length() == 0) {
            return false;
        }

        // 2. Verificar contenido del archivo
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Si no hay filas (excepto cabecera), no hay cortes
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return false;
            }

            LocalDate hoy = LocalDate.now();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
				 {
					continue; // Saltar cabecera
				}

                Cell fechaCell = row.getCell(0);
                if (fechaCell != null) {
                    try {
                        LocalDate fechaCorte = fechaCell.getLocalDateTimeCellValue().toLocalDate();
                        if (fechaCorte.equals(hoy)) {
                            return true;
                        }
                    } catch (Exception e) {
                        System.err.println("Error al leer fecha en fila " + row.getRowNum() + ": " + e.getMessage());
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al verificar cortes: " + e.getMessage());
            throw new IOException("El archivo de cortes está corrupto");
        }
        return false;
    }


    public Map<String, Double> calcularVentasDelDia(LocalDate fecha) throws IOException {
        Map<String, Double> ventas = new HashMap<>();
        ventas.put("EFECTIVO", 0.0);
        ventas.put("TARJETA", 0.0);

        try (Workbook workbook = WorkbookFactory.create(new File("data/ventas_diarias.xlsx"))) {
            Sheet sheet = workbook.getSheetAt(0);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
				 {
					continue; // Saltar cabecera
				}

                // Leer fecha
                String fechaStr = leerCeldaExcel(row.getCell(0));
                if (fechaStr.isEmpty()) {
					continue;
				}

                LocalDate fechaVenta;
                try {
                    fechaVenta = LocalDate.parse(fechaStr, dateFormatter);
                } catch (Exception e) {
                    continue; // Si la fecha no es válida
                }

                if (fechaVenta.equals(fecha)) {
                    // Leer tipo de pago (asegurando mayúsculas)
                    String tipoPago = leerCeldaExcel(row.getCell(2)).toUpperCase();

                    // Leer monto (manejo seguro de números)
                    double monto;
                    try {
                        monto = Double.parseDouble(leerCeldaExcel(row.getCell(3)));
                    } catch (NumberFormatException e) {
                        continue; // Saltar si no es número válido
                    }

                    if (tipoPago.equals("EFECTIVO")) {
                        ventas.put("EFECTIVO", ventas.get("EFECTIVO") + monto);
                    } else if (tipoPago.equals("TARJETA")) {
                        ventas.put("TARJETA", ventas.get("TARJETA") + monto);
                    }
                }
            }
        }
        return ventas;
    }


    private static void crearCabeceras(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] columnas = {"Fecha/Hora", "Monto Inicial", "Ventas Efectivo",
                            "Ventas Tarjeta", "Retiros", "Monto Final", "Observaciones"};

        for (int i = 0; i < columnas.length; i++) {
            header.createCell(i).setCellValue(columnas[i]);
        }
    }

    public static List<CorteCaja> obtenerCortesPorFecha(LocalDate fecha) throws IOException {
        List<CorteCaja> cortes = new ArrayList<>();
        // Implementación similar a leerProductosDesdeExcel() pero filtrando por fecha
        return cortes;
    }

    public static void generarReporteHistorico(LocalDate desde, LocalDate hasta) {
        // Genera un reporte consolidado entre fechas
    }

    public static List<CorteCaja> obtenerCortesDelDia() throws IOException {
        List<CorteCaja> cortes = new ArrayList<>();
        // Implementación similar a ExcelController.leerProductosDesdeExcel()
        // ...
        return cortes;
    }
}