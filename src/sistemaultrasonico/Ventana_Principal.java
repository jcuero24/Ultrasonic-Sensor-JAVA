package sistemaultrasonico;

import java.awt.*;
import javax.swing.*;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class Ventana_Principal extends JFrame {

    private SerialPort puerto;
    private JLabel etiquetaDistancia;

    public Ventana_Principal() {
        setTitle("Sistema Ultrasonico - Comunicación Serial JSSC");
        setSize(400, 200);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        etiquetaDistancia = new JLabel("Distancia: --- cm", SwingConstants.CENTER);
        etiquetaDistancia.setFont(new Font("Arial", Font.BOLD, 24));
        add(etiquetaDistancia, BorderLayout.CENTER);

        conectarPuerto();
    }

    private void conectarPuerto() {
        puerto = new SerialPort("COM3"); // ⚠️ Cambia "COM3" por tu puerto real

        try {
            puerto.openPort();
            puerto.setParams(
                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );

            // Listener que detecta nuevos datos
            puerto.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent evento) {
                    if (evento.isRXCHAR()) { // Si hay datos disponibles
                        try {
                            String data = puerto.readString(evento.getEventValue());
                            if (data != null && !data.trim().isEmpty()) {
                                SwingUtilities.invokeLater(() -> {
                                    etiquetaDistancia.setText("Distancia: " + data.trim() + " cm");
                                });
                            }
                        } catch (SerialPortException ex) {
                            System.out.println("Error al leer datos: " + ex);
                        }
                    }
                }
            });

            System.out.println("Conectado correctamente al puerto " + puerto.getPortName());

        } catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el puerto: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Ventana_Principal().setVisible(true);
        });
    }
}
