package controlador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFRecibo {
    private static final String RECIBOS_DIR = "data/recibos/";

    public static Map<String, Double> calcularTotalesDelDia(LocalDate fecha) {
        Map<String, Double> totals = new HashMap<>();
        totals.put("EFECTIVO", 0.0);
        totals.put("TARJETA", 0.0);

        try (Stream<Path> paths = Files.walk(Paths.get(RECIBOS_DIR))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".pdf"))
                 .forEach(path -> {
                     try {
                         PDDocument doc = PDDocument.load(path.toFile());
                         String text = new PDFTextStripper().getText(doc);
                         doc.close();

                         if (text.contains(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))) {
                             String tipoPago = extraerTipoPago(text);
                             double total = extraerTotal(text);
                             totals.put(tipoPago, totals.get(tipoPago) + total);
                         }
                     } catch (IOException e) {
                         System.err.println("Error leyendo PDF: " + path.getFileName());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Error accediendo a recibos: " + e.getMessage());
        }
        return totals;
    }

    private static String extraerTipoPago(String pdfText) {
        return pdfText.contains("Forma de pago: EFECTIVO") ? "EFECTIVO" : "TARJETA";
    }

    private static double extraerTotal(String pdfText) {
        // Busca el patrón "Total: $X,XXX.XX"
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
            "Total:\\s*\\$([\\d,]+\\.[\\d]{2})").matcher(pdfText);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1).replace(",", ""));
        }
        return 0.0;
    }


    public static Map<String, Double> calcularTotalesPorFecha(LocalDate fecha) {
        Map<String, Double> totals = new HashMap<>();
        totals.put("EFECTIVO", 0.0);
        totals.put("TARJETA", 0.0);
        totals.put("RETIROS", 0.0); // Nuevo campo

        try (Stream<Path> paths = Files.walk(Paths.get(RECIBOS_DIR))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".pdf"))
                 .forEach(path -> {
                     try {
                         String nombreArchivo = path.getFileName().toString();
                         LocalDate fechaRecibo = extraerFechaDelNombre(nombreArchivo);

                         if (fechaRecibo.equals(fecha)) {
                             PDDocument doc = PDDocument.load(path.toFile());
                             String text = new PDFTextStripper().getText(doc);
                             doc.close();

                             String tipoPago = extraerTipoPago(text);
                             double total = extraerTotal(text);
                             totals.put(tipoPago, totals.get(tipoPago) + total);

                             // Detectar retiros (si el PDF es de corte)
                             if (text.contains("REPORTE DE CORTE")) {
                                 double retiros = extraerRetiros(text);
                                 totals.put("RETIROS", totals.get("RETIROS") + retiros);
                             }
                         }
                     } catch (Exception e) {
                         System.err.println("Error procesando: " + path.getFileName());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Error accediendo a recibos: " + e.getMessage());
        }
        return totals;
    }

    private static LocalDate extraerFechaDelNombre(String nombreArchivo) {
        // Ejemplo: "recibo_20240515_142530.pdf"
        String fechaStr = nombreArchivo.split("_")[1]; // 20240515
        return LocalDate.parse(fechaStr, DateTimeFormatter.BASIC_ISO_DATE);
    }

    private static double extraerRetiros(String pdfText) {
        // Busca "Retiros: $X,XXX.XX"
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
            "Retiros:\\s*\\$([\\d,]+\\.[\\d]{2})").matcher(pdfText);
        return matcher.find() ? Double.parseDouble(matcher.group(1).replace(",", "")) : 0.0;
    }


}