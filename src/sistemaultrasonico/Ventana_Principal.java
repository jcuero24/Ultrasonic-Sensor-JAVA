package sistemaultrasonico;

import javax.swing.*;
import java.awt.*;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Ventana_Principal extends JFrame {

    private SerialPort puerto;
    private JLabel labelDistancia;
    private JButton botonConectar;
    private JButton botonEncenderLED;
    private JButton botonApagarLED;
    private JTextArea areaMensajes;
    private boolean conectado = false;

    public Ventana_Principal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema Ultrasonico con jSSC");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de controles
        JPanel panelSuperior = new JPanel(new GridLayout(2, 2, 10, 10));
        botonConectar = new JButton("Conectar");
        botonEncenderLED = new JButton("Encender LED");
        botonApagarLED = new JButton("Apagar LED");
        labelDistancia = new JLabel("Distancia: --- cm", SwingConstants.CENTER);
        labelDistancia.setFont(new Font("Arial", Font.BOLD, 18));

        panelSuperior.add(botonConectar);
        panelSuperior.add(labelDistancia);
        panelSuperior.add(botonEncenderLED);
        panelSuperior.add(botonApagarLED);

        add(panelSuperior, BorderLayout.NORTH);

        // Área de mensajes
        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);
        add(new JScrollPane(areaMensajes), BorderLayout.CENTER);

        // Acciones de botones
        botonConectar.addActionListener(e -> conectarPuerto());
        botonEncenderLED.addActionListener(e -> enviarComando('1'));
        botonApagarLED.addActionListener(e -> enviarComando('0'));
    }

    private void conectarPuerto() {
        if (conectado) {
            cerrarPuerto();
            return;
        }

        String puertoDetectado = detectarPuertoArduino();

        if (puertoDetectado == null) {
            JOptionPane.showMessageDialog(this,
                    "️ No se detectó ningún Arduino.\nConéctalo y vuelve a intentarlo.",
                    "Puerto no encontrado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            puerto = new SerialPort(puertoDetectado);
            puerto.openPort();
            puerto.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            puerto.addEventListener(new SerialPortReader());

            areaMensajes.append(" Conectado al puerto " + puertoDetectado + "\n");
            botonConectar.setText("Desconectar");
            conectado = true;

        } catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al conectar el puerto:\n" + ex.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String detectarPuertoArduino() {
        String[] puertosDisponibles = SerialPortList.getPortNames();

        if (puertosDisponibles.length == 0) {
            areaMensajes.append(" No hay puertos seriales disponibles.\n");
            return null;
        }

        areaMensajes.append(" Puertos detectados:\n");
        for (String puerto : puertosDisponibles) {
            areaMensajes.append("   - " + puerto + "\n");
        }

        // Estrategia: buscar un puerto típico de Arduino
        for (String puerto : puertosDisponibles) {
            if (puerto.toLowerCase().contains("usb") || puerto.toLowerCase().contains("com")) {
                areaMensajes.append(" Posible Arduino encontrado en: " + puerto + "\n");
                return puerto;
            }
        }

        return puertosDisponibles[0]; // Si no detecta nada especial, usa el primero
    }

    private void cerrarPuerto() {
        try {
            if (puerto != null && puerto.isOpened()) {
                puerto.closePort();
                areaMensajes.append(" Puerto desconectado.\n");
            }
        } catch (SerialPortException ex) {
            areaMensajes.append("Error al cerrar puerto: " + ex.getMessage() + "\n");
        } finally {
            botonConectar.setText("Conectar");
            conectado = false;
        }
    }

    private void enviarComando(char comando) {
        if (!conectado) {
            JOptionPane.showMessageDialog(this, "Primero conecta el puerto.");
            return;
        }

        try {
            puerto.writeByte((byte) comando);
            areaMensajes.append("️ Enviado comando: " + comando + "\n");
        } catch (SerialPortException ex) {
            areaMensajes.append("Error al enviar comando: " + ex.getMessage() + "\n");
        }
    }

    private class SerialPortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String data = puerto.readString(event.getEventValue());
                    if (data != null) {
                        SwingUtilities.invokeLater(() -> procesarDato(data.trim()));
                    }
                } catch (SerialPortException ex) {
                    areaMensajes.append("Error al leer datos: " + ex.getMessage() + "\n");
                }
            }
        }
    }

    private void procesarDato(String data) {
        areaMensajes.append(" Recibido: " + data + "\n");

        if (data.startsWith("DIST:")) {
            String valor = data.substring(5);
            if (!valor.equals("ERROR")) {
                labelDistancia.setText("Distancia: " + valor + " cm");
            } else {
                labelDistancia.setText("Distancia: Error");
            }
        } else if (data.startsWith("ACK:LED:ON")) {
            areaMensajes.append(" LED encendido\n");
        } else if (data.startsWith("ACK:LED:OFF")) {
            areaMensajes.append(" LED apagado\n");
        } else if (data.startsWith("ERR:COMANDO")) {
            areaMensajes.append("️ Comando inválido recibido\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana_Principal().setVisible(true));
    }
}
