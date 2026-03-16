import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Juego "Bulo o Realidad".
 *
 * <p>El jugador ve un bulo y tiene que elegir qué dato real lo desmiente.
 * Se juegan 5 rondas y al final se guarda la puntuación si está entre
 * las tres mejores de la historia.</p>
 *
 * <p>Ficheros necesarios en la carpeta {@code datos/}:</p>
 * <ul>
 *   <li>{@code memes.txt} – un bulo por línea</li>
 *   <li>{@code realidades.json} – objetos con campo {@code "texto"}</li>
 *   <li>{@code soluciones.xml} – elementos {@code <solucion bulo="X" realidad="Y"/>}</li>
 * </ul>
 *
 * <p>Los resultados se guardan en {@code resultados/mejores.txt}
 * con el formato {@code NOMBRE;PUNTOS} (TT7).</p>
 *
 * @author Equipo ProyectoMeme
 * @version 2.0
 */
public class ProyectoMeme {

    /** Bulos leídos de {@code datos/memes.txt}. Cada posición es un bulo distinto. */
    static ArrayList<String> bulos = new ArrayList<>();

    /** Realidades leídas de {@code datos/realidades.json}. */
    static ArrayList<String> realidades = new ArrayList<>();

    /**
     * Relaciona cada bulo con su realidad correcta.
     * Clave: índice del bulo. Valor: índice de la realidad (ambos base 0).
     */
    static HashMap<Integer, Integer> soluciones = new HashMap<>();

    /** Índices de bulos que ya han salido en esta partida, para no repetirlos. */
    static ArrayList<Integer> indicesBulosYaMostrados = new ArrayList<>();

    /** Puntos conseguidos al terminar la partida. Lo usan HU8, HU9 y HU10. */
    static Integer puntosFinales = 0;

    /** Lee lo que escribe el jugador por teclado. */
    static Scanner teclado = new Scanner(System.in);

    // ---------------------------------------------------------------
    // MAIN
    // ---------------------------------------------------------------

    /**
     * Arranca el programa y ejecuta todas las fases del juego en orden.
     *
     * @param args No se utilizan.
     * @throws Exception Si ocurre algún problema de lectura o escritura de ficheros.
     */
    public static void main(String[] args) throws Exception {

        System.out.println("=== Bulo o Realidad ===");

        // HU1 - Verificar que existen carpeta datos y sus tres ficheros
        if (!existenFicherosNecesarios()) {
            System.out.println("El programa no puede continuar.");
            return;
        }

        // HU2 - Crear carpeta resultados y mejores.txt si hacen falta
        crearCarpetaResultadosSiFaltara();

        // HU3 - Cargar los bulos desde memes.txt
        cargarBulos();

        // HU4 - Cargar las realidades desde realidades.json
        realidades = cargarRealidades("datos/realidades.json");
        System.out.println("Realidades cargadas: " + realidades.size());

        // Cargar el mapa bulo-realidad desde soluciones.xml
        soluciones = cargarSoluciones("datos/soluciones.xml");
        System.out.println("Soluciones cargadas: " + soluciones.size());

        // HU7 - Jugar las 5 rondas
        jugarPartida();

        // HU8 - Enseñar cuántos puntos ha sacado el jugador
        mostrarPuntuacionFinal();

        // HU9 - Guardar el nombre si está entre los tres mejores
        guardarEnRankingSiProcede();

        // HU10 - Mostrar el ranking y despedirse
        mostrarRankingYDespedirse();

        teclado.close();
    }

    // ---------------------------------------------------------------
    // HU1 - Comprobar que existen los ficheros de datos
    // ---------------------------------------------------------------

    /**
     * <b>HU1</b> – Comprueba que existe la carpeta {@code datos/} y los tres
     * ficheros que el juego necesita para funcionar.
     *
     * <p>Si falta algo informa por pantalla y devuelve {@code false}.</p>
     *
     * @return {@code true} si todo está en su sitio; {@code false} si falta algo.
     */
    public static Boolean existenFicherosNecesarios() {
        Path carpetaDatos = Paths.get("datos");

        if (!Files.exists(carpetaDatos) || !Files.isDirectory(carpetaDatos)) {
            System.out.println("ERROR: No se encuentra la carpeta 'datos'.");
            return false;
        }

        String[] nombresFicheros = {"memes.txt", "realidades.json", "soluciones.xml"};
        Boolean todoPresente = true;

        for (String nombreFichero : nombresFicheros) {
            Path rutaFichero = carpetaDatos.resolve(nombreFichero);
            if (!Files.exists(rutaFichero)) {
                System.out.println("ERROR: Falta el fichero '" + nombreFichero + "'.");
                todoPresente = false;
            } else {
                System.out.println("OK: " + nombreFichero);
            }
        }

        return todoPresente;
    }

