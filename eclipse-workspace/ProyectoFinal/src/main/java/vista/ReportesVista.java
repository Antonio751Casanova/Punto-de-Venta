package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.itextpdf.io.font.constants.StandardFonts;
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

import controlador.ExcelControlador;
import modelo.Producto;

public class ReportesVista extends JFrame {
    // Colores de la paleta naranja/azul
    private static final Color COLOR_PRIMARIO = new Color(0, 102, 204); // Azul oscuro
    private static final Color COLOR_SECUNDARIO = new Color(255, 153, 51); // Naranja
    private static final Color COLOR_FONDO = new Color(240, 245, 250); // Azul muy claro
    private static final Color COLOR_TABLA_HEADER = new Color(0, 76, 153); // Azul más oscuro
    private static final Color COLOR_TABLA_SELECTION = new Color(255, 204, 153); // Naranja claro

    private JTabbedPane tabbedPane;
    private Timer timerActualizacion;
    private ChartPanel chartPanel;

    public ReportesVista() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Reportes de Ventas");
        setSize(1000, 700);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);

        // Configurar colores de UI
        UIManager.put("TabbedPane.selected", COLOR_SECUNDARIO);
        UIManager.put("TabbedPane.contentAreaColor", COLOR_FONDO);
        UIManager.put("TabbedPane.background", COLOR_FONDO);
        UIManager.put("Table.selectionBackground", COLOR_TABLA_SELECTION);
        UIManager.put("Table.selectionForeground", Color.BLACK);

        // Inicializar el ChartPanel con estilo
        chartPanel = new ChartPanel(crearGraficaBarrasMejorada());
        chartPanel.setPreferredSize(new Dimension(900, 500));
        chartPanel.setBackground(COLOR_FONDO);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        iniciarActualizacionAutomatica();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                detenerActualizacion();
            }
        });
    }

    private void detenerActualizacion() {
        if (timerActualizacion != null && timerActualizacion.isRunning()) {
            timerActualizacion.stop();
        }
    }

    @Override
    public void dispose() {
        timerActualizacion.stop();
        super.dispose();
    }

    private void initUI() {
        tabbedPane = new JTabbedPane(SwingConstants.TOP);
        tabbedPane.setBackground(COLOR_FONDO);
        tabbedPane.setForeground(COLOR_PRIMARIO);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Pestaña 1: Gráficos
        JPanel panelGraficos = new JPanel(new BorderLayout());
        panelGraficos.setBackground(COLOR_FONDO);
        panelGraficos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabbedPane.addTab("Gráficos", panelGraficos);
        crearPanelGraficos(panelGraficos);

        // Pestaña 2: Datos
        JPanel panelDatos = new JPanel(new BorderLayout());
        panelDatos.setBackground(COLOR_FONDO);
        panelDatos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabbedPane.addTab("Datos", panelDatos);
        crearTablaDatos(panelDatos);

        // Pestaña 3: Exportar
        JPanel panelExportar = new JPanel(new BorderLayout());
        panelExportar.setBackground(COLOR_FONDO);
        panelExportar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        tabbedPane.addTab("Exportar", panelExportar);
        crearPanelExportar(panelExportar);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JFreeChart crearGraficaBarrasMejorada() {
        try {
            // Configurar paleta de colores naranja/azul
            Color[] colors = generarPaletaColores(20);

            List<Producto> top20 = obtenerTop20Productos();
            DefaultCategoryDataset dataset = crearDataset(top20);

            JFreeChart chart = ChartFactory.createBarChart(
                "TOP 20 - Productos Más Vendidos (" + LocalDate.now().getYear() + ")",
                "Productos",
                "Unidades Vendidas",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );

            personalizarGrafico(chart, colors);
            return chart;

        } catch (Exception e) {
            e.printStackTrace();
            return crearGraficoVacio("Datos no disponibles");
        }
    }

    private List<Producto> obtenerTop20Productos() throws Exception {
        return ExcelControlador.leerProductosDesdeExcel().stream()
            .filter(p -> p.getVentas() > 0)
            .sorted(Comparator.comparingInt(Producto::getVentas).reversed())
            .limit(20)
            .collect(Collectors.toList());
    }

    private JFreeChart crearGraficoVacio(String mensaje) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1, "Error", "Datos");

        JFreeChart chart = ChartFactory.createBarChart(
            mensaje, "", "", dataset,
            PlotOrientation.VERTICAL, false, false, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRenderer().setSeriesPaint(0, COLOR_SECUNDARIO);
        plot.setNoDataMessage("No hay datos disponibles");
        plot.setBackgroundPaint(COLOR_FONDO);

        return chart;
    }

    private Color[] generarPaletaColores(int cantidad) {
        Color[] colors = new Color[cantidad];
        // Degradado entre azul y naranja
        for (int i = 0; i < cantidad; i++) {
            float ratio = (float) i / (float) cantidad;
            int red = (int) (COLOR_PRIMARIO.getRed() * (1 - ratio) + COLOR_SECUNDARIO.getRed() * ratio);
            int green = (int) (COLOR_PRIMARIO.getGreen() * (1 - ratio) + COLOR_SECUNDARIO.getGreen() * ratio);
            int blue = (int) (COLOR_PRIMARIO.getBlue() * (1 - ratio) + COLOR_SECUNDARIO.getBlue() * ratio);
            colors[i] = new Color(red, green, blue);
        }
        return colors;
    }

    private DefaultCategoryDataset crearDataset(List<Producto> productos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        productos.forEach(p ->
            dataset.addValue(p.getVentas(), "Ventas", p.getNombre())
        );
        return dataset;
    }

    private void personalizarGrafico(JFreeChart chart, Color[] colors) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Aplicar colores
        for (int i = 0; i < colors.length; i++) {
            renderer.setSeriesPaint(i, colors[i]);
        }

        // Configurar tooltips
        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
            "<html><b>{1}</b><br>Ventas: <b>{2}</b> unidades</html>",
            new DecimalFormat("#,###")
        ));

        // Otras configuraciones
        plot.setBackgroundPaint(COLOR_FONDO);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.getDomainAxis().setCategoryMargin(0.1);
        plot.getRangeAxis().setUpperMargin(0.1);

        // Mejorar leyenda
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 10));
        chart.setBackgroundPaint(COLOR_FONDO);

        // Títulos con mejor fuente
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private void iniciarActualizacionAutomatica() {
        if (timerActualizacion != null && timerActualizacion.isRunning()) {
            timerActualizacion.stop();
        }

        timerActualizacion = new Timer(3000, e -> {
            if (this.isVisible()) {
                SwingUtilities.invokeLater(() -> {
                    chartPanel.setChart(crearGraficaBarrasMejorada());
                    chartPanel.repaint();
                });
            }
        });
        timerActualizacion.start();
    }

    private void crearPanelGraficos(JPanel panel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> ventasProductos = obtenerDatosVentasReales();

        ventasProductos.forEach((producto, ventas) ->
            dataset.addValue(ventas, "Ventas", producto));

        JFreeChart chart = ChartFactory.createBarChart(
            "Productos Más Vendidos",
            "Producto",
            "Cantidad Vendida",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        // Personalizar gráfico
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRenderer().setSeriesPaint(0, COLOR_PRIMARIO);
        plot.setBackgroundPaint(COLOR_FONDO);

        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Configurar tooltips
        plot.getRenderer().setDefaultToolTipGenerator(
            new StandardCategoryToolTipGenerator(
                "{1}: {2} unidades vendidas",
                new DecimalFormat("0")
            )
        );

        ChartPanel chartPanel = new ChartPanel(chart) {
            @Override
            public void mouseClicked(MouseEvent e) {
                ChartEntity entity = getEntityForPoint(e.getX(), e.getY());
                if (entity instanceof CategoryItemEntity) {
                    CategoryItemEntity item = (CategoryItemEntity) entity;
                    String producto = (String) item.getColumnKey();
                    Number ventas = dataset.getValue("Ventas", producto);
                    JOptionPane.showMessageDialog(
                        this,
                        "<html><b>Detalles del Producto</b><br><br>" +
                        "<b>Producto:</b> " + producto + "<br>" +
                        "<b>Ventas:</b> " + ventas + " unidades</html>",
                        "Detalles",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        };

        chartPanel.setPreferredSize(new Dimension(900, 500));
        chartPanel.setBackground(COLOR_FONDO);
        panel.add(chartPanel, BorderLayout.CENTER);
    }

    private void crearTablaDatos(JPanel panel) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? String.class : Integer.class;
            }
        };

        String[] columnas = {"Producto", "Ventas", "Stock Actual", "Stock Mínimo"};
        for (String col : columnas) {
            model.addColumn(col);
        }

        try {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    List<Producto> productos = ExcelControlador.leerProductosDesdeExcel();

                    SwingUtilities.invokeLater(() -> {
                        productos.forEach(p -> {
                            model.addRow(new Object[]{
                                p.getNombre(),
                                p.getVentas(),
                                p.getStock(),
                                p.getStockMinimo()
                            });
                        });
                    });
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(panel,
                            "Error al cargar datos: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();

            JTable tabla = new JTable(model);
            tabla.setAutoCreateRowSorter(true);
            tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tabla.setRowHeight(25);
            tabla.setShowGrid(true);
            tabla.setGridColor(new Color(220, 220, 220));
            tabla.setSelectionBackground(COLOR_TABLA_SELECTION);
            tabla.setSelectionForeground(Color.BLACK);

            // Configurar header de la tabla
            tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tabla.getTableHeader().setBackground(COLOR_TABLA_HEADER);
            tabla.getTableHeader().setForeground(Color.BLACK);
            tabla.getTableHeader().setReorderingAllowed(false);

            // Centrar todo el contenido de la tabla
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            centerRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            for (int i = 0; i < tabla.getColumnCount(); i++) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            // Configurar renderer personalizado
            tabla.setDefaultRenderer(Object.class, new StockCellRenderer());

            // Configurar tamaño de columnas
            tabla.getColumnModel().getColumn(0).setPreferredWidth(200); // Producto
            tabla.getColumnModel().getColumn(1).setPreferredWidth(80);  // Ventas
            tabla.getColumnModel().getColumn(2).setPreferredWidth(100); // Stock Actual
            tabla.getColumnModel().getColumn(3).setPreferredWidth(100); // Stock Mínimo

            JScrollPane scrollPane = new JScrollPane(tabla);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(Color.WHITE);
            panel.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(panel,
                "Error al inicializar tabla: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    class StockCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (column == 2 || column == 3) { // Columnas de stock
                try {
                    int stock = (Integer) table.getModel().getValueAt(row, 2);
                    int stockMin = (Integer) table.getModel().getValueAt(row, 3);

                    if (stock <= stockMin) {
                        setBackground(new Color(255, 200, 200)); // Rojo claro
                        setForeground(Color.BLACK);
                        setOpaque(true);
                    } else if (stock <= stockMin + 5) { // Stock cercano al mínimo
                        setBackground(new Color(255, 238, 186)); // Amarillo claro
                        setOpaque(true);
                    } else {
                        setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                        setOpaque(isSelected);
                    }
                } catch (Exception e) {
                    setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                }
            } else {
                setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            }
            return this;
        }
    }

    private Map<String, Integer> obtenerDatosVentasReales() {
        try {
            Path excelPath = Paths.get(ExcelControlador.RUTA_DEFAULT);
            if (!Files.exists(excelPath)) {
                throw new FileNotFoundException("Archivo Excel no encontrado en: " + excelPath);
            }

            List<Producto> productos = ExcelControlador.leerProductosDesdeExcel();
            if (productos == null || productos.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, Integer> ventasPorProducto = new LinkedHashMap<>();

            for (Producto p : productos) {
                if (p == null || p.getNombre() == null || p.getNombre().trim().isEmpty()) {
                    continue;
                }

                if (p.getVentas() < 0) {
                    System.err.println("Advertencia: Ventas negativas para " + p.getNombre());
                    continue;
                }

                if (ventasPorProducto.containsKey(p.getNombre())) {
                    System.out.println("Advertencia: Producto duplicado - " + p.getNombre());
                    continue;
                }

                ventasPorProducto.put(p.getNombre(), p.getVentas());
            }

            return ventasPorProducto;

        } catch (Exception e) {
            System.err.println("Error al obtener datos de ventas: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    private void exportarAExcel(ActionEvent e) {
        try {
            File carpetaExcel = new File("data/reportes/reporte_Excel");
            if (!carpetaExcel.exists()) {
                carpetaExcel.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivo = "reporte_ventas_" + timestamp + ".xlsx";
            File archivo = new File(carpetaExcel, nombreArchivo);

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheetDatos = workbook.createSheet("Datos de Ventas");
                List<Producto> productos = ExcelControlador.leerProductosDesdeExcel();

                crearEncabezadosConEstilo(workbook, sheetDatos);
                llenarDatosConEstilo(workbook, sheetDatos, productos);
                autoajustarColumnas(sheetDatos);
                agregarGraficaAExcel(workbook, sheetDatos);

                guardarArchivoExcel(workbook, archivo);

                JOptionPane.showMessageDialog(this,
                    "<html><b>Reporte generado con éxito</b><br>" +
                    archivo.getAbsolutePath() + "</html>",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "<html><b>Error al exportar a Excel</b><br>" +
                ex.getMessage() + "</html>",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void crearEncabezadosConEstilo(Workbook workbook, Sheet sheet) {
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Producto", "Ventas", "Stock Actual", "Stock Mínimo"};

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void llenarDatosConEstilo(Workbook workbook, Sheet sheet, List<Producto> productos) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        CellStyle warningStyle = workbook.createCellStyle();
        warningStyle.cloneStyleFrom(dataStyle);
        warningStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int rowNum = 1;
        for (Producto p : productos) {
            Row row = sheet.createRow(rowNum++);

            org.apache.poi.ss.usermodel.Cell cellProducto = row.createCell(0);
            cellProducto.setCellValue(p.getNombre());
            cellProducto.setCellStyle(dataStyle);

            org.apache.poi.ss.usermodel.Cell cellVentas = row.createCell(1);
            cellVentas.setCellValue(p.getVentas());
            cellVentas.setCellStyle(dataStyle);

            org.apache.poi.ss.usermodel.Cell cellStock = row.createCell(2);
            cellStock.setCellValue(p.getStock());
            cellStock.setCellStyle(dataStyle);

            org.apache.poi.ss.usermodel.Cell cellStockMin = row.createCell(3);
            cellStockMin.setCellValue(p.getStockMinimo());

            if (p.getStock() <= p.getStockMinimo()) {
                cellStock.setCellStyle(warningStyle);
                cellStockMin.setCellStyle(warningStyle);
            } else {
                cellStockMin.setCellStyle(dataStyle);
            }
        }
    }

    private void autoajustarColumnas(Sheet sheet) {
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void agregarGraficaAExcel(Workbook workbook, Sheet sheet) throws IOException {
        JFreeChart chart = crearGraficaBarrasActualizada(ExcelControlador.leerProductosDesdeExcel());
        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOutputStream, chart, 800, 500);
        byte[] chartImageBytes = chartOutputStream.toByteArray();

        int pictureIdx = workbook.addPicture(chartImageBytes, Workbook.PICTURE_TYPE_PNG);
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();

        anchor.setCol1(0);
        anchor.setRow1(sheet.getLastRowNum() + 3);
        drawing.createPicture(anchor, pictureIdx);
    }

    private void guardarArchivoExcel(Workbook workbook, File archivo) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            workbook.write(fos);
        }
    }

    private void crearPanelExportar(JPanel panel) {
        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 15, 15));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        panelBotones.setBackground(COLOR_FONDO);

        JButton btnExcel = crearBotonEstilizado("Exportar a Excel", COLOR_PRIMARIO);
        btnExcel.addActionListener(this::exportarAExcel);

        JButton btnPDF = crearBotonEstilizado("Generar Reporte PDF", COLOR_SECUNDARIO);
        btnPDF.addActionListener(this::generarPDF);

        panelBotones.add(btnExcel);
        panelBotones.add(btnPDF);

        panel.add(panelBotones, BorderLayout.CENTER);
    }

    private JButton crearBotonEstilizado(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setBackground(colorFondo);
        boton.setForeground(Color.BLACK);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorFondo.darker(), 2),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
			public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo.brighter());
            }
            @Override
			public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo);
            }
        });

        return boton;
    }

    private void generarPDF(ActionEvent e) {
        try {
            List<Producto> productos = ExcelControlador.leerProductosDesdeExcel();

            SwingUtilities.invokeLater(() -> {
                ReporteVistaDiaologo preview = new ReporteVistaDiaologo(this, productos);
                preview.setVisible(true);
            });

            File carpetaPDF = new File("data/reportes/reporte_PDF");
            if (!carpetaPDF.exists()) {
                carpetaPDF.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivo = "reporte_ventas_" + timestamp + ".pdf";
            File archivo = new File(carpetaPDF, nombreArchivo);

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(archivo));
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(40, 40, 40, 40);

         // Título con estilo
            Paragraph titulo = new Paragraph("Reporte de Ventas - " + timestamp)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(18)
                .setFontColor(new DeviceRgb(
                    COLOR_PRIMARIO.getRed(),      // Componente Rojo (0-255)
                    COLOR_PRIMARIO.getGreen(),    // Componente Verde (0-255)
                    COLOR_PRIMARIO.getBlue()      // Componente Azul (0-255)
                ))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(titulo);

            agregarGraficaAPDF(document, productos);
            document.add(new Paragraph("\n"));
            agregarTablaAPDF(document, productos);

            document.close();

            JOptionPane.showMessageDialog(this,
                "<html><b>Reporte PDF generado con éxito</b><br>" +
                archivo.getAbsolutePath() + "</html>",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "<html><b>Error al generar PDF</b><br>" +
                ex.getMessage() + "</html>",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void agregarGraficaAPDF(Document document, List<Producto> productos) throws IOException {
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

        // Personalización de la gráfica
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(COLOR_FONDO);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_PRIMARIO);

        // Mejorar títulos
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        return chart;
    }

    private void agregarTablaAPDF(Document document, List<Producto> productos) throws IOException {
        float[] columnWidths = {3f, 1f, 1f, 1f};
        Table table = new Table(columnWidths).useAllAvailableWidth();

        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Style cellStyle = new Style()
            .setPadding(5)
            .setTextAlignment(TextAlignment.CENTER)
            .setFont(fontNormal);

        Style headerStyle = new Style()
        	    .setBackgroundColor(new DeviceRgb(
        	        COLOR_TABLA_HEADER.getRed(),
        	        COLOR_TABLA_HEADER.getGreen(),
        	        COLOR_TABLA_HEADER.getBlue()
        	    ))
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ReportesVista vista = new ReportesVista();
            vista.setLocationRelativeTo(null);
            vista.setVisible(true);
        });
    }
}