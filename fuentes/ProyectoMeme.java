import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Juego "Bulo o Realidad".
 *
 * <p>El jugador ve un bulo y tiene que elegir qué dato real lo desmiente.
 * Se juegan 5 rondas y al final se guarda la puntuación si está entre
 * las tres mejores.</p>
 *
 * <p>Necesita en {@code datos/}: {@code memes.txt}, {@code realidades.json}
 * y {@code soluciones.xml}. Guarda el ranking en {@code resultados/mejores.txt}.</p>
 *
 * @author Equipo ProyectoMeme
 * @version 4.0
 */
public class ProyectoMeme {

    /** Bulos leídos de {@code memes.txt}. */
    static ArrayList<String> listaDeBulos = new ArrayList<>();

    /** Realidades leídas de {@code realidades.json}. */
    static ArrayList<String> listaDeRealidades = new ArrayList<>();

    /** Relaciona el índice de cada bulo con el índice de su realidad correcta. */
    static HashMap<Integer, Integer> mapaSoluciones = new HashMap<>();

    /** Bulos que ya han salido en esta partida para no repetirlos. */
    static ArrayList<Integer> bulosUsados = new ArrayList<>();

    /** Puntos conseguidos en la partida actual. */
    static int puntuacionFinal = 0;

    /** Scanner para leer lo que escribe el jugador. */
    static Scanner teclado = new Scanner(System.in);
    
    /** Ruta donde se encuentra la carpeta datos (detectada en HU1) */
    static Path rutaDatos = null;

    // ------------------------------------------------------------------

    /**
     * Arranca el programa y ejecuta el juego de principio a fin.
     *
     * @param args No se usan.
     * @throws Exception Si falla alguna lectura o escritura de fichero.
     */
    public static void main(String[] args) throws Exception {

        System.out.println("=== Bulo o Realidad ===");

        if (!existenFicheros()) {       // HU1
            System.out.println("El programa no puede continuar.");
            return;
        }

        prepararCarpetaResultados();    // HU2
        cargarBulos();                  // HU3
        cargarRealidades();             // HU4
        cargarSoluciones();

        jugarPartida();                 // HU5 + HU6 + HU7
        mostrarPuntuacion();            // HU8
        guardarSiEsTop3();              // HU9
        mostrarRanking();               // HU10

        teclado.close();
    }

    // ------------------------------------------------------------------
    // HU1 – Comprobar que existen los ficheros necesarios
    // ------------------------------------------------------------------

    /**
     * <b>HU1</b> – Comprueba que existe la carpeta {@code datos/} y los tres
     * ficheros necesarios. Informa por pantalla de cada uno.
     *
     * @return {@code true} si todo está bien; {@code false} si falta algo.
     */
    public static boolean existenFicheros() {
        // Intentar con diferentes rutas posibles
        Path[] rutasPosibles = {
            Paths.get("datos"),                    // desde raíz del proyecto
            Paths.get("../datos")                   // desde fuentes/ subiendo un nivel
        };
        
        for (Path ruta : rutasPosibles) {
            if (Files.isDirectory(ruta)) {
                rutaDatos = ruta;
                break;
            }
        }
        
        if (rutaDatos == null) {
            System.out.println("ERROR: no existe la carpeta 'datos'.");
            return false;
        }

        boolean todoOk = true;
        String[] ficheros = {"memes.txt", "realidades.json", "soluciones.xml"};
        
        for (String fichero : ficheros) {
            if (Files.exists(rutaDatos.resolve(fichero))) {
                System.out.println("OK: " + fichero);
            } else {
                System.out.println("ERROR: falta " + fichero);
                todoOk = false;
            }
        }

        return todoOk;
    }

    // ------------------------------------------------------------------
    // HU2 – Crear carpeta y fichero de resultados si no existen
    // ------------------------------------------------------------------

    /**
     * <b>HU2</b> – Crea la carpeta {@code resultados/} y el fichero
     * {@code mejores.txt} si todavía no existen.
     *
     * @throws IOException Si no se pueden crear.
     */
    public static void prepararCarpetaResultados() throws IOException {
        Files.createDirectories(Paths.get("resultados"));

        Path mejores = Paths.get("resultados/mejores.txt");
        if (!Files.exists(mejores)) {
            Files.createFile(mejores);
        }
    }

    // ------------------------------------------------------------------
    // HU3 – Leer los bulos de memes.txt
    // ------------------------------------------------------------------

    /**
     * <b>HU3</b> – Lee {@code datos/memes.txt} y guarda cada línea como un bulo.
     * Las líneas vacías se descartan.
     *
     * @throws IOException Si el fichero no puede leerse.
     */
    public static void cargarBulos() throws IOException {
        List<String> lineas = Files.readAllLines(rutaDatos.resolve("memes.txt"));

        for (String linea : lineas) {
            if (!linea.isBlank()) {
                listaDeBulos.add(linea);
            }
        }

        System.out.println("Bulos cargados: " + listaDeBulos.size());
    }

    // ------------------------------------------------------------------
    // HU4 – Leer las realidades de realidades.json
    // ------------------------------------------------------------------

