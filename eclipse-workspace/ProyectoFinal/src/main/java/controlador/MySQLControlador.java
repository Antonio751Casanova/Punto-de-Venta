package controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import modelo.Movimiento;
import modelo.Producto;

/**
 * Controlador centralizado para operaciones JDBC hacia MySQL.
 * Actúa como backend único para reemplazar la persistencia previa en Excel.
 */
public class MySQLControlador {

    private static final String DEFAULT_URL =
        System.getenv().getOrDefault(
            "MYSQL_URL",
            "jdbc:mysql://localhost:3306/puntoventa?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false");
    private static final String DEFAULT_USER = System.getenv().getOrDefault("MYSQL_USER", "root");
    private static final String DEFAULT_PASSWORD = System.getenv().getOrDefault("MYSQL_PASSWORD", "");

    static {
        ensureSchema();
    }

    private MySQLControlador() {
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    public static void ensureSchema() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS productos ("
                    + "id INT PRIMARY KEY,"
                    + "nombre VARCHAR(200) NOT NULL,"
                    + "marca VARCHAR(200),"
                    + "precio DOUBLE DEFAULT 0,"
                    + "descripcion TEXT,"
                    + "imagen VARCHAR(255),"
                    + "categoria VARCHAR(100),"
                    + "stock INT DEFAULT 0,"
                    + "stock_minimo INT DEFAULT 0,"
                    + "proveedor VARCHAR(200),"
                    + "ventas INT DEFAULT 0,"
                    + "actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                    + ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS movimientos ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "id_producto INT,"
                    + "nombre_producto VARCHAR(200),"
                    + "tipo VARCHAR(20),"
                    + "cantidad INT,"
                    + "stock_resultante INT,"
                    + "motivo VARCHAR(255),"
                    + "FOREIGN KEY (id_producto) REFERENCES productos(id) ON DELETE SET NULL"
                    + ")");
        } catch (SQLException e) {
            System.err.println("No se pudo validar el esquema MySQL: " + e.getMessage());
        }
    }

    public static List<Producto> obtenerProductos() throws SQLException {
        ensureSchema();
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, marca, precio, descripcion, imagen, categoria, "
                + "stock, stock_minimo, proveedor, ventas "
                + "FROM productos ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(new Producto(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("marca"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getString("imagen"),
                    rs.getInt("stock"),
                    rs.getInt("stock_minimo"),
                    rs.getString("proveedor"),
                    rs.getInt("ventas"),
                    rs.getString("categoria")
                ));
            }
        }
        return productos;
    }

    public static List<Movimiento> obtenerMovimientos() throws SQLException {
        ensureSchema();
        List<Movimiento> movimientos = new ArrayList<>();
        String sql = "SELECT fecha, id_producto, nombre_producto, tipo, "
                + "cantidad, stock_resultante, motivo "
                + "FROM movimientos ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                movimientos.add(new Movimiento(
                    rs.getTimestamp("fecha"),
                    rs.getInt("id_producto"),
                    rs.getString("nombre_producto"),
                    rs.getString("tipo"),
                    rs.getInt("cantidad"),
                    rs.getInt("stock_resultante"),
                    rs.getString("motivo")
                ));
            }
        }
        return movimientos;
    }

    public static void upsertProducto(Producto producto) throws SQLException {
        ensureSchema();
        String sql = "INSERT INTO productos (id, nombre, marca, precio, descripcion, imagen, "
                + "categoria, stock, stock_minimo, proveedor, ventas, actualizado_en) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "nombre = VALUES(nombre), "
                + "marca = VALUES(marca), "
                + "precio = VALUES(precio), "
                + "descripcion = VALUES(descripcion), "
                + "imagen = VALUES(imagen), "
                + "categoria = VALUES(categoria), "
                + "stock = VALUES(stock), "
                + "stock_minimo = VALUES(stock_minimo), "
                + "proveedor = VALUES(proveedor), "
                + "ventas = VALUES(ventas)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getMarca());
            ps.setDouble(4, producto.getPrecio());
            ps.setString(5, producto.getDescripcion());
            ps.setString(6, producto.getImagenPath());
            ps.setString(7, producto.getCategoria());
            ps.setInt(8, producto.getStock());
            ps.setInt(9, producto.getStockMinimo());
            ps.setString(10, producto.getProveedor());
            ps.setInt(11, producto.getVentas());
            ps.setTimestamp(12, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }

    public static boolean actualizarCampoProducto(int idProducto, String columna, Object valor) throws SQLException {
        ensureSchema();
        String sql = "UPDATE productos SET " + columna + " = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (valor instanceof Number) {
                ps.setObject(1, valor);
            } else {
                ps.setString(1, valor != null ? valor.toString() : null);
            }
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    public static void actualizarVentas(int idProducto, int ventas) throws SQLException {
        ensureSchema();
        String sql = "UPDATE productos SET ventas = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ventas);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    public static boolean actualizarStockMinimo(int idProducto, int nuevoMinimo) throws SQLException {
        return actualizarCampoProducto(idProducto, "stock_minimo", nuevoMinimo);
    }

    public static boolean registrarMovimientoInventario(int idProducto, String nombre, int cantidad, String motivo) throws SQLException {
        ensureSchema();
        String selectSql = "SELECT stock FROM productos WHERE id = ?";
        String updateSql = "UPDATE productos SET stock = ? WHERE id = ?";
        String movimientoSql = "INSERT INTO movimientos "
                + "(fecha, id_producto, nombre_producto, tipo, cantidad, stock_resultante, motivo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            int stockActual;
            try (PreparedStatement sel = conn.prepareStatement(selectSql)) {
                sel.setInt(1, idProducto);
                try (ResultSet rs = sel.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    stockActual = rs.getInt(1);
                }
            }

            int nuevoStock = stockActual + cantidad;
            if (nuevoStock < 0) {
                conn.rollback();
                throw new IllegalStateException("El stock no puede ser negativo");
            }

            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setInt(1, nuevoStock);
                upd.setInt(2, idProducto);
                upd.executeUpdate();
            }

            try (PreparedStatement mov = conn.prepareStatement(movimientoSql)) {
                mov.setTimestamp(1, Timestamp.from(Instant.now()));
                mov.setInt(2, idProducto);
                mov.setString(3, nombre);
                mov.setString(4, cantidad >= 0 ? "ENTRADA" : "SALIDA");
                mov.setInt(5, Math.abs(cantidad));
                mov.setInt(6, nuevoStock);
                mov.setString(7, motivo);
                mov.executeUpdate();
            }

            conn.commit();
            return true;
        }
    }
}
