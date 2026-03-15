import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Juego "Bulo o Realidad".
 * El jugador identifica qué realidad desmiente cada bulo.
 */
public class ProyectoMeme {

    // Listas con los datos leídos de los ficheros
    static ArrayList<String> listaDeBulos      = new ArrayList<>();
    static ArrayList<String> listaDeRealidades = new ArrayList<>();

    // Mapa soluciones: clave = número de bulo, valor = número de realidad correcta
    static HashMap<Integer, Integer> mapaSoluciones = new HashMap<>();

    // Para leer lo que escribe el usuario
    static Scanner teclado = new Scanner(System.in);

    // Para no repetir bulos ya mostrados
    static ArrayList<Integer> bulosUsados = new ArrayList<>();

    // Variable global para guardar la puntuación final (HU8)
    static int puntuacionFinal = 0;

    // -------------------------------------------------------------------------
    // MAIN
    // -------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        
        System.out.println("=== Bulo o Realidad ===");
        
        // HU1 - Comprobar que existen los ficheros necesarios
        if (!comprobarArchivosIniciales()) {
            System.out.println("El programa no puede continuar.");
            return;
        }
        
        // HU2 - Crear carpeta resultados y mejores.txt si no existen
        hu2();
        
        // HU3 - Leer los bulos del fichero memes.txt
        hu3();
        
        // HU4 - Leer las realidades del fichero realidades.json
        listaDeRealidades = leerRealidades("datos/realidades.json");
        System.out.println("Realidades cargadas: " + listaDeRealidades.size());
        
        // Leer las soluciones del fichero soluciones.xml
        mapaSoluciones = leerSoluciones("datos/soluciones.xml");
        System.out.println("Soluciones cargadas: " + mapaSoluciones.size());
        
        // HU7 - Jugar 5 rondas mostrando marcador
        hu7();
        
        // HU8 - Mostrar la puntuación final
        hu8();
        
