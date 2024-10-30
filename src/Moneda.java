public class Moneda{
    private String baseMoneda;
    private String monedaObjetivo;
    private double importeBase;
    private double importeConvertidoCompra;
    private double importeConvertidoVenta;

    public Moneda(String baseMoneda, String monedaObjetivo, double importeBase, double importeConvertidoCompra, double importeConvertidoVenta) {
        this.baseMoneda = baseMoneda;
        this.monedaObjetivo = monedaObjetivo;
        this.importeBase = importeBase;
        this.importeConvertidoCompra = importeConvertidoCompra;
        this.importeConvertidoVenta = importeConvertidoVenta;
    }

    // Método para dar formato a la conversión de monedas.
    public String formatoMoneda() {
        return String.format("| %-3s -> %-3s   | %-14.2f | %-15.2f | %-15.2f |",
                baseMoneda, monedaObjetivo, importeBase, importeConvertidoCompra, importeConvertidoVenta);
    }
}