    // ---------------------------------------------------------------
    // HU2 - Crear carpeta y fichero de resultados si no existen
    // ---------------------------------------------------------------

    /**
     * <b>HU2</b> – Crea la carpeta {@code resultados/} y el fichero
     * {@code mejores.txt} dentro de ella si todavía no existen.
     *
     * <p>Si ya existen no hace nada, así que es seguro llamarlo siempre al inicio.</p>
     *
     * @throws IOException Si el sistema no permite crear la carpeta o el fichero.
     */
    public static void crearCarpetaResultadosSiFaltara() throws IOException {
        Path carpetaResultados = Paths.get("resultados");
        Path ficheroMejores    = Paths.get("resultados/mejores.txt");

        if (!Files.exists(carpetaResultados)) {
            Files.createDirectories(carpetaResultados);
        }
        if (!Files.exists(ficheroMejores)) {
            Files.createFile(ficheroMejores);
        }
    }

    // ---------------------------------------------------------------
    // HU3 - Leer los bulos de memes.txt
    // ---------------------------------------------------------------

    /**
     * <b>HU3</b> – Lee {@code datos/memes.txt} línea a línea y guarda cada
     * bulo no vacío en la lista {@link #bulos}.
     *
     * <p>Formato del fichero (TT1): una frase por línea, sin separadores extra.</p>
     *
     * @throws IOException Si el fichero no puede leerse.
     */
    public static void cargarBulos() throws IOException {
        Path rutaMemes = Paths.get("datos/memes.txt");
        bulos = new ArrayList<>(Files.readAllLines(rutaMemes));
        bulos.removeIf(String::isBlank);
        System.out.println("Bulos cargados: " + bulos.size());
    }

    // ---------------------------------------------------------------
    // HU4 - Leer las realidades de realidades.json
    // ---------------------------------------------------------------

