package persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import modelo.Producto;

public class PersistenciaTXT {
    public void guardarProductos(List<Producto> productos, String archivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (Producto producto : productos) {
                writer.write(producto.getId() + "," + producto.getNombre() + "," + producto.getPrecio() + "," + producto.getCantidad());
                writer.newLine();
            }
        }
    }

    public List<Producto> cargarProductos(String archivo) throws IOException {
        List<Producto> productos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                Producto producto = new Producto(Integer.parseInt(datos[0]), datos[1], Double.parseDouble(datos[2]), Integer.parseInt(datos[3]));
                productos.add(producto);
            }
        }
        return productos;
    }
}
