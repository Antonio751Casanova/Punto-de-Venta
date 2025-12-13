package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import controlador.ExcelControlador;
import modelo.Producto;

public class MenuPrincipal extends JFrame {

    private final Color azulPrincipal = new Color(33, 97, 140);
    private final Color naranjaPrincipal = new Color(243, 156, 18);
    private final Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 26);
    private final Font fuenteBoton = new Font("Segoe UI", Font.PLAIN, 16);

    public MenuPrincipal() {
        setTitle("Sistema de Punto de Venta");
        setSize(600, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        verificarStockBajo();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Encabezado
        JLabel titleLabel = new JLabel("Sistema de Punto de Venta", SwingConstants.CENTER);
        titleLabel.setFont(fuenteTitulo);
        titleLabel.setForeground(azulPrincipal);
        titleLabel.setBorder(new EmptyBorder(30, 10, 20, 10));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 2, 20, 20));
        buttonPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        buttonPanel.setBackground(Color.WHITE);

        buttonPanel.add(createStyledButton("Punto de Venta", azulPrincipal, e -> new VentaVista().setVisible(true)));
        buttonPanel.add(createStyledButton("Productos", naranjaPrincipal, e -> new ProductosVista().setVisible(true)));
        buttonPanel.add(createStyledButton("Inventario", azulPrincipal, e -> new InventarioVista().setVisible(true)));
        buttonPanel.add(createStyledButton("Reportes", naranjaPrincipal, e -> new ReportesVista().setVisible(true)));
        buttonPanel.add(createStyledButton("Corte de Caja", azulPrincipal, e -> new CorteCajaVista().setVisible(true)));
        buttonPanel.add(createStyledButton("Salir", new Color(192, 57, 43), e -> System.exit(0)));

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Pie de página
        JLabel footerLabel = new JLabel("© 2025 Punto de Venta | Versión Profesional", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(fuenteBoton);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void verificarStockBajo() {
        try {
            List<Producto> productos = ExcelControlador.leerProductosDesdeExcel();
            List<Producto> productosBajos = productos.stream()
                .filter(p -> p.getStock() <= p.getStockMinimo())
                .collect(Collectors.toList());

            if (!productosBajos.isEmpty()) {
                String mensaje = "<html><body style='width: 300px;'><h3>¡Stock bajo para " + productosBajos.size() + " productos!</h3><ul>";

                mensaje += productosBajos.stream()
                    .map(p -> "<li>" + p.getNombre() + " (Stock: " + p.getStock() + "/" + p.getStockMinimo() + ")</li>")
                    .collect(Collectors.joining());

                mensaje += "</ul></body></html>";

                JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    "Alerta de Inventario",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al verificar stock: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(MenuPrincipal::new);
    }
}
