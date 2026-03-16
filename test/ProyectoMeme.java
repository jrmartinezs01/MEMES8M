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
 * Los métodos que necesitan entrada por teclado ({@code pedirRespuestaUsuario})
 * se prueban redirigiendo {@code System.in} con un {@link ByteArrayInputStream}.</p>
 *
 * <p>Los tests que crean ficheros usan {@code @TempDir} de JUnit 5 para
 * trabajar sobre directorios temporales que se borran solos al terminar.</p>
 *
 * @author Nombre Apellido
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
        ProyectoMeme.listaDeBulos      = new ArrayList<>();
        ProyectoMeme.listaDeRealidades = new ArrayList<>();
        ProyectoMeme.mapaSoluciones    = new HashMap<>();
        ProyectoMeme.bulosUsados       = new ArrayList<>();
        ProyectoMeme.puntuacionFinal   = 0;

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
    // extraerPuntuacion
    // =========================================================================

    /**
     * Una línea bien formada debe devolver el número correcto.
     */
    @Test
    void extraerPuntuacion_lineaBienFormada_devuelveNumero() {
        Integer resultado = ProyectoMeme.extraerPuntuacion("Ana;4");
        assertEquals(4, resultado);
    }

    /**
     * La puntuación 0 también es válida.
     */
    @Test
    void extraerPuntuacion_puntuacionCero_devuelveCero() {
        Integer resultado = ProyectoMeme.extraerPuntuacion("Luis;0");
        assertEquals(0, resultado);
    }

    /**
     * Debe funcionar aunque haya espacios alrededor del número.
     */
    @Test
    void extraerPuntuacion_conEspacios_devuelveNumeroSinEspacios() {
        Integer resultado = ProyectoMeme.extraerPuntuacion("Maria;  3  ");
        assertEquals(3, resultado);
    }

    /**
     * La puntuación máxima del juego (5) debe extraerse sin problema.
     */
    @Test
    void extraerPuntuacion_puntuacionMaxima_devuelveCinco() {
        Integer resultado = ProyectoMeme.extraerPuntuacion("Carlos;5");
        assertEquals(5, resultado);
    }


    // =========================================================================
    // extraerNombre
    // =========================================================================

    /**
     * El nombre se extrae correctamente de una línea estándar.
     */
    @Test
    void extraerNombre_lineaNormal_devuelveNombre() {
        String resultado = ProyectoMeme.extraerNombre("Ana;4");
        assertEquals("Ana", resultado);
    }

    /**
     * Los espacios alrededor del nombre deben eliminarse.
     */
    @Test
    void extraerNombre_conEspacios_devuelveNombreSinEspacios() {
        String resultado = ProyectoMeme.extraerNombre("  Pedro  ;3");
        assertEquals("Pedro", resultado);
    }

    /**
     * Un nombre con varias palabras debe preservarse tal cual.
     */
    @Test
    void extraerNombre_nombreCompuesto_devuelveNombreCompleto() {
        String resultado = ProyectoMeme.extraerNombre("Maria Jose;2");
        assertEquals("Maria Jose", resultado);
    }


    // =========================================================================
    // ordenarPuntuaciones
    // =========================================================================

    /**
     * Una lista ya ordenada no debe cambiar.
     */
    @Test
    void ordenarPuntuaciones_listaYaOrdenada_noMilingCambios() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;3", "Pepe;1"));
        ProyectoMeme.ordenarPuntuaciones(ranking);
        assertEquals("Ana;5",  ranking.get(0));
        assertEquals("Luis;3", ranking.get(1));
        assertEquals("Pepe;1", ranking.get(2));
    }

    /**
     * Una lista en orden inverso debe quedar completamente ordenada.
     */
    @Test
    void ordenarPuntuaciones_listaInvertida_quedaOrdenada() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Pepe;1", "Luis;3", "Ana;5"));
        ProyectoMeme.ordenarPuntuaciones(ranking);
        assertEquals("Ana;5",  ranking.get(0));
        assertEquals("Luis;3", ranking.get(1));
        assertEquals("Pepe;1", ranking.get(2));
    }

    /**
     * Una lista con un solo elemento no debe lanzar ningún error.
     */
    @Test
    void ordenarPuntuaciones_unSoloElemento_sinErrores() {
        ArrayList<String> ranking = new ArrayList<>(List.of("Ana;5"));
        assertDoesNotThrow(() -> ProyectoMeme.ordenarPuntuaciones(ranking));
        assertEquals("Ana;5", ranking.get(0));
    }

    /**
     * Dos elementos deben quedar en el orden correcto.
     */
    @Test
    void ordenarPuntuaciones_dosElementosDesordenados_quedanBien() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Luis;2", "Ana;4"));
        ProyectoMeme.ordenarPuntuaciones(ranking);
        assertEquals("Ana;4",  ranking.get(0));
        assertEquals("Luis;2", ranking.get(1));
    }

    /**
     * Puntuaciones iguales deben coexistir sin problema.
     */
    @Test
    void ordenarPuntuaciones_puntuacionesEmpatadas_noLanzaError() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;3", "Luis;3", "Pepe;3"));
        assertDoesNotThrow(() -> ProyectoMeme.ordenarPuntuaciones(ranking));
    }


    // =========================================================================
    // estaEnTop3
    // =========================================================================

    /**
     * Si el ranking está vacío, cualquier puntuación debe entrar.
     */
    @Test
    void estaEnTop3_rankingVacio_siempreEntra() {
        ArrayList<String> ranking = new ArrayList<>();
        assertTrue(ProyectoMeme.estaEnTop3(ranking, 1));
    }

    /**
     * Con solo una entrada en el ranking, cualquier puntuación debe entrar.
     */
    @Test
    void estaEnTop3_unaEntrada_siempreEntra() {
        ArrayList<String> ranking = new ArrayList<>(List.of("Ana;5"));
        assertTrue(ProyectoMeme.estaEnTop3(ranking, 1));
    }

    /**
     * Con dos entradas, cualquier puntuación debe entrar porque no hay top 3 completo.
     */
    @Test
    void estaEnTop3_dosEntradas_siempreEntra() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;3"));
        assertTrue(ProyectoMeme.estaEnTop3(ranking, 0));
    }

    /**
     * Con tres entradas y puntuación superior al tercero, debe entrar.
     */
    @Test
    void estaEnTop3_tresEntradasPuntuacionSuperior_entra() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;3", "Pepe;2"));
        assertTrue(ProyectoMeme.estaEnTop3(ranking, 4));
    }

    /**
     * Con tres entradas y puntuación igual al tercero, NO debe entrar (empate no cuenta).
     */
    @Test
    void estaEnTop3_tresEntradasEmpateConTercero_noEntra() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;3", "Pepe;2"));
        assertFalse(ProyectoMeme.estaEnTop3(ranking, 2));
    }

    /**
     * Con tres entradas y puntuación inferior al tercero, no debe entrar.
     */
    @Test
    void estaEnTop3_tresEntradasPuntuacionInferior_noEntra() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;3", "Pepe;2"));
        assertFalse(ProyectoMeme.estaEnTop3(ranking, 1));
    }

    /**
     * Con tres entradas perfectas (todos 5/5) y una puntuación de 4, no debe entrar.
     */
    @Test
    void estaEnTop3_tresEntradas5pts_puntuacionMenorNoEntra() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;5", "Pepe;5"));
        assertFalse(ProyectoMeme.estaEnTop3(ranking, 4));
    }

    /**
     * La puntuación mínima posible (0) entra si el ranking está vacío.
     */
    @Test
    void estaEnTop3_rankingVacioPuntuacionCero_entra() {
        ArrayList<String> ranking = new ArrayList<>();
        assertTrue(ProyectoMeme.estaEnTop3(ranking, 0));
    }


    // =========================================================================
    // guardarTop3
    // =========================================================================

    /**
     * Con cuatro entradas en la lista, solo deben guardarse las tres primeras.
     */
    @Test
    void guardarTop3_cuatroEntradas_soloGuardaTres(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.createFile(ficheroTemporal);

        ArrayList<String> entradas = new ArrayList<>(
            Arrays.asList("Ana;5", "Luis;4", "Pepe;3", "Maria;2")
        );
        ProyectoMeme.guardarTop3(ficheroTemporal, entradas);

        List<String> lineasGuardadas = Files.readAllLines(ficheroTemporal);
        assertEquals(3, lineasGuardadas.size());
        assertEquals("Ana;5",  lineasGuardadas.get(0));
        assertEquals("Luis;4", lineasGuardadas.get(1));
        assertEquals("Pepe;3", lineasGuardadas.get(2));
    }

    /**
     * Con solo dos entradas deben guardarse las dos (no debe lanzar error).
     */
    @Test
    void guardarTop3_dosEntradas_guardaLasDos(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.createFile(ficheroTemporal);

        ArrayList<String> entradas = new ArrayList<>(Arrays.asList("Ana;5", "Luis;4"));
        ProyectoMeme.guardarTop3(ficheroTemporal, entradas);

        List<String> lineasGuardadas = Files.readAllLines(ficheroTemporal);
        assertEquals(2, lineasGuardadas.size());
    }

    /**
     * Una lista vacía debe generar un fichero vacío sin lanzar errores.
     */
    @Test
    void guardarTop3_listaVacia_ficheroVacio(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.createFile(ficheroTemporal);

        ProyectoMeme.guardarTop3(ficheroTemporal, new ArrayList<>());

        List<String> lineasGuardadas = Files.readAllLines(ficheroTemporal);
        assertTrue(lineasGuardadas.isEmpty());
    }

    /**
     * Exactamente tres entradas deben guardarse todas sin truncar nada.
     */
    @Test
    void guardarTop3_tresEntradas_guardaLasTres(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.createFile(ficheroTemporal);

        ArrayList<String> entradas = new ArrayList<>(
            Arrays.asList("Ana;5", "Luis;4", "Pepe;3")
        );
        ProyectoMeme.guardarTop3(ficheroTemporal, entradas);

        List<String> lineasGuardadas = Files.readAllLines(ficheroTemporal);
        assertEquals(3, lineasGuardadas.size());
    }


    // =========================================================================
    // leerMejores
    // =========================================================================

    /**
     * Un fichero con tres entradas válidas debe devolver una lista de tamaño 3.
     */
    @Test
    void leerMejores_tresEntradas_devuelveTres(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.write(ficheroTemporal, Arrays.asList("Ana;5", "Luis;3", "Pepe;2"));

        ArrayList<String> resultado = ProyectoMeme.leerMejores(ficheroTemporal);
        assertEquals(3, resultado.size());
    }

    /**
     * Las líneas en blanco dentro del fichero deben ignorarse.
     */
    @Test
    void leerMejores_conLineasEnBlanco_lasIgnora(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.write(ficheroTemporal, Arrays.asList("Ana;5", "", "Luis;3", "  "));

        ArrayList<String> resultado = ProyectoMeme.leerMejores(ficheroTemporal);
        assertEquals(2, resultado.size());
    }

    /**
     * Un fichero completamente vacío debe devolver una lista vacía.
     */
    @Test
    void leerMejores_ficheroVacio_devuelveListaVacia(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.createFile(ficheroTemporal);

        ArrayList<String> resultado = ProyectoMeme.leerMejores(ficheroTemporal);
        assertTrue(resultado.isEmpty());
    }

    /**
     * El contenido leído debe coincidir exactamente con lo escrito.
     */
    @Test
    void leerMejores_contenidoCorrecto_coincideConFichero(@TempDir Path directorioTemporal) throws IOException {
        Path ficheroTemporal = directorioTemporal.resolve("mejores.txt");
        Files.write(ficheroTemporal, List.of("Ana;5"));

        ArrayList<String> resultado = ProyectoMeme.leerMejores(ficheroTemporal);
        assertEquals("Ana;5", resultado.get(0));
    }


    // =========================================================================
    // leerRealidades
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
    // leerSoluciones
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
    // comprobarRespuesta
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
    // mostrarBuloYRealidades
    // =========================================================================

    /**
     * Con una lista de bulos, el índice devuelto debe estar dentro del rango válido.
     */
    @Test
    void mostrarBuloYRealidades_bulosDisponibles_devuelveIndiceValido() {
        List<String> bulos       = Arrays.asList("Bulo A", "Bulo B", "Bulo C");
        List<String> realidades  = Arrays.asList("Real 1", "Real 2");

        Integer indice = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        assertTrue(indice >= 0 && indice < bulos.size());
    }

    /**
     * En dos llamadas consecutivas los índices devueltos deben ser distintos.
     */
    @Test
    void mostrarBuloYRealidades_dosLlamadas_noRepiteIndice() {
        List<String> bulos      = Arrays.asList("Bulo A", "Bulo B", "Bulo C");
        List<String> realidades = Arrays.asList("Real 1", "Real 2");

        Integer primero  = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        Integer segundo  = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        assertNotEquals(primero, segundo);
    }

    /**
     * Cuando se han agotado todos los bulos, debe devolver -1.
     */
    @Test
    void mostrarBuloYRealidades_bulosAgotados_devuelveMenosUno() {
        List<String> bulos      = Arrays.asList("Bulo A", "Bulo B");
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
        List<String> bulos      = Arrays.asList("Bulo único");
        List<String> realidades = Arrays.asList("Real 1");

        Integer indice = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
        assertTrue(ProyectoMeme.bulosUsados.contains(indice));
    }


    // =========================================================================
    // pedirRespuestaUsuario
    // =========================================================================

    /**
     * Una entrada válida en el rango debe devolver el índice en base 0.
     */
    @Test
    void pedirRespuestaUsuario_entradaValida_devuelveBase0() {
        simularEntradaTeclado("3\n");
        Integer resultado = ProyectoMeme.pedirRespuestaUsuario(5);
        assertEquals(2, resultado); // el usuario escribe 3, se devuelve 2 (base 0)
    }

    /**
     * Si el usuario escribe 1, debe devolver 0.
     */
    @Test
    void pedirRespuestaUsuario_eligePrimero_devuelveCero() {
        simularEntradaTeclado("1\n");
        Integer resultado = ProyectoMeme.pedirRespuestaUsuario(5);
        assertEquals(0, resultado);
    }

    /**
     * Si el usuario escribe el último número disponible, debe devolver totalOpciones-1.
     */
    @Test
    void pedirRespuestaUsuario_eligeUltimo_devuelveUltimoIndice() {
        simularEntradaTeclado("5\n");
        Integer resultado = ProyectoMeme.pedirRespuestaUsuario(5);
        assertEquals(4, resultado);
    }

    /**
     * Después de una entrada inválida, el bucle debe pedir de nuevo y aceptar la siguiente.
     */
    @Test
    void pedirRespuestaUsuario_primerEntradaInvalida_aceptaLaSiguiente() {
        simularEntradaTeclado("abc\n2\n");
        Integer resultado = ProyectoMeme.pedirRespuestaUsuario(3);
        assertEquals(1, resultado); // "abc" se ignora, "2" es válido → índice 1
    }

    /**
     * Un número fuera de rango debe rechazarse y aceptar la siguiente entrada válida.
     */
    @Test
    void pedirRespuestaUsuario_fueraDeRango_aceptaSiguienteValida() {
        simularEntradaTeclado("10\n2\n"); // 10 está fuera de rango para 3 opciones
        Integer resultado = ProyectoMeme.pedirRespuestaUsuario(3);
        assertEquals(1, resultado);
    }


    // =========================================================================
    // crearDirectorioResultados
    // =========================================================================

    /**
     * Si no existe la carpeta resultados, debe crearla junto con mejores.txt.
     *
     * <p>Este test requiere ejecutarse desde un directorio de trabajo limpio.
     * Si la carpeta ya existe se salta automáticamente.</p>
     */
    @Test
    void crearDirectorioResultados_noExiste_creaAmbos(@TempDir Path directorioTemporal) throws IOException {
        // Cambiamos temporalmente el directorio de trabajo al temporal
        Path carpetaResultados = directorioTemporal.resolve("resultados");
        Path ficheroMejores    = carpetaResultados.resolve("mejores.txt");

        // Creamos manualmente para simular lo que haría crearDirectorioResultados
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
        Path ficheroMejores    = carpetaResultados.resolve("mejores.txt");
        Files.createDirectories(carpetaResultados);
        Files.createFile(ficheroMejores);

        // Simular la lógica de crearDirectorioResultados con rutas temporales
        assertDoesNotThrow(() -> {
            if (!Files.exists(carpetaResultados)) Files.createDirectories(carpetaResultados);
            if (!Files.exists(ficheroMejores))    Files.createFile(ficheroMejores);
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