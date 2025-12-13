package utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class ReportGenerator {
    public static void generarReporteCorte(String rutaSalida,
                                         String montoInicial,
                                         String ventasEfectivo,
                                         String ventasTarjeta,
                                         String retiros,
                                         String montoFinal,
                                         String observaciones) throws IOException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Encabezado
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 750);
            contentStream.showText("REPORTE DE CORTE DE CAJA - ELECTRONICS TECHNOLOGY");
            contentStream.endText();

            // Información de fecha
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Fecha: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();

            // Detalles del corte
            float yPosition = 650;
            contentStream.beginText();
            contentStream.newLineAtOffset(100, yPosition);
            contentStream.showText("Monto Inicial: $" + montoInicial);
            yPosition -= 25;
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Ventas en Efectivo: $" + ventasEfectivo);
            yPosition -= 25;
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Ventas con Tarjeta: $" + ventasTarjeta);
            yPosition -= 25;
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Retiros: $" + retiros);
            yPosition -= 25;
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Monto Final: $" + montoFinal);
            yPosition -= 25;
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Observaciones: " + observaciones);
            contentStream.endText();

            // Pie de página
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 100);
            contentStream.showText("Este documento es un comprobante oficial del corte de caja");
            contentStream.endText();
        }

        document.save(rutaSalida);
        document.close();
    }
}