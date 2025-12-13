package controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import modelo.Producto;

public class InventarioControlador {
    private static InventarioControlador instance;
    private List<Producto> productos;

    // Patrón Singleton
    public static InventarioControlador getInstance() {
        if (instance == null) {
            instance = new InventarioControlador();
        }
        return instance;
    }

    private InventarioControlador() {
        cargarProductosDesdeExcel();
    }

    private void cargarProductosDesdeExcel() {
        try {
            productos = ExcelControlador.leerProductosDesdeExcel();
        } catch (Exception e) {
            System.err.println("Error al cargar productos desde Excel: " + e.getMessage());
            productos = new ArrayList<>(); // Lista vacía para evitar NullPointerException
        }
    }

    public List<Producto> obtenerTodosProductos() {
        return new ArrayList<>(productos); // Retorna copia para evitar modificaciones externas
    }

    public List<Producto> obtenerProductosConAlerta() {
        return productos.stream()
            .filter(p -> p.getStock() <= p.getStockMinimo())
            .collect(Collectors.toList());
    }

    public Producto buscarProductoPorId(int id) {
        return productos.stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public boolean actualizarStock(int idProducto, int cantidad, String motivo) {
        try {
            return ExcelControlador.registrarMovimientoInventario(
                idProducto,
                buscarProductoPorId(idProducto).getNombre(),
                cantidad,
                motivo,
                buscarProductoPorId(idProducto).getStock() + cantidad
            );
        } catch (Exception e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }

    public boolean reponerStock(int idProducto, int cantidad) {
        return actualizarStock(idProducto, cantidad, "REPOSICIÓN DE STOCK");
    }

    public boolean ajustarStockMinimo(int idProducto, int nuevoMinimo) {
    	// Actualizar en Excel
    	try {
    	    boolean actualizado = ExcelControlador.actualizarStockMinimoEnExcel(
    	        idProducto,
    	        nuevoMinimo
    	    );

    	    if (actualizado) {
    	        return true;
    	    } else {
    	        System.err.println("No se pudo actualizar el stock mínimo en Excel");
    	        return false;
    	    }
    	} catch (Exception e) {
    	    System.err.println("Error al actualizar stock mínimo en Excel: " + e.getMessage());
    	    e.printStackTrace();
    	    return false;
    	}
    }

    public List<Producto> buscarProductos(String criterio) {
        String criterioLower = criterio.toLowerCase();
        return productos.stream()
            .filter(p -> String.valueOf(p.getId()).contains(criterioLower) ||
                         p.getNombre().toLowerCase().contains(criterioLower) ||
                         p.getProveedor().toLowerCase().contains(criterioLower))
            .collect(Collectors.toList());
    }




}

