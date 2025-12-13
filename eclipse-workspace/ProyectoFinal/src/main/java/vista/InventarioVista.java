package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import controlador.ExcelControlador;
import controlador.ProductoControlador;
import modelo.Movimiento;
import modelo.Producto;
import utils.RegistroMovimientoDialog;

//En vista/InventarioView.java
public class InventarioVista extends JFrame {

	private static final Color COLOR_NARANJA = new Color(255, 140, 0);
    private static final Color COLOR_AZUL = new Color(30, 144, 255);
    private static final Color COLOR_AZUL_OSCURO = new Color(0, 71, 171);
    private static final Color COLOR_BLANCO = Color.WHITE;
    private static final Color COLOR_GRIS_CLARO = new Color(240, 240, 240);

 private JTable tablaInventario;
 private DefaultTableModel modelo;
 private JButton btnReponer, btnMovimientos;
 private static InventarioVista instance;
 private JTable tablaMovimientos;


 public InventarioVista() {
     // Configuración inicial del frame
     setTitle("Sistema de Gestión de Inventario");
     setSize(1000, 700);
     setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     getContentPane().setBackground(COLOR_BLANCO);

     // Configurar layout principal
     JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
     mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
     mainPanel.setBackground(COLOR_BLANCO);

     // 1. Panel superior (título y botón actualizar)
     JPanel panelSuperior = crearPanelSuperior();
     mainPanel.add(panelSuperior, BorderLayout.NORTH);

     // 2. Panel central (tabla de inventario)
     inicializarComponentesTabla();
     JScrollPane scrollInventario = new JScrollPane(tablaInventario);
     scrollInventario.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(COLOR_AZUL),
         "Inventario Actual",
         0, 0,
         new Font("Segoe UI", Font.BOLD, 14),
         COLOR_AZUL_OSCURO
     ));
     mainPanel.add(scrollInventario, BorderLayout.CENTER);

     // 3. Panel inferior (botones y tabla de movimientos)
     JPanel panelInferior = crearPanelInferior();
     mainPanel.add(panelInferior, BorderLayout.SOUTH);

     add(mainPanel);

     inicializarComponentes();
     configurarTablaMovimientos();
     cargarDatos();
     cargarDatosMovimientos();
 }

 private void inicializarComponentes() {
	    // 1. Configuración básica del JFrame
	    setTitle("Sistema de Gestión de Inventario");
	    setSize(1000, 700);
	    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    getContentPane().setBackground(COLOR_BLANCO);

	    // 2. Configurar layout principal
	    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    mainPanel.setBackground(COLOR_BLANCO);

	    // 3. Inicializar y configurar la tabla de inventario
	    modelo = new DefaultTableModel();
	    String[] columnas = {"ID", "Nombre", "Stock", "Stock Mínimo", "Proveedor", "Estado"};
	    modelo.setColumnIdentifiers(columnas);

	    tablaInventario = new JTable(modelo);
	    configurarTabla(); // Método que ya tienes implementado

	    // 4. Crear panel superior con título y botón de actualización
	    JPanel panelSuperior = new JPanel(new BorderLayout());
	    panelSuperior.setBackground(COLOR_BLANCO);

	    JLabel lblTitulo = new JLabel("GESTIÓN DE INVENTARIO", SwingConstants.CENTER);
	    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
	    lblTitulo.setForeground(COLOR_AZUL_OSCURO);
	    lblTitulo.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

	    JButton btnActualizar = crearBotonPersonalizado("Actualizar", COLOR_AZUL);
	    btnActualizar.addActionListener(e -> cargarDatos());

	    JPanel panelBotonesSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	    panelBotonesSuperior.setBackground(COLOR_BLANCO);
	    panelBotonesSuperior.add(btnActualizar);

	    panelSuperior.add(lblTitulo, BorderLayout.CENTER);
	    panelSuperior.add(panelBotonesSuperior, BorderLayout.EAST);

	    // 5. Panel central con tabla de inventario
	    JScrollPane scrollInventario = new JScrollPane(tablaInventario);
	    scrollInventario.setBorder(BorderFactory.createTitledBorder(
	        BorderFactory.createLineBorder(COLOR_AZUL),
	        "Inventario Actual",
	        0, 0,
	        new Font("Segoe UI", Font.BOLD, 14),
	        COLOR_AZUL_OSCURO
	    ));

	    // 6. Panel inferior con botones y tabla de movimientos
	    JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
	    panelInferior.setBackground(COLOR_BLANCO);

	    // Panel de botones principales
	    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
	    panelBotones.setBackground(COLOR_BLANCO);

	    btnReponer = crearBotonPersonalizado("Reponer Stock", COLOR_NARANJA);
	    btnMovimientos = crearBotonPersonalizado("Registrar Movimiento", COLOR_AZUL);

	    btnReponer.addActionListener(e -> abrirDialogoReposicion());
	    btnMovimientos.addActionListener(e -> abrirDialogoMovimiento());

	    panelBotones.add(btnReponer);
	    panelBotones.add(btnMovimientos);

	    // Panel de movimientos
	    JPanel panelMovimientos = new JPanel(new BorderLayout());
	    panelMovimientos.setBorder(BorderFactory.createTitledBorder(
	        BorderFactory.createLineBorder(COLOR_AZUL),
	        "Historial de Movimientos",
	        0, 0,
	        new Font("Segoe UI", Font.BOLD, 14),
	        COLOR_AZUL_OSCURO
	    ));
	    panelMovimientos.setBackground(COLOR_BLANCO);

	    tablaMovimientos = new JTable();
	    configurarTablaMovimientos();
	    panelMovimientos.add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);

	    panelInferior.add(panelBotones, BorderLayout.NORTH);
	    panelInferior.add(panelMovimientos, BorderLayout.CENTER);

	    // 7. Ensamblar todos los componentes
	    mainPanel.add(panelSuperior, BorderLayout.NORTH);
	    mainPanel.add(scrollInventario, BorderLayout.CENTER);
	    mainPanel.add(panelInferior, BorderLayout.SOUTH);

	    add(mainPanel);
	}



 private JPanel crearPanelSuperior() {
     JPanel panel = new JPanel(new BorderLayout());
     panel.setBackground(COLOR_BLANCO);

     // Título
     JLabel lblTitulo = new JLabel("GESTIÓN DE INVENTARIO", SwingConstants.CENTER);
     lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
     lblTitulo.setForeground(COLOR_AZUL_OSCURO);
     lblTitulo.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

     // Panel de botones
     JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
     panelBotones.setBackground(COLOR_BLANCO);

     JButton btnActualizar = crearBotonPersonalizado("Actualizar", COLOR_AZUL);
     btnActualizar.addActionListener(e -> cargarDatos());

     panelBotones.add(btnActualizar);
     panel.add(lblTitulo, BorderLayout.CENTER);
     panel.add(panelBotones, BorderLayout.EAST);

     return panel;
 }

 private JPanel crearPanelInferior() {
     JPanel panel = new JPanel(new BorderLayout(10, 10));
     panel.setBackground(COLOR_BLANCO);

     // Panel de botones principales
     JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
     panelBotones.setBackground(COLOR_BLANCO);

     btnReponer = crearBotonPersonalizado("Reponer Stock", COLOR_NARANJA);
     btnMovimientos = crearBotonPersonalizado("Registrar Movimiento", COLOR_AZUL);

     btnReponer.addActionListener(e -> abrirDialogoReposicion());
     btnMovimientos.addActionListener(e -> abrirDialogoMovimiento());

     panelBotones.add(btnReponer);
     panelBotones.add(btnMovimientos);

     // Panel de movimientos
     JPanel panelMovimientos = new JPanel(new BorderLayout());
     panelMovimientos.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createLineBorder(COLOR_AZUL),
         "Historial de Movimientos",
         0, 0,
         new Font("Segoe UI", Font.BOLD, 14),
         COLOR_AZUL_OSCURO
     ));
     panelMovimientos.setBackground(COLOR_BLANCO);

     tablaMovimientos = new JTable();
     configurarTablaMovimientos();
     panelMovimientos.add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);

     panel.add(panelBotones, BorderLayout.NORTH);
     panel.add(panelMovimientos, BorderLayout.CENTER);

     return panel;
 }

 private JButton crearBotonPersonalizado(String texto, Color colorFondo) {
     JButton boton = new JButton(texto);
     boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
     boton.setBackground(colorFondo);
     boton.setForeground(COLOR_BLANCO);
     boton.setFocusPainted(false);
     boton.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(colorFondo.darker()),
         BorderFactory.createEmptyBorder(8, 20, 8, 20)
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

 private void inicializarComponentesTabla() {
     modelo = new DefaultTableModel();
     String[] columnas = {"ID", "Nombre", "Stock", "Stock Mínimo", "Proveedor", "Estado"};
     modelo.setColumnIdentifiers(columnas);

     tablaInventario = new JTable(modelo);
     tablaInventario.setFont(new Font("Segoe UI", Font.PLAIN, 12));
     tablaInventario.setRowHeight(30);
     tablaInventario.setGridColor(COLOR_GRIS_CLARO);
     tablaInventario.setShowGrid(true);
     tablaInventario.setIntercellSpacing(new Dimension(0, 0));

     // Configurar header
     tablaInventario.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
     tablaInventario.getTableHeader().setBackground(COLOR_AZUL_OSCURO);
     tablaInventario.getTableHeader().setForeground(COLOR_BLANCO);
     tablaInventario.getTableHeader().setReorderingAllowed(false);

     // Renderer para celdas
     DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
     centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

     for (int i = 0; i < tablaInventario.getColumnCount(); i++) {
         tablaInventario.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
     }

     // Renderer personalizado para stock bajo
     tablaInventario.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
         /**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
         public Component getTableCellRendererComponent(JTable table, Object value,
                 boolean isSelected, boolean hasFocus, int row, int column) {
             Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

             int stock = (int) table.getModel().getValueAt(row, 2);
             int stockMinimo = (int) table.getModel().getValueAt(row, 3);

             if (stock <= stockMinimo) {
                 c.setBackground(new Color(255, 230, 230)); // Rojo claro
                 c.setFont(c.getFont().deriveFont(Font.BOLD));
                 ((JLabel) c).setForeground(Color.RED);
             } else {
                 c.setBackground(isSelected ? new Color(200, 230, 255) : COLOR_BLANCO);
                 ((JLabel) c).setForeground(Color.BLACK);
             }
             return c;
         }
     });
 }

 public static InventarioVista getInstance() {
	    if (instance == null) {
	        instance = new InventarioVista();
	    }
	    return instance;
	}

 public void cargarDatos() {
     try {
         List<Producto> productos = ExcelControlador.leerProductosDesdeExcel();
         modelo.setRowCount(0); // Limpiar tabla

         for (Producto p : productos) {
             String estado = (p.getStock() <= p.getStockMinimo()) ? "¡REPONER!" : "OK";
             modelo.addRow(new Object[]{
                 p.getId(),
                 p.getNombre(),
                 p.getStock(),
                 p.getStockMinimo(),
                 p.getProveedor(),
                 estado
             });
         }

         // Resaltar filas con stock bajo
         tablaInventario.setDefaultRenderer(Object.class, new StockBajoRenderer());
     } catch (Exception e) {
         JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(),
                                   "Error", JOptionPane.ERROR_MESSAGE);
     }
 }

 private class StockBajoRenderer extends DefaultTableCellRenderer {
	    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

		@Override
	    public Component getTableCellRendererComponent(
	            JTable table, Object value, boolean isSelected,
	            boolean hasFocus, int row, int column) {

	        Component c = super.getTableCellRendererComponent(
	            table, value, isSelected, hasFocus, row, column);

	        int stock = (int) table.getModel().getValueAt(row, 2);
	        int stockMinimo = (int) table.getModel().getValueAt(row, 3);

	        if (stock <= stockMinimo) {
	            c.setBackground(new Color(255, 200, 200)); // Rojo claro
	            c.setFont(c.getFont().deriveFont(Font.BOLD));
	        } else {
	            c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
	        }
	        return c;
	    }
	}

 private void abrirDialogoReposicion() {
     int filaSeleccionada = tablaInventario.getSelectedRow();
     if (filaSeleccionada == -1) {
         JOptionPane.showMessageDialog(this, "Seleccione un producto", "Error", JOptionPane.WARNING_MESSAGE);
         return;
     }

     int idProducto = (int) modelo.getValueAt(filaSeleccionada, 0);
     String productoNombre = (String) modelo.getValueAt(filaSeleccionada, 1);

     JDialog dialogo = new JDialog(this, "Reponer Stock", true);
     dialogo.setLayout(new GridLayout(3, 2));

     dialogo.add(new JLabel("Producto:"));
     dialogo.add(new JLabel(productoNombre));

     dialogo.add(new JLabel("Cantidad a reponer:"));
     JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
     dialogo.add(spinner);

     JButton btnConfirmar = new JButton("Confirmar");
     btnConfirmar.addActionListener(e -> {
         int cantidad = (int) spinner.getValue();
         actualizarStock(idProducto, cantidad, "REPOSICIÓN");
         dialogo.dispose();
     });

     dialogo.add(btnConfirmar);
     dialogo.pack();
     dialogo.setLocationRelativeTo(this);
     dialogo.setVisible(true);
 }

 private void actualizarStock(int idProducto, int cantidad, String tipoMovimiento) {
     // Implementar lógica para actualizar Excel
     // (Usar ProductoController para modificar el archivo)
	 ProductoControlador.actualizarStockEnExcel(idProducto, cantidad, tipoMovimiento);
     cargarDatos(); // Refrescar tabla
 }

 private void abrirDialogoMovimiento() {
	    // 1. Validar selección
	    int filaSeleccionada = tablaInventario.getSelectedRow();
	    if (filaSeleccionada == -1) {
	        mostrarMensajeError("Seleccione un producto de la tabla primero", "Error");
	        return;
	    }

	    // 2. Obtener datos del producto
	    Object[] datosProducto = obtenerDatosProducto(filaSeleccionada);
	    if (datosProducto == null) {
	        mostrarMensajeError("La fila seleccionada no contiene un producto válido", "Error");
	        return;
	    }

	    // 3. Crear diálogo mejorado
	    RegistroMovimientoDialog dialogo = new RegistroMovimientoDialog(
	        this,
	        (String) datosProducto[1], // nombre
	        (int) datosProducto[2]    // stock
	    );
	    dialogo.setVisible(true);

	    // 4. Procesar registro
	    if (dialogo.fueRegistrado()) {
	        registrarMovimiento(
	            (int) datosProducto[0], // id
	            (String) datosProducto[1], // nombre
	            dialogo.getTipoMovimiento(),
	            dialogo.getCantidad(),
	            dialogo.getMotivo(),
	            (int) datosProducto[2] // stock
	        );
	    }
	}

	// --- Métodos auxiliares ---

	private Object[] obtenerDatosProducto(int fila) {
	    try {
	        return new Object[]{
	            Integer.parseInt(modelo.getValueAt(fila, 0).toString()),
	            modelo.getValueAt(fila, 1).toString(),
	            Integer.parseInt(modelo.getValueAt(fila, 2).toString())
	        };
	    } catch (Exception e) {
	        return null;
	    }
	}

	private void registrarMovimiento(int id, String nombre, String tipo,
            int cantidad, String motivo, int stock) {
try {
boolean resultado = ProductoControlador.registrarMovimientoEnExcel(
id, nombre, tipo, cantidad, motivo, stock
);

if (resultado) {
cargarDatos();
cargarDatosMovimientos(); // Actualizar tabla de movimientos
mostrarMensajeExito("Movimiento registrado exitosamente");
} else {
throw new Exception("Error en el controlador");
}
} catch (Exception ex) {
mostrarMensajeError("Error al registrar: " + ex.getMessage(), "Error");
ex.printStackTrace();
}
}

	private void configurarTabla() {

		 // Centrar el contenido de todas las celdas
		    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		    for (int i = 0; i < tablaInventario.getColumnCount(); i++) {
		        tablaInventario.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		    }

		    // Configuración adicional (opcional)
		    tablaInventario.setRowHeight(25); // Altura de filas
		    tablaInventario.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); // Estilo de cabecera
		    tablaInventario.setGridColor(new Color(200, 200, 200)); // Color de grid

	     // Renderer para stock bajo
	     tablaInventario.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
	         /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
	         public Component getTableCellRendererComponent(JTable table, Object value,
	                 boolean isSelected, boolean hasFocus, int row, int column) {
	             Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	             int stock = (int) table.getModel().getValueAt(row, 2);
	             int stockMinimo = (int) table.getModel().getValueAt(row, 3);

	             if (stock <= stockMinimo) {
	                 c.setBackground(Color.PINK);
	                 c.setFont(c.getFont().deriveFont(Font.BOLD));
	             } else {
	                 c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
	             }
	             return c;
	         }
	     });
	 }

	private void actualizarTablaMovimientos() {
	    // 1. Limpiar selección
	    tablaMovimientos.clearSelection();

	    // 2. Forzar actualización
	    cargarDatosMovimientos();

	    // 3. Aplicar estilos (opcional)
	    configurarTablaMovimientos();
	}

	private void cargarDatosMovimientos() {
	    try {
	        // 1. Obtener datos del controlador
	        List<Movimiento> movimientos = ExcelControlador.leerMovimientosDesdeExcel();

	        // 2. Configurar modelo
	        DefaultTableModel modeloTablaMovimientos = new DefaultTableModel(
	            new Object[]{"Fecha", "ID", "Producto", "Tipo", "Cantidad", "Stock Resultante", "Motivo"},
	            0
	        ) {
	            /**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
	            public Class<?> getColumnClass(int column) {
	                if (column == 4 || column == 5) {
	                    return Integer.class;
	                }
	                return String.class;
	            }
	        };

	        // 3. Llenar tabla
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        for (Movimiento mov : movimientos) {
	            modeloTablaMovimientos.addRow(new Object[]{
	                dateFormat.format(mov.getFecha()),
	                mov.getIdProducto(),
	                mov.getNombreProducto(),
	                mov.getTipo(),
	                mov.getCantidad(),
	                mov.getStockResultante(),
	                mov.getMotivo()
	            });
	        }

	        // 4. Aplicar modelo
	        tablaMovimientos.setModel(modeloTablaMovimientos);

	        // 5. Ajustar columnas
	        tablaMovimientos.getColumnModel().getColumn(0).setPreferredWidth(150);
	        tablaMovimientos.getColumnModel().getColumn(2).setPreferredWidth(200);
	        tablaMovimientos.getColumnModel().getColumn(6).setPreferredWidth(250);

	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this,
	            "Error al cargar movimientos: " + e.getMessage(),
	            "Error",
	            JOptionPane.ERROR_MESSAGE);
	    }
	}



	private void mostrarMensajeError(String mensaje, String titulo) {
	    JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
	}

	private void mostrarMensajeExito(String mensaje) {
	    JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
	}

 private void configurarTablaMovimientos() {
    // 1. Modelo de tabla con columnas
    String[] columnas = {"Fecha", "ID", "Producto", "Tipo", "Cantidad", "Stock", "Motivo"};
    DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public Class<?> getColumnClass(int column) {
            return column == 4 || column == 5 ? Integer.class : String.class;
        }
    };
    tablaMovimientos.setModel(modelo);

    // 2. Renderers personalizados
    tablaMovimientos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Resaltar entradas/salidas
            String tipo = (String) table.getValueAt(row, 3);
            if ("ENTRADA".equalsIgnoreCase(tipo)) {
                c.setBackground(new Color(220, 255, 220)); // Verde claro
            } else {
                c.setBackground(new Color(255, 220, 220)); // Rojo claro
            }

            if (isSelected) {
                c.setBackground(new Color(180, 210, 255)); // Azul claro para selección
            }

            // Centrar contenido
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    });

    // 3. Ajustes visuales
    tablaMovimientos.setRowHeight(25);
    tablaMovimientos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    tablaMovimientos.setGridColor(new Color(200, 200, 200));
    tablaMovimientos.setShowGrid(true);
    tablaMovimientos.setIntercellSpacing(new Dimension(1, 1));

    // 4. Ajustar columnas
    tablaMovimientos.getColumnModel().getColumn(0).setPreferredWidth(150); // Fecha
    tablaMovimientos.getColumnModel().getColumn(2).setPreferredWidth(200); // Producto
    tablaMovimientos.getColumnModel().getColumn(6).setPreferredWidth(250); // Motivo
}


}