        teclado.close();
    }

    // -------------------------------------------------------------------------
    // HU1 - Comprobar que existe la carpeta datos y los tres ficheros
    // -------------------------------------------------------------------------
    public static boolean comprobarArchivosIniciales() {

        Path carpetaDatos = Paths.get("datos");

        if (!Files.exists(carpetaDatos) || !Files.isDirectory(carpetaDatos)) {
            System.out.println("ERROR: No existe la carpeta 'datos'");
            return false;
        }

        String[] ficheros = { "memes.txt", "realidades.json", "soluciones.xml" };
        boolean todoBien = true;

        for (String fichero : ficheros) {
            Path ruta = carpetaDatos.resolve(fichero);
            if (!Files.exists(ruta)) {
                System.out.println("ERROR: Falta el fichero " + fichero);
                todoBien = false;
            } else {
                System.out.println("OK: " + fichero);
            }
        }

        return todoBien;
    }

    // -------------------------------------------------------------------------
    // HU2 - Crear carpeta resultados y fichero mejores.txt si no existen
    // -------------------------------------------------------------------------
    public static void hu2() throws IOException {

        Path carpeta = Paths.get("resultados");
        Path fichero = Paths.get("resultados/mejores.txt");

        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }

        if (!Files.exists(fichero)) {
            Files.createFile(fichero);
        }
    }

    // -------------------------------------------------------------------------
    // HU3 - Leer memes.txt y guardar cada línea como un bulo
    // -------------------------------------------------------------------------
    public static void hu3() throws IOException {

        Path ruta = Paths.get("datos/memes.txt");
        listaDeBulos = new ArrayList<>(Files.readAllLines(ruta));
        listaDeBulos.removeIf(String::isBlank);
        System.out.println("Bulos cargados: " + listaDeBulos.size());
    }

    // -------------------------------------------------------------------------
    // HU4 - Leer realidades.json y extraer el campo "texto" de cada objeto
    // -------------------------------------------------------------------------
    public static ArrayList<String> leerRealidades(String ruta) throws IOException {

        List<String> lineas = Files.readAllLines(Paths.get(ruta));
        ArrayList<String> realidades = new ArrayList<>();

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.contains("\"texto\"")) {
                int inicio   = linea.indexOf("\"texto\"") + "\"texto\"".length();
                int abre     = linea.indexOf('"', inicio + 1) + 1;
                int cierra   = linea.indexOf('"', abre);
                String texto = linea.substring(abre, cierra).trim();
                if (!texto.isBlank()) {
                    realidades.add(texto);
                }
            }
        }

        return realidades;
    }

    // -------------------------------------------------------------------------
    // Leer soluciones.xml → mapa  índice_bulo : índice_realidad  (ambos 0-based)
    // Formato esperado en el XML: <solucion bulo="0" realidad="2"/>
    // -------------------------------------------------------------------------
    public static HashMap<Integer, Integer> leerSoluciones(String ruta) throws IOException {

        List<String> lineas = Files.readAllLines(Paths.get(ruta));
        HashMap<Integer, Integer> soluciones = new HashMap<>();

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.startsWith("<solucion")) {

                // Extraer el número del atributo bulo="X"
                String buscaBulo   = "bulo=\"";
                int inicioBulo     = linea.indexOf(buscaBulo) + buscaBulo.length();
                int finBulo        = linea.indexOf('"', inicioBulo);
                Integer numeroBulo = Integer.parseInt(linea.substring(inicioBulo, finBulo));

                // Extraer el número del atributo realidad="X"
                String buscaReal   = "realidad=\"";
                int inicioReal     = linea.indexOf(buscaReal) + buscaReal.length();
                int finReal        = linea.indexOf('"', inicioReal);
                Integer numeroReal = Integer.parseInt(linea.substring(inicioReal, finReal));

                soluciones.put(numeroBulo, numeroReal);
            }
        }

        return soluciones;
    }

    // -------------------------------------------------------------------------
    // HU5 - Elegir un bulo al azar sin repetir y mostrar las realidades
    // -------------------------------------------------------------------------
    public static int mostrarBuloYRealidades(List<String> bulos, List<String> realidades) {

        if (bulosUsados.size() >= bulos.size()) {
            System.out.println("No quedan bulos por mostrar.");
            return -1;
        }

        // Seleccionar índice al azar que no se haya usado ya
        Random random  = new Random();
        int indiceBulo = random.nextInt(bulos.size());
        while (bulosUsados.contains(indiceBulo)) {
            indiceBulo = random.nextInt(bulos.size());
        }
        bulosUsados.add(indiceBulo);

        // Mostrar el bulo
        System.out.println("\n=================================================");
        System.out.println("BULO: " + bulos.get(indiceBulo));
        System.out.println("\n¿Qué dato real desmiente este bulo?");
        System.out.println("-------------------------------------------------");

        // Mostrar la lista numerada de realidades
        for (int i = 0; i < realidades.size(); i++) {
            System.out.println((i + 1) + ". " + realidades.get(i));
        }
        System.out.println("=================================================");

        return indiceBulo;
    }

    // -------------------------------------------------------------------------
    // HU6 - Pedir al usuario que elija un número
    // -------------------------------------------------------------------------
    public static int pedirRespuestaUsuario(int totalRealidades) {

        Integer eleccion = null;

        while (eleccion == null) {
            System.out.print("Tu elección (1-" + totalRealidades + "): ");
            String entrada = teclado.nextLine().trim();

            try {
                eleccion = Integer.parseInt(entrada);
                if (eleccion < 1 || eleccion > totalRealidades) {
                    System.out.println("Número fuera de rango. Elige entre 1 y " + totalRealidades);
                    eleccion = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Eso no es un número. Inténtalo de nuevo.");
            }
        }

        return eleccion - 1; // convertir a 0-based
    }

    // -------------------------------------------------------------------------
    // HU6 (continuación) - Comprobar si la respuesta es correcta
    // -------------------------------------------------------------------------
    public static boolean comprobarRespuesta(int indiceBulo, int respuestaUsuario) {

        Integer respuestaCorrecta = mapaSoluciones.get(indiceBulo);

        if (respuestaCorrecta == null) {
            System.out.println("No hay solución registrada para este bulo.");
            return false;
        }

        return respuestaCorrecta.equals(respuestaUsuario);
    }

    // =========================================================
    // HU7 - Mostrar marcador y repetir hasta completar 5 memes
    // =========================================================
    public static void hu7() {
        
        int rondasJugadas = 0;
        int puntuacionTotal = 0;
        
        // Limpiar la lista de bulos usados por si acaso
        bulosUsados.clear();
        
        // Bucle para jugar 5 rondas
        while (rondasJugadas < 5) {
            
            System.out.println("\n=== RONDA " + (rondasJugadas + 1) + " DE 5 ===");
            
            // HU5 - Mostrar un bulo al azar y la lista de realidades
            int indiceBulo = mostrarBuloYRealidades(listaDeBulos, listaDeRealidades);
            
            if (indiceBulo != -1) {
                // HU6 - Pedir respuesta y comprobar
                int respuesta = pedirRespuestaUsuario(listaDeRealidades.size());
                boolean acierto = comprobarRespuesta(indiceBulo, respuesta);
                
                if (acierto) {
                    System.out.println("✅ ¡Correcto!");
                    puntuacionTotal++;
                } else {
                    Integer correcta = mapaSoluciones.get(indiceBulo);
                    if (correcta != null) {
                        System.out.println("❌ Incorrecto. La respuesta correcta era la " + (correcta + 1));
                    } else {
                        System.out.println("❌ Incorrecto.");
                    }
                }
                
                // HU7 - Mostrar marcador actualizado
                rondasJugadas++;
                mostrarMarcador(puntuacionTotal, rondasJugadas);
            }
        }
        
        // Guardar la puntuación final en la variable global para HU8
        puntuacionFinal = puntuacionTotal;
    }

    /**
     * Muestra el marcador actual después de cada ronda
     * @param puntos Puntos actuales
     * @param rondas Número de rondas jugadas
     */
    public static void mostrarMarcador(int puntos, int rondas) {
        System.out.println("\n╔════════════════════════════╗");
        System.out.println("║        MARCADOR           ║");
        System.out.println("╠════════════════════════════╣");
        System.out.printf("║  PUNTOS: %d/%d              ║%n", puntos, rondas);
        System.out.printf("║  RONDAS RESTANTES: %d       ║%n", (5 - rondas));
        System.out.println("╚════════════════════════════╝");
        
        if (rondas < 5) {
            System.out.println("\nPresiona ENTER para continuar...");
            teclado.nextLine();
        }
    }

    // =========================================================
    // HU8 - Mostrar la puntuación final alcanzada por el usuario
    // =========================================================
    public static void hu8() {
        
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║        PUNTUACIÓN FINAL           ║");
        System.out.println("╠════════════════════════════════════╣");
        
        // Mostrar la puntuación con formato
        System.out.printf("║                                      ║%n");
        System.out.printf("║          %d / 5 PUNTOS              ║%n", puntuacionFinal);
        System.out.printf("║                                      ║%n");
        
        // Mostrar mensaje según la puntuación
        System.out.println("╠════════════════════════════════════╣");
        if (puntuacionFinal == 5) {
            System.out.println("║   ¡PERFECTO! Has acertado todo     ║");
        } else if (puntuacionFinal >= 3) {
            System.out.println("║      ¡BIEN! Buena puntuación       ║");
        } else if (puntuacionFinal >= 1) {
            System.out.println("║     Puedes mejorar la próxima      ║");
        } else {
            System.out.println("║    ¡Ánimo! La próxima será mejor   ║");
        }
        System.out.println("╚════════════════════════════════════╝");
        
        // Calcular porcentaje de aciertos
        double porcentaje = (puntuacionFinal * 100.0) / 5.0;
        System.out.printf("\n📊 Porcentaje de aciertos: %.1f%%\n", porcentaje);
    }
}