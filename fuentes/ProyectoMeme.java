import java.nio.file.*;
import java.util.*;
import java.io.IOException;

/**
 * Clase principal del juego "Bulo o Realidad".
 * El jugador tiene que identificar que realidad desmiente cada bulo sobre igualdad de genero.
 * Se juegan 5 rondas y al final se guarda la puntuacion si esta entre las 3 mejores.
 *
 * @version 1.0
 */
public class ProyectoMeme {

    /** Lista con los textos de los bulos leidos de memes.txt */
    static ArrayList<String> listaDeBulos = new ArrayList<>();

    /** Lista con los textos de las realidades leidas de realidades.json */
    static ArrayList<String> listaDeRealidades = new ArrayList<>();

    /** Lista con las respuestas correctas leidas de soluciones.xml. Mismo indice que listaDeBulos */
    static ArrayList<String> listaDeSoluciones = new ArrayList<>();

    /** Scanner para leer lo que escribe el usuario por teclado */
    static Scanner lecturaDelTeclado = new Scanner(System.in);

    /**
     * Metodo principal. Lanza todas las historias de usuario en orden.
     *
     * @param args argumentos de la linea de comandos (no se usan)
     * @throws Exception si hay algun error leyendo o escribiendo ficheros
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Bulo o Realidad ===");

        // HU1 - Comprobar archivos iniciales
        if (!comprobarArchivosIniciales()) {
            System.out.println("❌ El programa no puede continuar. Corrige los errores.");
            return;
        }

        hu2();
        
        // HU4 - Leer realidades
        listaDeRealidades = (ArrayList<String>) leerRealidades("datos/realidades.json");
        System.out.println("✅ Realidades cargadas: " + listaDeRealidades.size());
        
        hu3();
        cargarSoluciones();

        Integer puntosFinales = jugar();

        hu8(puntosFinales);
        hu9(puntosFinales);
        hu10();

        lecturaDelTeclado.close();
    }

    /**
     * HU1 - Comprueba que existe el directorio datos y que contiene
     * los tres ficheros necesarios: memes.txt, realidades.json y soluciones.xml.
     * Si falta algo detiene el programa.
     *
     * @return true si todo existe, false si falta algo
     */
    public static boolean comprobarArchivosIniciales() {
        // Ruta a la carpeta datos (está al mismo nivel que fuentes)
        Path rutaDatos = Paths.get("datos");
        
        // Comprobar si existe la carpeta datos
        if (!Files.exists(rutaDatos)) {
            System.out.println("ERROR: No existe la carpeta 'datos'");
            return false;
        }
        
        if (!Files.isDirectory(rutaDatos)) {
            System.out.println("ERROR: 'datos' no es una carpeta");
            return false;
        }
        
        // Lista de archivos necesarios
        String[] archivosNecesarios = {"memes.txt", "realidades.json", "soluciones.xml"};
        boolean todoCorrecto = true;
        
        // Comprobar cada archivo
        for (String archivo : archivosNecesarios) {
            Path rutaArchivo = rutaDatos.resolve(archivo);
            
            if (!Files.exists(rutaArchivo)) {
                System.out.println("ERROR: No existe el archivo: " + archivo);
                todoCorrecto = false;
            }
            else if (!Files.isRegularFile(rutaArchivo)) {
                System.out.println("ERROR: No es un archivo válido: " + archivo);
                todoCorrecto = false;
            }
            else {
                System.out.println("✓ Encontrado: " + archivo);
            }
        }
        
        return todoCorrecto;
    }

    /**
     * HU2 - Comprueba si existe el directorio resultados y el fichero mejores.txt.
     * Si no existen los crea.
     *
     * @throws Exception si hay un error al crear el directorio o el fichero
     */
    public static void hu2() throws Exception {
        Path rutaDirectorioResultados = Paths.get("resultados");
        Path rutaFicheroMejores = Paths.get("resultados/mejores.txt");

        if (!Files.exists(rutaDirectorioResultados))
            Files.createDirectories(rutaDirectorioResultados);

        if (!Files.exists(rutaFicheroMejores))
            Files.createFile(rutaFicheroMejores);
    }

    /**
     * HU3 - Lee el fichero de memes y genera una estructura de datos
     */
    public static void hu3() throws Exception {
        Path rutaMemes = Paths.get("datos/memes.txt");
        listaDeBulos = (ArrayList<String>) Files.readAllLines(rutaMemes);
        System.out.println("✅ Bulos cargados: " + listaDeBulos.size());
    }

    /**
     * HU4 - Lee el fichero de realidades y genera una estructura de datos (Lista)
     * @param ruta Ruta del archivo realidades.json
     * @return Lista con las realidades
     * @throws IOException si hay error al leer el archivo
     */
    public static List<String> leerRealidades(String ruta) throws IOException {
        Path path = Paths.get(ruta);
        List<String> lineas = Files.readAllLines(path);
        List<String> realidades = new ArrayList<>();
        
        System.out.println("\n📖 Leyendo archivo: " + ruta);
        
        // Procesar cada línea (asumiendo que cada línea es una realidad)
        for (String linea : lineas) {
            // Limpiar la línea (quitar espacios extras)
            linea = linea.trim();
            
            // Solo añadir si no está vacía
            if (!linea.isEmpty()) {
                realidades.add(linea);
                System.out.println("   → Realidad añadida: " + linea);
            }
        }
        
        System.out.println("📊 Total realidades cargadas: " + realidades.size());
        return realidades;
    }

    // Los demás métodos (cargarSoluciones, jugar, hu8, hu9, hu10) continúan igual...
}

