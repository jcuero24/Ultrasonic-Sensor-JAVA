package sistemaultrasonico;

//import com.panamahitek.Arduino.PanamaHitek_Arduino;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;

public class Ventana_Principal extends JFrame {

  //  PanamaHitek_Arduino arduino = new PanamaHitek_Arduino();
    int distancia = 0;
    boolean ledEncendido = false;

    public Ventana_Principal() {
        setTitle("Sistema Combinado - Sensor Ultrasonico y LED");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            arduino.arduinoRXTX("COM1", 9600, listener); // Cambia COM3 por tu puerto real
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Repaint cada medio segundo
        new Timer(500, e -> repaint()).start();
    }

    SerialPortEventListener listener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (arduino.isMessageAvailable()) {
                    String mensaje = arduino.printMessage();
                    distancia = Integer.parseInt(mensaje.trim());
                    System.out.println("Distancia: " + distancia + " cm");

                    // Aquí puedes poner condiciones de distancia
                    // y encender un LED según el valor recibido
                }
            } catch (SerialPortException | NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana_Principal().setVisible(true));
    }
}
