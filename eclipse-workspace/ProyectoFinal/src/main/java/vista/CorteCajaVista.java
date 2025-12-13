package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import utils.ExcelManager;
import utils.PDFProcessor;

public class CorteCajaVista extends JFrame {
    // Paleta de colores
    private static final Color COLOR_AZUL_PRIMARIO = new Color(0, 102, 204);
    private static final Color COLOR_AZUL_HOVER = new Color(0, 128, 255);
    private static final Color COLOR_NARANJA_PRIMARIO = new Color(255, 140, 0);
    private static final Color COLOR_NARANJA_HOVER = new Color(255, 165, 50);
    private static final Color COLOR_FONDO = new Color(245, 245, 245);
    private static final Color COLOR_TEXTO_OSCURO = new Color(50, 50, 50);
    private static final Color COLOR_BORDE = new Color(200, 200, 200);

    // Componentes
    private JTextField txtMontoInicial, txtVentasEfectivo, txtVentasTarjeta,
                      txtVentasTransferencia, txtRetiros, txtObservaciones;
    private JLabel lblMontoFinal, lblFecha;
    private JButton btnCalcular, btnGuardar, btnSeleccionarFecha, btnCargarRecibos;

    public CorteCajaVista() {
        setTitle("Sistema de Corte de Caja - Electronics Technology");
        setSize(700, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);

        initComponents();
        configurarEventos();
    }

