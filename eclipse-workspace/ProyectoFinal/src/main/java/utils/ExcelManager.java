package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelManager {
    private static final String FILE_PATH = "data/cortes_caja.xlsx";
    private static final String[] HEADERS = {
        "Fecha", "Monto Inicial", "Ventas Efectivo",
        "Ventas Tarjeta", "Ventas Transferencia", "Retiros",
        "Monto Final", "Observaciones"
    };

    public static void guardarCorte(LocalDate fecha,
                                  double montoInicial,
                                  double ventasEfectivo,
                                  double ventasTarjeta,
                                  double ventasTransferencia,
                                  double retiros,
                                  double montoFinal,
                                  String observaciones) throws IOException {

        Workbook workbook;
        Sheet sheet;
        File file = new File(FILE_PATH);

        if (file.exists()) {
            workbook = WorkbookFactory.create(new FileInputStream(file));
            sheet = workbook.getSheetAt(0);

            // Verificar si las cabeceras coinciden
            if (sheet.getRow(0) == null || sheet.getRow(0).getPhysicalNumberOfCells() != HEADERS.length) {
                workbook.close();
                file.delete(); // Eliminar archivo si la estructura no coincide
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Cortes de Caja");
                crearCabeceras(sheet);
            }
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Cortes de Caja");
            crearCabeceras(sheet);
        }

        int lastRow = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRow + 1);

        // Formatear fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Llenar datos
        row.createCell(0).setCellValue(fecha.format(formatter));
        row.createCell(1).setCellValue(montoInicial);
        row.createCell(2).setCellValue(ventasEfectivo);
        row.createCell(3).setCellValue(ventasTarjeta);
        row.createCell(4).setCellValue(ventasTransferencia);
        row.createCell(5).setCellValue(retiros);
        row.createCell(6).setCellValue(montoFinal);
        row.createCell(7).setCellValue(observaciones);

        // Autoajustar columnas
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        } finally {
            workbook.close();
        }
    }

    private static void crearCabeceras(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);

            // Estilo para cabeceras
            CellStyle style = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }
    }

    public static void generarReporteCorteDiario(LocalDate fecha, String rutaSalida) throws IOException {
        Map<String, Double> ventas = PDFProcessor.procesarRecibosDelDia(fecha);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Corte " + fecha.format(DateTimeFormatter.ISO_DATE));

        // Cabeceras
        String[] headers = {"Concepto", "Monto"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Datos
        int rowNum = 1;
        for (Map.Entry<String, Double> entry : ventas.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Guardar
        try (FileOutputStream fos = new FileOutputStream(rutaSalida)) {
            workbook.write(fos);
        } finally {
            workbook.close();
        }
    }
}