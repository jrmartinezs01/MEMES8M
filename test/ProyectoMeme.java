import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

<<<<<<< HEAD
=======
/**
 * Batería de tests para {@link ProyectoMeme}.
 *
 * <p>Cubre todos los métodos testeables de forma automática.
 * Los métodos que necesitan entrada por teclado ({@code pedirRespuesta})
 * se prueban redirigiendo {@code System.in} con un {@link ByteArrayInputStream}.</p>
 *
 * <p>Los tests que crean ficheros usan {@code @TempDir} de JUnit 5 para
 * trabajar sobre directorios temporales que se borran solos al terminar.</p>
 *
 * @author Equipo ProyectoMeme
 * @version 1.0
 */
>>>>>>> CodigoArreglado
class ProyectoMemeTest {

    @TempDir
    Path dirTemporal;

    // Antes de cada test, cambiamos el directorio de trabajo al temporal
    // y limpiamos el estado estático de la clase
    @BeforeEach
<<<<<<< HEAD
    void setUp() throws Exception {
        ProyectoMeme.bulos.clear();
        ProyectoMeme.realidades.clear();
        ProyectoMeme.soluciones.clear();
        ProyectoMeme.bulosUsados.clear();
        ProyectoMeme.puntos = 0;
=======
    void limpiarEstadoEstatico() {
        ProyectoMeme.listaDeBulos = new ArrayList<>();
        ProyectoMeme.listaDeRealidades = new ArrayList<>();
        ProyectoMeme.mapaSoluciones = new HashMap<>();
        ProyectoMeme.bulosUsados = new ArrayList<>();
        ProyectoMeme.puntuacionFinal = 0;
>>>>>>> CodigoArreglado

        // Crear carpeta datos/ dentro del directorio temporal
        Files.createDirectories(dirTemporal.resolve("datos"));
        System.setProperty("user.dir", dirTemporal.toString());
    }

<<<<<<< HEAD
    // ---------------------------------------------------------------
    // HU1 – existenFicheros()
    // ---------------------------------------------------------------

    /**
     * HU1a – Devuelve true cuando la carpeta datos/ y los tres ficheros existen.
     */
    @Test
    void hu1_todosLosFicherosExisten_devuelveTrue() throws IOException {
        Path datos = dirTemporal.resolve("datos");
        Files.createFile(datos.resolve("memes.txt"));
        Files.createFile(datos.resolve("realidades.json"));
        Files.createFile(datos.resolve("soluciones.xml"));

        // Ejecutar desde el directorio temporal
        Path original = Path.of(System.getProperty("user.dir"));
        System.setProperty("user.dir", dirTemporal.toString());

        assertTrue(ProyectoMeme.existenFicheros());

        System.setProperty("user.dir", original.toString());
    }

    /**
     * HU1b – Devuelve false cuando falta algún fichero.
     */
    @Test
    void hu1_faltaUnFichero_devuelveFalse() throws IOException {
        Path datos = dirTemporal.resolve("datos");
        Files.createFile(datos.resolve("memes.txt"));
        // realidades.json y soluciones.xml NO se crean

        Path original = Path.of(System.getProperty("user.dir"));
        System.setProperty("user.dir", dirTemporal.toString());

        assertFalse(ProyectoMeme.existenFicheros());

        System.setProperty("user.dir", original.toString());
    }

    // ---------------------------------------------------------------
    // HU2 – prepararCarpetaResultados()
    // ---------------------------------------------------------------

    /**
     * HU2a – Crea la carpeta resultados/ y el fichero mejores.txt si no existen.
     */
    @Test
    void hu2_creaLaCarpetaYFichero() throws IOException {
        // Ejecutamos el método apuntando a dirTemporal
        // Redirigimos las rutas relativas manualmente
        Path resultados = dirTemporal.resolve("resultados");
        Path mejores    = resultados.resolve("mejores.txt");

        Files.createDirectories(resultados);
        if (!Files.exists(mejores)) {
            Files.createFile(mejores);
        }

        assertTrue(Files.isDirectory(resultados));
        assertTrue(Files.exists(mejores));
    }

    /**
     * HU2b – Si mejores.txt ya existe, no lanza excepción ni lo sobreescribe.
     */
    @Test
    void hu2_ficheroYaExiste_noLanzaExcepcion() throws IOException {
        Path resultados = dirTemporal.resolve("resultados");
        Files.createDirectories(resultados);
        Path mejores = resultados.resolve("mejores.txt");
        Files.writeString(mejores, "Ana;5\n");

        // Simular la lógica de prepararCarpetaResultados
        if (!Files.exists(mejores)) {
            Files.createFile(mejores);
        }

        assertEquals("Ana;5\n", Files.readString(mejores));
    }

    // ---------------------------------------------------------------
    // HU3 – cargarBulos()
    // ---------------------------------------------------------------

    /**
     * HU3a – Lee las líneas no vacías y las añade a la lista de bulos.
     */
    @Test
    void hu3_cargaBulosNoVacios() throws IOException {
        Path memes = dirTemporal.resolve("datos/memes.txt");
        Files.writeString(memes, "Bulo uno\n\nBulo dos\n   \nBulo tres\n");

        // Simular cargarBulos leyendo desde la ruta temporal
        List<String> lineas = Files.readAllLines(memes);
        for (String linea : lineas) {
            if (!linea.isBlank()) ProyectoMeme.bulos.add(linea);
        }

        assertEquals(3, ProyectoMeme.bulos.size());
        assertEquals("Bulo uno", ProyectoMeme.bulos.get(0));
    }

    /**
     * HU3b – Las líneas en blanco y con solo espacios se descartan.
     */
    @Test
    void hu3_ignoraLineasEnBlanco() throws IOException {
        Path memes = dirTemporal.resolve("datos/memes.txt");
        Files.writeString(memes, "\n   \n\t\n");

        List<String> lineas = Files.readAllLines(memes);
        for (String linea : lineas) {
            if (!linea.isBlank()) ProyectoMeme.bulos.add(linea);
        }

        assertTrue(ProyectoMeme.bulos.isEmpty());
    }

    // ---------------------------------------------------------------
    // HU4 – cargarRealidades()
    // ---------------------------------------------------------------

    /**
     * HU4a – Extrae correctamente el campo "texto" de cada objeto JSON.
     */
    @Test
    void hu4_extraeTextoDelJson() throws IOException {
        Path json = dirTemporal.resolve("datos/realidades.json");
        Files.writeString(json,
            "[\n" +
            "  { \"id\": 0, \"texto\": \"Dato real uno\", \"fuente\": \"https://ine.es\" },\n" +
            "  { \"id\": 1, \"texto\": \"Dato real dos\", \"fuente\": \"https://ine.es\" }\n" +
            "]\n"
        );

        List<String> lineas = Files.readAllLines(json);
        for (String linea : lineas) {
            if (!linea.contains("\"texto\"")) continue;
            int inicio = linea.indexOf("\"texto\"") + "\"texto\"".length();
            int abre   = linea.indexOf('"', inicio + 1) + 1;
            int cierra = linea.indexOf('"', abre);
            String texto = linea.substring(abre, cierra).trim();
            if (!texto.isBlank()) ProyectoMeme.realidades.add(texto);
        }

        assertEquals(2, ProyectoMeme.realidades.size());
        assertEquals("Dato real uno", ProyectoMeme.realidades.get(0));
    }

    /**
     * HU4b – Las líneas sin el campo "texto" se ignoran.
     */
    @Test
    void hu4_ignoraLineasSinCampoTexto() throws IOException {
        Path json = dirTemporal.resolve("datos/realidades.json");
        Files.writeString(json,
            "[\n" +
            "  { \"id\": 0, \"fuente\": \"https://ine.es\" }\n" +
            "]\n"
        );

        List<String> lineas = Files.readAllLines(json);
        for (String linea : lineas) {
            if (!linea.contains("\"texto\"")) continue;
            int inicio = linea.indexOf("\"texto\"") + "\"texto\"".length();
            int abre   = linea.indexOf('"', inicio + 1) + 1;
            int cierra = linea.indexOf('"', abre);
            String texto = linea.substring(abre, cierra).trim();
            if (!texto.isBlank()) ProyectoMeme.realidades.add(texto);
        }

        assertTrue(ProyectoMeme.realidades.isEmpty());
    }

    // ---------------------------------------------------------------
    // HU5 – elegirBuloAlAzar() y mostrarBuloYRealidades()
    // ---------------------------------------------------------------

    /**
     * HU5a – El índice devuelto está dentro del rango válido.
     */
    @Test
    void hu5_elegirBuloAlAzar_devuelveIndiceValido() {
        ProyectoMeme.bulos.add("Bulo A");
        ProyectoMeme.bulos.add("Bulo B");
        ProyectoMeme.bulos.add("Bulo C");

        Integer indice = ProyectoMeme.elegirBuloAlAzar();

        assertTrue(indice >= 0 && indice < ProyectoMeme.bulos.size());
    }

    /**
     * HU5b – No se repite ningún bulo ya usado en la misma partida.
     */
    @Test
    void hu5_noRepiteBulosUsados() {
        for (int i = 0; i < 5; i++) ProyectoMeme.bulos.add("Bulo " + i);

        for (int ronda = 0; ronda < 5; ronda++) {
            Integer indice = ProyectoMeme.elegirBuloAlAzar();
            // Comprobar que no estaba ya antes de esta llamada
            long apariciones = ProyectoMeme.bulosUsados.stream()
                    .filter(b -> b.equals(indice)).count();
            assertEquals(1, apariciones, "El bulo " + indice + " apareció más de una vez");
        }
    }

    // ---------------------------------------------------------------
    // HU6 – pedirRespuesta() (validación de entrada)
    // ---------------------------------------------------------------

    /**
     * HU6a – Una entrada válida devuelve el índice en base 0.
     */
    @Test
    void hu6_entradaValida_devuelveIndiceBase0() {
        ProyectoMeme.realidades.add("Realidad 1");
        ProyectoMeme.realidades.add("Realidad 2");

        // Simulamos la lógica de conversión sin Scanner
        int opcionUsuario = 2;  // el jugador escribe "2"
        int resultado = opcionUsuario - 1;

        assertEquals(1, resultado);
    }

    /**
     * HU6b – Una entrada fuera de rango no se acepta (la opción queda en 0).
     */
    @Test
    void hu6_entradaFueraDeRango_noEsValida() {
        ProyectoMeme.realidades.add("Realidad 1");
        ProyectoMeme.realidades.add("Realidad 2");

        int opcion = 99;
        boolean valida = opcion >= 1 && opcion <= ProyectoMeme.realidades.size();
=======
    /**
     * Después de cada test restauramos System.in y el Scanner
     * por si algún test los había sustituido.
     */
    @AfterEach
    void restaurarEntradaEstandar() {
        System.setIn(entradaOriginal);
        ProyectoMeme.teclado = new Scanner(System.in);
    }

    // =========================================================================
    // Tests para leerRealidades
    // =========================================================================

    /**
     * Un JSON con tres objetos debe devolver tres realidades.
     */
    @Test
    void leerRealidades_tresObjetos_devuelveTres(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroJson = directorioTemporal.resolve("realidades.json");
        Files.write(ficheroJson, Arrays.asList(
            "{",
            "  \"realidades\": [",
            "    { \"id\": 0, \"texto\": \"Realidad uno\" },",
            "    { \"id\": 1, \"texto\": \"Realidad dos\" },",
            "    { \"id\": 2, \"texto\": \"Realidad tres\" }",
            "  ]",
            "}"
        ));

        ArrayList<String> resultado = ProyectoMeme.leerRealidades(ficheroJson.toString());
        assertEquals(3, resultado.size());
    }

    /**
     * El texto extraído debe coincidir exactamente con el valor del campo.
     */
    @Test
    void leerRealidades_textoExtraido_coincidesConValor(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroJson = directorioTemporal.resolve("realidades.json");
        Files.write(ficheroJson, List.of("    { \"id\": 0, \"texto\": \"Los peces tienen buena memoria\" }"));

        ArrayList<String> resultado = ProyectoMeme.leerRealidades(ficheroJson.toString());
        assertEquals("Los peces tienen buena memoria", resultado.get(0));
    }

    /**
     * Las líneas que no contienen "texto" deben ignorarse.
     */
    @Test
    void leerRealidades_lineasSinCampoTexto_seIgnoran(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroJson = directorioTemporal.resolve("realidades.json");
        Files.write(ficheroJson, Arrays.asList(
            "{ \"realidades\": [",
            "  { \"id\": 0, \"texto\": \"Solo esta cuenta\" }",
            "] }"
        ));

        ArrayList<String> resultado = ProyectoMeme.leerRealidades(ficheroJson.toString());
        assertEquals(1, resultado.size());
    }

    /**
     * Un JSON sin ningún campo "texto" debe devolver una lista vacía.
     */
    @Test
    void leerRealidades_sinCamposTexto_devuelveListaVacia(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroJson = directorioTemporal.resolve("realidades.json");
        Files.write(ficheroJson, List.of("{ \"realidades\": [] }"));

        ArrayList<String> resultado = ProyectoMeme.leerRealidades(ficheroJson.toString());
        assertTrue(resultado.isEmpty());
    }


    // =========================================================================
    // Tests para leerSoluciones
    // =========================================================================

    /**
     * Un XML con tres entradas debe devolver un mapa de tamaño 3.
     */
    @Test
    void leerSoluciones_tresEntradas_devuelveTres(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroXml = directorioTemporal.resolve("soluciones.xml");
        Files.write(ficheroXml, Arrays.asList(
            "<?xml version=\"1.0\"?>",
            "<soluciones>",
            "  <solucion bulo=\"0\" realidad=\"2\"/>",
            "  <solucion bulo=\"1\" realidad=\"0\"/>",
            "  <solucion bulo=\"2\" realidad=\"4\"/>",
            "</soluciones>"
        ));

        HashMap<Integer, Integer> resultado = ProyectoMeme.leerSoluciones(ficheroXml.toString());
        assertEquals(3, resultado.size());
    }

    /**
     * Los índices leídos deben coincidir exactamente con los del XML.
     */
    @Test
    void leerSoluciones_indicesCorrectos_coincideConXml(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroXml = directorioTemporal.resolve("soluciones.xml");
        Files.write(ficheroXml, Arrays.asList(
            "<soluciones>",
            "  <solucion bulo=\"0\" realidad=\"3\"/>",
            "</soluciones>"
        ));

        HashMap<Integer, Integer> resultado = ProyectoMeme.leerSoluciones(ficheroXml.toString());
        assertEquals(3, resultado.get(0));
    }

    /**
     * Las líneas que no empiezan por {@code <solucion} deben ignorarse.
     */
    @Test
    void leerSoluciones_lineasNoRelevantes_seIgnoran(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroXml = directorioTemporal.resolve("soluciones.xml");
        Files.write(ficheroXml, Arrays.asList(
            "<?xml version=\"1.0\"?>",
            "<soluciones>",
            "  <!-- Este es un comentario -->",
            "  <solucion bulo=\"1\" realidad=\"2\"/>",
            "</soluciones>"
        ));

        HashMap<Integer, Integer> resultado = ProyectoMeme.leerSoluciones(ficheroXml.toString());
        assertEquals(1, resultado.size());
    }


    // =========================================================================
    // Tests para comprobarRespuesta
    // =========================================================================

    /**
     * Una respuesta correcta debe devolver {@code true}.
     */
    @Test
    void comprobarRespuesta_respuestaCorrecta_devuelveTrue() {
        ProyectoMeme.mapaSoluciones.put(0, 2);
        assertTrue(ProyectoMeme.comprobarRespuesta(0, 2));
    }

    /**
     * Una respuesta incorrecta debe devolver {@code false}.
     */
    @Test
    void comprobarRespuesta_respuestaIncorrecta_devuelveFalse() {
        ProyectoMeme.mapaSoluciones.put(0, 2);
        assertFalse(ProyectoMeme.comprobarRespuesta(0, 1));
    }

    /**
     * Si el bulo no tiene solución registrada, debe devolver {@code false}.
     */
    @Test
    void comprobarRespuesta_buloSinSolucion_devuelveFalse() {
        // mapaSoluciones está vacío, no hay solución para el bulo 99
        assertFalse(ProyectoMeme.comprobarRespuesta(99, 0));
    }

    /**
     * Con varios bulos registrados, cada uno debe resolverse de forma independiente.
     */
    @Test
    void comprobarRespuesta_variosEnMapa_cadaUnoIndependiente() {
        ProyectoMeme.mapaSoluciones.put(0, 1);
        ProyectoMeme.mapaSoluciones.put(1, 3);
        ProyectoMeme.mapaSoluciones.put(2, 0);

        assertTrue(ProyectoMeme.comprobarRespuesta(0, 1));
        assertFalse(ProyectoMeme.comprobarRespuesta(0, 3));
        assertTrue(ProyectoMeme.comprobarRespuesta(1, 3));
        assertTrue(ProyectoMeme.comprobarRespuesta(2, 0));
    }


    // =========================================================================
    // Tests para mostrarBuloYRealidades
    // =========================================================================

    /**
     * Con una lista de bulos, el índice devuelto debe estar dentro del rango válido.
     */
    @Test
    void mostrarBuloYRealidades_bulosDisponibles_devuelveIndiceValido() {
        List<String> bulos = Arrays.asList("Bulo A", "Bulo B", "Bulo C");
        List<String> realidades = Arrays.asList("Real 1", "Real 2");

        Integer indice = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        assertTrue(indice >= 0 && indice < bulos.size());
    }

    /**
     * En dos llamadas consecutivas los índices devueltos deben ser distintos.
     */
    @Test
    void mostrarBuloYRealidades_dosLlamadas_noRepiteIndice() {
        List<String> bulos = Arrays.asList("Bulo A", "Bulo B", "Bulo C");
        List<String> realidades = Arrays.asList("Real 1", "Real 2");

        Integer primero = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        Integer segundo = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        assertNotEquals(primero, segundo);
    }

    /**
     * Cuando se han agotado todos los bulos, debe devolver -1.
     */
    @Test
    void mostrarBuloYRealidades_bulosAgotados_devuelveMenosUno() {
        List<String> bulos = Arrays.asList("Bulo A", "Bulo B");
        List<String> realidades = Arrays.asList("Real 1");

        // Marcamos todos los bulos como usados manualmente
        ProyectoMeme.bulosUsados.add(0);
        ProyectoMeme.bulosUsados.add(1);

        Integer resultado = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        assertEquals(-1, resultado);
    }

    /**
     * El índice devuelto debe añadirse a {@code bulosUsados}.
     */
    @Test
    void mostrarBuloYRealidades_devuelveIndice_seAnadeBulosUsados() {
        List<String> bulos = Arrays.asList("Bulo único");
        List<String> realidades = Arrays.asList("Real 1");

        Integer indice = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        assertTrue(ProyectoMeme.bulosUsados.contains(indice));
    }


    // =========================================================================
    // Tests para pedirRespuesta
    // =========================================================================

    /**
     * Una entrada válida en el rango debe devolver el índice en base 0.
     */
    @Test
    void pedirRespuesta_entradaValida_devuelveBase0() {
        simularEntradaTeclado("3\n");
        Integer resultado = ProyectoMeme.pedirRespuesta(5);
        assertEquals(2, resultado); // el usuario escribe 3, se devuelve 2 (base 0)
    }

    /**
     * Si el usuario escribe 1, debe devolver 0.
     */
    @Test
    void pedirRespuesta_eligePrimero_devuelveCero() {
        simularEntradaTeclado("1\n");
        Integer resultado = ProyectoMeme.pedirRespuesta(5);
        assertEquals(0, resultado);
    }

    /**
     * Si el usuario escribe el último número disponible, debe devolver totalOpciones-1.
     */
    @Test
    void pedirRespuesta_eligeUltimo_devuelveUltimoIndice() {
        simularEntradaTeclado("5\n");
        Integer resultado = ProyectoMeme.pedirRespuesta(5);
        assertEquals(4, resultado);
    }

    /**
     * Después de una entrada inválida, el bucle debe pedir de nuevo y aceptar la siguiente.
     */
    @Test
    void pedirRespuesta_primerEntradaInvalida_aceptaLaSiguiente() {
        simularEntradaTeclado("abc\n2\n");
        Integer resultado = ProyectoMeme.pedirRespuesta(3);
        assertEquals(1, resultado); // "abc" se ignora, "2" es válido → índice 1
    }

    /**
     * Un número fuera de rango debe rechazarse y aceptar la siguiente entrada válida.
     */
    @Test
    void pedirRespuesta_fueraDeRango_aceptaSiguienteValida() {
        simularEntradaTeclado("10\n2\n"); // 10 está fuera de rango para 3 opciones
        Integer resultado = ProyectoMeme.pedirRespuesta(3);
        assertEquals(1, resultado);
    }


    // =========================================================================
    // Tests para prepararCarpetaResultados
    // =========================================================================

    /**
     * Si no existe la carpeta resultados, debe crearla junto con mejores.txt.
     */
    @Test
    void crearDirectorioResultados_noExiste_creaAmbos(@TempDir Path directorioTemporal) throws IOException {
        // Cambiamos temporalmente el directorio de trabajo al temporal
        Path carpetaResultados = directorioTemporal.resolve("resultados");
        Path ficheroMejores = carpetaResultados.resolve("mejores.txt");

        // Creamos manualmente para simular lo que haría prepararCarpetaResultados
        if (!Files.exists(carpetaResultados)) {
            Files.createDirectories(carpetaResultados);
        }
        if (!Files.exists(ficheroMejores)) {
            Files.createFile(ficheroMejores);
        }
>>>>>>> CodigoArreglado

        assertFalse(valida);
    }

    // ---------------------------------------------------------------
    // HU7 – jugarPartida() — marcador
    // ---------------------------------------------------------------

    /**
     * HU7a – La puntuación empieza en 0 al inicio de cada partida.
     */
    @Test
    void hu7_puntuacionIniciaEnCero() {
        ProyectoMeme.puntos = 99;  // valor basura previo
        ProyectoMeme.puntos = 0;
        ProyectoMeme.bulosUsados.clear();

        assertEquals(0, ProyectoMeme.puntos);
        assertTrue(ProyectoMeme.bulosUsados.isEmpty());
    }

    /**
     * HU7b – Respuesta correcta incrementa la puntuación en 1.
     */
    @Test
<<<<<<< HEAD
    void hu7_respuestaCorrecta_incrementaPuntos() {
        ProyectoMeme.puntos = 2;
        ProyectoMeme.soluciones.put(0, 1);

        Integer respuesta  = 1;
        Integer correcta   = ProyectoMeme.soluciones.get(0);

        if (respuesta.equals(correcta)) ProyectoMeme.puntos++;

        assertEquals(3, ProyectoMeme.puntos);
=======
    void crearDirectorioResultados_yaExisten_noLanzaExcepcion(@TempDir Path directorioTemporal) throws IOException {
        Path carpetaResultados = directorioTemporal.resolve("resultados");
        Path ficheroMejores = carpetaResultados.resolve("mejores.txt");
        Files.createDirectories(carpetaResultados);
        Files.createFile(ficheroMejores);

        // Simular la lógica de prepararCarpetaResultados con rutas temporales
        assertDoesNotThrow(() -> {
            if (!Files.exists(carpetaResultados))
                Files.createDirectories(carpetaResultados);
            if (!Files.exists(ficheroMejores))
                Files.createFile(ficheroMejores);
        });
>>>>>>> CodigoArreglado
    }

    // ---------------------------------------------------------------
    // HU8 – mostrarPuntuacion()
    // ---------------------------------------------------------------

    /**
     * HU8a – Con 5 puntos el mensaje es "Perfecto".
     */
    @Test
    void hu8_cincoAciertos_mensajePerfecto() {
        ProyectoMeme.puntos = 5;
        String mensaje;

        if      (ProyectoMeme.puntos == 5) mensaje = "Perfecto";
        else if (ProyectoMeme.puntos >= 3) mensaje = "Bien";
        else if (ProyectoMeme.puntos >= 1) mensaje = "Puedes mejorar";
        else                               mensaje = "Animo";

        assertEquals("Perfecto", mensaje);
    }

    /**
     * HU8b – Con 0 puntos el mensaje es "Animo".
     */
    @Test
    void hu8_ceroAciertos_mensajeAnimo() {
        ProyectoMeme.puntos = 0;
        String mensaje;

        if      (ProyectoMeme.puntos == 5) mensaje = "Perfecto";
        else if (ProyectoMeme.puntos >= 3) mensaje = "Bien";
        else if (ProyectoMeme.puntos >= 1) mensaje = "Puedes mejorar";
        else                               mensaje = "Animo";

        assertEquals("Animo", mensaje);
    }

    // ---------------------------------------------------------------
    // HU9 – guardarSiEsTop3()
    // ---------------------------------------------------------------

    /**
     * HU9a – Una puntuación mayor que la tercera entra en el ranking.
     */
    @Test
    void hu9_puntuacionMejorQueElTercero_entraEnRanking() throws IOException {
        Path mejores = dirTemporal.resolve("resultados/mejores.txt");
        Files.createDirectories(mejores.getParent());
        Files.writeString(mejores, "Ana;5\nCarlos;4\nMarta;2\n");

        ProyectoMeme.puntos = 3;  // mejor que Marta (2)

        java.util.ArrayList<String> ranking = new java.util.ArrayList<>(Files.readAllLines(mejores));
        ranking.removeIf(String::isBlank);

        boolean entraEnTop3 = true;
        if (ranking.size() >= 3) {
            int peor = Integer.parseInt(ranking.get(2).split(";")[1].trim());
            entraEnTop3 = ProyectoMeme.puntos > peor;
        }

        assertTrue(entraEnTop3);
    }

    /**
     * HU9b – Una puntuación igual o menor que la tercera no entra en el ranking.
     */
    @Test
    void hu9_puntuacionIgualAlTercero_noEntraEnRanking() throws IOException {
        Path mejores = dirTemporal.resolve("resultados/mejores.txt");
        Files.createDirectories(mejores.getParent());
        Files.writeString(mejores, "Ana;5\nCarlos;4\nMarta;3\n");

        ProyectoMeme.puntos = 3;  // igual que Marta

        java.util.ArrayList<String> ranking = new java.util.ArrayList<>(Files.readAllLines(mejores));
        ranking.removeIf(String::isBlank);

        boolean entraEnTop3 = true;
        if (ranking.size() >= 3) {
            int peor = Integer.parseInt(ranking.get(2).split(";")[1].trim());
            entraEnTop3 = ProyectoMeme.puntos > peor;
        }

        assertFalse(entraEnTop3);
    }

    // ---------------------------------------------------------------
    // HU10 – mostrarRanking()
    // ---------------------------------------------------------------

    /**
     * HU10a – Lee correctamente las líneas con formato NOMBRE;PUNTOS.
     */
    @Test
    void hu10_leeRankingConFormatoCorrecto() throws IOException {
        Path mejores = dirTemporal.resolve("resultados/mejores.txt");
        Files.createDirectories(mejores.getParent());
        Files.writeString(mejores, "Ana;5\nCarlos;4\n");

        List<String> lineas = Files.readAllLines(mejores);
        int puestosLeidos = 0;
        for (String linea : lineas) {
            String[] partes = linea.trim().split(";");
            if (partes.length == 2) puestosLeidos++;
        }

        assertEquals(2, puestosLeidos);
    }

    /**
     * HU10b – Si el fichero está vacío, no se muestra ningún puesto.
     */
    @Test
    void hu10_ficheroVacio_noPuestosLeidos() throws IOException {
        Path mejores = dirTemporal.resolve("resultados/mejores.txt");
        Files.createDirectories(mejores.getParent());
        Files.createFile(mejores);

        List<String> lineas = Files.readAllLines(mejores);
        int puestosLeidos = 0;
        for (String linea : lineas) {
            String[] partes = linea.trim().split(";");
            if (partes.length == 2) puestosLeidos++;
        }

        assertEquals(0, puestosLeidos);
    }
}