    private void initComponents() {
        // Panel principal con sombra y bordes redondeados
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(COLOR_FONDO);

        // 1. Panel superior (Título y fecha)
        JPanel panelSuperior = crearPanelSuperior();

        // 2. Panel central (Formulario con estilo moderno)
        JPanel panelFormulario = new JPanel(new GridLayout(7, 2, 15, 15));
        panelFormulario.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDE, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setOpaque(true);

        // Campos del formulario con estilo mejorado
        agregarCampo(panelFormulario, "Monto Inicial ($):", txtMontoInicial = crearTextField());
        agregarCampo(panelFormulario, "Ventas en Efectivo ($):", txtVentasEfectivo = crearTextField());
        agregarCampo(panelFormulario, "Ventas con Tarjeta ($):", txtVentasTarjeta = crearTextField());
        agregarCampo(panelFormulario, "Ventas por Transferencia ($):", txtVentasTransferencia = crearTextField());
        agregarCampo(panelFormulario, "Retiros ($):", txtRetiros = crearTextField("0.00"));

        // Monto Final con diseño destacado
        JPanel panelMontoFinal = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelMontoFinal.setBackground(Color.WHITE);
        JLabel lblMontoFinalText = new JLabel("Monto Final ($):");
        lblMontoFinalText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMontoFinalText.setForeground(COLOR_TEXTO_OSCURO);
        panelMontoFinal.add(lblMontoFinalText);

        lblMontoFinal = new JLabel("0.00");
        lblMontoFinal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMontoFinal.setForeground(COLOR_AZUL_PRIMARIO);
        panelMontoFinal.add(lblMontoFinal);

        panelFormulario.add(panelMontoFinal);
        panelFormulario.add(new JLabel()); // Espacio vacío para alineación

        agregarCampo(panelFormulario, "Observaciones:", txtObservaciones = crearTextField());

        // 3. Panel inferior (Botones con efectos)
        JPanel panelBotones = crearPanelBotones();

        // Ensamblar la interfaz
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_AZUL_PRIMARIO);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Título con sombra de texto
        JLabel lblTitulo = new JLabel("CORTE DE CAJA", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        // Panel de fecha
        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFecha.setOpaque(false);

        lblFecha = new JLabel("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFecha.setForeground(Color.WHITE);
        panelFecha.add(lblFecha);

        // Botones superiores con estilo moderno
        btnSeleccionarFecha = crearBoton("Cambiar Fecha", COLOR_NARANJA_PRIMARIO, COLOR_NARANJA_HOVER);
        btnCargarRecibos = crearBoton("Cargar Recibos", COLOR_AZUL_PRIMARIO.darker(), COLOR_AZUL_HOVER);

        JPanel panelBotonesSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotonesSuperior.setOpaque(false);
        panelBotonesSuperior.add(btnSeleccionarFecha);
        panelBotonesSuperior.add(btnCargarRecibos);

        panel.add(lblTitulo, BorderLayout.CENTER);
        panel.add(panelFecha, BorderLayout.WEST);
        panel.add(panelBotonesSuperior, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.setOpaque(false);

        btnCalcular = crearBoton("Calcular Monto", COLOR_AZUL_PRIMARIO, COLOR_AZUL_HOVER);
        btnGuardar = crearBoton("Guardar Corte", COLOR_NARANJA_PRIMARIO, COLOR_NARANJA_HOVER);

        panel.add(btnCalcular);
        panel.add(btnGuardar);

        return panel;
    }

    private JButton crearBoton(String texto, Color color, Color hoverColor) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.darker(), 1, true),
            new EmptyBorder(8, 25, 8, 25)
        ));
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Efecto hover
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(color);
            }
        });

        return boton;
    }

    private JTextField crearTextField() {
        return crearTextField("");
    }

    private JTextField crearTextField(String texto) {
        JTextField field = new JTextField(texto);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDE, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private void agregarCampo(JPanel panel, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(COLOR_TEXTO_OSCURO);
        panel.add(lbl);
        panel.add(field);
    }

    private void configurarEventos() {
        // DocumentListener para cálculo automático
        javax.swing.event.DocumentListener listener = new javax.swing.event.DocumentListener() {
            @Override
			public void changedUpdate(javax.swing.event.DocumentEvent e) { calcular(); }
            @Override
			public void insertUpdate(javax.swing.event.DocumentEvent e) { calcular(); }
            @Override
			public void removeUpdate(javax.swing.event.DocumentEvent e) { calcular(); }

            private void calcular() {
                try {
                    double total = parseDouble(txtMontoInicial.getText()) +
                                 parseDouble(txtVentasEfectivo.getText()) +
                                 parseDouble(txtVentasTarjeta.getText()) +
                                 parseDouble(txtVentasTransferencia.getText()) -
                                 parseDouble(txtRetiros.getText());

                    lblMontoFinal.setText(String.format("$%,.2f", total));
                } catch (NumberFormatException ex) {
                    lblMontoFinal.setText("Error en datos");
                }
            }
        };

        txtMontoInicial.getDocument().addDocumentListener(listener);
        txtVentasEfectivo.getDocument().addDocumentListener(listener);
        txtVentasTarjeta.getDocument().addDocumentListener(listener);
        txtVentasTransferencia.getDocument().addDocumentListener(listener);
        txtRetiros.getDocument().addDocumentListener(listener);

        // Botón Guardar
        btnGuardar.addActionListener(e -> guardarCorte());

        // Botón Cargar Recibos
        btnCargarRecibos.addActionListener(e -> {
            String fechaTexto = lblFecha.getText().replace("Fecha: ", "");
            LocalDate fecha = fechaTexto.isEmpty() ?
                LocalDate.now() :
                LocalDate.parse(fechaTexto, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            cargarRecibosDelDia(fecha);
        });

        // Botón Seleccionar Fecha
        btnSeleccionarFecha.addActionListener(e -> {
            try {
                List<LocalDate> fechas = PDFProcessor.obtenerFechasConRecibos();
                if (fechas.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "No hay recibos registrados en el sistema",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                SeleccionarFechaDialog dialog = new SeleccionarFechaDialog(this, fechas);
                dialog.setVisible(true);

                if (dialog.getFechaSeleccionada() != null) {
                    cargarRecibosDelDia(dialog.getFechaSeleccionada());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error al leer recibos: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
			return 0.0;
		}
        try {
            return Double.parseDouble(value.replace("$", "").replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void cargarRecibosDelDia(LocalDate fecha) {
        try {
            if (!PDFProcessor.existenRecibosParaFecha(fecha)) {
                JOptionPane.showMessageDialog(this,
                    "No se encontraron recibos para el " +
                    fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            Map<String, Double> ventas = PDFProcessor.procesarRecibosDelDia(fecha);
            txtVentasEfectivo.setText(String.format("%.2f", ventas.getOrDefault("EFECTIVO", 0.0)));
            txtVentasTarjeta.setText(String.format("%.2f", ventas.getOrDefault("TARJETA", 0.0)));
            txtVentasTransferencia.setText(String.format("%.2f", ventas.getOrDefault("TRANSFERENCIA", 0.0)));
            lblFecha.setText("Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            calcular();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar recibos:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calcular() {
        try {
            double total = parseDouble(txtMontoInicial.getText()) +
                          parseDouble(txtVentasEfectivo.getText()) +
                          parseDouble(txtVentasTarjeta.getText()) +
                          parseDouble(txtVentasTransferencia.getText()) -
                          parseDouble(txtRetiros.getText());

            lblMontoFinal.setText(String.format("$%,.2f", total));
        } catch (NumberFormatException ex) {
            lblMontoFinal.setText("Error en datos");
        }
    }

    private void guardarCorte() {
        try {
            if (txtMontoInicial.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("El monto inicial es requerido");
            }

            ExcelManager.guardarCorte(
                LocalDate.parse(lblFecha.getText().replace("Fecha: ", ""),
                              DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                parseDouble(txtMontoInicial.getText()),
                parseDouble(txtVentasEfectivo.getText()),
                parseDouble(txtVentasTarjeta.getText()),
                parseDouble(txtVentasTransferencia.getText()),
                parseDouble(txtRetiros.getText()),
                parseDouble(lblMontoFinal.getText()),
                txtObservaciones.getText()
            );

            JOptionPane.showMessageDialog(this,
                "Corte guardado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            limpiarCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtVentasEfectivo.setText("");
        txtVentasTarjeta.setText("");
        txtVentasTransferencia.setText("");
        txtRetiros.setText("0.00");
        txtObservaciones.setText("");
        lblMontoFinal.setText("0.00");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CorteCajaVista view = new CorteCajaVista();
            view.setVisible(true);
        });
    }
}