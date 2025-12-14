package modelo;

public class Producto {
    private int id;
    private String nombre;
    private String marca;
    private double precio;
    private String descripcion;
    private String imagenPath; // Ruta de la imagen
    private int stock;
    private int stockMinimo;  // Nuevo: Nivel mínimo para alertas
    private String proveedor; // Nuevo: Proveedor principal
    private int ventas;
    private String categoria;


    // Constructor, getters y setters
    public Producto(int id, String nombre, String marca, double precio, String descripcion, String imagenPath, int stock,
            int stockMinimo, String proveedor, int ventas, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagenPath = imagenPath;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.proveedor = proveedor;
        this.ventas=ventas;
        this.categoria = categoria;
    }



    public Producto(int numericCellValue, String stringCellValue, String stringCellValue2, double numericCellValue2,
			double numericCellValue3, double numericCellValue4, String stringCellValue3) {
		// TODO Auto-generated constructor stub
	}

	public String getEstado() {
        if (stock <= stockMinimo) {
            return "ALERTA";
        } else if (stock <= stockMinimo + 3) { // 3 unidades por encima del mínimo
            return "PRECAUCIÓN";
        } else {
            return "OK";
        }
    }

    @Override
    public String toString() {
        return String.format("%d - %s (Stock: %d, Mín: %d)",
                            id, nombre, stock, stockMinimo);
    }


	public int getVentas() {
		return ventas;
	}

	public void setVentas(int ventas) {
		this.ventas = ventas;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getStockMinimo() {
		return stockMinimo;
	}

	public void setStockMinimo(int stockMinimo) {
		this.stockMinimo = stockMinimo;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getImagenPath() {
		return imagenPath;
	}

    public void setImagenPath(String imagenPath) {
        this.imagenPath = imagenPath;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }


}
