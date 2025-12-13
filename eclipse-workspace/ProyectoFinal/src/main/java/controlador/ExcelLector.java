package controlador;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ExcelLector implements Runnable {
    private final DefaultTableModel modeloTabla;
    private final Path directoryPath;
    private volatile boolean running = true;
    private WatchService watchService;
    private static ExcelLector instance;


    public ExcelLector(DefaultTableModel modeloTabla, String filePath) {
        this.modeloTabla = modeloTabla;
        this.directoryPath = Paths.get(filePath).getParent();
    }

    @Override
    public void run() {
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            this.watchService = ws;
            directoryPath.register(watchService, ENTRY_MODIFY);

            System.out.println("[ExcelFileWatcher] Observador iniciado para: " + directoryPath);

            while (running) {
                WatchKey key;
                try {
                    key = watchService.take(); // Espera bloqueante
                } catch (InterruptedException e) {
                    System.out.println("[ExcelFileWatcher] Observador interrumpido");
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equals("productos.xlsx")) {
                        handleFileChange();
                    }
                }

                if (!key.reset()) {
                    System.err.println("[ExcelFileWatcher] WatchKey ya no válido");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[ExcelFileWatcher] Error inicializando WatchService: " + e.getMessage());
        } finally {
            System.out.println("[ExcelFileWatcher] Observador detenido");
            cleanup();
        }
    }

    public static void listenerObserverIdor() {
        if (instance != null) {
            instance.stop();
        }
    }

 // En ExcelFileWatcher.java
    public static void detenerObservador() {
        if (instance != null) {
			instance.stop();
		}
    }

    public static void reiniciarObservador() {
        if (instance != null) {
			new Thread(instance).start();
		}
    }

    private void handleFileChange() {
        try {
            Thread.sleep(300); // Pausa para evitar notificaciones duplicadas
            SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("[ExcelFileWatcher] Detectado cambio en productos.xlsx");
                    ProductoControlador.cargarDatosExcel(modeloTabla);
                } catch (Exception e) {
                    System.err.println("[ExcelFileWatcher] Error al actualizar tabla: " + e.getMessage());
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        this.running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                System.err.println("[ExcelFileWatcher] Error cerrando WatchService: " + e.getMessage());
            }
        }
    }

    private void cleanup() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // Ignorar error en limpieza final
            }
        }
    }
}