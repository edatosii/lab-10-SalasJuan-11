package co.umariana.modelo;

import java.util.ArrayList;
import java.util.List;

public class ArbolGenealogico {

    private static class Nodo {
        final Persona persona;
        Nodo padre;
        Nodo madre;
        final List<Nodo> hijos = new ArrayList<>();

        Nodo(Persona persona) {
            this.persona = persona;
        }
    }

    private final Nodo raiz;
    private int cantidad;

    public ArbolGenealogico(Persona sujeto) {
        if (sujeto == null) {
            throw new IllegalArgumentException("El sujeto no puede ser null.");
        }
        sujeto.validar();
        if (!"1".equals(sujeto.getPosicion())) {
            throw new ArbolInvalidoException("el sujeto debe tener la posición 1.");
        }
        this.raiz = new Nodo(sujeto);
        this.cantidad = 1;
    }

    public Persona sujeto() {
        return raiz.persona;
    }

    public int tamano() {
        return cantidad;
    }

    public Persona buscar(String posicion) {
        Nodo nodo = buscarNodo(posicion);
        return nodo == null ? null : nodo.persona;
    }

    public boolean contiene(String posicion) {
        return buscarNodo(posicion) != null;
    }

    public Persona agregar(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("La persona no puede ser null.");
        }
        persona.validar();

        String pos = persona.getPosicion();
        long[] n = partes(pos);
        if (n == null) {
            throw new ArbolInvalidoException("la posición '" + pos + "' no es válida.");
        }
        if (n.length == 1 && n[0] == 1) {
            throw new ArbolInvalidoException("la posición 1 (el sujeto) ya existe.");
        }
        if (buscarNodo(pos) != null) {
            throw new ArbolInvalidoException("la posición " + pos + " está repetida.");
        }

        Nodo nuevo = new Nodo(persona);

        if (n.length == 1) {
            long posHijo = n[0] / 2;
            Nodo hijo = buscarNodo(String.valueOf(posHijo));
            if (hijo == null) {
                throw new ArbolInvalidoException("falta la posición " + posHijo
                        + ", que es la persona de la que " + pos + " es progenitor.");
            }
            if (persona.getNacimiento() >= hijo.persona.getNacimiento()) {
                throw new ArbolInvalidoException("la posición " + pos
                        + " debe haber nacido antes que su hijo (posición " + posHijo + ").");
            }
            if (n[0] % 2 == 0) {
                hijo.padre = nuevo;
            } else {
                hijo.madre = nuevo;
            }
        } else {
            String posProg = progenitor(n);
            Nodo prog = buscarNodo(posProg);
            if (prog == null) {
                throw new ArbolInvalidoException("falta la posición " + posProg
                        + ", que es el progenitor de " + pos + ".");
            }
            if (persona.getNacimiento() <= prog.persona.getNacimiento()) {
                throw new ArbolInvalidoException("la posición " + pos
                        + " debe haber nacido después de su progenitor (posición " + posProg + ").");
            }
            long numero = n[n.length - 1];
            int indice = 0;
            while (indice < prog.hijos.size() && numeroHijo(prog.hijos.get(indice)) < numero) {
                indice++;
            }
            prog.hijos.add(indice, nuevo);
        }

