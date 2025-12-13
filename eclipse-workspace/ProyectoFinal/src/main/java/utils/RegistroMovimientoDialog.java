package utils;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class RegistroMovimientoDialog extends JDialog {
    private JComboBox<String> cmbTipo;
    private JSpinner spnCantidad;
    private JTextField txtMotivo;
    private boolean registrado = false;

    public RegistroMovimientoDialog(JFrame parent, String producto, int stockActual) {
        super(parent, "Registrar Movimiento", true);

        // Panel principal con borde y espaciado
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 1. Fila: Producto
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Producto:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(producto), gbc);

        // 2. Fila: Stock Actual
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Stock Actual:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(stockActual)), gbc);

        // 3. Fila: Tipo Movimiento
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        cmbTipo = new JComboBox<>(new String[]{"ENTRADA", "SALIDA"});
        panel.add(cmbTipo, gbc);

        // 4. Fila: Cantidad
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 1;
        spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(spnCantidad, gbc);

        // 5. Fila: Motivo
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Motivo:"), gbc);
        gbc.gridx = 1;
        txtMotivo = new JTextField(15);
        panel.add(txtMotivo, gbc);

        // 6. Fila: Botones
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> {
            registrado = true;
            dispose();
        });

        panelBotones.add(btnCancelar);
        panelBotones.add(btnRegistrar);
        panel.add(panelBotones, gbc);

        add(panel);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public Object[] getDatos() {
        if (!registrado) {
			return null;
		}
        return new Object[]{
            new Date(),
            cmbTipo.getSelectedItem(),
            (Integer) spnCantidad.getValue(),
            txtMotivo.getText().trim()
        };
    }

    private boolean validarDatos(int stockActual) {
        String motivo = txtMotivo.getText().trim();
        int cantidad = (int) spnCantidad.getValue();
        String tipo = (String) cmbTipo.getSelectedItem();

        if (motivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe especificar un motivo", "Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (tipo.equals("SALIDA") && cantidad > stockActual) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // Getters
    public boolean fueRegistrado() { return registrado; }
    public String getTipoMovimiento() { return (String) cmbTipo.getSelectedItem(); }
    public int getCantidad() { return (int) spnCantidad.getValue(); }
    public String getMotivo() { return txtMotivo.getText().trim(); }



}