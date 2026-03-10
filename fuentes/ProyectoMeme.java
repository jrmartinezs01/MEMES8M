import java.nio.file.*;
import java.util.*;

<<<<<<< HEAD
<<<<<<< HEAD
public class meme {
    
=======
=======
/**
 * Clase principal del juego "Bulo o Realidad".
 * El jugador tiene que identificar que realidad desmiente cada bulo sobre igualdad de genero.
 * Se juegan 5 rondas y al final se guarda la puntuacion si esta entre las 3 mejores.
 *
 * @version 1.0
 */
>>>>>>> 520ac6ea3b0a0ea97323541ce2dac54b59fc4da1
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
<<<<<<< HEAD
     * Metodo principal. Lanza todas las historias de usuario en orden.
     *
     * @param args argumentos de la linea de comandos (no se usan)
     * @throws Exception si hay algun error leyendo o escribiendo ficheros
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Bulo o Realidad ===");

        hu1();
        hu2();
        hu3();
        hu4();
        cargarSoluciones();

        Integer puntosFinales = jugar();

        hu8(puntosFinales);
        hu9(puntosFinales);
        hu10();

        lecturaDelTeclado.close();
    }
<<<<<<< HEAD
>>>>>>> a311a3897c6e4ca4dd0bf93eb0746dc9adf5543f
}
=======

    /**
     * HU1 - Comprueba que existe el directorio datos y que contiene
     * los tres ficheros necesarios: memes.txt, realidades.json y soluciones.xml.
     * Si falta algo detiene el programa.
     *
     * @throws Exception si el directorio datos no existe
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
=======
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
>>>>>>> 3b480ff1f2506c0123266ce202e0ccb5edf8e177