        cantidad++;
        return persona;
    }

    public Persona agregarPadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        return agregarProgenitor(posicion, false, nombre, nacimiento, fallecimiento);
    }

    public Persona agregarMadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        return agregarProgenitor(posicion, true, nombre, nacimiento, fallecimiento);
    }

    private Persona agregarProgenitor(String posicion, boolean madre, String nombre,
                                      int nacimiento, Integer fallecimiento) {
        if (buscarNodo(posicion) == null) {
            throw new ArbolInvalidoException("la posición " + posicion + " no existe en el árbol.");
        }
        long[] n = partes(posicion);
        if (n.length != 1) {
            throw new ArbolInvalidoException("la posición " + posicion
                    + " no es el sujeto ni un ascendiente: solo ellos pueden tener padres.");
        }
        String nueva = String.valueOf(n[0] * 2 + (madre ? 1 : 0));
        if (buscarNodo(nueva) != null) {
            throw new ArbolInvalidoException("la posición " + nueva + " ya existe.");
        }
        return agregar(new Persona(nueva, nombre, nacimiento, fallecimiento));
    }

    public Persona agregarHijo(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        Nodo nodo = buscarNodo(posicion);
        if (nodo == null) {
            throw new ArbolInvalidoException("la posición " + posicion + " no existe en el árbol.");
        }
        long[] n = partes(posicion);
        if (n.length == 1 && n[0] != 1) {
            throw new ArbolInvalidoException("la posición " + posicion
                    + " no es el sujeto ni un descendiente: solo ellos pueden tener hijos.");
        }
        long mayor = 0;
        for (Nodo hijo : nodo.hijos) {
            mayor = Math.max(mayor, numeroHijo(hijo));
        }
        String nueva = posicion + "." + (mayor + 1);
        return agregar(new Persona(nueva, nombre, nacimiento, fallecimiento));
    }

    public int eliminar(String posicion) {
        long[] n = partes(posicion);
        if (n != null && n.length == 1 && n[0] == 1) {
            throw new ArbolInvalidoException("el sujeto (posición 1) no se puede eliminar.");
        }
        Nodo nodo = buscarNodo(posicion);
        if (nodo == null) {
            throw new ArbolInvalidoException("la posición " + posicion + " no existe en el árbol.");
        }

        int eliminados;
        if (n.length == 1) {
            Nodo hijo = buscarNodo(String.valueOf(n[0] / 2));
            if (n[0] % 2 == 0) {
                hijo.padre = null;
            } else {
                hijo.madre = null;
            }
            eliminados = contarAscendientes(nodo);
        } else {
            Nodo prog = buscarNodo(progenitor(n));
            prog.hijos.remove(nodo);
            eliminados = contarDescendientes(nodo);
        }
        cantidad -= eliminados;
        return eliminados;
    }

    private int contarAscendientes(Nodo nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + contarAscendientes(nodo.padre) + contarAscendientes(nodo.madre);
    }

    private int contarDescendientes(Nodo nodo) {
        int total = 1;
        for (Nodo hijo : nodo.hijos) {
            total += contarDescendientes(hijo);
        }
        return total;
    }

    public List<Persona> miembros() {
        List<Persona> lista = new ArrayList<>();
        lista.add(raiz.persona);
        lista.addAll(ascendientes());
        lista.addAll(descendientes());
        return lista;
    }

    public List<Persona> ascendientes() {
        List<Nodo> cola = new ArrayList<>();
        cola.add(raiz);
        List<Persona> lista = new ArrayList<>();
        for (int i = 0; i < cola.size(); i++) {
            Nodo actual = cola.get(i);
            if (actual != raiz) {
                lista.add(actual.persona);
            }
            if (actual.padre != null) {
                cola.add(actual.padre);
            }
            if (actual.madre != null) {
                cola.add(actual.madre);
            }
        }
        return lista;
    }

    public List<Persona> descendientes() {
        List<Persona> lista = new ArrayList<>();
        for (Nodo hijo : raiz.hijos) {
            preorden(hijo, lista);
        }
        return lista;
    }

    private void preorden(Nodo nodo, List<Persona> lista) {
        lista.add(nodo.persona);
        for (Nodo hijo : nodo.hijos) {
            preorden(hijo, lista);
        }
    }

    private Nodo buscarNodo(String posicion) {
        long[] n = partes(posicion);
        if (n == null) {
            return null;
        }
        Nodo actual = raiz;
        if (n.length == 1) {
            String bits = Long.toBinaryString(n[0]);
            for (int i = 1; i < bits.length() && actual != null; i++) {
                actual = bits.charAt(i) == '0' ? actual.padre : actual.madre;
            }
        } else {
            for (int i = 1; i < n.length && actual != null; i++) {
                actual = hijoNumero(actual, n[i]);
            }
        }
        return actual;
    }

    private Nodo hijoNumero(Nodo nodo, long numero) {
        for (Nodo hijo : nodo.hijos) {
            if (numeroHijo(hijo) == numero) {
                return hijo;
            }
        }
        return null;
    }

    private long numeroHijo(Nodo hijo) {
        String p = hijo.persona.getPosicion();
        return Long.parseLong(p.substring(p.lastIndexOf('.') + 1));
    }

    private static String progenitor(long[] n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n.length - 1; i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(n[i]);
        }
        return sb.toString();
    }

    private static long[] partes(String posicion) {
        if (posicion == null) {
            return null;
        }
        String[] texto = posicion.split("\\.", -1);
        long[] numeros = new long[texto.length];
        for (int i = 0; i < texto.length; i++) {
            if (!esNumero(texto[i])) {
                return null;
            }
            numeros[i] = Long.parseLong(texto[i]);
        }
        if (numeros.length > 1 && numeros[0] != 1) {
            return null;
        }
        return numeros;
    }

    private static boolean esNumero(String s) {
        if (s.isEmpty() || s.length() > 18 || s.charAt(0) < '1' || s.charAt(0) > '9') {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}