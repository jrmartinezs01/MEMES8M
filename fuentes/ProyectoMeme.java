import java.nio.file.*;
import java.util.*;

/**
 * Clase principal del juego "Bulo o Realidad".
 * El jugador tiene que identificar que realidad desmiente cada bulo sobre igualdad de genero.
 * Se juegan 5 rondas y al final se guarda la puntuacion si esta entre las 3 mejores.
 *
 * @author Alumno
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

    /**
     * HU1 - Comprueba que existe el directorio datos y que contiene
     * los tres ficheros necesarios: memes.txt, realidades.json y soluciones.xml.
     * Si falta algo detiene el programa.
     *
     * @throws Exception si el directorio datos no existe
     */
    public static void hu1() throws Exception {
        Path rutaDirectorioDatos = Paths.get("datos");

        if (!Files.isDirectory(rutaDirectorioDatos))
            throw new Exception("No existe el directorio datos");

        List<Path> archivosEncontrados = Files.list(rutaDirectorioDatos).toList();
        List<String> nombresDeArchivosEncontrados = new ArrayList<>();
        for (Path archivoEncontrado : archivosEncontrados) {
            nombresDeArchivosEncontrados.add(archivoEncontrado.getFileName().toString());
        }

        Boolean todosLosArchivosExisten = true;
        if (!nombresDeArchivosEncontrados.contains("memes.txt")) {
            System.out.println("Falta el fichero: memes.txt");
            todosLosArchivosExisten = false;
        }
        if (!nombresDeArchivosEncontrados.contains("realidades.json")) {
            System.out.println("Falta el fichero: realidades.json");
            todosLosArchivosExisten = false;
        }
        if (!nombresDeArchivosEncontrados.contains("soluciones.xml")) {
            System.out.println("Falta el fichero: soluciones.xml");
            todosLosArchivosExisten = false;
        }

        if (!todosLosArchivosExisten) {
            System.out.println("Faltan ficheros, el programa se cierra.");
            System.exit(1);
        }
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
     * HU3 - Lee el fichero memes.txt y carga los textos de los bulos
     * en la lista listaDeBulos.
     * Formato de cada linea: "numeroDeId texto del bulo"
     *
     * @throws Exception si hay un error al leer el fichero
     */
    public static void hu3() throws Exception {
        List<String> lineasDelFicheroMemes = Files.readAllLines(Paths.get("datos/memes.txt"));
        for (String lineaActual : lineasDelFicheroMemes) {
            if (lineaActual.trim().isEmpty()) continue;
            Integer posicionDelPrimerEspacio = lineaActual.indexOf(' ');
            String textoDeBulo = lineaActual.substring(posicionDelPrimerEspacio + 1);
            listaDeBulos.add(textoDeBulo);
        }
    }

    /**
     * HU4 - Lee el fichero realidades.json y carga los textos de las realidades
     * en la lista listaDeRealidades. Solo guarda el campo "texto" de cada objeto.
     *
     * @throws Exception si hay un error al leer el fichero
     */
    public static void hu4() throws Exception {
        List<String> lineasDelFicheroRealidades = Files.readAllLines(Paths.get("datos/realidades.json"));
        for (String lineaActual : lineasDelFicheroRealidades) {
            lineaActual = lineaActual.trim();
            if (lineaActual.startsWith("\"texto\"")) {
                String textoDeRealidad = extraerValorJson(lineaActual);
                listaDeRealidades.add(textoDeRealidad);
            }
        }
    }

    /**
     * Lee el fichero soluciones.xml y carga los textos de las respuestas correctas
     * en la lista listaDeSoluciones.
     * El indice de cada solucion coincide con el indice de su bulo en listaDeBulos.
     *
     * @throws Exception si hay un error al leer el fichero
     */
    public static void cargarSoluciones() throws Exception {
        List<String> lineasDelFicheroSoluciones = Files.readAllLines(Paths.get("datos/soluciones.xml"));
        for (String lineaActual : lineasDelFicheroSoluciones) {
            lineaActual = lineaActual.trim();
            if (lineaActual.startsWith("<solucion>")) {
                String textoDeRespuestaCorrecta = lineaActual.replace("<solucion>", "").replace("</solucion>", "").trim();
                listaDeSoluciones.add(textoDeRespuestaCorrecta);
            }
        }
    }

    /**
     * HU5 + HU6 + HU7 - Contiene el bucle principal del juego.
     * Muestra 5 rondas con un bulo aleatorio sin repetir y una lista de realidades mezcladas.
     * El jugador elige la realidad correcta y suma un punto si acierta.
     * Al final de cada ronda muestra el marcador.
     *
     * @return los puntos totales conseguidos durante las 5 rondas
     */
    public static Integer jugar() {
        Integer puntosAcumulados = 0;
        ArrayList<Integer> indicesDeMemesYaUsados = new ArrayList<>();
        Random generadorAleatorio = new Random();

        for (Integer numeroDeRondaActual = 1; numeroDeRondaActual <= 5; numeroDeRondaActual++) {

            // elegir un bulo al azar que no se haya usado ya
            Integer indiceDelBuloElegido;
            do {
                indiceDelBuloElegido = generadorAleatorio.nextInt(listaDeBulos.size());
            } while (indicesDeMemesYaUsados.contains(indiceDelBuloElegido));
            indicesDeMemesYaUsados.add(indiceDelBuloElegido);

            System.out.println("\n--- Ronda " + numeroDeRondaActual + " de 5 | Puntos: " + puntosAcumulados + " ---");
            System.out.println("Bulo: " + listaDeBulos.get(indiceDelBuloElegido));
            System.out.println("\nElige la realidad que lo desmiente:");

            // mezclamos las realidades para que no salgan siempre en el mismo orden
            ArrayList<Integer> ordenAleatorioDeRealidades = new ArrayList<>();
            for (Integer indiceDeRealidad = 0; indiceDeRealidad < listaDeRealidades.size(); indiceDeRealidad++) {
                ordenAleatorioDeRealidades.add(indiceDeRealidad);
            }
            Collections.shuffle(ordenAleatorioDeRealidades);

            for (Integer posicionEnPantalla = 0; posicionEnPantalla < listaDeRealidades.size(); posicionEnPantalla++) {
                Integer indiceReal = ordenAleatorioDeRealidades.get(posicionEnPantalla);
                System.out.println((posicionEnPantalla + 1) + ". " + listaDeRealidades.get(indiceReal));
            }

            System.out.print("\nRespuesta: ");
            Integer numeroDeRespuestaDelUsuario = -1;
            try {
                numeroDeRespuestaDelUsuario = Integer.parseInt(lecturaDelTeclado.nextLine().trim());
            } catch (NumberFormatException errorDeFormato) {
                // si no mete un numero se trata como fallo
            }

            String textoDeRealidadElegida = "";
            if (numeroDeRespuestaDelUsuario >= 1 && numeroDeRespuestaDelUsuario <= listaDeRealidades.size()) {
                Integer indiceElegido = ordenAleatorioDeRealidades.get(numeroDeRespuestaDelUsuario - 1);
                textoDeRealidadElegida = listaDeRealidades.get(indiceElegido);
            }

            // la solucion correcta tiene el mismo indice que el bulo
            String textoDeRespuestaCorrecta = listaDeSoluciones.get(indiceDelBuloElegido);

            if (textoDeRealidadElegida.equals(textoDeRespuestaCorrecta)) {
                puntosAcumulados++;
                System.out.println("Correcto! +1 punto");
            } else {
                System.out.println("Incorrecto.");
                System.out.println("La respuesta era: " + textoDeRespuestaCorrecta);
            }

            System.out.println("Marcador: " + puntosAcumulados + "/" + numeroDeRondaActual);
        }

        return puntosAcumulados;
    }

    /**
     * HU8 - Muestra por pantalla la puntuacion final del jugador.
     *
     * @param puntosFinalesDelJugador puntos obtenidos al terminar las 5 rondas
     */
    public static void hu8(Integer puntosFinalesDelJugador) {
        System.out.println("\n=== Puntuacion final: " + puntosFinalesDelJugador + "/5 ===");
    }

    /**
     * HU9 - Comprueba si la puntuacion del jugador esta entre las 3 mejores.
     * Si es asi pide su nombre y guarda la puntuacion en mejores.txt.
     * El ranking se ordena de mayor a menor usando burbuja y se limita a 3 entradas.
     *
     * @param puntosFinalesDelJugador puntos obtenidos al terminar las 5 rondas
     * @throws Exception si hay un error al leer o escribir el fichero de ranking
     */
    public static void hu9(Integer puntosFinalesDelJugador) throws Exception {
        ArrayList<String> lineasDelRankingActual = leerRanking();

        // miramos si el jugador entra en el top 3
        Boolean elJugadorEntraEnElTop3 = lineasDelRankingActual.size() < 3;
        if (!elJugadorEntraEnElTop3) {
            for (String lineaDelRanking : lineasDelRankingActual) {
                Integer puntosDeEsaLinea = Integer.parseInt(lineaDelRanking.split(",")[1]);
                if (puntosFinalesDelJugador > puntosDeEsaLinea) {
                    elJugadorEntraEnElTop3 = true;
                    break;
                }
            }
        }

        if (elJugadorEntraEnElTop3) {
            System.out.print("Estas entre los 3 mejores! Escribe tu nombre: ");
            String nombreIntroducidoPorElJugador = lecturaDelTeclado.nextLine().trim();
            lineasDelRankingActual.add(nombreIntroducidoPorElJugador + "," + puntosFinalesDelJugador);

            // ordenar de mayor a menor puntuacion (burbuja simple)
            for (Integer i = 0; i < lineasDelRankingActual.size() - 1; i++) {
                for (Integer j = 0; j < lineasDelRankingActual.size() - 1 - i; j++) {
                    Integer puntosJ  = Integer.parseInt(lineasDelRankingActual.get(j).split(",")[1]);
                    Integer puntosJ1 = Integer.parseInt(lineasDelRankingActual.get(j + 1).split(",")[1]);
                    if (puntosJ < puntosJ1) {
                        String lineaAux = lineasDelRankingActual.get(j);
                        lineasDelRankingActual.set(j, lineasDelRankingActual.get(j + 1));
                        lineasDelRankingActual.set(j + 1, lineaAux);
                    }
                }
            }

            while (lineasDelRankingActual.size() > 3) lineasDelRankingActual.remove(3);

            guardarRanking(lineasDelRankingActual);
        }
    }

    /**
     * HU10 - Muestra la lista de las mejores puntuaciones guardadas y se despide.
     *
     * @throws Exception si hay un error al leer el fichero de ranking
     */
    public static void hu10() throws Exception {
        System.out.println("\n=== Mejores puntuaciones ===");
        ArrayList<String> lineasDelRankingGuardado = leerRanking();

        if (lineasDelRankingGuardado.isEmpty()) {
            System.out.println("Todavia no hay puntuaciones.");
        } else {
            for (Integer posicionEnRanking = 0; posicionEnRanking < lineasDelRankingGuardado.size(); posicionEnRanking++) {
                String[] partesDeLineaDeRanking = lineasDelRankingGuardado.get(posicionEnRanking).split(",");
                System.out.println((posicionEnRanking + 1) + ". " + partesDeLineaDeRanking[0] + " - " + partesDeLineaDeRanking[1] + "/5");
            }
        }
        System.out.println("\nHasta luego!");
    }

    /**
     * Extrae el valor de texto de una linea JSON con el formato: "clave": "valor"
     * Busca el texto que hay entre las comillas despues de los dos puntos.
     *
     * @param lineaDeJson linea del fichero JSON de la que extraer el valor
     * @return el texto del valor, o una cadena vacia si no se encuentra
     */
    public static String extraerValorJson(String lineaDeJson) {
        Integer posicionDeLosDosPuntos = lineaDeJson.indexOf(':');
        Integer posicionDeAperturaDeTexto = lineaDeJson.indexOf('"', posicionDeLosDosPuntos) + 1;
        Integer posicionDeCierreDeTexto = lineaDeJson.lastIndexOf('"');
        if (posicionDeAperturaDeTexto >= posicionDeCierreDeTexto) return "";
        return lineaDeJson.substring(posicionDeAperturaDeTexto, posicionDeCierreDeTexto);
    }

    /**
     * Lee el fichero resultados/mejores.txt y devuelve su contenido como lista de Strings.
     * Cada linea tiene el formato: "nombre,puntos"
     *
     * @return lista con las lineas del fichero de ranking, sin lineas vacias
     * @throws Exception si hay un error al leer el fichero
     */
    public static ArrayList<String> leerRanking() throws Exception {
        ArrayList<String> listaDeLineasDeRanking = new ArrayList<>();
        List<String> lineasDelFicheroMejores = Files.readAllLines(Paths.get("resultados/mejores.txt"));
        for (String lineaActualDeRanking : lineasDelFicheroMejores) {
            if (!lineaActualDeRanking.trim().isEmpty())
                listaDeLineasDeRanking.add(lineaActualDeRanking.trim());
        }
        return listaDeLineasDeRanking;
    }

    /**
     * Guarda la lista del ranking en el fichero resultados/mejores.txt sobreescribiendolo.
     * Cada elemento de la lista se escribe como una linea con formato "nombre,puntos"
     *
     * @param lineasDelRankingActualizado lista con las lineas a guardar
     * @throws Exception si hay un error al escribir el fichero
     */
    public static void guardarRanking(ArrayList<String> lineasDelRankingActualizado) throws Exception {
        Files.write(Paths.get("resultados/mejores.txt"), lineasDelRankingActualizado);
    }
}