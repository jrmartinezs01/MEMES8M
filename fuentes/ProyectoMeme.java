import java.nio.file.*;
import java.util.*;

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

   
}