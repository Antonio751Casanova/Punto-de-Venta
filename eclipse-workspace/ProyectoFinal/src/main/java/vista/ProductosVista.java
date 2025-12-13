package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import controlador.ExcelLector;
import controlador.ProductoControlador;

public class ProductosVista extends JFrame {
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private JComboBox<String> comboFiltros;
    private JComboBox<String> comboCategorias;
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
	public ProductosVista() {
        // Configuración básica de la ventana
        setTitle("Gestión de Productos - Punto de Venta");
        setSize(1000, 650);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        ProductoControlador.verificarEstructuraExcel("data/productos.xlsx");


        // 1. Verificar y crear estructura de archivos
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File excelFile = new File("data/productos.xlsx");
        if (!excelFile.exists()) {
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fos = new FileOutputStream(excelFile)) {

                workbook.createSheet("Productos");

                // Crear cabeceras básicas
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.createRow(0);
                String[] headers = {"ID", "Nombre", "Marca", "Precio", "Descripción", "Imagen", "Categoría"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                workbook.write(fos);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al crear archivo Excel: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        // 2. Crear modelo de tabla VACÍO (cambio clave)
        modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Marca", "Precio", "Descripción", "Imagen", "Categoría"},
            0
        ) {


        	 @Override
        	    public Class<?> getColumnClass(int columnIndex) {
        	        switch (columnIndex) {
        	            case 0: case 7: case 8: case 10: return Integer.class;
        	            case 3: return Double.class;
        	            case 5: return String.class; // Imagen como String
        	            default: return String.class;
        	        }
        	    }

			 @Override
			    public boolean isCellEditable(int row, int column) {
			        return column != 0 && column != 5; // ID e Imagen no editables
			    }

        };




        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setRowHeight(70);
     // Mejorar apariencia general
        tablaProductos.setFillsViewportHeight(true);
        tablaProductos.setSelectionBackground(new Color(200, 220, 255));
        tablaProductos.setSelectionForeground(Color.BLACK);
     // Fuente más compacta
        Font fuenteCompacta = new Font("Segoe UI", Font.PLAIN, 11);
        tablaProductos.setFont(fuenteCompacta);
        tablaProductos.getTableHeader().setFont(fuenteCompacta.deriveFont(Font.BOLD));

        // Asegurar que la columna de imagen tenga espacio suficiente
        tablaProductos.getColumnModel().getColumn(5).setMinWidth(120);
        tablaProductos.getColumnModel().getColumn(5).setMaxWidth(200);
        configurarTabla();

        // 3. Configurar componentes principales
        add(configurarPanelBusqueda(), BorderLayout.NORTH);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        add(configurarPanelBotones(), BorderLayout.SOUTH);

        // 4. Configurar observador de archivo (en segundo plano)
        ProductoControlador.iniciarObservadorExcel(modeloTabla);
        new Thread(new ExcelLector(modeloTabla, "data/productos.xlsx")).start();

        // 5. Configurar redimensionamiento
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarAnchoColumnas();
            }
        });

        // 6. Configurar menú de opciones
        configurarMenuOpciones();

        // 7. Mostrar advertencia
        JOptionPane.showMessageDialog(this,
            "No modifique el Excel manualmente mientras la aplicación esté abierta",
            "Advertencia Importante",
            JOptionPane.WARNING_MESSAGE);

        // 8. Mostrar ventana
        setVisible(true);
        revalidate();

     // Configurar específicamente el renderizador para la columna de imágenes
        tablaProductos.getColumnModel().getColumn(5).setCellRenderer(new ImageRenderer());

        // Configurar altura de filas para que se vean las imágenes
        tablaProductos.setRowHeight(80);

        // Verificar rutas (código de diagnóstico)
        System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));
        File imgDir = new File("imagenes");
        System.out.println("Directorio de imágenes existe: " + imgDir.exists());
        if (imgDir.exists()) {
            System.out.println("Contenido de imágenes: " + Arrays.toString(imgDir.list()));
        }
    }



    private void ajustarAnchoColumnas() {
        TableColumnModel columnModel = tablaProductos.getColumnModel();
        int anchoTotal = tablaProductos.getWidth();

        // Distribuir el ancho (ajusta los porcentajes según necesites)
        int[] porcentajes = {5, 15, 10, 10, 20, 15, 15, 10};

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn col = columnModel.getColumn(i);
            col.setPreferredWidth(anchoTotal * porcentajes[i] / 100);
        }
    }

    @SuppressWarnings("unused")
	private void inicializarComponentes() {

    	// 1. Modelo y tabla
        modeloTabla = crearModeloTabla();
        tablaProductos = new JTable(modeloTabla);

        // 2. Configuraciones
        configurarTabla();


        // 1. Configuración inicial de la tabla
        setLayout(new BorderLayout());

        // 2. Crear modelo con validaciones
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Marca", "Precio", "Descripción", "Imagen", "Categoría"}, 0) {
            /**
			 *
			 */
			private static final long serialVersionUID = 6085288914530057464L;

			@Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;
                    case 3: return Double.class;
                    case 5: return Icon.class;
                    default: return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 5; // ID e Imagen no editables
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                try {
                    // Validar precio
                    if (column == 3) {
                        double precio = value instanceof Number ? ((Number)value).doubleValue() : 0;
                        if (precio < 0) {
							throw new IllegalArgumentException("Precio no puede ser negativo");
						}
                    }
                    // Validar categoría
                    else if (column == 6 && !value.toString().matches("(?i)Laptop|Smartphone|Accesorio")) {
                        throw new IllegalArgumentException("Categoría inválida");
                    }

                    super.setValueAt(value, row, column);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // 3. Configurar tabla
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaProductos.setFillsViewportHeight(true);

        // 4. Configuración visual
        configurarTabla();
        configurarEditorSpinner();

        // 5. Listener para cambios
        modeloTabla.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();
                Object valor = modeloTabla.getValueAt(fila, columna);

                // Validar nuevamente antes de guardar
                if (valor != null && !valor.toString().isEmpty()) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                        	ProductoControlador.actualizarCeldaEnExcel(
                                fila, columna, valor, "data/productos.xlsx"
                            );
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get(); // Para capturar excepciones
                            } catch (Exception ex) {
                                System.err.println("Error al guardar: " + ex.getMessage());
                            }
                        }
                    }.execute();
                }
            }
        });

        // 6. Paneles y componentes
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setPreferredSize(new Dimension(900, 600));

        add(configurarPanelBusqueda(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(configurarPanelBotones(), BorderLayout.SOUTH);

        // 7. Opciones adicionales
        configurarMenuOpciones();

        // 8. Cargar datos iniciales
        ProductoControlador.cargarDatosExcel(modeloTabla);
    }

    private void configurarMenuOpciones() {
        JMenuBar menuBar = new JMenuBar();

        // Menú principal de opciones
        JMenu menuOpciones = new JMenu("Opciones");
        menuOpciones.setMnemonic(KeyEvent.VK_O); // Atajo Alt+O

        // Opción para activar/desactivar tabla
        JCheckBoxMenuItem itemActivar = new JCheckBoxMenuItem("Tabla activa", true);
        itemActivar.setMnemonic(KeyEvent.VK_T);
        itemActivar.addActionListener(e -> {
            boolean activa = itemActivar.isSelected(); // Cambiado de getState() a isSelected()

            tablaProductos.setEnabled(activa);

            if (activa) {
                // Al reactivar, reconfigurar completamente la tabla
                configurarTabla();
            } else {
                // Al desactivar, aplicar estilo deshabilitado
                tablaProductos.setBackground(new Color(240, 240, 240));
                tablaProductos.setForeground(Color.GRAY);
            }
        });



        // Opción para limpiar filtros
        JMenuItem itemLimpiarFiltros = new JMenuItem("Limpiar filtros", KeyEvent.VK_L);
        itemLimpiarFiltros.addActionListener(e -> {
            comboCategorias.setSelectedIndex(0);
            txtBusqueda.setText("");
            tablaProductos.setRowSorter(null);
        });

        menuOpciones.add(itemActivar);
        menuOpciones.addSeparator();
        menuOpciones.add(itemLimpiarFiltros);

        menuBar.add(menuOpciones);
        setJMenuBar(menuBar);
    }


    private DefaultTableModel crearModeloTabla() {
        return new DefaultTableModel(new Object[]{"ID", "Nombre", "Marca", "Precio", "Descripción", "Imagen", "Categoría"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;
                    case 3: return Double.class;
                    case 5: return Icon.class;
                    default: return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Hacer editables todas las columnas excepto ID (0) e Imagen (5)
                return column != 0 && column != 5;
            }

            // AQUÍ VA EL NUEVO MÉTODO:
            @Override
            public void setValueAt(Object value, int row, int column) {
                try {
                    // Validar ID (aunque no debería ser editable por el isCellEditable)
                    if (column == 0 && value instanceof Integer && (Integer)value <= 0) {
                        throw new IllegalArgumentException("ID debe ser positivo");
                    }
                    // Validar precio
                    else if (column == 3) {
                        double precio;
                        if (value instanceof Double) {
                            precio = (Double) value;
                        } else if (value instanceof String) {
                            precio = Double.parseDouble(value.toString());
                        } else {
                            precio = 0.0;
                        }

                        if (precio < 0) {
                            throw new IllegalArgumentException("Precio no puede ser negativo");
                        }
                    }
                    // Validar categoría
                    else if (column == 6 && !value.toString().matches("(?i)Laptop|Smartphone|Accesorio")) {
                        throw new IllegalArgumentException("Categoría inválida. Use: Laptop, Smartphone o Accesorio");
                    }

                    super.setValueAt(value, row, column);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null,
                        "Formato numérico inválido",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        e.getMessage(),
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
    }

    private void configurarTabla() {


    	// Configurar renderizador para columna de imágenes
        tablaProductos.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                // Para otras columnas que no sean la de imagen
                if (column != 5) {
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }

                // Para la columna de imagen (5)
                return new ImageRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // Configurar específicamente la columna de imágenes
        TableColumn imageColumn = tablaProductos.getColumnModel().getColumn(5);
        imageColumn.setCellRenderer(new ImageRenderer());

        // 1. Configuración base de la tabla
        tablaProductos.setRowHeight(100); // Ajusta este valor
        tablaProductos.setIntercellSpacing(new Dimension(5, 3)); // Reducir espaciado
        tablaProductos.setShowHorizontalLines(true);
        tablaProductos.setShowVerticalLines(false);
        tablaProductos.setGridColor(new Color(230, 230, 230));

        // 3. Restaurar apariencia
        tablaProductos.setBackground(Color.WHITE);
        tablaProductos.setForeground(Color.BLACK);
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // 4. Forzar repintado
        tablaProductos.repaint();

        // 2. Configurar renderers personalizados
        configurarRenderers();

        // 3. Configurar editores personalizados
        configurarEditores();

        // 4. Configuración de columnas
        configurarAnchoColumnas();

        // 5. Estilo de cabecera
        configurarCabecera();

     // En configurarTabla():
        tablaProductos.setShowHorizontalLines(true);
        tablaProductos.setShowVerticalLines(false);
        tablaProductos.setGridColor(new Color(220, 220, 220));
    }

    private void configurarRenderers() {
        // Renderizador centrado para la mayoría de columnas
        DefaultTableCellRenderer paddedRenderer   = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);

                // Formateo especial para IDs (números enteros)
                if (column == 0 && value instanceof Number) {
                    setText(String.valueOf(((Number)value).intValue()));
                }
                // Formateo especial para precios
                else if (column == 3 && value instanceof Number) {
                    setText(String.format("$%,.2f", ((Number)value).doubleValue()));
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }

                // Alternar colores de filas
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }

             // Formatear como entero sin decimales
                if (value instanceof Number) {
                    setText(String.valueOf(((Number)value).intValue()));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                        getBorder(),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    return this;
                }
        };

     // Aplicar renderizador centrado a todas las columnas
        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            tablaProductos.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }



        // Renderizador especial para precios (alineación derecha + formato monetario)
        tablaProductos.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
        	@Override
            protected void setValue(Object value) {
                if (value instanceof Double) {
                    setText(String.format("$%,.2f", value));
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setForeground(Color.BLACK); // Cambiado de Color.RED a Color.BLACK
                } else {
                    setText("");
                }
            }
        });

        // Renderizador para imágenes (mejorado)
        tablaProductos.setDefaultRenderer(Icon.class, new ImageRenderer());
        tablaProductos.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
              super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 5 && value != null && !value.toString().isEmpty()) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setText("(Imagen)");

                    if (value instanceof Integer) {
                        setText(value.toString()); // Mostrar como entero sin decimales
                    }
                }
                return this;
            }
        });
    }

    private void configurarEditores() {
        // Editor para precios con validación
        JTextField editorPrecio = new JTextField();
        editorPrecio.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaProductos.setDefaultEditor(Double.class, new DefaultCellEditor(editorPrecio) {
        	@Override
            public boolean stopCellEditing() {
                try {
                    String value = editorPrecio.getText().replace("$", "").replace(",", "").trim();
                    if (value.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "El precio no puede estar vacío", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    double precio = Double.parseDouble(value);
                    if (precio < 0) {
                        JOptionPane.showMessageDialog(null, "El precio no puede ser negativo", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Ingrese un precio válido (ej: 1500.50)", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            @Override
            public Object getCellEditorValue() {
                try {
                    String value = editorPrecio.getText().replace("$", "").replace(",", "").trim();
                    return value.isEmpty() ? 0.0 : Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        });

        // Editor para categorías con JComboBox
        JComboBox<String> comboEditor = new JComboBox<>(new String[]{"Laptop", "Smartphone", "Accesorio"});
        comboEditor.setEditable(false);
        tablaProductos.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboEditor));

        // Editor genérico para texto
        tablaProductos.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
    }

    private void configurarAnchoColumnas() {
        int[] anchos = {40, 120, 80, 70, 150, 100, 90}; // Valores reducidos

        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            TableColumn col = tablaProductos.getColumnModel().getColumn(i);
            col.setPreferredWidth(anchos[i]);
        }

        // Permitir redimensionamiento solo para columnas con texto largo
        tablaProductos.getColumnModel().getColumn(1).setMinWidth(100); // Nombre
        tablaProductos.getColumnModel().getColumn(4).setMinWidth(120); // Descripción
    }
    private void configurarCabecera() {
        JTableHeader header = tablaProductos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        // Renderizador centrado para cabeceras
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }


    private class ImageRenderer extends DefaultTableCellRenderer {
    	private final int IMG_WIDTH = 60;  // Reducir ancho
        private final int IMG_HEIGHT = 60; // Reducir alto
        private final int PADDING = 5;     // Reducir espaciado interno

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = new JLabel();
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

            // Configurar colores según selección
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                label.setForeground(Color.BLACK);
            }

            if (value instanceof String && !((String) value).isEmpty()) {
                try {
                    String imagePath = "imagenes/" + value.toString();
                    if (new File(imagePath).exists()) {
                        ImageIcon icon = new ImageIcon(imagePath);

                        // Escalar manteniendo proporciones
                        Image img = icon.getImage();
                        double aspectRatio = (double) img.getWidth(null) / img.getHeight(null);

                        int width, height;
                        if (aspectRatio > 1) {
                            // Imagen horizontal
                            width = IMG_WIDTH;
                            height = (int) (width / aspectRatio);
                        } else {
                            // Imagen vertical
                            height = IMG_HEIGHT;
                            width = (int) (height * aspectRatio);
                        }

                        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        label.setIcon(new ImageIcon(scaledImg));
                        label.setText("");
                    } else {
                        label.setIcon(null);
                        label.setText("Sin imagen");
                    }
                } catch (Exception e) {
                    label.setIcon(null);
                    label.setText("Error");
                }
            } else {
                label.setIcon(null);
                label.setText("Sin dato");
            }

            return label;
        }
    }


    private void configurarEditorSpinner() {
        if (tablaProductos.getColumnCount() <= 3) {
			return;
		}

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.1);
        JSpinner spinner = new JSpinner(spinnerModel);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#0.00");
        spinner.setEditor(editor);

        DefaultCellEditor spinnerEditor = new DefaultCellEditor(editor.getTextField()) {
            @Override
            public Object getCellEditorValue() {
                try {
                    return Double.parseDouble(editor.getTextField().getText());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        };

        tablaProductos.getColumnModel().getColumn(3).setCellEditor(spinnerEditor);
    }

    private JPanel  configurarPanelBusqueda() {
    	 JPanel panelBusqueda = new JPanel();

        txtBusqueda = new JTextField(20);
        comboFiltros = new JComboBox<>(new String[]{"Todos", "ID", "Nombre", "Marca", "Descripción"});
        comboCategorias = new JComboBox<>(new String[]{"Todas", "Laptop", "Smartphone", "Accesorio"});
        JButton btnBuscar = new JButton("Buscar");

        btnBuscar.addActionListener(e -> buscarProductos());
        comboCategorias.addActionListener(e -> filtrarPorCategoria());

        panelBusqueda.add(new JLabel("Filtrar por:"));
        panelBusqueda.add(comboFiltros);
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(new JLabel("Categoría:"));
        panelBusqueda.add(comboCategorias);
        panelBusqueda.add(btnBuscar);

        add(panelBusqueda, BorderLayout.NORTH);

        comboCategorias.addActionListener(e -> {
            try {
                filtrarPorCategoria();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Asegurar que el modelo tenga datos
        if (modeloTabla.getRowCount() == 0) {
        	ProductoControlador.cargarDatosExcel(modeloTabla);
        }

        return panelBusqueda;
    }

    private JPanel configurarPanelBotones() {
        JPanel panelBotones = new JPanel();

        JButton btnCargarExcel = new JButton("Cargar Excel");
        btnCargarExcel.addActionListener(e -> {
            // Limpiar tabla antes de cargar
            modeloTabla.setRowCount(0);

            // Mostrar diálogo de carga
            JOptionPane.showMessageDialog(this,
                "Cargando datos desde Excel...",
                "Cargando",
                JOptionPane.INFORMATION_MESSAGE);

            // Cargar datos
            ProductoControlador.cargarDatosExcel(modeloTabla);
        });

        btnCargarExcel.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                	ProductoControlador.cargarDatosExcel(modeloTabla);
                    return null;
                }

                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(ProductosVista.this,
                        "Datos cargados correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }.execute();
        });

        JButton btnNuevo = new JButton("Nuevo Producto");

        JButton btnGuardar = new JButton("Guardar Cambios");
        JButton btnEliminar = new JButton("Eliminar");

     // Nuevo botón de recarga
        JButton btnRecargar = new JButton("Forzar Recarga");
        btnRecargar.addActionListener(e -> {
        	ProductoControlador.cargarDatosExcel(modeloTabla);
        });

        btnNuevo.addActionListener(e -> agregarNuevoProducto());
        btnCargarExcel.addActionListener(e -> ProductoControlador.cargarDatosExcel(modeloTabla));
        btnGuardar.addActionListener(e -> ProductoControlador.guardarCambiosExcel(modeloTabla, "data/productos.xlsx"));
        btnEliminar.addActionListener(e -> eliminarProducto());

        panelBotones.add(btnNuevo);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);

        add(panelBotones, BorderLayout.SOUTH);

        return panelBotones;
    }



    private void agregarNuevoProducto() {
        // Verificar existencia del archivo primero
        if (!new File("data/productos.xlsx").exists()) {
            JOptionPane.showMessageDialog(this,
                "El archivo de productos no existe. Cree uno primero.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Nuevo Producto", true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        // Componentes del formulario
        JTextField txtId = new JTextField(String.valueOf(obtenerSiguienteID()));
        txtId.setEditable(false); // ID generado automáticamente

        JTextField txtNombre = new JTextField();
        JTextField txtMarca = new JTextField();
        JSpinner spnPrecio = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.1));
        JTextField txtDescripcion = new JTextField();
        JTextField txtImagen = new JTextField();
        JComboBox<String> cbCategoria = new JComboBox<>(new String[]{"Smartphone", "Laptop", "Accesorio"});
        JTextField txtProveedor = new JTextField();

        // Stock mínimo fijo
        JSpinner spnStockMinimo = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        spnStockMinimo.setEnabled(false); // No editable

        // Configurar spinner de precio
        spnPrecio.setEditor(new JSpinner.NumberEditor(spnPrecio, "#0.00"));

        // Selector de imagen
        JButton btnSeleccionarImagen = new JButton("Seleccionar");
        btnSeleccionarImagen.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(new File("imagenes"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png"));
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                txtImagen.setText(fileChooser.getSelectedFile().getName());
            }
        });

        // Añadir componentes al diálogo
        agregarCampo(dialog, "ID:", txtId);
        agregarCampo(dialog, "Nombre:", txtNombre);
        agregarCampo(dialog, "Marca:", txtMarca);
        agregarCampo(dialog, "Precio:", spnPrecio);
        agregarCampo(dialog, "Descripción:", txtDescripcion);
        agregarCampoConBoton(dialog, "Imagen:", txtImagen, btnSeleccionarImagen);
        agregarCampo(dialog, "Categoría:", cbCategoria);
        agregarCampo(dialog, "Proveedor:", txtProveedor);
        agregarCampo(dialog, "Stock Mínimo:", spnStockMinimo);

        // Botón guardar
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> {
            try {
                if (validarCampos(txtNombre, txtMarca, txtProveedor)) {
                    modeloTabla.addRow(new Object[]{
                        Integer.parseInt(txtId.getText()), // ID numérico
                        txtNombre.getText(),
                        txtMarca.getText(),
                        spnPrecio.getValue(),
                        txtDescripcion.getText(),
                        txtImagen.getText(),
                        cbCategoria.getSelectedItem(),
                        0, // Stock inicial (0)
                        spnStockMinimo.getValue(), // Stock mínimo (5)
                        txtProveedor.getText(), // Proveedor
                        0  // Ventas iniciales (0)
                    });
                    dialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel());
        dialog.add(btnGuardar);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Método para obtener el siguiente ID numérico
    private int obtenerSiguienteID() {
        try (Workbook workbook = WorkbookFactory.create(new File("data/productos.xlsx"))) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            int maxId = 0;

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell idCell = row.getCell(0); // Columna ID
                    if (idCell != null) {
                        try {
                            int currentId = (int)idCell.getNumericCellValue();
                            if (currentId > maxId) {
                                maxId = currentId;
                            }
                        } catch (Exception e) {
                            // Si hay error al leer como número, ignorar esta fila
                        }
                    }
                }
            }
            return maxId + 1;
        } catch (Exception e) {
            return 1; // Si hay error, empezar desde 1
        }
    }

    private void agregarCampo(JDialog dialog, String label, Component field) {
        dialog.add(new JLabel(label));
        dialog.add(field);
    }

    private void agregarCampoConBoton(JDialog dialog, String label, JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        dialog.add(new JLabel(label));
        dialog.add(panel);
    }

    private boolean validarCampos(JTextField... campos) {
        for (JTextField campo : campos) {
            if (campo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Todos los campos obligatorios deben estar completos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void buscarProductos() {
        String texto = txtBusqueda.getText().trim().toLowerCase();
        String filtro = comboFiltros.getSelectedItem().toString();

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);

        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                if (filtro.equals("ID")) {
                    sorter.setRowFilter(RowFilter.numberFilter(
                        RowFilter.ComparisonType.EQUAL,
                        Integer.parseInt(texto),
                        0));
                } else {
                    int col = filtro.equals("Nombre") ? 1 :
                             filtro.equals("Marca") ? 2 :
                             filtro.equals("Descripción") ? 4 : -1;
                    if (col != -1) {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, col));
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Para buscar por ID debe ingresar un número",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void filtrarPorCategoria() {
        String categoria = comboCategorias.getSelectedItem().toString();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);

        if (categoria.equals("Todas")) {
            sorter.setRowFilter(null);
        } else {
            // Filtro insensible a mayúsculas/minúsculas
            sorter.setRowFilter(RowFilter.regexFilter("(?i)^" + Pattern.quote(categoria) + "$", 6));
        }
    }

    private void eliminarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila >= 0) {
            modeloTabla.removeRow(fila);
        } else {
            JOptionPane.showMessageDialog(this,
                "Seleccione un producto primero",
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }
}