    /**
     * <b>HU4</b> – Lee {@code datos/realidades.json} y extrae el campo
     * {@code "texto"} de cada objeto. El análisis es manual, línea a línea.
     *
     * @throws IOException Si el fichero no puede leerse.
     */
    public static void cargarRealidades() throws IOException {
        List<String> lineas = Files.readAllLines(rutaDatos.resolve("realidades.json"));

        for (String linea : lineas) {
            linea = linea.trim();
            if (!linea.contains("\"texto\"")) continue;

            int inicio = linea.indexOf("\"texto\"") + "\"texto\"".length();
            int abre = linea.indexOf('"', inicio + 1) + 1;
            int cierra = linea.indexOf('"', abre);
            
            if (abre > 0 && cierra > abre) {
                String texto = linea.substring(abre, cierra).trim();
                if (!texto.isBlank()) {
                    listaDeRealidades.add(texto);
                }
            }
        }

        System.out.println("Realidades cargadas: " + listaDeRealidades.size());
    }

    // ------------------------------------------------------------------
    // Leer las soluciones de soluciones.xml
    // ------------------------------------------------------------------

    /**
     * Lee {@code datos/soluciones.xml} y rellena el mapa
     * índice_bulo → índice_realidad correcta (ambos en base 0).
     *
     * @throws IOException Si el fichero no puede leerse.
     */
    public static void cargarSoluciones() throws IOException {
        List<String> lineas = Files.readAllLines(rutaDatos.resolve("soluciones.xml"));
        int contador = 0;

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.contains("<solucion")) {
                try {
                    Integer indiceBulo = leerAtributo(linea, "bulo");
                    Integer indiceRealidad = leerAtributo(linea, "realidad");
                    mapaSoluciones.put(indiceBulo, indiceRealidad);
                    contador++;
                } catch (NumberFormatException e) {
                    System.out.println("Advertencia: línea XML ignorada - " + linea);
                }
            }
        }

        System.out.println("Soluciones cargadas: " + contador);
    }

    /**
     * Extrae el valor entero de un atributo XML con formato {@code nombre="valor"}.
     * Versión más robusta que maneja errores de formato.
     *
     * @param linea  Línea XML donde buscar.
     * @param nombre Nombre del atributo.
     * @return Valor del atributo como {@code Integer}.
     * @throws NumberFormatException si no encuentra el atributo o el valor no es número
     */
    public static Integer leerAtributo(String linea, String nombre) {
        String busqueda = nombre + "=\"";
        int inicio = linea.indexOf(busqueda);
        
        if (inicio == -1) {
            throw new NumberFormatException("No se encontró el atributo: " + nombre);
        }
        
        inicio += busqueda.length();
        int fin = linea.indexOf('"', inicio);
        
        if (fin == -1) {
            throw new NumberFormatException("Formato incorrecto para atributo: " + nombre);
        }
        
        String valor = linea.substring(inicio, fin).trim();
        return Integer.parseInt(valor);
    }

    // ------------------------------------------------------------------
    // HU5 + HU6 + HU7 – Bucle principal del juego
    // ------------------------------------------------------------------

    /**
     * <b>HU7</b> – Juega las 5 rondas. En cada ronda muestra un bulo (HU5),
     * pide la respuesta (HU6) y actualiza el marcador.
     */
    public static void jugarPartida() {
        puntuacionFinal = 0;
        bulosUsados.clear();

        for (int ronda = 1; ronda <= 5; ronda++) {
            System.out.println("\n--- Ronda " + ronda + " de 5 ---");

            Integer indiceBulo = elegirBuloAlAzar();
            mostrarBuloYRealidades(indiceBulo);

            Integer respuesta = pedirRespuesta();

            if (respuesta.equals(mapaSoluciones.get(indiceBulo))) {
                System.out.println("¡Correcto!");
                puntuacionFinal++;
            } else {
                Integer correcta = mapaSoluciones.get(indiceBulo);
                if (correcta != null) {
                    System.out.println("Incorrecto. La correcta era la " + (correcta + 1) + ".");
                } else {
                    System.out.println("Incorrecto.");
                }
            }

            System.out.println("Marcador: " + puntuacionFinal + "/" + ronda);

            if (ronda < 5) {
                System.out.println("Pulsa ENTER para continuar...");
                teclado.nextLine();
            }
        }
    }

    /**
     * <b>HU5</b> – Elige al azar un índice de bulo que no haya salido todavía.
     *
     * @return Índice del bulo elegido.
     */
    public static Integer elegirBuloAlAzar() {
        Random azar = new Random();
        Integer indice = azar.nextInt(listaDeBulos.size());

        while (bulosUsados.contains(indice)) {
            indice = azar.nextInt(listaDeBulos.size());
        }

        bulosUsados.add(indice);
        return indice;
    }

    /**
     * <b>HU5</b> – Muestra el bulo y la lista numerada de realidades.
     *
     * @param indiceBulo Índice del bulo a mostrar.
     */
    public static void mostrarBuloYRealidades(Integer indiceBulo) {
        System.out.println("BULO: " + listaDeBulos.get(indiceBulo));
        System.out.println("¿Qué realidad lo desmiente?");

        for (int i = 0; i < listaDeRealidades.size(); i++) {
            System.out.println((i + 1) + ". " + listaDeRealidades.get(i));
        }
    }

    /**
     * <b>HU6</b> – Pide al jugador un número válido y lo devuelve en base 0.
     *
     * @return Índice de la realidad elegida (base 0).
     */
    public static Integer pedirRespuesta() {
        Integer opcion = 0;

        while (opcion < 1 || opcion > listaDeRealidades.size()) {
            System.out.print("Tu elección (1-" + listaDeRealidades.size() + "): ");
            try {
                String entrada = teclado.nextLine().trim();
                if (entrada.isEmpty()) continue;
                opcion = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduce un número válido.");
                opcion = 0;
            }
        }

        return opcion - 1;
    }

    // ------------------------------------------------------------------
    // HU8 – Mostrar la puntuación final
    // ------------------------------------------------------------------

    /**
     * <b>HU8</b> – Muestra los puntos obtenidos y un mensaje según el resultado.
     */
    public static void mostrarPuntuacion() {
        System.out.println("\n=== PUNTUACION FINAL: " + puntuacionFinal + "/5 ===");

        if (puntuacionFinal == 5) {
            System.out.println("¡Perfecto, todo correcto!");
        } else if (puntuacionFinal >= 3) {
            System.out.println("¡Bien, buena puntuación!");
        } else if (puntuacionFinal >= 1) {
            System.out.println("Puedes mejorar la próxima vez.");
        } else {
            System.out.println("Ánimo, la próxima será mejor.");
        }
    }

    // ------------------------------------------------------------------
    // HU9 – Guardar en el ranking si está entre los tres mejores
    // ------------------------------------------------------------------

    /**
     * <b>HU9</b> – Lee el ranking actual y, si la puntuación entra en el top 3,
     * pide el nombre al jugador y actualiza {@code resultados/mejores.txt}.
     *
     * @throws IOException Si el fichero no puede leerse o escribirse.
     */
    public static void guardarSiEsTop3() throws IOException {
        Path rankingPath = Paths.get("resultados/mejores.txt");
        
        // Leer el ranking actual
        ArrayList<String> ranking = new ArrayList<>(
            Files.readAllLines(rankingPath)
        );
        ranking.removeIf(String::isBlank);

        // Comprobar si entra en el top 3
        boolean esTop3 = false;
        
        if (ranking.size() < 3) {
            esTop3 = true;
        } else {
            // Obtener la puntuación más baja del top 3
            int puntuacionMasBaja = Integer.MAX_VALUE;
            for (String linea : ranking) {
                String[] partes = linea.split(";");
                if (partes.length == 2) {
                    try {
                        int pts = Integer.parseInt(partes[1].trim());
                        if (pts < puntuacionMasBaja) {
                            puntuacionMasBaja = pts;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar líneas mal formateadas
                    }
                }
            }
            
            if (puntuacionFinal > puntuacionMasBaja) {
                esTop3 = true;
            }
        }

        if (!esTop3) {
            System.out.println("No has entrado en el top 3. ¡Sigue intentándolo!");
            return;
        }

        // Pedir el nombre al jugador
        System.out.println("¡Enhorabuena! Estás entre los tres mejores.");
        System.out.print("Escribe tu nombre: ");
        String nombre = teclado.nextLine().trim();
        if (nombre.isBlank()) {
            nombre = "Anónimo";
        }

        // Añadir la nueva entrada
        ranking.add(nombre + ";" + puntuacionFinal);

        // Ordenar de mayor a menor puntuación
        ranking.sort((a, b) -> {
            try {
                int puntosA = Integer.parseInt(a.split(";")[1].trim());
                int puntosB = Integer.parseInt(b.split(";")[1].trim());
                return Integer.compare(puntosB, puntosA);
            } catch (Exception e) {
                return 0;
            }
        });

        // Quedarse solo con los tres primeros
        while (ranking.size() > 3) {
            ranking.remove(3);
        }

        // Escribir el fichero actualizado
        Files.write(rankingPath, ranking);
        System.out.println("Guardado. ¡Bien jugado, " + nombre + "!");
    }

    // ------------------------------------------------------------------
    // HU10 – Mostrar el ranking y despedirse
    // ------------------------------------------------------------------

    /**
     * <b>HU10</b> – Muestra el ranking de mejores puntuaciones y se despide.
     *
     * @throws IOException Si el fichero no puede leerse.
     */
    public static void mostrarRanking() throws IOException {
        System.out.println("\n=== TOP 3 ===");

        List<String> lineas = Files.readAllLines(Paths.get("resultados/mejores.txt"));
        int puesto = 1;

        for (String linea : lineas) {
            if (linea.isBlank()) continue;
            String[] partes = linea.trim().split(";");
            if (partes.length == 2) {
                System.out.println(puesto + ". " + partes[0].trim() + " - " + partes[1].trim() + "/5");
                puesto++;
            }
        }

        if (puesto == 1) {
            System.out.println("Todavía no hay puntuaciones.");
        }

        System.out.println("\n¡Gracias por jugar! Hasta la próxima!");
    }
}