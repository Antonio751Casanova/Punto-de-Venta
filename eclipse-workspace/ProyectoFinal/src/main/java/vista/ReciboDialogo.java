package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import modelo.Producto;

public class ReciboDialogo extends JDialog {
    public ReciboDialogo(JFrame parent, List<Producto> carrito, double subtotal,
                       double iva, double total, String tipoPago,
                       double montoRecibido, double cambio) {
        super(parent, "Recibo de Venta", true);
        setSize(400, 600);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        // Encabezado
        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setLayout(new BorderLayout());
        panelEncabezado.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("ELECTRONICS TECHNOLOGY", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel lblDireccion = new JLabel("Calle Jardin Dorado #464, Fracc. Los Prados", SwingConstants.CENTER);
        lblDireccion.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel lblCiudad = new JLabel("Altamira, Tamaulipas", SwingConstants.CENTER);
        lblCiudad.setFont(new Font("Arial", Font.PLAIN, 12));

        panelEncabezado.add(lblTitulo, BorderLayout.NORTH);
        panelEncabezado.add(lblDireccion, BorderLayout.CENTER);
        panelEncabezado.add(lblCiudad, BorderLayout.SOUTH);

        // Separador
        JSeparator separador = new JSeparator(SwingConstants.HORIZONTAL);
        separador.setForeground(Color.BLACK);

        // Información de fecha y cajero
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new GridLayout(0, 1));
        panelInfo.setBackground(Color.WHITE);

        panelInfo.add(crearLineaInfo("Fecha:", ahora.format(fechaFormat)));
        panelInfo.add(crearLineaInfo("Hora:", ahora.format(horaFormat)));
        panelInfo.add(crearLineaInfo("Cajero:", "Juan Antonio"));
        panelInfo.add(crearLineaInfo("Folio:", "133751"));

        // Cuerpo del recibo
        JTextArea areaRecibo = new JTextArea();
        areaRecibo.setEditable(false);
        areaRecibo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaRecibo.setBackground(Color.WHITE);

        // Construir el contenido del recibo
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(" DETALLE DE COMPRA\n");
        sb.append("--------------------------------\n");
        sb.append(String.format(" %-3s %-15s %-8s %-4s %8s\n",
                              "ID", "Nombre", "Marca", "Cant.", "Precio"));
        sb.append("--------------------------------\n");

        for (Producto p : carrito) {
            sb.append(String.format(" %-3d %-15s %-8s %-4d %8.2f\n",
                                 p.getId(),
                                 p.getNombre().length() > 15 ? p.getNombre().substring(0, 12) + "..." : p.getNombre(),
                                 p.getMarca().length() > 8 ? p.getMarca().substring(0, 5) + "..." : p.getMarca(),
                                 1,
                                 p.getPrecio()));
        }

        sb.append("--------------------------------\n\n");
        sb.append(String.format("%22s %10.2f\n", "Subtotal:", subtotal));
        sb.append(String.format("%22s %10.2f\n", "IVA (13%):", iva));
        sb.append(String.format("%22s %10.2f\n", "Total:", total));
        sb.append("\n--------------------------------\n");
        sb.append(String.format("%22s %10s\n", "Forma de pago:", tipoPago));
        sb.append(String.format("%22s %10.2f\n", "Monto recibido:", montoRecibido));
        sb.append(String.format("%22s %10.2f\n", "Cambio:", cambio));
        sb.append("\n--------------------------------\n");
        sb.append(" ¡Gracias por su compra!\n");
        sb.append(" Visítenos nuevamente en\n");
        sb.append(" ELECTRONICS TECHNOLOGY\n");

        areaRecibo.setText(sb.toString());

        // Agregar componentes al panel principal
        panel.add(panelEncabezado, BorderLayout.NORTH);
        panel.add(separador, BorderLayout.CENTER);
        panel.add(panelInfo, BorderLayout.AFTER_LINE_ENDS);
        panel.add(new JScrollPane(areaRecibo), BorderLayout.CENTER);

        add(panel);
    }

    private JPanel crearLineaInfo(String etiqueta, String valor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(lblEtiqueta, BorderLayout.WEST);
        panel.add(lblValor, BorderLayout.EAST);

        return panel;
    }
}