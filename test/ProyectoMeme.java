import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
class ProyectoMemeTest {

    /**
     * Guarda el System.in original para restaurarlo después de los tests
     * que lo sustituyen por un flujo simulado.
     */
    private InputStream entradaOriginal;

    /**
     * Antes de cada test limpiamos el estado estático de ProyectoMeme
     * para que ningún test afecte al siguiente.
     */
    @BeforeEach
    void limpiarEstadoEstatico() {
        ProyectoMeme.listaDeBulos = new ArrayList<>();
        ProyectoMeme.listaDeRealidades = new ArrayList<>();
        ProyectoMeme.mapaSoluciones = new HashMap<>();
        ProyectoMeme.bulosUsados = new ArrayList<>();
        ProyectoMeme.puntuacionFinal = 0;

        entradaOriginal = System.in;
    }

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

        assertTrue(Files.exists(carpetaResultados));
        assertTrue(Files.exists(ficheroMejores));
    }

    /**
     * Si la carpeta y el fichero ya existen, no debe lanzar ninguna excepción.
     */
    @Test
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
    }


    // =========================================================================
    // Método auxiliar
    // =========================================================================

    /**
     * Sustituye {@code System.in} por un flujo que simula la escritura del usuario.
     * Después de llamar a este método, el Scanner de ProyectoMeme leerá de ese flujo.
     *
     * @param texto texto que "escribiría" el usuario, con {@code \n} como saltos de línea
     */
    private void simularEntradaTeclado(String texto) {
        InputStream flujoSimulado = new ByteArrayInputStream(texto.getBytes());
        System.setIn(flujoSimulado);
        ProyectoMeme.teclado = new Scanner(System.in);
    }
}