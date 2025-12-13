package controlador;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import modelo.CorteCaja;
import modelo.Producto;

public class PDFGenerador {
    private static final String NEGOCIO = "ELECTRONICS TECHNOLOGY";
    private static final String DIRECCION = "Calle Jardin Dorado #464, Fracc. Los Prados";
    private static final String CIUDAD = "Altamira, Tamaulipas";
    private static final float MARGEN = 50;
    private static final float ANCHO_PAGINA = 550;

    public static void generarRecibo(List<Producto> productos, double subtotal, double iva, double total,
            String tipoPago, double pago, double cambio, String cajero, int folio,
            String logoPath) {

        // 1. Manejo mejorado del logo
        String rutaLogoVerificada = null;
        try {
            rutaLogoVerificada = verificarRutaLogo(logoPath);
        } catch (IllegalArgumentException e) {
            System.err.println("Advertencia: " + e.getMessage() + ". Se generará recibo sin logo.");
        }

        // 2. Crear directorio de recibos si no existe
        File directorioRecibos = new File("data/recibos");
        if (!directorioRecibos.exists()) {
            directorioRecibos.mkdirs();
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = 750;

                // 1. Logo (con manejo más robusto)
                if (rutaLogoVerificada != null) {
                    try {
                        PDImageXObject logo = PDImageXObject.createFromFile(rutaLogoVerificada, document);
                        float logoWidth = 150;
                        float logoHeight = logo.getHeight() * (logoWidth / logo.getWidth());
                        float logoX = (ANCHO_PAGINA - logoWidth) / 2;

                        contentStream.drawImage(logo, logoX, yPosition - logoHeight, logoWidth, logoHeight);
                        yPosition -= logoHeight + 20;
                    } catch (IOException e) {
                        System.err.println("Error al cargar el logo, se omitirá: " + e.getMessage());
                    }
                }



                // 2. Encabezado del negocio
                dibujarTextoCentrado(contentStream, NEGOCIO, MARGEN, yPosition, PDType1Font.HELVETICA_BOLD, 16);
                yPosition -= 20;
                dibujarTextoCentrado(contentStream, DIRECCION, MARGEN, yPosition, PDType1Font.HELVETICA, 10);
                yPosition -= 15;
                dibujarTextoCentrado(contentStream, CIUDAD, MARGEN, yPosition, PDType1Font.HELVETICA, 10);
                yPosition -= 25;
                dibujarLinea(contentStream, MARGEN, yPosition, ANCHO_PAGINA - 2*MARGEN);
                yPosition -= 20;

                // 3. Información de la venta
                String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                String[] fechaHoraParts = fechaHora.split(" ");

                dibujarTexto(contentStream, "Fecha: " + fechaHoraParts[0], MARGEN, yPosition, PDType1Font.HELVETICA, 10);
                dibujarTexto(contentStream, "Hora: " + fechaHoraParts[1], MARGEN + 150, yPosition, PDType1Font.HELVETICA, 10);
                dibujarTexto(contentStream, "Cajero: " + cajero, MARGEN + 300, yPosition, PDType1Font.HELVETICA, 10);
                yPosition -= 15;
                dibujarTexto(contentStream, "Folio: " + folio, MARGEN, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                yPosition -= 25;

                // 4. Tabla de productos
                dibujarTextoCentrado(contentStream, "DETALLE DE COMPRA", MARGEN, yPosition, PDType1Font.HELVETICA_BOLD, 12);
                yPosition -= 20;
                dibujarLinea(contentStream, MARGEN, yPosition, ANCHO_PAGINA - 2*MARGEN);
                yPosition -= 15;

                // Encabezados de tabla
                dibujarTexto(contentStream, "ID", MARGEN, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                dibujarTexto(contentStream, "Nombre", MARGEN + 40, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                dibujarTexto(contentStream, "Marca", MARGEN + 200, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                dibujarTexto(contentStream, "Cant.", MARGEN + 280, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                dibujarTexto(contentStream, "Precio", MARGEN + 340, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                yPosition -= 15;
                dibujarLinea(contentStream, MARGEN, yPosition, ANCHO_PAGINA - 2*MARGEN);
                yPosition -= 10;

                // Productos
             // En el bucle de productos del método generarRecibo:
                for (Producto producto : productos) {
                    if (producto != null) { // Verificación adicional
                        dibujarTexto(contentStream, String.valueOf(producto.getId()), MARGEN, yPosition, PDType1Font.HELVETICA, 10);

                        String nombre = producto.getNombre();
                        if (nombre.length() > 25) {
                            nombre = nombre.substring(0, 22) + "...";
                        }
                        dibujarTexto(contentStream, nombre, MARGEN + 40, yPosition, PDType1Font.HELVETICA, 10);

                        dibujarTexto(contentStream, producto.getMarca(), MARGEN + 200, yPosition, PDType1Font.HELVETICA, 10);
                        dibujarTexto(contentStream, "1", MARGEN + 280, yPosition, PDType1Font.HELVETICA, 10); // Cantidad fija en 1
                        dibujarTexto(contentStream, String.format("$%,.2f", producto.getPrecio()), MARGEN + 340, yPosition, PDType1Font.HELVETICA, 10);
                        yPosition -= 15;
                    }
                }

                yPosition -= 15;
                dibujarLinea(contentStream, MARGEN, yPosition, ANCHO_PAGINA - 2*MARGEN);
                yPosition -= 20;

                // 5. Totales
                dibujarTexto(contentStream, "Subtotal:", MARGEN + 300, yPosition, PDType1Font.HELVETICA, 10);
                dibujarTexto(contentStream, String.format("$%,.2f", subtotal), MARGEN + 400, yPosition, PDType1Font.HELVETICA, 10);
                yPosition -= 15;

                dibujarTexto(contentStream, "IVA (16%):", MARGEN + 300, yPosition, PDType1Font.HELVETICA, 10);
                dibujarTexto(contentStream, String.format("$%,.2f", iva), MARGEN + 400, yPosition, PDType1Font.HELVETICA, 10);
                yPosition -= 15;

                dibujarTexto(contentStream, "Total:", MARGEN + 300, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                dibujarTexto(contentStream, String.format("$%,.2f", total), MARGEN + 400, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                yPosition -= 25;
                dibujarLinea(contentStream, MARGEN, yPosition, ANCHO_PAGINA - 2*MARGEN);
                yPosition -= 20;

                // 6. Información de pago
                dibujarTexto(contentStream, "Forma de pago:", MARGEN, yPosition, PDType1Font.HELVETICA, 10);
                dibujarTexto(contentStream, tipoPago, MARGEN + 100, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                yPosition -= 15;

                dibujarTexto(contentStream, "Monto recibido:", MARGEN, yPosition, PDType1Font.HELVETICA, 10);
                dibujarTexto(contentStream, String.format("$%,.2f", pago), MARGEN + 100, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                yPosition -= 15;

                dibujarTexto(contentStream, "Cambio:", MARGEN, yPosition, PDType1Font.HELVETICA, 10);
                dibujarTexto(contentStream, String.format("$%,.2f", cambio), MARGEN + 100, yPosition, PDType1Font.HELVETICA_BOLD, 10);
                yPosition -= 25;
                dibujarLinea(contentStream, MARGEN, yPosition, ANCHO_PAGINA - 2*MARGEN);
                yPosition -= 20;

                // 7. Pie de página
                dibujarTextoCentrado(contentStream, "¡Gracias por su compra!", MARGEN, yPosition, PDType1Font.HELVETICA_BOLD, 12);
                yPosition -= 15;
                dibujarTextoCentrado(contentStream, "Visítenos nuevamente en " + NEGOCIO, MARGEN, yPosition, PDType1Font.HELVETICA, 10);
                yPosition -= 15;
                dibujarLinea(contentStream, MARGEN, yPosition, ANCHO_PAGINA - 2*MARGEN);
            }

            String nombreArchivo = "recibo_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            String rutaCompleta = "data/recibos/" + nombreArchivo;

            document.save(rutaCompleta);
            System.out.println("Recibo generado: " + rutaCompleta);

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Método auxiliar para verificar la ruta del logo
     */
    private static String verificarRutaLogo(String logoPath) throws IllegalArgumentException {
        if (logoPath == null || logoPath.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del logo no puede estar vacía");
        }

        // Normalizar rutas para diferentes sistemas operativos
        String rutaNormalizada = logoPath.replace('/', File.separatorChar)
                                       .replace('\\', File.separatorChar);

        File archivoLogo = new File(rutaNormalizada);
        if (!archivoLogo.exists()) {
            // Intentar cargar como recurso interno
            URL urlLogo = PDFGenerador.class.getResource(rutaNormalizada.startsWith("/") ?
                         rutaNormalizada : "/" + rutaNormalizada);
            if (urlLogo != null) {
                return urlLogo.getPath();
            }
            throw new IllegalArgumentException("El archivo de logo no existe en: " + rutaNormalizada);
        }

        return rutaNormalizada;
    }




    // Métodos auxiliares
    private static void dibujarTextoCentrado(PDPageContentStream contentStream, String texto, float margen, float y,
                                           PDType1Font font, float fontSize) throws IOException {
        float anchoTexto = font.getStringWidth(texto) / 1000 * fontSize;
        float x = margen + (ANCHO_PAGINA - 2*margen - anchoTexto) / 2;
        dibujarTexto(contentStream, texto, x, y, font, fontSize);
    }

    private static void dibujarTexto(PDPageContentStream contentStream, String texto, float x, float y,
                                   PDType1Font font, float fontSize) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(texto);
        contentStream.endText();
    }


    public static void generarPDFCorte(CorteCaja corte, String rutaSalida) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Configuración inicial
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("REPORTE DE CORTE DE CAJA");
            contentStream.endText();

            // Detalles del corte
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 650);
            contentStream.showText("Fecha: " + corte.getFechaHoraFormateada());
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Monto Inicial: $" + String.format("%,.2f", corte.getMontoInicial()));
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Ventas Efectivo: $" + String.format("%,.2f", corte.getVentasEfectivo()));
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Ventas Tarjeta: $" + String.format("%,.2f", corte.getVentasTarjeta()));
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Retiros: $" + String.format("%,.2f", corte.getRetiros()));
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Monto Final: $" + String.format("%,.2f", corte.getMontoFinal()));
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Observaciones: " + corte.getObservaciones());
            contentStream.endText();
        }

        document.save(rutaSalida);
        document.close();
    }

    private static void dibujarLinea(PDPageContentStream contentStream, float x, float y, float longitud) throws IOException {
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + longitud, y);
        contentStream.stroke();
    }

}