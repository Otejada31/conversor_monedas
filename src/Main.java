import com.google.gson.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    // Crear un mapa de monedas con su símbolo
    private static final Map<Integer, String> MONEDAS = new HashMap<>();

    // Archivo para guardar las conversiones
    private static final String ARCHIVO_CONVERSIONES = "conversiones.txt";

    static {
        MONEDAS.put(1, "USD"); // Dólar estadounidense
        MONEDAS.put(2, "EUR"); // Euro
        MONEDAS.put(3, "JPY"); // Yen japonés
        MONEDAS.put(4, "GBP"); // Libra esterlina
        MONEDAS.put(5, "AUD"); // Dólar australiano
        MONEDAS.put(6, "CAD"); // Dólar canadiense
        MONEDAS.put(7, "COP"); // Peso colombiano
        MONEDAS.put(8, "BRL"); // Real brasileño
    }

    public static void main(String[] args) {

        Configuracion configuracion = new Configuracion();
        String apiKey = configuracion.getProperty("api.exchangerate.key");
        Scanner scanner = new Scanner(System.in);
        ClienteHTTP clienteHTTP = new ClienteHTTP();

        while (true) {
            System.out.println("\n==================================");
            System.out.println("====== Conversor de Monedas ======");
            System.out.println("==================================");
            System.out.println("\n1. Convertir un importe");
            System.out.println("2. Ver valores de cotización");
            System.out.println("3. Ver conversiones anteriores");
            System.out.println("4. Salir");

            int opcion = 0;

            // Intentar capturar una opción válida del menú
            while (true) {
                try {
                    System.out.print("\nEscoja una opción: ");
                    opcion = Integer.parseInt(scanner.nextLine());
                    if (opcion < 1 || opcion > 4) {
                        System.out.println("Opción inválida. Por favor, ingrese un número del 1 al 4.");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Por favor, ingrese un número entero.");
                }
            }

            String baseMoneda = "";
            JsonObject rates = null;

            if (opcion == 1 || opcion == 2) {
                System.out.println("\nMonedas disponibles:");
                mostrarMonedas();

                // Intentar capturar una moneda válida
                while (true) {
                    try {
                        System.out.print("\nSeleccione la moneda base (número entero): ");
                        int monedaSeleccionada = Integer.parseInt(scanner.nextLine());
                        baseMoneda = MONEDAS.get(monedaSeleccionada);
                        if (baseMoneda != null) {
                            break;
                        } else {
                            System.out.println("Moneda no válida. Seleccione un número entre 1 y 8.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida. Por favor, ingrese un número entero.");
                    }
                }

                // Realizar solicitud para obtener los datos de la API
                String datosJSON = clienteHTTP.obtenerDatos(baseMoneda);
                JsonObject jsonObject = JsonParser.parseString(datosJSON).getAsJsonObject();
                rates = jsonObject.getAsJsonObject("conversion_rates");
            }

            switch (opcion) {
                case 1:
                    convertirMoneda(scanner, rates, baseMoneda);
                    break;
                case 2:
                    verValoresDeCotizacion(scanner, rates);
                    break;
                case 3:
                    verConversionesAnteriores();
                    break;
                case 4:
                    System.out.println("\nSaliendo...");
                    return;
            }
        }
    }

    // Método para mostrar las monedas disponibles en dos columnas
    private static void mostrarMonedas() {
        System.out.println("1. USD       5. AUD");
        System.out.println("2. EUR       6. CAD");
        System.out.println("3. JPY       7. COP");
        System.out.println("4. GBP       8. BRL");
    }

    // Método para convertir la moneda
    public static void convertirMoneda(Scanner scanner, JsonObject rates, String baseMoneda) {
        String monedaObjetivo = "";

        // Intentar capturar una moneda válida
        while (true) {
            try {
                System.out.print("Selecciona la moneda a la que deseas convertir (número entero): ");
                int monedaObjetivoSeleccionada = Integer.parseInt(scanner.nextLine());
                monedaObjetivo = MONEDAS.get(monedaObjetivoSeleccionada);
                if (monedaObjetivo != null) {
                    break;
                } else {
                    System.out.println("Moneda no válida. Selecciona un número entre 1 y 8.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingresa un número entero.");
            }
        }

        int importe = 0;

        // Intentar capturar un importe válido
        while (true) {
            try {
                System.out.print("Ingresa el importe a convertir (número entero): ");
                importe = Integer.parseInt(scanner.nextLine());
                if (importe > 0) {
                    break;
                } else {
                    System.out.println("El importe debe ser mayor a 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingresa un número entero.");
            }
        }

        // Verificar si la moneda objetivo existe en las tasas de conversión
        if (rates.has(monedaObjetivo)) {
            double tasaConversion = rates.get(monedaObjetivo).getAsDouble();
            double tasaCompra = tasaConversion * 0.99;
            double tasaVenta = tasaConversion * 1.01;

            double importeConvertidoCompra = importe * tasaCompra;
            double importeConvertidoVenta = importe * tasaVenta;

            Moneda conversion = new Moneda(baseMoneda, monedaObjetivo, importe, importeConvertidoCompra, importeConvertidoVenta);
            guardarConversion(conversion);

            // Mostrar el resultado
            System.out.println("\nResultado de la Conversión:");
            System.out.println("| De -> A      | Importe Base   | Valor de Compra | Valor de Venta  |");
            System.out.println("|--------------|----------------|-----------------|-----------------|");
            System.out.println(conversion.formatoMoneda());
        } else {
            System.out.println("\nLa moneda ingresada no está disponible en las tasas de conversión.");
        }
    }

    // Método para ver los valores de cotización de una moneda específica
    public static void verValoresDeCotizacion(Scanner scanner, JsonObject rates) {
        String moneda = "";

        // Intentar capturar una moneda válida
        while (true) {
            try {
                System.out.print("Selecciona la moneda de la cual deseas ver la cotización (número entero): ");
                int monedaSeleccionada = Integer.parseInt(scanner.nextLine());
                moneda = MONEDAS.get(monedaSeleccionada);
                if (moneda != null) {
                    break;
                } else {
                    System.out.println("Moneda no válida. Selecciona un número entre 1 y 8.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingresa un número entero.");
            }
        }

        // Verificar si la moneda existe en las tasas de conversión
        if (rates.has(moneda)) {
            double tasaConversion = rates.get(moneda).getAsDouble();
            double tasaCompra = tasaConversion * 0.99;
            double tasaVenta = tasaConversion * 1.01;

            // Mostrar cotizaciones de compra y venta
            System.out.println("\nCotizaciones para " + moneda + ":");
            System.out.println("\n| Moneda     | Valor de Compra | Valor de Venta  |");
            System.out.println("|------------|-----------------|-----------------|");
            System.out.printf("| %-10s | %-15.4f | %-15.4f |\n", moneda, tasaCompra, tasaVenta);
        } else {
            System.out.println("\nLa moneda ingresada no está disponible.");
        }
    }

    // Método para guardar la conversión en un archivo
    public static void guardarConversion(Moneda conversion) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CONVERSIONES, true))) {
            writer.write(conversion.formatoMoneda());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("\nError al guardar la conversión: " + e.getMessage());
        }
    }

    // Método para ver las conversiones anteriores
    public static void verConversionesAnteriores() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_CONVERSIONES))) {
            String linea;
            System.out.println("\n=== Conversiones Anteriores ===");
            while ((linea = reader.readLine()) != null) {
                System.out.println(linea);
            }
        } catch (IOException e) {
            System.out.println("\nError al leer el archivo de conversiones: " + e.getMessage());
        }
    }
}

