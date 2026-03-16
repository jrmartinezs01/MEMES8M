import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Clase principal del juego "Bulo o Realidad".
 *
 * <p>El jugador ve un bulo (meme falso) y tiene que elegir, de una lista
 * numerada, qué dato real lo desmiente. Se juegan 5 rondas y al final
 * se guarda la puntuación si es de las tres mejores.</p>
 *
 * <p>Ficheros que usa el programa:</p>
 * <ul>
 *   <li>{@code datos/memes.txt}        – un bulo por línea</li>
 *   <li>{@code datos/realidades.json}  – objetos con campo "texto"</li>
 *   <li>{@code datos/soluciones.xml}   – relación bulo-realidad</li>
 *   <li>{@code resultados/mejores.txt} – ranking de los tres mejores</li>
 * </ul>
 *
 * @author Nombre Apellido
 * @version 1.0
 */
public class ProyectoMeme {

    /** Bulos leídos de memes.txt. */
    static ArrayList<String> listaDeBulos = new ArrayList<>();

    /** Realidades leídas de realidades.json. */
    static ArrayList<String> listaDeRealidades = new ArrayList<>();

    /** Clave: índice del bulo. Valor: índice de la realidad correcta (base 0). */
    static HashMap<Integer, Integer> mapaSoluciones = new HashMap<>();

    /** Para leer la entrada del usuario por teclado. */
    static Scanner teclado = new Scanner(System.in);

    /** Índices de los bulos que ya han salido, para no repetirlos. */
    static ArrayList<Integer> bulosUsados = new ArrayList<>();

    /** Puntos que ha sacado el jugador en la partida (se rellena en jugarPartida). */
    static Integer puntuacionFinal = 0;


    // -------------------------------------------------------------------------
    // MAIN
    // -------------------------------------------------------------------------

    /**
     * Punto de entrada del programa. Llama a cada historia de usuario
     * en orden y cierra el scanner al terminar.
     *
     * @param args argumentos de línea de comandos (no se usan)
     * @throws Exception si ocurre cualquier error de lectura/escritura
     */
    public static void main(String[] args) throws Exception {

        System.out.println("=== Bulo o Realidad ===");

        // HU1 – sin los ficheros de datos no tiene sentido continuar
        if (!comprobarArchivosIniciales()) {
            System.out.println("El programa no puede continuar.");
            return;
        }

        crearDirectorioResultados(); // HU2 – crea resultados/ y mejores.txt si no existen

        cargarBulos(); // HU3 – carga los bulos en listaDeBulos

        // HU4 – cargamos realidades y soluciones desde sus ficheros
        listaDeRealidades = leerRealidades("datos/realidades.json");
        System.out.println("Realidades cargadas: " + listaDeRealidades.size());

        mapaSoluciones = leerSoluciones("datos/soluciones.xml");
        System.out.println("Soluciones cargadas: " + mapaSoluciones.size());

        jugarPartida(); // HU7 – bucle principal: 5 rondas con marcador

        mostrarPuntuacionFinal(); // HU8 – muestra el resultado al terminar

        guardarPuntuacionSiEsMejor(); // HU9 – guarda si entra en el top 3

        mostrarRankingYDespedirse(); // HU10 – muestra el ranking y se despide

        teclado.close();
    }


    // -------------------------------------------------------------------------
    // HU1
    // -------------------------------------------------------------------------

