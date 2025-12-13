package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
// Para los gráficos
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.itextpdf.io.font.constants.StandardFonts;
// Para la generación de PDF
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import modelo.Producto;

public class ReporteVistaDiaologo extends JDialog {
    private JButton btnAceptar;
    private JButton btnGenerarPDF;
    private JTextArea areaReporte;
    private JFrame parent;
    private List<Producto> productos;

    public ReporteVistaDiaologo(JFrame parent, List<Producto> productos) {
        super(parent, "Vista Previa del Reporte", true);
        setSize(700, 800);
        setLocationRelativeTo(parent);
        this.parent = parent;
        this.productos = productos;

        // Área de texto para mostrar el reporte
        areaReporte = new JTextArea(generarContenidoReporte(productos));
        areaReporte.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaReporte.setEditable(false);

        // Botones
        btnAceptar = new JButton("Cerrar");
        btnGenerarPDF = new JButton("Generar PDF");

        btnAceptar.addActionListener(e -> dispose());
        btnGenerarPDF.addActionListener(e -> {
            // Aquí podrías llamar al método para generar el PDF
            dispose();
        });

        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnGenerarPDF);
        panelBotones.add(btnAceptar);

        btnGenerarPDF.addActionListener(e -> {
            generarPDF();
            dispose();
        });


        // Configurar layout
        setLayout(new BorderLayout());
        add(new JScrollPane(areaReporte), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void generarPDF() {
        try {
            File carpetaPDF = new File("data/reportes/reporte_PDF");
            if (!carpetaPDF.exists()) {
                carpetaPDF.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivo = "reporte_ventas_" + timestamp + ".pdf";
            File archivo = new File(carpetaPDF, nombreArchivo);

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(archivo));
            Document document = new Document(pdfDoc, PageSize.A4.rotate());

            // Agregar contenido al PDF
            Paragraph titulo = new Paragraph("Reporte de Ventas - " + timestamp)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(titulo);

            agregarGraficaAPDF(document);
            document.add(new Paragraph("\n"));
            agregarTablaAPDF(document);

            document.close();

            JOptionPane.showMessageDialog(parent,
                "Reporte PDF generado exitosamente:\n" + archivo.getAbsolutePath(),
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                "Error al generar PDF: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void agregarGraficaAPDF(Document document) throws IOException {
        JFreeChart chart = crearGraficaBarrasActualizada(productos);
        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOutputStream, chart, 800, 500);
        byte[] chartImageBytes = chartOutputStream.toByteArray();
        ImageData imageData = ImageDataFactory.create(chartImageBytes);
        Image chartImage = new Image(imageData)
            .setAutoScale(true)
            .setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(chartImage);
    }

    private void agregarTablaAPDF(Document document) throws IOException {
        float[] columnWidths = {3f, 1f, 1f, 1f};
        Table table = new Table(columnWidths).useAllAvailableWidth();
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Style cellStyle = new Style()
            .setPadding(5)
            .setTextAlignment(TextAlignment.CENTER)
            .setFont(fontNormal);

        Style headerStyle = new Style()
            .setBackgroundColor(new DeviceRgb(70, 130, 180))
            .setFontColor(DeviceRgb.WHITE)
            .setFont(fontBold)
            .setTextAlignment(TextAlignment.CENTER);

        table.addHeaderCell(new Cell().add(new Paragraph("Producto").addStyle(headerStyle)));
        table.addHeaderCell(new Cell().add(new Paragraph("Ventas").addStyle(headerStyle)));
        table.addHeaderCell(new Cell().add(new Paragraph("Stock").addStyle(headerStyle)));
        table.addHeaderCell(new Cell().add(new Paragraph("Stock Mín").addStyle(headerStyle)));

        for (Producto p : productos) {
            table.addCell(new Cell().add(new Paragraph(p.getNombre()).addStyle(cellStyle)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getVentas())).addStyle(cellStyle)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getStock())).addStyle(cellStyle)));

            Cell stockMinCell = new Cell().add(new Paragraph(String.valueOf(p.getStockMinimo())).addStyle(cellStyle));
            if (p.getStock() <= p.getStockMinimo()) {
                stockMinCell.setBackgroundColor(new DeviceRgb(255, 200, 200));
            }
            table.addCell(stockMinCell);
        }

        document.add(table);
    }

    private JFreeChart crearGraficaBarrasActualizada(List<Producto> productos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        productos.stream()
            .sorted(Comparator.comparingInt(Producto::getVentas).reversed())
            .limit(20)
            .forEach(p -> dataset.addValue(p.getVentas(), "Ventas", p.getNombre()));

        JFreeChart chart = ChartFactory.createBarChart(
            "TOP 20 Productos Más Vendidos",
            "Productos",
            "Unidades Vendidas",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));

        return chart;
    }

    private String generarContenidoReporte(List<Producto> productos) {
        StringBuilder sb = new StringBuilder();

        // Encabezado
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                        REPORTE DE VENTAS - ELECTRONICS TECHNOLOGY              \n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n\n");

        // Sección de productos más vendidos
        sb.append("PRODUCTOS MÁS VENDIDOS:\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-40s %10s %10s %10s\n", "Producto", "Ventas", "Stock", "Stock Mín"));
        sb.append("--------------------------------------------------------------------------------\n");

        productos.stream()
            .sorted((p1, p2) -> Integer.compare(p2.getVentas(), p1.getVentas()))
            .limit(20)
            .forEach(p -> {
                sb.append(String.format("%-40s %10d %10d %10d\n",
                    p.getNombre(), p.getVentas(), p.getStock(), p.getStockMinimo()));
            });

        sb.append("\n\n");

        // Sección de productos con bajo stock
        sb.append("PRODUCTOS CON STOCK BAJO:\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-40s %10s %10s\n", "Producto", "Stock", "Stock Mín"));
        sb.append("--------------------------------------------------------------------------------\n");

        productos.stream()
            .filter(p -> p.getStock() <= p.getStockMinimo())
            .forEach(p -> {
                sb.append(String.format("%-40s %10d %10d\n",
                    p.getNombre(), p.getStock(), p.getStockMinimo()));
            });

        sb.append("\n════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                     FIN DEL REPORTE - " + java.time.LocalDate.now() + "           \n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }
}