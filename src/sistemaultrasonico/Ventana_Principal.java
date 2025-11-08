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
    private Indicador indicador; //  indicador visual
    private JButton botonEncender, botonApagar; //  botones de control

    public Ventana_Principal() {
        setTitle("Sistema Ultrasonico - Comunicación Serial JSSC");
        setSize(500, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Etiqueta con el texto de la distancia
        etiquetaDistancia = new JLabel("Distancia: --- cm", SwingConstants.CENTER);
        etiquetaDistancia.setFont(new Font("Arial", Font.BOLD, 24));
        add(etiquetaDistancia, BorderLayout.NORTH);

        // Indicador visual
        indicador = new Indicador();
        add(indicador, BorderLayout.CENTER);

        // Panel con los botones
        JPanel panelBotones = new JPanel();
        botonEncender = new JButton("Encender LED");
        botonApagar = new JButton("Apagar LED");

        botonEncender.addActionListener(e -> enviarComando("1"));
        botonApagar.addActionListener(e -> enviarComando("0"));

        panelBotones.add(botonEncender);
        panelBotones.add(botonApagar);
        add(panelBotones, BorderLayout.SOUTH);

        conectarPuerto();
    }

    private void conectarPuerto() {
        puerto = new SerialPort("COM3"); // ️ Cambiar el Arduino usa otro COM

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
                                    try {
                                        int d = Integer.parseInt(data.trim());
                                        indicador.setDistancia(d);
                                    } catch (NumberFormatException ex) {
                                        System.out.println("Dato inválido: " + data);
                                    }
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

    private void enviarComando(String comando) {
        try {
            if (puerto.isOpened()) {
                puerto.writeString(comando);
                System.out.println("Comando enviado: " + comando);
            }
        } catch (SerialPortException ex) {
            System.out.println("Error al enviar comando: " + ex);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Ventana_Principal().setVisible(true);
        });
    }
}

/**
 * Clase para mostrar un círculo que cambia según la distancia
 */
class Indicador extends JPanel {

    private int distancia = 0;

    public void setDistancia(int d) {
        this.distancia = d;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int ancho = getWidth();
        int alto = getHeight();

        // Escalamos la distancia a un tamaño visible
        int radio = Math.min(ancho, alto) / 4 + distancia / 2;
        if (radio > 150) radio = 150;

        // Cambiamos color según distancia
        if (distancia < 10) g.setColor(Color.RED);
        else if (distancia < 25) g.setColor(Color.ORANGE);
        else g.setColor(Color.GREEN);

        // Dibujar círculo centrado
        g.fillOval(ancho / 2 - radio / 2, alto / 2 - radio / 2, radio, radio);
    }
}