    /**
     * Comprueba que exista la carpeta {@code datos} y los tres ficheros
     * necesarios para que el juego funcione.
     *
     * <p>Si falta alguno muestra un mensaje de error por pantalla pero
     * sigue revisando el resto para informar de todos los problemas
     * de una sola vez.</p>
     *
     * @return {@code true} si todo está en orden, {@code false} si falta algo
     */
    public static Boolean comprobarArchivosIniciales() {

        Path carpetaDatos = Paths.get("datos");

        if (!Files.exists(carpetaDatos) || !Files.isDirectory(carpetaDatos)) {
            System.out.println("ERROR: No existe la carpeta 'datos'");
            return false;
        }

        String[] nombresFicheros = { "memes.txt", "realidades.json", "soluciones.xml" };
        Boolean todosPresentes = true;

        for (String nombreFichero : nombresFicheros) {
            Path rutaFichero = carpetaDatos.resolve(nombreFichero);
            if (!Files.exists(rutaFichero)) {
                System.out.println("ERROR: Falta el fichero " + nombreFichero);
                todosPresentes = false;
            } else {
                System.out.println("OK: " + nombreFichero);
            }
        }

        return todosPresentes;
    }


    // -------------------------------------------------------------------------
    // HU2
    // -------------------------------------------------------------------------

    /**
     * Crea la carpeta {@code resultados} y el fichero {@code mejores.txt}
     * si todavía no existen. Si ya existen no hace nada.
     *
     * @throws IOException si no se puede crear la carpeta o el fichero
     */
    public static void crearDirectorioResultados() throws IOException {

        Path carpetaResultados = Paths.get("resultados");
        Path ficheroMejores    = Paths.get("resultados/mejores.txt");

        if (!Files.exists(carpetaResultados)) {
            Files.createDirectories(carpetaResultados);
        }

        if (!Files.exists(ficheroMejores)) {
            Files.createFile(ficheroMejores);
        }
    }


    // -------------------------------------------------------------------------
    // HU3
    // -------------------------------------------------------------------------

    /**
     * Lee el fichero {@code datos/memes.txt} línea a línea y guarda cada
     * bulo en {@link #listaDeBulos}. Las líneas en blanco se ignoran.
     *
     * @throws IOException si el fichero no se puede leer
     */
    public static void cargarBulos() throws IOException {

        Path rutaMemes = Paths.get("datos/memes.txt");
        listaDeBulos = new ArrayList<>(Files.readAllLines(rutaMemes));
        listaDeBulos.removeIf(String::isBlank);
        System.out.println("Bulos cargados: " + listaDeBulos.size());
    }


    // -------------------------------------------------------------------------
    // HU4 – lectura del JSON sin librerías externas
    // -------------------------------------------------------------------------

    /**
     * Lee el fichero JSON de realidades y extrae el valor del campo
     * {@code "texto"} de cada objeto.
     *
     * <p>El parseo es manual (sin librerías) porque el formato es sencillo
     * y predecible: una propiedad {@code "texto"} por línea.</p>
     *
     * @param rutaFichero ruta al fichero {@code realidades.json}
     * @return lista con el texto de cada realidad en el mismo orden que
     *         aparece en el fichero
     * @throws IOException si el fichero no se puede leer
     */
    public static ArrayList<String> leerRealidades(String rutaFichero) throws IOException {

        List<String> lineas       = Files.readAllLines(Paths.get(rutaFichero));
        ArrayList<String> textos  = new ArrayList<>();

        for (String linea : lineas) {
            linea = linea.trim();

            if (!linea.contains("\"texto\"")) {
                continue;
            }

            // Localizar el valor entre las comillas que siguen a la clave "texto"
            Integer posicionTrasLlave      = linea.indexOf("\"texto\"") + "\"texto\"".length();
            Integer posicionAbreComillas   = linea.indexOf('"', posicionTrasLlave + 1) + 1;
            Integer posicionCierraComillas = linea.indexOf('"', posicionAbreComillas);
            String textoExtraido = linea.substring(posicionAbreComillas, posicionCierraComillas).trim();

            if (!textoExtraido.isBlank()) {
                textos.add(textoExtraido);
            }
        }

        return textos;
    }


