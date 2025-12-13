package main;


import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import vista.MenuPrincipal;

public class MainApp {

	static {
	    // Verificar integridad al iniciar
	    File movimientosFile = new File("data/movimientos.xlsx");
	    if (movimientosFile.exists()) {
	        try (Workbook wb = WorkbookFactory.create(movimientosFile)) {
	            // Solo verificar
	        } catch (Exception e) {
	            System.err.println("Archivo corrupto, eliminando...");
	            movimientosFile.delete();
	        }
	    }
	}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuPrincipal().setVisible(true);
        });

    }


}