package co.umariana.modelo;

public final class Posicion {

    public static final String SUJETO = "1";
    public static final int MAX_AHNENTAFEL = 2047;
    public static final int MAX_ORDEN = 9999;
    public static final int MAX_LONGITUD = 50;

    private Posicion() {
    }

    public static boolean esSujeto(String posicion) {
        return SUJETO.equals(posicion);
    }

    public static boolean esAhnentafel(String posicion) {
        if (posicion == null || !posicion.matches("[1-9][0-9]{0,3}")) {
            return false;
        }
        return Integer.parseInt(posicion) <= MAX_AHNENTAFEL;
    }

    public static boolean esDAboville(String posicion) {
        if (posicion == null || posicion.length() > MAX_LONGITUD) {
            return false;
        }
        return posicion.matches("1(\\.[1-9][0-9]{0,3})+");
    }

    public static boolean esValida(String posicion) {
        return esAhnentafel(posicion) || esDAboville(posicion);
    }

    public static boolean esAscendiente(String posicion) {
        return esAhnentafel(posicion) && !esSujeto(posicion);
    }

    public static boolean esDescendiente(String posicion) {
        return esDAboville(posicion);
    }

    public static String posicionDelPadre(String ahnentafel) {
        if (!esAhnentafel(ahnentafel)) {
            throw new IllegalArgumentException("La posición '" + ahnentafel + "' no es Ahnentafel válida.");
        }
        return String.valueOf(Integer.parseInt(ahnentafel) * 2);
    }

    public static String posicionDeLaMadre(String ahnentafel) {
        if (!esAhnentafel(ahnentafel)) {
            throw new IllegalArgumentException("La posición '" + ahnentafel + "' no es Ahnentafel válida.");
        }
        return String.valueOf(Integer.parseInt(ahnentafel) * 2 + 1);
    }

    public static String posicionDelHijoEnLinea(String ahnentafel) {
        if (!esAscendiente(ahnentafel)) {
            throw new IllegalArgumentException("La posición '" + ahnentafel + "' no es un ascendiente.");
        }
        return String.valueOf(Integer.parseInt(ahnentafel) / 2);
    }

    public static String posicionDelProgenitor(String daboville) {
        if (!esDAboville(daboville)) {
            throw new IllegalArgumentException("La posición '" + daboville + "' no es un descendiente.");
        }
        return daboville.substring(0, daboville.lastIndexOf('.'));
    }

    public static int orden(String daboville) {
        if (!esDAboville(daboville)) {
            throw new IllegalArgumentException("La posición '" + daboville + "' no es un descendiente.");
        }
        return Integer.parseInt(daboville.substring(daboville.lastIndexOf('.') + 1));
    }

    public static int generacion(String posicion) {
        if (esAhnentafel(posicion)) {
            return 31 - Integer.numberOfLeadingZeros(Integer.parseInt(posicion));
        }
        if (esDAboville(posicion)) {
            int puntos = 0;
            for (int i = 0; i < posicion.length(); i++) {
                if (posicion.charAt(i) == '.') {
                    puntos++;
                }
            }
            return puntos;
        }
        throw new IllegalArgumentException("La posición '" + posicion + "' no es válida.");
    }
}