    /**
     * Lee el fichero XML de soluciones y construye un mapa que relaciona
     * el índice de cada bulo con el índice de su realidad correcta.
     *
     * <p>Formato esperado de cada línea significativa:</p>
     * <pre>{@code <solucion bulo="0" realidad="2"/>}</pre>
     * <p>Ambos índices son base 0, igual que las listas internas.</p>
     *
     * @param rutaFichero ruta al fichero {@code soluciones.xml}
     * @return mapa {@code indiceBulo -> indiceRealidad}
     * @throws IOException si el fichero no se puede leer
     */
    public static HashMap<Integer, Integer> leerSoluciones(String rutaFichero) throws IOException {

        List<String> lineas = Files.readAllLines(Paths.get(rutaFichero));
        HashMap<Integer, Integer> soluciones = new HashMap<>();

        for (String linea : lineas) {
            linea = linea.trim();

            if (!linea.startsWith("<solucion")) {
                continue;
            }

            // Extraer el valor del atributo bulo="X"
            String atributoBulo         = "bulo=\"";
            Integer posicionInicioBulo  = linea.indexOf(atributoBulo) + atributoBulo.length();
            Integer posicionFinBulo     = linea.indexOf('"', posicionInicioBulo);
            Integer indiceBulo          = Integer.parseInt(linea.substring(posicionInicioBulo, posicionFinBulo));

            // Extraer el valor del atributo realidad="X"
            String atributoRealidad        = "realidad=\"";
            Integer posicionInicioRealidad = linea.indexOf(atributoRealidad) + atributoRealidad.length();
            Integer posicionFinRealidad    = linea.indexOf('"', posicionInicioRealidad);
            Integer indiceRealidad         = Integer.parseInt(linea.substring(posicionInicioRealidad, posicionFinRealidad));

            soluciones.put(indiceBulo, indiceRealidad);
        }

        return soluciones;
    }


    // -------------------------------------------------------------------------
    // HU5
    // -------------------------------------------------------------------------

    /**
     * Elige un bulo al azar que no se haya mostrado todavía y presenta
     * la lista numerada de realidades para que el jugador elija.
     *
     * <p>El índice del bulo elegido se añade a {@link #bulosUsados}
     * para evitar repeticiones en rondas posteriores.</p>
     *
     * @param listaBulos      lista completa de bulos disponibles
     * @param listaRealidades lista de realidades que se mostrarán al jugador
     * @return índice (base 0) del bulo que se ha mostrado,
     *         o {@code -1} si ya se han agotado todos los bulos
     */
    public static Integer mostrarBuloYRealidades(List<String> listaBulos, List<String> listaRealidades) {

        if (bulosUsados.size() >= listaBulos.size()) {
            System.out.println("No quedan bulos por mostrar.");
            return -1;
        }

        // Elegir un índice aleatorio que no hayamos usado ya
        Random generadorAleatorio = new Random();
        Integer indiceBuloElegido = generadorAleatorio.nextInt(listaBulos.size());
        while (bulosUsados.contains(indiceBuloElegido)) {
            indiceBuloElegido = generadorAleatorio.nextInt(listaBulos.size());
        }
        bulosUsados.add(indiceBuloElegido);

        System.out.println("\n=================================================");
        System.out.println("BULO: " + listaBulos.get(indiceBuloElegido));
        System.out.println("\n¿Qué dato real desmiente este bulo?");
        System.out.println("-------------------------------------------------");

        for (int i = 0; i < listaRealidades.size(); i++) {
            System.out.println((i + 1) + ". " + listaRealidades.get(i));
        }
        System.out.println("=================================================");

        return indiceBuloElegido;
    }


    // -------------------------------------------------------------------------
    // HU6
    // -------------------------------------------------------------------------

