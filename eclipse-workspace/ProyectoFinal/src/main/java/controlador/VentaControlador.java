package controlador;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import modelo.Producto;
import vista.InventarioVista;
import vista.ReciboVistaDiaologo;

public class VentaControlador {
    private List<Producto> carrito = new ArrayList<>();

    // Añadir producto al carrito
    public void agregarProducto(Producto producto) {
        carrito.add(producto);
    }

    // Calcular total
    public double calcularTotal() {
        return carrito.stream().mapToDouble(Producto::getPrecio).sum();
    }


    public String generarContenidoRecibo(List<Producto> carrito, double subtotal, double iva,
            double total, String tipoPago, double montoRecibido,
            double cambio, String cajero, int folio) {
StringBuilder sb = new StringBuilder();

// Encabezado
sb.append("# ELECTRONICS TECHNOLOGY\n");
sb.append("Calle Jardin Dorado #464, Fracc. Los Prados\n");
sb.append("Altamira, Tamaulipas\n");
sb.append("----------------------------------------\n");
sb.append(String.format("Fecha: %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
sb.append(String.format("Hora: %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
sb.append(String.format("Cajero: %s\n", cajero));
sb.append(String.format("Folio: %d\n", folio));
sb.append("\n## DETALLE DE COMPRA\n\n");

// Tabla de productos
sb.append(String.format("| %-5s | %-20s | %-10s | %5s | %10s |\n",
   "ID", "Nombre", "Marca", "Cant.", "Precio"));
sb.append("------------------------------------------------------------\n");

for (Producto p : carrito) {
sb.append(String.format("| %-5d | %-20s | %-10s | %5d | %10.2f |\n",
       p.getId(), p.getNombre(), p.getMarca(), 1, p.getPrecio()));
}

// Totales
sb.append("\n----------------------------------------\n");
sb.append(String.format("Subtotal: $%,.2f\n", subtotal));
sb.append(String.format("IVA (16%%): $%,.2f\n", iva));
sb.append(String.format("Total: $%,.2f\n\n", total));
sb.append(String.format("Forma de pago: %s\n", tipoPago));
sb.append(String.format("Monto recibido: $%,.2f\n", montoRecibido));
sb.append(String.format("Cambio: $%,.2f\n\n", cambio));
sb.append("¡Gracias por su compra!\n");
sb.append("Visítenos nuevamente en ELECTRONICS TECHNOLOGY");

return sb.toString();
}

  public void finalizarVenta(List<Producto> carrito, String tipoPago, double montoRecibido, JFrame parent) {
// Validación temprana
if (carrito == null || carrito.isEmpty()) {
throw new IllegalStateException("El carrito está vacío");
}

// Cálculos financieros
double subtotal = calcularSubtotal(carrito);
double iva = subtotal * 0.16;
double total = subtotal + iva;
double cambio = montoRecibido - total;

if (cambio < 0) {
throw new IllegalArgumentException("Monto recibido insuficiente");
}

try {
// Generar contenido del recibo
String contenidoRecibo = generarContenidoRecibo(
carrito, subtotal, iva, total,
tipoPago, montoRecibido, cambio,
"Juan Antonio", 133751
);

// Mostrar vista previa
SwingUtilities.invokeLater(() -> {
	ReciboVistaDiaologo preview = new ReciboVistaDiaologo(parent, contenidoRecibo);
preview.setVisible(true);
});

// Actualizar stock y ventas
for (Producto producto : carrito) {
int nuevasVentas = producto.getVentas() + 1;
producto.setVentas(nuevasVentas);

ExcelControlador.actualizarVentasEnExcel(
producto.getId(),
nuevasVentas
);

ProductoControlador.actualizarStockEnExcel(
producto.getId(),
-1,
"VENTA-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
);
}

// Actualizar vista de inventario
SwingUtilities.invokeLater(() -> {
	InventarioVista.getInstance().cargarDatos();
});

// Generar PDF (opcionalmente podrías mover esto a un botón en el diálogo)
try {
	PDFGenerador.generarRecibo(
carrito,
subtotal,
iva,
total,
tipoPago,
montoRecibido,
cambio,
"Juan Antonio",
133751,
obtenerRutaLogoSegura()
);
} catch (Exception e) {
JOptionPane.showMessageDialog(parent,
"Error al generar PDF: " + e.getMessage(),
"Error",
JOptionPane.ERROR_MESSAGE);
}

} catch (Exception e) {
throw new RuntimeException("Error al procesar la venta: " + e.getMessage(), e);
} finally {
carrito.clear();
}
}



    private double calcularSubtotal(List<Producto> carrito) {
        return carrito.stream().mapToDouble(Producto::getPrecio).sum();
    }

    private String obtenerRutaLogoSegura() {
        // 1. Primero intentar con ruta del proyecto
        String rutaRelativa = "data/imagenes/iconos/electronics.jpg";
        File archivoLogo = new File(rutaRelativa);

        if (archivoLogo.exists()) {
            return rutaRelativa;
        }

        // 2. Intentar como recurso interno
        URL urlLogo = getClass().getResource("/imagenes/iconos/electronics.jpg");
        if (urlLogo != null) {
            return urlLogo.getPath();
        }

        // 3. Si no se encuentra, devolver null (se generará sin logo)
        return null;
    }



    public double calcularSubtotal() {
        return carrito.stream().mapToDouble(Producto::getPrecio).sum();
    }
 // En VentaController.java
    public boolean eliminarProducto(int indice) {
        if (indice >= 0 && indice < carrito.size()) {
            carrito.remove(indice);
            return true;
        }
        return false;
    }

    public List<Producto> getCarrito() {
        return new ArrayList<>(carrito); // Retorna copia para evitar modificaciones externas
    }
 // En VentaController.java
    public void limpiarCarrito() {
        carrito.clear();
    }

}