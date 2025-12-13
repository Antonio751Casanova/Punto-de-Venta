package controlador;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ReporteControlador {
    private String rutaExcel = "data/productos.xlsx";
    private String rutaRecibos = "data/recibos/";

    // Método para obtener productos más vendidos (ejemplo)
    public Map<String, Integer> getProductosMasVendidos() {
        Map<String, Integer> productosVendidos = new HashMap<>();
        // Simulación: Leer desde Excel o PDFs (implementa según tu lógica)
        productosVendidos.put("Laptop HP", 45);
        productosVendidos.put("iPhone 16", 30);
        return productosVendidos;
    }



    // Exportar reporte a Excel
    public void exportarAExcel(Map<String, Integer> datos, String rutaSalida) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte");
            int rowNum = 0;

            // Encabezados
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Producto");
            headerRow.createCell(1).setCellValue("Cantidad Vendida");

            // Datos
            for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            // Corrección: Usar FileOutputStream en lugar de File directamente
            try (FileOutputStream outputStream = new FileOutputStream(rutaSalida)) {
                workbook.write(outputStream); // ¡Aquí está el cambio clave!
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al exportar: " + e.getMessage());
        }
    }


}