    /**
     * <b>HU4</b> – Lee el JSON de realidades y devuelve una lista con el texto
     * de cada una. El análisis es manual línea a línea (sin librería externa).
     *
     * <p>Formato del fichero (TT3):
     * <pre>
     * {
     *   "realidades": [
     *     {"id": 0, "texto": "El texto de la realidad..."},
     *     ...
     *   ]
     * }
     * </pre>
     * Solo se extrae el valor del campo {@code "texto"}.</p>
     *
     * @param rutaFichero Ruta al fichero JSON.
     * @return Lista con los textos de las realidades en el orden del fichero.
     * @throws IOException Si el fichero no puede leerse.
     */
    public static ArrayList<String> cargarRealidades(String rutaFichero) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get(rutaFichero));
        ArrayList<String> textos = new ArrayList<>();

        for (String linea : lineas) {
            linea = linea.trim();
            if (!linea.contains("\"texto\"")) continue;

            Integer posicionClave  = linea.indexOf("\"texto\"") + "\"texto\"".length();
            Integer abreComillas   = linea.indexOf('"', posicionClave + 1) + 1;
            Integer cierraComillas = linea.indexOf('"', abreComillas);
            String textoExtraido   = linea.substring(abreComillas, cierraComillas).trim();

            if (!textoExtraido.isBlank()) {
                textos.add(textoExtraido);
            }
        }

        return textos;
    }

    // ---------------------------------------------------------------
    // Leer soluciones de soluciones.xml
    // ---------------------------------------------------------------

    /**
     * Lee {@code soluciones.xml} y construye el mapa que relaciona
     * cada bulo con su realidad correcta (índices en base 0).
     *
     * <p>Formato del fichero (TT5):
     * <pre>
     * &lt;soluciones&gt;
     *   &lt;solucion bulo="0" realidad="2"/&gt;
     * &lt;/soluciones&gt;
     * </pre>
     * </p>
     *
     * @param rutaFichero Ruta al fichero XML.
     * @return Mapa con la correspondencia índice_bulo → índice_realidad.
     * @throws IOException Si el fichero no puede leerse.
     */
    public static HashMap<Integer, Integer> cargarSoluciones(String rutaFichero) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get(rutaFichero));
        HashMap<Integer, Integer> mapaResultado = new HashMap<>();

        for (String linea : lineas) {
            linea = linea.trim();
            if (!linea.startsWith("<solucion")) continue;

            String marcaBulo       = "bulo=\"";
            Integer inicioBulo     = linea.indexOf(marcaBulo) + marcaBulo.length();
            Integer finBulo        = linea.indexOf('"', inicioBulo);
            Integer indiceBulo     = Integer.parseInt(linea.substring(inicioBulo, finBulo));

            String marcaRealidad   = "realidad=\"";
            Integer inicioRealidad = linea.indexOf(marcaRealidad) + marcaRealidad.length();
            Integer finRealidad    = linea.indexOf('"', inicioRealidad);
            Integer indiceRealidad = Integer.parseInt(linea.substring(inicioRealidad, finRealidad));

            mapaResultado.put(indiceBulo, indiceRealidad);
        }

        return mapaResultado;
    }

    // ---------------------------------------------------------------
    // HU5 - Elegir un bulo al azar y mostrar las realidades
    // ---------------------------------------------------------------

    /**
     * <b>HU5</b> – Elige un bulo al azar que no haya salido antes en esta
     * partida y lo muestra junto con la lista numerada de realidades.
     *
     * <p>El índice elegido queda registrado en {@link #indicesBulosYaMostrados}
     * para evitar que se repita. Si ya se usaron todos devuelve {@code -1}.</p>
     *
     * @return Índice (base 0) del bulo mostrado, o {@code -1} si no quedan.
     */
    public static Integer elegirYMostrarBulo() {
        if (indicesBulosYaMostrados.size() >= bulos.size()) {
            System.out.println("No quedan bulos disponibles.");
            return -1;
        }

        Random aleatorio   = new Random();
        Integer indiceBulo = aleatorio.nextInt(bulos.size());

        while (indicesBulosYaMostrados.contains(indiceBulo)) {
            indiceBulo = aleatorio.nextInt(bulos.size());
        }
        indicesBulosYaMostrados.add(indiceBulo);

        System.out.println("\n=================================================");
        System.out.println("BULO: " + bulos.get(indiceBulo));
        System.out.println("\n¿Qué dato real desmiente este bulo?");
        System.out.println("-------------------------------------------------");

        for (int i = 0; i < realidades.size(); i++) {
            System.out.println((i + 1) + ". " + realidades.get(i));
        }

        System.out.println("=================================================");
        return indiceBulo;
    }

    // ---------------------------------------------------------------
    // HU6 - Leer la respuesta del jugador y comprobarla
    // ---------------------------------------------------------------

    /**
     * <b>HU6</b> – Pide al jugador que escriba el número de la realidad
     * que cree correcta. Repite la pregunta si el valor está fuera de rango
     * o no es un número.
     *
     * @param totalOpciones Cuántas realidades hay disponibles para elegir.
     * @return Índice elegido en base 0 (lo que escribió el jugador menos 1).
     */
    public static Integer pedirOpcionAlJugador(Integer totalOpciones) {
        Integer opcionElegida = null;

        while (opcionElegida == null) {
            System.out.print("Tu elección (1-" + totalOpciones + "): ");
            String texto = teclado.nextLine().trim();

            try {
                opcionElegida = Integer.parseInt(texto);
                if (opcionElegida < 1 || opcionElegida > totalOpciones) {
                    System.out.println("Ese número no está en la lista. Prueba entre 1 y " + totalOpciones + ".");
                    opcionElegida = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Eso no parece un número. Escribe solo el número de la opción.");
            }
        }

        return opcionElegida - 1; // pasar a base 0
    }

    /**
     * <b>HU6</b> – Mira en el mapa de soluciones si la opción elegida por
     * el jugador coincide con la realidad correcta para ese bulo.
     *
     * @param indiceBulo    Índice del bulo que está en pantalla.
     * @param opcionJugador Índice de la realidad que eligió el jugador (base 0).
     * @return {@code true} si acertó; {@code false} si falló o no hay solución.
     */
    public static Boolean esRespuestaCorrecta(Integer indiceBulo, Integer opcionJugador) {
        Integer realidadCorrecta = soluciones.get(indiceBulo);

        if (realidadCorrecta == null) {
            System.out.println("No hay solución guardada para este bulo.");
            return false;
        }

        return realidadCorrecta.equals(opcionJugador);
    }

    // ---------------------------------------------------------------
    // HU7 - Bucle de juego: 5 rondas con marcador
    // ---------------------------------------------------------------

    /**
     * <b>HU7</b> – Controla las 5 rondas de la partida. En cada ronda muestra
     * un bulo, recoge la respuesta del jugador, le dice si ha acertado y
     * actualiza el marcador. Al acabar guarda los puntos en {@link #puntosFinales}.
     */
    public static void jugarPartida() {
        Integer puntosAcumulados = 0;
        Integer rondasJugadas    = 0;

        indicesBulosYaMostrados.clear();

        while (rondasJugadas < 5) {
            System.out.println("\n=== RONDA " + (rondasJugadas + 1) + " DE 5 ===");

            Integer indiceBulo = elegirYMostrarBulo();

            if (indiceBulo != -1) {
                Integer opcionJugador = pedirOpcionAlJugador(realidades.size());
                Boolean acierto       = esRespuestaCorrecta(indiceBulo, opcionJugador);

                if (acierto) {
                    System.out.println("Correcto!");
                    puntosAcumulados++;
                } else {
                    Integer correcta = soluciones.get(indiceBulo);
                    if (correcta != null) {
                        System.out.println("Incorrecto. Era la opcion " + (correcta + 1) + ".");
                    } else {
                        System.out.println("Incorrecto.");
                    }
                }

                rondasJugadas++;
                mostrarMarcador(puntosAcumulados, rondasJugadas);
            }
        }

        puntosFinales = puntosAcumulados;
    }

    /**
     * Pinta el marcador tras cada ronda: puntos conseguidos, rondas jugadas
     * y rondas que quedan. Si no es la última ronda espera a que el jugador
     * pulse ENTER para continuar.
     *
     * @param puntosHastaAhora    Puntos acumulados hasta este momento.
     * @param rondasCompletadas   Número de rondas que se han jugado ya.
     */
    public static void mostrarMarcador(Integer puntosHastaAhora, Integer rondasCompletadas) {
        Integer rondasQueQuedan = 5 - rondasCompletadas;

        System.out.println("\n+----------------------------+");
        System.out.println("|         MARCADOR           |");
        System.out.println("+----------------------------+");
        System.out.printf( "|  Puntos: %d de %d           |%n", puntosHastaAhora, rondasCompletadas);
        System.out.printf( "|  Rondas restantes: %d       |%n", rondasQueQuedan);
        System.out.println("+----------------------------+");

        if (rondasCompletadas < 5) {
            System.out.println("\nPulsa ENTER para seguir...");
            teclado.nextLine();
        }
    }

    // ---------------------------------------------------------------
    // HU8 - Mostrar la puntuación final
    // ---------------------------------------------------------------

    /**
     * <b>HU8</b> – Muestra al jugador cuántos puntos ha sacado, un mensaje
     * según cómo le fue y el porcentaje de aciertos sobre 5 rondas.
     *
     * <p>Criterios del mensaje:</p>
     * <ul>
     *   <li>5 puntos → perfecto</li>
     *   <li>3 o 4 → buen resultado</li>
     *   <li>1 o 2 → puede mejorar</li>
     *   <li>0 → ánimo</li>
     * </ul>
     */
    public static void mostrarPuntuacionFinal() {
        System.out.println("\n+------------------------------------+");
        System.out.println("|        PUNTUACION FINAL            |");
        System.out.println("+------------------------------------+");
        System.out.printf( "|          %d / 5 puntos             |%n", puntosFinales);
        System.out.println("+------------------------------------+");

        if (puntosFinales == 5) {
            System.out.println("|  Perfecto, has acertado todo!      |");
        } else if (puntosFinales >= 3) {
            System.out.println("|  Bien, buena puntuacion.           |");
        } else if (puntosFinales >= 1) {
            System.out.println("|  Puedes mejorar la proxima vez.    |");
        } else {
            System.out.println("|  Animo, la proxima sera mejor.     |");
        }

        System.out.println("+------------------------------------+");

        Double porcentaje = (puntosFinales * 100.0) / 5.0;
        System.out.printf("%nPorcentaje de aciertos: %.1f%%%n", porcentaje);
    }

    // ---------------------------------------------------------------
    // HU9 - Guardar la puntuación si está entre las tres mejores
    // ---------------------------------------------------------------

    /**
     * <b>HU9</b> – Lee el ranking actual y, si la puntuación de esta partida
     * mejora alguna de las tres primeras posiciones, pide el nombre al jugador
     * y actualiza el fichero {@code resultados/mejores.txt}.
     *
     * <p>El fichero sigue el formato TT7: cada línea tiene {@code NOMBRE;PUNTOS}.
     * Solo se conservan los tres mejores registros.</p>
     *
     * @throws IOException Si el fichero de ranking no puede leerse o escribirse.
     */
    public static void guardarEnRankingSiProcede() throws IOException {
        Path ficheroRanking  = Paths.get("resultados/mejores.txt");
        List<String> lineas  = Files.readAllLines(ficheroRanking);

        ArrayList<String>  nombresGuardados = new ArrayList<>();
        ArrayList<Integer> puntosGuardados  = new ArrayList<>();

        // Parsear las entradas existentes del fichero
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isBlank() || !linea.contains(";")) continue;

            String[] partes = linea.split(";");
            if (partes.length != 2) continue;

            try {
                nombresGuardados.add(partes[0].trim());
                puntosGuardados.add(Integer.parseInt(partes[1].trim()));
            } catch (NumberFormatException e) {
                // línea mal formada, se ignora
            }
        }

        // Ver si la puntuación de ahora entra en el top 3
        Boolean hayMenosDeTresPuestos = nombresGuardados.size() < 3;
        Boolean superaAlTercero       = !hayMenosDeTresPuestos
                                        && puntosFinales > puntosGuardados.get(2);

        if (!hayMenosDeTresPuestos && !superaAlTercero) {
            System.out.println("\nNo has entrado en el top 3. Sigue intentandolo!");
            return;
        }

        // Pedir el nombre del jugador
        System.out.println("\nEnhorabuena! Tu puntuacion esta entre las tres mejores.");
        System.out.print("Escribe tu nombre: ");
        String nombreJugador = teclado.nextLine().trim();
        if (nombreJugador.isBlank()) {
            nombreJugador = "Anonimo";
        }

        // Añadir la nueva entrada
        nombresGuardados.add(nombreJugador);
        puntosGuardados.add(puntosFinales);

        // Ordenación burbuja descendente (simple, sin Comparator)
        for (int i = 0; i < puntosGuardados.size() - 1; i++) {
            for (int j = 0; j < puntosGuardados.size() - 1 - i; j++) {
                if (puntosGuardados.get(j) < puntosGuardados.get(j + 1)) {
                    Integer auxPuntos = puntosGuardados.get(j);
                    puntosGuardados.set(j, puntosGuardados.get(j + 1));
                    puntosGuardados.set(j + 1, auxPuntos);

                    String auxNombre = nombresGuardados.get(j);
                    nombresGuardados.set(j, nombresGuardados.get(j + 1));
                    nombresGuardados.set(j + 1, auxNombre);
                }
            }
        }

        // Quedarse solo con los tres primeros
        while (nombresGuardados.size() > 3) {
            nombresGuardados.remove(nombresGuardados.size() - 1);
            puntosGuardados.remove(puntosGuardados.size() - 1);
        }

        // Escribir el fichero actualizado
        StringBuilder contenido = new StringBuilder();
        for (int i = 0; i < nombresGuardados.size(); i++) {
            contenido.append(nombresGuardados.get(i))
                     .append(";")
                     .append(puntosGuardados.get(i))
                     .append("\n");
        }
        Files.writeString(ficheroRanking, contenido.toString());

        System.out.println("Puntuacion registrada. Buen trabajo, " + nombreJugador + "!");
    }

    // ---------------------------------------------------------------
    // HU10 - Mostrar el ranking y despedirse
    // ---------------------------------------------------------------

    /**
     * <b>HU10</b> – Lee el fichero {@code resultados/mejores.txt} y muestra
     * el ranking de las mejores puntuaciones con el nombre de cada jugador.
     * Después se despide.
     *
     * <p>Si el fichero está vacío o no tiene entradas válidas lo indica.</p>
     *
     * @throws IOException Si el fichero de ranking no puede leerse.
     */
    public static void mostrarRankingYDespedirse() throws IOException {
        Path ficheroRanking = Paths.get("resultados/mejores.txt");
        List<String> lineas = Files.readAllLines(ficheroRanking);

        System.out.println("\n+--------------------------------------+");
        System.out.println("|       MEJORES PUNTUACIONES           |");
        System.out.println("+--------------------------------------+");

        Integer puesto      = 1;
        Boolean hayEntradas = false;

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isBlank() || !linea.contains(";")) continue;

            String[] partes = linea.split(";");
            if (partes.length != 2) continue;

            System.out.printf("|  %d. %-23s %s/5  |%n",
                    puesto, partes[0].trim(), partes[1].trim());

            puesto++;
            hayEntradas = true;
        }

        if (!hayEntradas) {
            System.out.println("|  Todavia no hay puntuaciones.        |");
        }

        System.out.println("+--------------------------------------+");
        System.out.println("\nGracias por jugar. Hasta la proxima!");
    }
}