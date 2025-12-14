package main;


import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import vista.MenuPrincipal;
import controlador.MySQLControlador;

public class MainApp {

	static {
	    MySQLControlador.ensureSchema();
	}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuPrincipal().setVisible(true);
        });

    }


}
