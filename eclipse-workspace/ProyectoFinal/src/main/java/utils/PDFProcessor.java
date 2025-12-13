package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFProcessor {
    private static final String RECIBOS_DIR = "data/recibos/";

    public static Map<String, Double> procesarRecibosDelDia(LocalDate fecha) {
        Map<String, Double> resultados = new HashMap<>();
        resultados.put("EFECTIVO", 0.0);
        resultados.put("TARJETA", 0.0);
        resultados.put("TRANSFERENCIA", 0.0);
        resultados.put("TOTAL_RECIBOS", 0.0);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaBusqueda = fecha.format(dateFormatter);

        try (Stream<Path> paths = Files.list(Paths.get(RECIBOS_DIR))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                 .forEach(path -> {
                     try (PDDocument doc = PDDocument.load(path.toFile())) {
                         String text = new PDFTextStripper().getText(doc);

                         if (text.contains(fechaBusqueda)) {
                             String tipoPago = extraerTipoPago(text);
                             double monto = extraerMonto(text);

                             resultados.put(tipoPago, resultados.get(tipoPago) + monto);
                             resultados.put("TOTAL_RECIBOS", resultados.get("TOTAL_RECIBOS") + 1);
                         }
                     } catch (IOException e) {
                         System.err.println("Error procesando: " + path.getFileName());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Error accediendo a recibos: " + e.getMessage());
        }
        return resultados;
    }

    /**
     * Obtiene todas las fechas distintas que tienen recibos PDF en la carpeta.
     * @return Lista de fechas ordenadas (sin duplicados)
     */
    public static List<LocalDate> obtenerFechasConRecibos() throws IOException {
        List<LocalDate> fechas = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Stream<Path> paths = Files.list(Paths.get(RECIBOS_DIR))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                 .forEach(path -> {
                     try (PDDocument doc = PDDocument.load(path.toFile())) {
                         String text = new PDFTextStripper().getText(doc);
                         // Busca patrones de fecha dd/MM/yyyy en el texto
                         Matcher matcher = Pattern.compile("\\d{2}/\\d{2}/\\d{4}").matcher(text);
                         if (matcher.find()) {
                             fechas.add(LocalDate.parse(matcher.group(), dateFormatter));
                         }
                     } catch (Exception e) {
                         System.err.println("Error procesando PDF: " + path.getFileName());
                     }
                 });
        }

        // Eliminar duplicados y ordenar
        return fechas.stream()
                     .distinct()
                     .sorted()
                     .collect(Collectors.toList());
    }

    /**
     * Verifica si existen recibos para una fecha específica.
     * @param fecha Fecha a verificar
     * @return true si hay al menos un recibo para esa fecha
     */
    public static boolean existenRecibosParaFecha(LocalDate fecha) throws IOException {
        return obtenerFechasConRecibos().contains(fecha);
    }

    private static String extraerTipoPago(String texto) {
        // Busca "EFECTIVO", "TARJETA" o "TRANSFERENCIA"
        Pattern pattern = Pattern.compile("(?i)(Forma de pago|Pago):?\\s*(EFECTIVO|TARJETA|TRANSFERENCIA|TRANSFER|CRÉDITO|DEBITO)");
        java.util.regex.Matcher matcher = pattern.matcher(texto);

        if (matcher.find()) {
            String tipo = matcher.group(2).toUpperCase();
            if (tipo.contains("EFECTIVO")) {
				return "EFECTIVO";
			}
            if (tipo.contains("TARJETA") || tipo.contains("CRÉDITO") || tipo.contains("DEBITO")) {
				return "TARJETA";
			}
            if (tipo.contains("TRANSFER")) {
				return "TRANSFERENCIA";
			}
        }
        return "TARJETA"; // Valor por defecto
    }

    private static double extraerMonto(String texto) {
        // Busca el total considerando diferentes formatos
        java.util.regex.Matcher matcher = Pattern.compile(
            "(?i)(Total|TOTAL|Importe):?\\s*\\$?\\s*([\\d,]+\\.[\\d]{2})").matcher(texto);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(2).replace(",", ""));
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear monto: " + matcher.group(2));
            }
        }
        return 0.0;
    }
}