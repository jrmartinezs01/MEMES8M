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
    // Usamos Integer (wrapper) en lugar de int porque HashMap lo requiere
    static HashMap<Integer, Integer> mapaSoluciones = new HashMap<>();

    // Para leer lo que escribe el usuario
    static Scanner teclado = new Scanner(System.in);

    // Para no repetir bulos ya mostrados
    static ArrayList<Integer> bulosUsados = new ArrayList<>();

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

        // HU5 - Mostrar un bulo al azar y la lista de realidades
        int indiceBulo = mostrarBuloYRealidades(listaDeBulos, listaDeRealidades);

        // HU6 - Pedir respuesta al usuario y comprobarla
        if (indiceBulo != -1) {
            int respuesta = pedirRespuestaUsuario(listaDeRealidades.size());
            boolean acierto = comprobarRespuesta(indiceBulo, respuesta);

            if (acierto) {
                System.out.println("¡Correcto!");
            } else {
                // Mostrar cuál era la respuesta correcta
                Integer correcta = mapaSoluciones.get(indiceBulo);
                System.out.println("Incorrecto. La respuesta correcta era la " + (correcta + 1));
            }
        }

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
    //
    // Formato esperado en el XML:
    //   <solucion bulo="0" realidad="2"/>
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
    // HU6 - Pedir al usuario que elija un número y comprobar si es correcto
    // -------------------------------------------------------------------------

    /**
     * Pide un número entre 1 y totalRealidades.
     * Usamos Integer (wrapper) para poder inicializarlo a null
     * y así saber que todavía no se ha introducido ningún valor válido.
     */
    public static int pedirRespuestaUsuario(int totalRealidades) {

        Integer eleccion = null;   // null = todavía sin respuesta válida

        while (eleccion == null) {
            System.out.print("Tu elección (1-" + totalRealidades + "): ");
            String entrada = teclado.nextLine().trim();

            try {
                eleccion = Integer.parseInt(entrada);   // String → Integer (autoboxing)
                if (eleccion < 1 || eleccion > totalRealidades) {
                    System.out.println("Número fuera de rango. Elige entre 1 y " + totalRealidades);
                    eleccion = null;   // volver a pedir
                }
            } catch (NumberFormatException e) {
                System.out.println("Eso no es un número. Inténtalo de nuevo.");
            }
        }

        return eleccion - 1;   // convertir a 0-based (el usuario ve 1, internamente es 0)
    }

    /**
     * Busca en mapaSoluciones la realidad correcta para ese bulo
     * y la compara con la respuesta del usuario usando .equals()
     * porque estamos comparando objetos Integer, no primitivos int.
     */
    public static boolean comprobarRespuesta(int indiceBulo, int respuestaUsuario) {

        Integer respuestaCorrecta = mapaSoluciones.get(indiceBulo);

        if (respuestaCorrecta == null) {
            System.out.println("No hay solución registrada para este bulo.");
            return false;
        }

        // Usamos .equals() y no == porque son objetos Integer
        return respuestaCorrecta.equals(respuestaUsuario);
    }

    // =========================================================
// HU7 - Bucle principal: 5 memes + marcador tras cada ronda
// =========================================================

public void iniciarPartida() throws IOException {
    puntuacion  = 0;
    rondaActual = 0;
    memesUsados = new ArrayList<>();

    // Bucle de 5 rondas (HU7)
    while (rondaActual < TOTAL_MEMES) {          // TOTAL_MEMES = 5
        rondaActual++;

        Meme memeActual = seleccionarMemeAleatorio(); // HU5
        mostrarRonda(memeActual);                     // HU5
        boolean acierto = procesarRespuesta(memeActual); // HU6

        mostrarMarcador(acierto);                    // ← HU7
    }

    mostrarPuntuacionFinal();   // HU8
    gestionarAlta();            // HU9
    mostrarRankingYDespedida(); // HU10
}


/**
 * Muestra el marcador actualizado tras cada ronda.
 * Si quedan rondas, hace una pausa hasta que el usuario pulse INTRO.
 *
 * @param acierto indica si la ronda anterior fue un acierto.
 */
private void mostrarMarcador(boolean acierto) {
    int rondasRestantes = TOTAL_MEMES - rondaActual;

    System.out.println();
    System.out.println("  ┌─────────────────────────────────────┐");
    System.out.printf( "  │  MARCADOR: %d / %d puntos             │%n",
                        puntuacion, rondaActual);

    if (rondasRestantes > 0) {
        System.out.printf(
                "  │  Quedan %d meme(s) por responder     │%n",
                rondasRestantes);
    } else {
        System.out.println(
                "  │  ¡Has completado los 5 memes!        │");
    }
    System.out.println("  └─────────────────────────────────────┘");
    System.out.println();

    // Pausa entre rondas para que el usuario lea el marcador
    if (rondasRestantes > 0) {
        System.out.print("  Pulsa INTRO para continuar...");
        scanner.nextLine();
        System.out.println();
    }
}
}