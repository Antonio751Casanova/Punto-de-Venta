package modelo;

import java.util.Date;

public class Movimiento {
    private Date fecha;
    private int idProducto;
    private String nombreProducto;
    private String tipo;
    private int cantidad;
    private int stockResultante;
    private String motivo;

    // Constructor
    public Movimiento(Date fecha, int idProducto, String nombreProducto,
                     String tipo, int cantidad, int stockResultante, String motivo) {
        this.fecha = fecha;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockResultante = stockResultante;
        this.motivo = motivo;
    }

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public int getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(int idProducto) {
		this.idProducto = idProducto;
	}

	public String getNombreProducto() {
		return nombreProducto;
	}

	public void setNombreProducto(String nombreProducto) {
		this.nombreProducto = nombreProducto;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public int getStockResultante() {
		return stockResultante;
	}

	public void setStockResultante(int stockResultante) {
		this.stockResultante = stockResultante;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}





}