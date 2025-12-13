package vista;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ReciboVistaDiaologo extends JDialog {
    private JButton btnAceptar;
    private JTextArea areaRecibo;
    private JButton btnImprimir;
    private JButton btnGuardarPDF;

    public ReciboVistaDiaologo(JFrame parent, String contenidoRecibo) {
        super(parent, "Vista Previa del Recibo", true);
        setSize(500, 600);
        setLocationRelativeTo(parent);

        // Área de texto para mostrar el recibo
        areaRecibo = new JTextArea(contenidoRecibo);
        areaRecibo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaRecibo.setEditable(false);

        // Botón Aceptar
        btnAceptar = new JButton("Aceptar");
        btnAceptar.addActionListener(e -> dispose());

        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnAceptar);

        // Configurar layout
        setLayout(new BorderLayout());
        add(new JScrollPane(areaRecibo), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

     // En el constructor:
        btnImprimir = new JButton("Imprimir");
        btnGuardarPDF = new JButton("Guardar PDF");

        btnImprimir.addActionListener(e -> {
            // Lógica para imprimir
            try {
                areaRecibo.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnGuardarPDF.addActionListener(e -> {
            // Lógica para guardar PDF
            // (Aquí podrías llamar al PDFGenerator)
            dispose();
        });

        // Añadir al panel de botones:
        panelBotones.add(btnImprimir);
        panelBotones.add(btnGuardarPDF);
        panelBotones.add(btnAceptar);
    }
}