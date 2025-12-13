package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

import controlador.ExcelControlador;
import controlador.VentaControlador;
import modelo.Producto;

public class VentaVista extends JFrame {
    private VentaControlador ventaController;
    private List<Producto> productosDisponibles;
    private JTextArea areaCarrito;
    private JComboBox<String> comboPago;
    private JTextField campoBusqueda, campoPago;
    private JLabel lblSubtotal, lblIva, lblTotal, lblCambio;
    private JPanel panelProductosContainer;

    public VentaVista() {
        // Configuración inicial de la ventana
        setTitle("Punto de Venta - Sistema de Ventas");
        setSize(1200, 800);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Inicializar componentes
        ventaController = new VentaControlador();

        try {
            productosDisponibles = ExcelControlador.leerProductosDesdeExcel();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            productosDisponibles = new ArrayList<>(); // Lista vacía para evitar NullPointerException
            e.printStackTrace();
        }

     // Configurar área del carrito
        areaCarrito = new JTextArea(15, 30);
        areaCarrito.setEditable(false);
        areaCarrito.setFont(new Font("Monospaced", Font.PLAIN, 12));
        configurarAreaCarrito();

        // Panel principal con márgenes
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --------------------------------------------
        // Aquí va la integración del panel izquierdo
        // --------------------------------------------
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.add(crearPanelBusqueda(), BorderLayout.NORTH);  // Barra de búsqueda
        panelIzquierdo.add(crearPanelProductos(), BorderLayout.CENTER); // Lista de productos

        // Panel derecho (carrito y pago)
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(crearPanelCarrito(), BorderLayout.CENTER);
        panelDerecho.add(crearPanelPago(), BorderLayout.SOUTH);

        // Agregar a la ventana principal
        mainPanel.add(panelIzquierdo, BorderLayout.CENTER);
        mainPanel.add(panelDerecho, BorderLayout.EAST);
        add(mainPanel);

        // Botón eliminar
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        btnEliminar.addActionListener(e -> eliminarProducto());

        // Mostrar todos los productos al iniciar
        mostrarTodosProductos();
    }

    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Buscar (ID o Nombre):"));
        campoBusqueda = new JTextField(20);
        panel.add(campoBusqueda);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarProductos());
        panel.add(btnBuscar);
        return panel;
    }




    private void buscarProductos() {
        String texto = campoBusqueda.getText().trim().toLowerCase();
        panelProductosContainer.removeAll();

        if (texto.isEmpty()) {
            mostrarTodosProductos();
            return;
        }

        // Cambiar a BoxLayout para resultados de búsqueda
        panelProductosContainer.setLayout(new BoxLayout(panelProductosContainer, BoxLayout.Y_AXIS));

        // Buscar productos
        List<Producto> resultados = productosDisponibles.stream()
            .filter(p -> String.valueOf(p.getId()).equals(texto) ||  // Búsqueda exacta por ID
                       p.getNombre().toLowerCase().contains(texto))  // Búsqueda parcial por nombre
            .collect(Collectors.toList());

        if (resultados.isEmpty()) {
            JLabel lblNoResultados = new JLabel("No se encontraron productos", SwingConstants.CENTER);
            lblNoResultados.setFont(new Font("Arial", Font.ITALIC, 14));
            panelProductosContainer.add(lblNoResultados);
        } else {
            // Mostrar resultados en un formato más compacto
            for (Producto producto : resultados) {
                JPanel tarjeta = crearTarjetaBusqueda(producto); // Usamos un diseño especial para búsqueda
                panelProductosContainer.add(tarjeta);
                panelProductosContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        panelProductosContainer.revalidate();
        panelProductosContainer.repaint();
    }

    private JPanel crearTarjetaBusqueda(Producto producto) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 5));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tarjeta.setBackground(Color.WHITE);

        // Panel izquierdo (info)
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(Color.WHITE);

        JLabel lblNombre = new JLabel(producto.getNombre());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblMarca = new JLabel("Marca: " + producto.getMarca());
        lblMarca.setFont(new Font("Arial", Font.PLAIN, 12));

        panelInfo.add(lblNombre);
        panelInfo.add(Box.createRigidArea(new Dimension(0, 5)));
        panelInfo.add(lblMarca);

        // Panel derecho (precio y botón)
        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setBackground(Color.WHITE);

        JLabel lblPrecio = new JLabel(String.format("$%,.2f", producto.getPrecio()));
        lblPrecio.setFont(new Font("Arial", Font.BOLD, 16));
        lblPrecio.setForeground(new Color(0, 100, 0));

        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setMaximumSize(new Dimension(100, 25));
        btnAgregar.addActionListener(e -> {
            ventaController.agregarProducto(producto);
            actualizarCarrito();
            calcularTotales();
        });

        panelDerecho.add(lblPrecio);
        panelDerecho.add(Box.createRigidArea(new Dimension(0, 10)));
        panelDerecho.add(btnAgregar);

        tarjeta.add(panelInfo, BorderLayout.CENTER);
        tarjeta.add(panelDerecho, BorderLayout.EAST);

        return tarjeta;
    }
    private void mostrarTodosProductos() {
        panelProductosContainer.removeAll();
        panelProductosContainer.setLayout(new GridLayout(0, 3, 10, 10));

        for (Producto producto : productosDisponibles) {
            panelProductosContainer.add(crearTarjetaProducto(producto));
        }

        panelProductosContainer.revalidate();
        panelProductosContainer.repaint();
    }




    private void eliminarProducto() {
        String seleccion = areaCarrito.getSelectedText();
        if (seleccion == null || seleccion.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un producto del carrito para eliminar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Obtener el número de línea seleccionada
            int lineaInicio = areaCarrito.getLineOfOffset(areaCarrito.getSelectionStart());

            // Las primeras 2 líneas son el encabezado (línea 0 y 1)
            if (lineaInicio < 2) {
                JOptionPane.showMessageDialog(this,
                    "Seleccione un producto válido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int indiceCarrito = lineaInicio - 2;

            if (ventaController.eliminarProducto(indiceCarrito)) {
                actualizarCarrito();
                calcularTotales();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar el producto",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al eliminar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private JScrollPane crearPanelProductos() {
        // Configurar el panel contenedor
        panelProductosContainer = new JPanel();
        panelProductosContainer.setLayout(new GridLayout(0, 3, 10, 10)); // 3 columnas con espaciado

        // Crear el JScrollPane que contendrá al panel
        JScrollPane scrollPane = new JScrollPane(panelProductosContainer);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private JPanel crearTarjetaProducto(Producto producto) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tarjeta.setBackground(Color.WHITE);

        // Nombre del producto
        JLabel lblNombre = new JLabel(producto.getNombre(), SwingConstants.CENTER);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Marca
        JLabel lblMarca = new JLabel(producto.getMarca(), SwingConstants.CENTER);
        lblMarca.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMarca.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Precio
        JLabel lblPrecio = new JLabel(String.format("$%,.2f", producto.getPrecio()), SwingConstants.CENTER);
        lblPrecio.setFont(new Font("Arial", Font.BOLD, 16));
        lblPrecio.setForeground(new Color(0, 100, 0)); // Verde oscuro
        lblPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón Agregar
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregar.setMaximumSize(new Dimension(100, 25));
        btnAgregar.addActionListener(e -> {
            ventaController.agregarProducto(producto);
            actualizarCarrito();
            calcularTotales();
        });

        // Separador
        JSeparator separador = new JSeparator(SwingConstants.HORIZONTAL);
        separador.setMaximumSize(new Dimension(200, 5));

        // Agregar componentes a la tarjeta
        tarjeta.add(lblNombre);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjeta.add(lblMarca);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 10)));
        tarjeta.add(lblPrecio);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 10)));
        tarjeta.add(separador);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjeta.add(btnAgregar);

        // Estilo para resaltar al pasar el mouse
        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
			public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(new Color(240, 240, 240));
            }
            @Override
			public void mouseExited(MouseEvent e) {
                tarjeta.setBackground(Color.WHITE);
            }
        });

        return tarjeta;
    }

    private JPanel crearPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Carrito de Compras"));

        areaCarrito = new JTextArea(15, 30);
        areaCarrito.setEditable(false);
        areaCarrito.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Agregar mouse listener para selección automática
        areaCarrito.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    int offset = areaCarrito.viewToModel(e.getPoint());
                    int linea = areaCarrito.getLineOfOffset(offset);
                    try {
						seleccionarLineaCarrito(linea);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        btnEliminar.addActionListener(e -> eliminarProducto());

        panel.add(new JScrollPane(areaCarrito), BorderLayout.CENTER);
        panel.add(btnEliminar, BorderLayout.SOUTH);
        return panel;
    }



    private JPanel crearPanelPago() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pago"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Fuentes
        Font fontNormal = new Font("Arial", Font.PLAIN, 14);
        Font fontBold = new Font("Arial", Font.BOLD, 14);

        // Componentes
        lblSubtotal = new JLabel("$0.00");
        lblIva = new JLabel("$0.00");
        lblTotal = new JLabel("$0.00");
        lblCambio = new JLabel("$0.00");

        comboPago = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        campoPago = new JTextField(10);

        JButton btnCalcular = new JButton("Calcular Cambio");
        JButton btnFinalizar = new JButton("Finalizar Venta");
        JButton btnLimpiar = new JButton("Limpiar Carrito");

        // Configurar GridBagConstraints
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Primera fila - Subtotal
        panel.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1;
        panel.add(lblSubtotal, gbc);

        // Segunda fila - IVA
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("IVA (13%):"), gbc);
        gbc.gridx = 1;
        panel.add(lblIva, gbc);

        // Tercera fila - Total
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Total:"), gbc);
        gbc.gridx = 1;
        lblTotal.setFont(fontBold);
        panel.add(lblTotal, gbc);

        // Cuarta fila - Tipo de Pago
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Tipo de Pago:"), gbc);
        gbc.gridx = 1;
        panel.add(comboPago, gbc);

        // Quinta fila - Monto Recibido
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Monto Recibido:"), gbc);
        gbc.gridx = 1;
        panel.add(campoPago, gbc);

        // Sexta fila - Cambio
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Cambio:"), gbc);
        gbc.gridx = 1;
        panel.add(lblCambio, gbc);

        // Botones
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnCalcular, gbc);

        gbc.gridy = 7;
        panel.add(btnFinalizar, gbc);

        gbc.gridy = 8;
        panel.add(btnLimpiar, gbc);

        // Acciones de los botones
        btnCalcular.addActionListener(e -> calcularCambio());
        btnFinalizar.addActionListener(e -> finalizarVenta());
        btnLimpiar.addActionListener(e -> limpiarCarrito());

        return panel;
    }



 // Método actualizado para actualizar el carrito
    private void actualizarCarrito() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %6s %10s\n", "Producto", "Cant.", "Subtotal"));
        sb.append("----------------------------------------\n");

        List<Producto> carrito = ventaController.getCarrito();
        for (Producto p : carrito) {
            sb.append(String.format("%-20s %6d %10.2f\n",
                p.getNombre(), 1, p.getPrecio()));
        }

        areaCarrito.setText(sb.toString());
    }

    // Agrega este MouseListener al inicializar el areaCarrito
    @SuppressWarnings("unused")
	private void configurarAreaCarrito() {
        areaCarrito.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    int offset = areaCarrito.viewToModel(e.getPoint());
                    int linea = areaCarrito.getLineOfOffset(offset);
                    seleccionarLineaCarrito(linea);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void seleccionarLineaCarrito(int linea) throws Exception {
        int start = areaCarrito.getLineStartOffset(linea);
        int end = areaCarrito.getLineEndOffset(linea);
        areaCarrito.setSelectionStart(start);
        areaCarrito.setSelectionEnd(end);
    }


    private void calcularTotales() {
        double subtotal = ventaController.calcularSubtotal();
        double iva = subtotal * 0.13;
        double total = subtotal + iva;

        lblSubtotal.setText(String.format("$%,.2f", subtotal));
        lblIva.setText(String.format("$%,.2f", iva));
        lblTotal.setText(String.format("$%,.2f", total));
    }

    private void calcularCambio() {
        try {
            double total = Double.parseDouble(lblTotal.getText().replace("$", "").replace(",", ""));
            double pago = Double.parseDouble(campoPago.getText());
            double cambio = pago - total;

            lblCambio.setText(String.format("$%,.2f", cambio));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void finalizarVenta() {
        try {
            double montoRecibido = Double.parseDouble(campoPago.getText());
            String tipoPago = comboPago.getSelectedItem().toString();

            ventaController.finalizarVenta(
                ventaController.getCarrito(),
                tipoPago,
                montoRecibido,
                this
            );

            JOptionPane.showMessageDialog(this, "Venta completada y recibo generado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCarrito();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCarrito() {
        ventaController.limpiarCarrito();
        areaCarrito.setText("");
        lblSubtotal.setText("$0.00");
        lblIva.setText("$0.00");
        lblTotal.setText("$0.00");
        lblCambio.setText("$0.00");
        campoPago.setText("");
    }

    class ProductoListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Producto) {
                Producto p = (Producto) value;
                setText(String.format("%d - %s - $%,.2f", p.getId(), p.getNombre(), p.getPrecio()));
            }
            return this;
        }
    }
}