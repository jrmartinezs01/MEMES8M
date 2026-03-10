import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Clase principal del juego "Bulo o Realidad".
 * El jugador tiene que identificar qué realidad desmiente cada bulo sobre igualdad de género.
 *
 * @version 1.0
 */
public class ProyectoMeme {

    /** Lista con los textos de los bulos leídos de memes.txt */
    static ArrayList<String> listaDeBulos = new ArrayList<>();

    /** Lista con los textos de las realidades leídas de realidades.json */
    static ArrayList<String> listaDeRealidades = new ArrayList<>();

    /** Scanner para leer lo que escribe el usuario por teclado */
    static Scanner lecturaDelTeclado = new Scanner(System.in);

    /** Conjunto de índices de bulos ya mostrados para evitar repeticiones (HU5 opcional) */
    static final Set<Integer> bulosUsados = new HashSet<>();

    /**
     * Método principal. Lanza las historias de usuario HU1 a HU5.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     * @throws Exception si hay algún error leyendo o escribiendo ficheros
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Bulo o Realidad ===");

        // HU1 - Comprobar archivos iniciales
        if (!comprobarArchivosIniciales()) {
            System.out.println("❌ El programa no puede continuar. Corrige los errores.");
            return;
        }

        // HU2 - Crear directorio y fichero de resultados si no existen
        hu2();

        // HU3 - Leer memes
        hu3();

        // HU4 - Leer realidades
        listaDeRealidades = leerRealidades("datos/realidades.json");
        System.out.println("✅ Realidades cargadas: " + listaDeRealidades.size());

        // HU5 - Mostrar un bulo al azar y la lista de realidades
        mostrarBuloYRealidades(listaDeBulos, listaDeRealidades);

        lecturaDelTeclado.close();
    }

    // =========================================================================
    // HU1
    // =========================================================================

    /**
     * HU1 - Comprueba que existe el directorio datos y que contiene
     * los tres ficheros necesarios: memes.txt, realidades.json y soluciones.xml.
     * Si falta algo informa al usuario y devuelve false.
     *
     * @return true si todo existe, false si falta algo
     */
    public static boolean comprobarArchivosIniciales() {
        Path rutaDatos = Paths.get("datos");

        if (!Files.exists(rutaDatos)) {
            System.out.println("ERROR: No existe la carpeta 'datos'");
            return false;
        }

        if (!Files.isDirectory(rutaDatos)) {
            System.out.println("ERROR: 'datos' no es una carpeta");
            return false;
        }

        String[] archivosNecesarios = {"memes.txt", "realidades.json", "soluciones.xml"};
        boolean todoCorrecto = true;

        for (String archivo : archivosNecesarios) {
            Path rutaArchivo = rutaDatos.resolve(archivo);
            if (!Files.exists(rutaArchivo)) {
                System.out.println("ERROR: No existe el archivo: " + archivo);
                todoCorrecto = false;
            } else if (!Files.isRegularFile(rutaArchivo)) {
                System.out.println("ERROR: No es un archivo válido: " + archivo);
                todoCorrecto = false;
            } else {
                System.out.println("✓ Encontrado: " + archivo);
            }
        }

        return todoCorrecto;
    }

    // =========================================================================
    // HU2
    // =========================================================================

    /**
     * HU2 - Comprueba si existe el directorio resultados y el fichero mejores.txt.
     * Si no existen los crea.
     *
     * @throws IOException si hay un error al crear el directorio o el fichero
     */
    public static void hu2() throws IOException {
        Path rutaDirectorioResultados = Paths.get("resultados");
        Path rutaFicheroMejores = Paths.get("resultados/mejores.txt");

        if (!Files.exists(rutaDirectorioResultados))
            Files.createDirectories(rutaDirectorioResultados);

        if (!Files.exists(rutaFicheroMejores))
            Files.createFile(rutaFicheroMejores);
    }

    // =========================================================================
    // HU3
    // =========================================================================

    /**
     * HU3 - Lee el fichero datos/memes.txt línea a línea y rellena listaDeBulos.
     * Cada línea no vacía es un bulo.
     *
     * @throws IOException si hay error al leer el fichero
     */
    public static void hu3() throws IOException {
        Path rutaMemes = Paths.get("datos/memes.txt");
        listaDeBulos = new ArrayList<>(Files.readAllLines(rutaMemes));
        listaDeBulos.removeIf(String::isBlank);
        System.out.println("✅ Bulos cargados: " + listaDeBulos.size());
    }

    // =========================================================================
    // HU4
    // =========================================================================

    /**
     * HU4 - Lee el fichero realidades.json y extrae las realidades.
     * Formato esperado: array JSON con objetos {"texto":"...", "fuente":"..."}.
     * Se extrae únicamente el campo "texto" de cada objeto.
     *
     * @param ruta ruta del archivo realidades.json
     * @return ArrayList con los textos de las realidades
     * @throws IOException si hay error al leer el archivo
     */
    public static ArrayList<String> leerRealidades(String ruta) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get(ruta));
        ArrayList<String> realidades = new ArrayList<>();

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.contains("\"texto\"")) {
                int inicio         = linea.indexOf("\"texto\"") + "\"texto\"".length();
                int primerComilla  = linea.indexOf('"', inicio + 1) + 1;
                int segundaComilla = linea.indexOf('"', primerComilla);
                if (primerComilla > 0 && segundaComilla > primerComilla) {
                    String texto = linea.substring(primerComilla, segundaComilla).trim();
                    if (!texto.isBlank()) realidades.add(texto);
                }
            }
        }

        return realidades;
    }

    // =========================================================================
    // HU5
    // =========================================================================

    /**
     * HU5 - Selecciona un índice de bulo al azar que no haya sido usado todavía.
     * Evita repetir bulos ya mostrados en la misma partida.
     *
     * @param totalBulos número total de bulos disponibles
     * @return índice del bulo seleccionado, o -1 si ya se usaron todos
     */
    public static int seleccionarBuloAleatorio(int totalBulos) {
        if (bulosUsados.size() >= totalBulos) return -1;

        Random random = new Random();
        int indice;
        do {
            indice = random.nextInt(totalBulos);
        } while (bulosUsados.contains(indice));

        bulosUsados.add(indice);
        return indice;
    }

    /**
     * HU5 - Muestra el bulo seleccionado al azar y la lista numerada de realidades.
     * Devuelve el índice del bulo mostrado para que HU6 pueda comprobar la respuesta.
     *
     * @param bulos      lista de todos los bulos
     * @param realidades lista de todas las realidades
     * @return índice del bulo mostrado, o -1 si no quedan bulos disponibles
     */
    public static int mostrarBuloYRealidades(List<String> bulos, List<String> realidades) {
        int indiceBulo = seleccionarBuloAleatorio(bulos.size());

        if (indiceBulo == -1) {
            System.out.println("No quedan bulos por mostrar.");
            return -1;
        }

        System.out.println("\n=================================================");
        System.out.println("                   BULO / MEME                   ");
        System.out.println("=================================================");
        System.out.println(bulos.get(indiceBulo));
        System.out.println("\n¿Qué dato real desmiente este bulo?");
        System.out.println("-------------------------------------------------");
        for (int i = 0; i < realidades.size(); i++) {
            System.out.println((i + 1) + ". " + realidades.get(i));
        }
        System.out.println("=================================================");

        return indiceBulo;
    }
}