    /**
     * Pide al usuario que introduzca un número entre 1 y
     * {@code totalOpciones}. Si escribe algo que no es un número
     * o está fuera de rango, vuelve a pedirlo hasta que sea válido.
     *
     * @param totalOpciones número de realidades disponibles para elegir
     * @return índice elegido en base 0 (lo que el usuario ve como 1 se
     *         devuelve como 0, etc.)
     */
    public static Integer pedirRespuestaUsuario(Integer totalOpciones) {

        Integer opcionElegida = null;

        while (opcionElegida == null) {
            System.out.print("Tu elección (1-" + totalOpciones + "): ");
            String entradaTeclado = teclado.nextLine().trim();

            try {
                opcionElegida = Integer.parseInt(entradaTeclado);
                if (opcionElegida < 1 || opcionElegida > totalOpciones) {
                    System.out.println("Número fuera de rango. Elige entre 1 y " + totalOpciones);
                    opcionElegida = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Eso no es un número. Inténtalo de nuevo.");
            }
        }

        // Convertimos a base 0 para comparar con el mapa de soluciones
        return opcionElegida - 1;
    }


    /**
     * Consulta en {@link #mapaSoluciones} si el índice de realidad que ha
     * elegido el usuario coincide con la respuesta correcta para ese bulo.
     *
     * @param indiceBulo       índice (base 0) del bulo que se estaba mostrando
     * @param respuestaUsuario índice (base 0) de la realidad elegida
     * @return {@code true} si la respuesta es correcta, {@code false} si no
     */
    public static Boolean comprobarRespuesta(Integer indiceBulo, Integer respuestaUsuario) {

        Integer respuestaCorrecta = mapaSoluciones.get(indiceBulo);

        if (respuestaCorrecta == null) {
            System.out.println("No hay solución registrada para este bulo.");
            return false;
        }

        return respuestaCorrecta.equals(respuestaUsuario);
    }


    // -------------------------------------------------------------------------
    // HU7
    // -------------------------------------------------------------------------

    /**
     * Controla el bucle principal del juego: 5 rondas consecutivas.
     * Después de cada ronda llama a {@link #mostrarMarcador} para que
     * el jugador sepa cómo va. Al terminar guarda el resultado en
     * {@link #puntuacionFinal}.
     */
    public static void jugarPartida() {

        Integer rondasJugadas   = 0;
        Integer puntuacionTotal = 0;
        bulosUsados.clear();

        while (rondasJugadas < 5) {

            System.out.println("\n=== RONDA " + (rondasJugadas + 1) + " DE 5 ===");

            Integer indiceBuloActual = mostrarBuloYRealidades(listaDeBulos, listaDeRealidades);

            if (indiceBuloActual == -1) {
                break; // se agotaron los bulos antes de llegar a 5
            }

            Integer respuestaJugador = pedirRespuestaUsuario(listaDeRealidades.size());
            Boolean esCorrecta       = comprobarRespuesta(indiceBuloActual, respuestaJugador);

            if (esCorrecta) {
                System.out.println("Correcto!");
                puntuacionTotal++;
            } else {
                Integer respuestaCorrecta = mapaSoluciones.get(indiceBuloActual);
                if (respuestaCorrecta != null) {
                    System.out.println("Incorrecto. La respuesta correcta era la " + (respuestaCorrecta + 1));
                } else {
                    System.out.println("Incorrecto.");
                }
            }

            rondasJugadas++;
            mostrarMarcador(puntuacionTotal, rondasJugadas);
        }

        puntuacionFinal = puntuacionTotal;
    }


    /**
     * Muestra por pantalla el marcador actual con los puntos conseguidos
     * y las rondas que quedan. Si la partida no ha terminado, espera a
     * que el jugador pulse ENTER antes de continuar.
     *
     * @param puntosActuales  puntos acumulados hasta este momento
     * @param rondasJugadas   número de rondas ya completadas
     */
    public static void mostrarMarcador(Integer puntosActuales, Integer rondasJugadas) {

        System.out.println("\n--- MARCADOR ---");
        System.out.println("Puntos: " + puntosActuales + "/" + rondasJugadas);
        System.out.println("Rondas restantes: " + (5 - rondasJugadas));
        System.out.println("----------------");

        if (rondasJugadas < 5) {
            System.out.println("Presiona ENTER para continuar...");
            teclado.nextLine();
        }
    }


    // -------------------------------------------------------------------------
    // HU8
    // -------------------------------------------------------------------------

    /**
     * Muestra la puntuación final de la partida junto con un mensaje
     * motivacional según el resultado y el porcentaje de aciertos.
     */
    public static void mostrarPuntuacionFinal() {

        System.out.println("\n=== PUNTUACION FINAL ===");
        System.out.println("Has conseguido " + puntuacionFinal + " de 5 puntos.");

        if (puntuacionFinal == 5) {
            System.out.println("¡Perfecto! Has acertado todo.");
        } else if (puntuacionFinal >= 3) {
            System.out.println("¡Bien! Buena puntuación.");
        } else if (puntuacionFinal >= 1) {
            System.out.println("Puedes mejorar la próxima vez.");
        } else {
            System.out.println("¡Ánimo! La próxima irá mejor.");
        }

        Double porcentajeAciertos = (puntuacionFinal * 100.0) / 5.0;
        System.out.printf("Porcentaje de aciertos: %.1f%%%n", porcentajeAciertos);
    }


    // -------------------------------------------------------------------------
    // HU9
    // -------------------------------------------------------------------------

    /**
     * Comprueba si la puntuación de la partida recién jugada merece estar
     * en el top 3. Si es así, pide el nombre al jugador, añade la entrada
     * al ranking y guarda los tres mejores en {@code resultados/mejores.txt}.
     *
     * <p>Si el jugador no escribe nada, su nombre se guarda como "Anonimo".</p>
     *
     * @throws IOException si no se puede leer o escribir el fichero de mejores
     */
    public static void guardarPuntuacionSiEsMejor() throws IOException {

        Path ficheroMejores          = Paths.get("resultados/mejores.txt");
        ArrayList<String> ranking    = leerMejores(ficheroMejores);

        if (estaEnTop3(ranking, puntuacionFinal)) {

            System.out.println("\n¡Enhorabuena! Tu puntuación está entre las 3 mejores.");
            System.out.print("Introduce tu nombre: ");
            String nombreJugador = teclado.nextLine().trim();

            if (nombreJugador.isBlank()) {
                nombreJugador = "Anonimo";
            }

            ranking.add(nombreJugador + ";" + puntuacionFinal);
            ordenarPuntuaciones(ranking);
            guardarTop3(ficheroMejores, ranking);

        } else {
            System.out.println("\nTu puntuación no está entre las 3 mejores. ¡Sigue intentándolo!");
        }
    }


    /**
     * Lee el fichero {@code mejores.txt} y devuelve sus líneas en una lista,
     * ignorando las que estén vacías.
     *
     * <p>Formato de cada línea: {@code nombre;puntuacion}</p>
     *
     * @param ficheroRanking ruta al fichero de mejores puntuaciones
     * @return lista de entradas del ranking (puede estar vacía si el
     *         fichero no tiene contenido todavía)
     * @throws IOException si el fichero no se puede leer
     */
    public static ArrayList<String> leerMejores(Path ficheroRanking) throws IOException {
        ArrayList<String> entradasRanking = new ArrayList<>(Files.readAllLines(ficheroRanking));
        entradasRanking.removeIf(String::isBlank);
        return entradasRanking;
    }


    /**
     * Determina si una puntuación dada merece entrar en el top 3.
     *
     * <p>Si el ranking tiene menos de 3 entradas siempre entra.
     * Si ya tiene 3, solo entra si supera estrictamente a la tercera
     * (en caso de empate no se guarda).</p>
     *
     * @param entradasRanking entradas actuales del ranking (formato {@code nombre;puntuacion})
     * @param puntuacionNueva puntuación a comparar con el ranking actual
     * @return {@code true} si la puntuación entra en el top 3
     */
    public static Boolean estaEnTop3(ArrayList<String> entradasRanking, Integer puntuacionNueva) {

        if (entradasRanking.size() < 3) {
            return true;
        }

        // Ordenamos para asegurarnos de que get(2) es realmente el tercero
        ordenarPuntuaciones(entradasRanking);
        Integer puntuacionTercero = extraerPuntuacion(entradasRanking.get(2));
        return puntuacionNueva > puntuacionTercero;
    }


    /**
     * Ordena la lista del ranking de mayor a menor puntuación usando
     * el algoritmo de burbuja.
     *
     * <p>Se modifica la lista original directamente, no se devuelve nada.</p>
     *
     * @param entradasRanking lista de entradas en formato {@code nombre;puntuacion}
     */
    public static void ordenarPuntuaciones(ArrayList<String> entradasRanking) {

        Integer totalEntradas = entradasRanking.size();

        for (int i = 0; i < totalEntradas - 1; i++) {
            for (int j = 0; j < totalEntradas - 1 - i; j++) {
                if (extraerPuntuacion(entradasRanking.get(j)) < extraerPuntuacion(entradasRanking.get(j + 1))) {
                    String entradaTemporal = entradasRanking.get(j);
                    entradasRanking.set(j, entradasRanking.get(j + 1));
                    entradasRanking.set(j + 1, entradaTemporal);
                }
            }
        }
    }


    /**
     * Extrae la puntuación numérica de una línea del ranking.
     *
     * @param entradaRanking línea con formato {@code nombre;puntuacion}
     * @return puntuación como {@code Integer}
     */
    public static Integer extraerPuntuacion(String entradaRanking) {
        return Integer.parseInt(entradaRanking.split(";")[1].trim());
    }


    /**
     * Extrae el nombre del jugador de una línea del ranking.
     *
     * @param entradaRanking línea con formato {@code nombre;puntuacion}
     * @return nombre del jugador
     */
    public static String extraerNombre(String entradaRanking) {
        return entradaRanking.split(";")[0].trim();
    }


    /**
     * Escribe en el fichero las tres primeras entradas de la lista
     * (que debe estar ya ordenada de mayor a menor).
     *
     * <p>Si la lista tiene menos de 3 elementos se guardan todos.</p>
     *
     * @param ficheroRanking  ruta al fichero donde guardar el ranking
     * @param entradasRanking lista ordenada de entradas del ranking
     * @throws IOException si no se puede escribir el fichero
     */
    public static void guardarTop3(Path ficheroRanking, ArrayList<String> entradasRanking) throws IOException {

        ArrayList<String> tresMejores = new ArrayList<>();

        for (int i = 0; i < entradasRanking.size() && i < 3; i++) {
            tresMejores.add(entradasRanking.get(i));
        }

        Files.write(ficheroRanking, tresMejores);
    }


    // -------------------------------------------------------------------------
    // HU10
    // -------------------------------------------------------------------------

    /**
     * Muestra el ranking de las tres mejores puntuaciones guardadas en
     * {@code resultados/mejores.txt} y se despide del jugador.
     *
     * <p>Si el fichero está vacío (nadie ha entrado en el top 3 todavía)
     * lo indica con un mensaje.</p>
     *
     * @throws IOException si el fichero de mejores no se puede leer
     */
    public static void mostrarRankingYDespedirse() throws IOException {

        Path ficheroMejores          = Paths.get("resultados/mejores.txt");
        ArrayList<String> ranking    = leerMejores(ficheroMejores);

        System.out.println("\n=== MEJORES PUNTUACIONES ===");

        if (ranking.isEmpty()) {
            System.out.println("Todavía no hay puntuaciones guardadas.");
        } else {
            for (int i = 0; i < ranking.size(); i++) {
                String nombreJugador      = extraerNombre(ranking.get(i));
                Integer puntuacionJugador = extraerPuntuacion(ranking.get(i));
                System.out.println((i + 1) + ". " + nombreJugador + " - " + puntuacionJugador + "/5 puntos");
            }
        }

        System.out.println("\n¡Gracias por jugar a Bulo o Realidad!");
        System.out.println("Recuerda contrastar siempre la información antes de compartirla.");
    }
}