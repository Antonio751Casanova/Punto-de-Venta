package vista;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class SeleccionarFechaDialog extends JDialog {
    private LocalDate fechaSeleccionada;

    public SeleccionarFechaDialog(JFrame parent, List<LocalDate> fechas) {
        super(parent, "Seleccionar Fecha con Recibos", true);
        setSize(350, 400);
        setLocationRelativeTo(parent);

        // Modelo para la lista
        DefaultListModel<String> model = new DefaultListModel<>();
        fechas.forEach(fecha ->
            model.addElement(fecha.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy"))));

        JList<String> listaFechas = new JList<>(model);
        listaFechas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaFechas.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnAceptar = new JButton("Cargar Recibos");
        btnAceptar.addActionListener(e -> {
            int selectedIndex = listaFechas.getSelectedIndex();
            if (selectedIndex >= 0) {
                fechaSeleccionada = fechas.get(selectedIndex);
                dispose();
            }
        });

        // Panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JLabel("Seleccione una fecha:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(listaFechas), BorderLayout.CENTER);
        panel.add(btnAceptar, BorderLayout.SOUTH);

        add(panel);
    }

    public LocalDate getFechaSeleccionada() {
        return fechaSeleccionada;
    }
}