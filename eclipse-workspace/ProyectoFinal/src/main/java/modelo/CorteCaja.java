package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CorteCaja {
    private LocalDateTime fechaHora;
    private double montoInicial;
    private double ventasEfectivo;
    private double ventasTarjeta;
    private double retiros;
    private double montoFinal;
    private String observaciones;

    public CorteCaja(double montoInicial, double ventasEfectivo, double ventasTarjeta, double retiros, String observaciones) {
        this.fechaHora = LocalDateTime.now();
        this.montoInicial = montoInicial;
        this.ventasEfectivo = ventasEfectivo;
        this.ventasTarjeta = ventasTarjeta;
        this.retiros = retiros;
        this.montoFinal = calcularMontoFinal();
        this.observaciones = observaciones;
    }

    private double calcularMontoFinal() {
        return montoInicial + ventasEfectivo + ventasTarjeta - retiros;
    }

    // Getters
    public String getFechaHoraFormateada() {
        return fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public double getMontoInicial() {
		return montoInicial;
	}

	public void setMontoInicial(double montoInicial) {
		this.montoInicial = montoInicial;
	}

	public double getVentasEfectivo() {
		return ventasEfectivo;
	}

	public void setVentasEfectivo(double ventasEfectivo) {
		this.ventasEfectivo = ventasEfectivo;
	}

	public double getVentasTarjeta() {
		return ventasTarjeta;
	}

	public void setVentasTarjeta(double ventasTarjeta) {
		this.ventasTarjeta = ventasTarjeta;
	}

	public double getRetiros() {
		return retiros;
	}

	public void setRetiros(double retiros) {
		this.retiros = retiros;
	}

	public double getMontoFinal() {
		return montoFinal;
	}

	public void setMontoFinal(double montoFinal) {
		this.montoFinal = montoFinal;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}




}