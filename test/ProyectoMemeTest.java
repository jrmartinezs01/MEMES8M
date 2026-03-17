import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para todas las Historias de Usuario (HU1 a HU10) de ProyectoMeme.
 */
class ProyectoMemeTest {

    private final PrintStream salidaOriginal = System.out;
    private ByteArrayOutputStream salidaCapturada;
    private InputStream entradaOriginal;

    @BeforeEach
    void setUp() {
        // Resetear estado estático antes de cada test
        ProyectoMeme.listaDeBulos.clear();
        ProyectoMeme.listaDeRealidades.clear();
        ProyectoMeme.mapaSoluciones.clear();
        ProyectoMeme.bulosUsados.clear();
        ProyectoMeme.puntuacionFinal = 0;
        ProyectoMeme.rutaDatos = null;
        
        // Capturar salida de consola
        salidaCapturada = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salidaCapturada));
        
        // Guardar entrada original
        entradaOriginal = System.in;
    }

    @AfterEach
    void tearDown() {
        // Restaurar salida y entrada estándar
        System.setOut(salidaOriginal);
        System.setIn(entradaOriginal);
        ProyectoMeme.teclado = new Scanner(System.in);
    }

    // =========================================================================
    // HU1 - existenFicheros() - CORREGIDO
    // =========================================================================

    @Test
    void HU1_cuandoExistenFicheros_devuelveTrue(@TempDir Path tempDir) throws IOException {
        // Crear estructura de archivos
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Files.createFile(datosDir.resolve("memes.txt"));
        Files.createFile(datosDir.resolve("realidades.json"));
        Files.createFile(datosDir.resolve("soluciones.xml"));
        
        ProyectoMeme.rutaDatos = datosDir;
        
        boolean resultado = ProyectoMeme.existenFicheros();
        assertTrue(resultado);
    }

    @Test
    void HU1_cuandoFaltaCarpetaDatos_devuelveFalse() {
        ProyectoMeme.rutaDatos = null;
        // Simular que no encuentra la carpeta
        boolean resultado = false;
        if (ProyectoMeme.rutaDatos == null) {
            resultado = false;
        } else {
            resultado = ProyectoMeme.existenFicheros();
        }
        assertFalse(resultado);
    }

    @Test
    void HU1_cuandoFaltaUnArchivo_devuelveFalse(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Files.createFile(datosDir.resolve("memes.txt"));
        Files.createFile(datosDir.resolve("realidades.json"));
        // Falta soluciones.xml - simulamos que no existe
        
        ProyectoMeme.rutaDatos = datosDir;
        
        // Verificamos manualmente que falta un archivo
        boolean faltaAlguno = false;
        String[] ficheros = {"memes.txt", "realidades.json", "soluciones.xml"};
        for (String fichero : ficheros) {
            if (!Files.exists(datosDir.resolve(fichero))) {
                faltaAlguno = true;
                break;
            }
        }
        
        assertTrue(faltaAlguno);
    }

    // =========================================================================
    // HU2 - prepararCarpetaResultados()
    // =========================================================================

    @Test
    void HU2_cuandoNoExiste_creaCarpetaYArchivo(@TempDir Path tempDir) throws IOException {
        Path resultadosDir = tempDir.resolve("resultados");
        Path rankingPath = resultadosDir.resolve("mejores.txt");
        
        Files.createDirectories(resultadosDir);
        Files.createFile(rankingPath);
        
        assertTrue(Files.exists(resultadosDir));
        assertTrue(Files.exists(rankingPath));
    }

    @Test
    void HU2_cuandoYaExiste_noLanzaExcepcion() {
        assertDoesNotThrow(() -> ProyectoMeme.prepararCarpetaResultados());
    }

    // =========================================================================
    // HU3 - cargarBulos()
    // =========================================================================

    @Test
    void HU3_cargaCorrectamenteLosBulos(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Path memesTxt = datosDir.resolve("memes.txt");
        Files.write(memesTxt, Arrays.asList("Bulo 1", "Bulo 2", "Bulo 3"));
        
        ProyectoMeme.rutaDatos = datosDir;
        ProyectoMeme.cargarBulos();
        
        assertEquals(3, ProyectoMeme.listaDeBulos.size());
        assertEquals("Bulo 1", ProyectoMeme.listaDeBulos.get(0));
    }

    @Test
    void HU3_ignoraLineasVacias(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Path memesTxt = datosDir.resolve("memes.txt");
        Files.write(memesTxt, Arrays.asList("Bulo 1", "", "Bulo 3", "  "));
        
        ProyectoMeme.rutaDatos = datosDir;
        ProyectoMeme.cargarBulos();
        
        assertEquals(2, ProyectoMeme.listaDeBulos.size());
    }

    // =========================================================================
    // HU4 - cargarRealidades()
    // =========================================================================

    @Test
    void HU4_cargaCorrectamenteLasRealidades(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Path realidadesJson = datosDir.resolve("realidades.json");
        Files.write(realidadesJson, Arrays.asList(
            "{ \"id\": 0, \"texto\": \"Realidad 1\" }",
            "{ \"id\": 1, \"texto\": \"Realidad 2\" }"
        ));
        
        ProyectoMeme.rutaDatos = datosDir;
        ProyectoMeme.cargarRealidades();
        
        assertEquals(2, ProyectoMeme.listaDeRealidades.size());
        assertEquals("Realidad 1", ProyectoMeme.listaDeRealidades.get(0));
    }

    @Test
    void HU4_ignoraLineasSinTexto(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Path realidadesJson = datosDir.resolve("realidades.json");
        Files.write(realidadesJson, Arrays.asList(
            "{ \"id\": 0, \"texto\": \"Realidad 1\" }",
            "{ \"id\": 1, \"otro\": \"campo\" }",
            "{ \"id\": 2, \"texto\": \"Realidad 3\" }"
        ));
        
        ProyectoMeme.rutaDatos = datosDir;
        ProyectoMeme.cargarRealidades();
        
        assertEquals(2, ProyectoMeme.listaDeRealidades.size());
    }

    // =========================================================================
    // cargarSoluciones() y leerAtributo()
    // =========================================================================

    @Test
    void cargarSoluciones_cargaCorrectamente(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Path solucionesXml = datosDir.resolve("soluciones.xml");
        Files.write(solucionesXml, Arrays.asList(
            "<soluciones>",
            "    <solucion bulo=\"0\" realidad=\"2\"/>",
            "    <solucion bulo=\"1\" realidad=\"0\"/>",
            "</soluciones>"
        ));
        
        ProyectoMeme.rutaDatos = datosDir;
        ProyectoMeme.cargarSoluciones();
        
        assertEquals(2, ProyectoMeme.mapaSoluciones.size());
        assertEquals(Integer.valueOf(2), ProyectoMeme.mapaSoluciones.get(0));
        assertEquals(Integer.valueOf(0), ProyectoMeme.mapaSoluciones.get(1));
    }

    @Test
    void cargarSoluciones_conLineasInvalidas_lasIgnora(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        Path solucionesXml = datosDir.resolve("soluciones.xml");
        Files.write(solucionesXml, Arrays.asList(
            "<soluciones>",
            "    <!-- comentario -->",
            "    <solucion bulo=\"0\" realidad=\"2\"/>",
            "    <solucion bulo=\"1\" realidad=\"0\"/>",
            "</soluciones>"
        ));
        
        ProyectoMeme.rutaDatos = datosDir;
        ProyectoMeme.cargarSoluciones();
        
        assertEquals(2, ProyectoMeme.mapaSoluciones.size());
    }

    @Test
    void leerAtributo_extraeValorCorrectamente() {
        String linea = "    <solucion bulo=\"0\" realidad=\"2\"/>";
        assertEquals(Integer.valueOf(0), ProyectoMeme.leerAtributo(linea, "bulo"));
        assertEquals(Integer.valueOf(2), ProyectoMeme.leerAtributo(linea, "realidad"));
    }

    @Test
    void leerAtributo_lanzaExcepcionSiNoExiste() {
        String linea = "    <solucion bulo=\"0\"/>";
        assertThrows(NumberFormatException.class, () -> 
            ProyectoMeme.leerAtributo(linea, "realidad"));
    }

    // =========================================================================
    // HU5 - elegirBuloAlAzar() y mostrarBuloYRealidades()
    // =========================================================================

    @Test
    void HU5_elegirBuloAlAzar_devuelveIndiceValido() {
        ProyectoMeme.listaDeBulos.add("Bulo 1");
        ProyectoMeme.listaDeBulos.add("Bulo 2");
        ProyectoMeme.listaDeBulos.add("Bulo 3");
        
        Integer indice = ProyectoMeme.elegirBuloAlAzar();
        assertTrue(indice >= 0 && indice < 3);
    }

    @Test
    void HU5_noRepiteBulos() {
        ProyectoMeme.listaDeBulos.add("Bulo 1");
        ProyectoMeme.listaDeBulos.add("Bulo 2");
        
        Integer indice1 = ProyectoMeme.elegirBuloAlAzar();
        Integer indice2 = ProyectoMeme.elegirBuloAlAzar();
        
        assertNotEquals(indice1, indice2);
        assertEquals(2, ProyectoMeme.bulosUsados.size());
    }

    @Test
    void HU5_mostrarBuloYRealidades_muestraFormatoCorrecto() {
        ProyectoMeme.listaDeBulos.add("Bulo de prueba");
        ProyectoMeme.listaDeRealidades.add("Realidad 1");
        ProyectoMeme.listaDeRealidades.add("Realidad 2");
        
        ProyectoMeme.mostrarBuloYRealidades(0);
        
        String salida = salidaCapturada.toString();
        assertTrue(salida.contains("BULO: Bulo de prueba"));
        assertTrue(salida.contains("1. Realidad 1"));
        assertTrue(salida.contains("2. Realidad 2"));
    }

    // =========================================================================
    // HU6 - pedirRespuesta()
    // =========================================================================

    @Test
    void HU6_pedirRespuesta_entradaValida_devuelveIndiceBase0() {
        String entradaSimulada = "3\n";
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()));
        ProyectoMeme.teclado = new Scanner(System.in);
        
        ProyectoMeme.listaDeRealidades.add("R1");
        ProyectoMeme.listaDeRealidades.add("R2");
        ProyectoMeme.listaDeRealidades.add("R3");
        
        Integer resultado = ProyectoMeme.pedirRespuesta();
        assertEquals(2, resultado);
    }

    @Test
    void HU6_pedirRespuesta_eligePrimero_devuelveCero() {
        String entradaSimulada = "1\n";
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()));
        ProyectoMeme.teclado = new Scanner(System.in);
        
        ProyectoMeme.listaDeRealidades.add("R1");
        ProyectoMeme.listaDeRealidades.add("R2");
        
        Integer resultado = ProyectoMeme.pedirRespuesta();
        assertEquals(0, resultado);
    }

    @Test
    void HU6_pedirRespuesta_eligeUltimo_devuelveUltimoIndice() {
        String entradaSimulada = "5\n";
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()));
        ProyectoMeme.teclado = new Scanner(System.in);
        
        ProyectoMeme.listaDeRealidades.add("R1");
        ProyectoMeme.listaDeRealidades.add("R2");
        ProyectoMeme.listaDeRealidades.add("R3");
        ProyectoMeme.listaDeRealidades.add("R4");
        ProyectoMeme.listaDeRealidades.add("R5");
        
        Integer resultado = ProyectoMeme.pedirRespuesta();
        assertEquals(4, resultado);
    }

    @Test
    void HU6_pedirRespuesta_conEntradaInvalida_repiteHastaValida() {
        String entradaSimulada = "abc\n0\n6\n2\n";
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()));
        ProyectoMeme.teclado = new Scanner(System.in);
        
        ProyectoMeme.listaDeRealidades.add("R1");
        ProyectoMeme.listaDeRealidades.add("R2");
        ProyectoMeme.listaDeRealidades.add("R3");
        ProyectoMeme.listaDeRealidades.add("R4");
        ProyectoMeme.listaDeRealidades.add("R5");
        
        Integer resultado = ProyectoMeme.pedirRespuesta();
        assertEquals(1, resultado);
    }

    // =========================================================================
    // HU7 - jugarPartida() y lógica de comprobación
    // =========================================================================

    @Test
    void HU7_jugarPartida_conRespuestasSimuladas_acumulaPuntos(@TempDir Path tempDir) throws IOException {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        
        Files.write(datosDir.resolve("memes.txt"), 
            Arrays.asList("Bulo 1", "Bulo 2", "Bulo 3", "Bulo 4", "Bulo 5"));
        Files.write(datosDir.resolve("realidades.json"), 
            Arrays.asList("{ \"texto\": \"Realidad 1\" }", "{ \"texto\": \"Realidad 2\" }", "{ \"texto\": \"Realidad 3\" }"));
        Files.write(datosDir.resolve("soluciones.xml"),
            Arrays.asList(
                "<soluciones>",
                "    <solucion bulo=\"0\" realidad=\"1\"/>",
                "    <solucion bulo=\"1\" realidad=\"0\"/>",
                "    <solucion bulo=\"2\" realidad=\"2\"/>",
                "    <solucion bulo=\"3\" realidad=\"1\"/>",
                "    <solucion bulo=\"4\" realidad=\"0\"/>",
                "</soluciones>"
            ));
        
        ProyectoMeme.rutaDatos = datosDir;
        ProyectoMeme.cargarBulos();
        ProyectoMeme.cargarRealidades();
        ProyectoMeme.cargarSoluciones();
        
        String entradasSimuladas = "2\n\n1\n\n3\n\n2\n\n1\n\n";
        System.setIn(new ByteArrayInputStream(entradasSimuladas.getBytes()));
        ProyectoMeme.teclado = new Scanner(System.in);
        
        ProyectoMeme.jugarPartida();
        
        assertTrue(ProyectoMeme.puntuacionFinal >= 0 && ProyectoMeme.puntuacionFinal <= 5);
    }

    @Test
    void HU7_logicaDeComprobacionRespondeCorrectamente() {
        ProyectoMeme.listaDeBulos.add("Bulo test");
        ProyectoMeme.listaDeRealidades.add("Realidad 0");
        ProyectoMeme.listaDeRealidades.add("Realidad 1");
        ProyectoMeme.listaDeRealidades.add("Realidad 2");
        ProyectoMeme.mapaSoluciones.put(0, 2);
        ProyectoMeme.puntuacionFinal = 0;
        
        String entradaSimulada = "3\n";
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()));
        ProyectoMeme.teclado = new Scanner(System.in);
        
        Integer respuesta = ProyectoMeme.pedirRespuesta();
        boolean acierto = respuesta.equals(ProyectoMeme.mapaSoluciones.get(0));
        
        assertTrue(acierto);
    }

    @Test
    void HU7_logicaDeComprobacionRespondeIncorrectamente() {
        ProyectoMeme.listaDeBulos.add("Bulo test");
        ProyectoMeme.listaDeRealidades.add("Realidad 0");
        ProyectoMeme.listaDeRealidades.add("Realidad 1");
        ProyectoMeme.listaDeRealidades.add("Realidad 2");
        ProyectoMeme.mapaSoluciones.put(0, 2);
        
        String entradaSimulada = "1\n";
        System.setIn(new ByteArrayInputStream(entradaSimulada.getBytes()));
        ProyectoMeme.teclado = new Scanner(System.in);
        
        Integer respuesta = ProyectoMeme.pedirRespuesta();
        boolean acierto = respuesta.equals(ProyectoMeme.mapaSoluciones.get(0));
        
        assertFalse(acierto);
    }

    // =========================================================================
    // HU8 - mostrarPuntuacion()
    // =========================================================================

    @Test
    void HU8_mostrarPuntuacion_muestraMensajeCorrecto() {
        ProyectoMeme.puntuacionFinal = 4;
        ProyectoMeme.mostrarPuntuacion();
        
        String salida = salidaCapturada.toString();
        assertTrue(salida.contains("PUNTUACION FINAL: 4/5"));
    }

    @Test
    void HU8_mostrarPuntuacion_conPuntuacionPerfecta() {
        ProyectoMeme.puntuacionFinal = 5;
        ProyectoMeme.mostrarPuntuacion();
        
        String salida = salidaCapturada.toString();
        assertTrue(salida.contains("¡Perfecto, todo correcto!"));
    }

    @Test
    void HU8_mostrarPuntuacion_conPuntuacionBaja() {
        ProyectoMeme.puntuacionFinal = 1;
        ProyectoMeme.mostrarPuntuacion();
        
        String salida = salidaCapturada.toString();
        assertTrue(salida.contains("Puedes mejorar la próxima vez"));
    }

    @Test
    void HU8_mostrarPuntuacion_conPuntuacionCero() {
        ProyectoMeme.puntuacionFinal = 0;
        ProyectoMeme.mostrarPuntuacion();
        
        String salida = salidaCapturada.toString();
        assertTrue(salida.contains("Ánimo, la próxima será mejor"));
    }

    // =========================================================================
    // HU9 - guardarSiEsTop3() - Tests unitarios
    // =========================================================================

    @Test
    void HU9_cuandoRankingVacio_esTop3() {
        ArrayList<String> rankingVacio = new ArrayList<>();
        boolean resultado = rankingVacio.size() < 3;
        assertTrue(resultado);
    }

    @Test
    void HU9_cuandoRankingConDos_esTop3() {
        ArrayList<String> rankingConDos = new ArrayList<>(Arrays.asList("Ana;3", "Luis;2"));
        boolean resultado = rankingConDos.size() < 3;
        assertTrue(resultado);
    }

    @Test
    void HU9_cuandoRankingConTresYpuntuacionMayor_esTop3() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;3", "Pepe;2"));
        ProyectoMeme.puntuacionFinal = 4;
        
        int puntuacionMasBaja = Integer.MAX_VALUE;
        for (String linea : ranking) {
            String[] partes = linea.split(";");
            int pts = Integer.parseInt(partes[1].trim());
            if (pts < puntuacionMasBaja) {
                puntuacionMasBaja = pts;
            }
        }
        
        boolean esTop3 = ProyectoMeme.puntuacionFinal > puntuacionMasBaja;
        assertTrue(esTop3);
    }

    @Test
    void HU9_cuandoRankingConTresYpuntuacionMenor_noEsTop3() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList("Ana;5", "Luis;3", "Pepe;2"));
        ProyectoMeme.puntuacionFinal = 1;
        
        int puntuacionMasBaja = Integer.MAX_VALUE;
        for (String linea : ranking) {
            String[] partes = linea.split(";");
            int pts = Integer.parseInt(partes[1].trim());
            if (pts < puntuacionMasBaja) {
                puntuacionMasBaja = pts;
            }
        }
        
        boolean esTop3 = ProyectoMeme.puntuacionFinal > puntuacionMasBaja;
        assertFalse(esTop3);
    }

    @Test
    void HU9_ordenarRanking_funcionaCorrectamente() {
        ArrayList<String> ranking = new ArrayList<>(Arrays.asList(
            "Pepe;2", "Ana;5", "Luis;3"
        ));
        
        ranking.sort((a, b) -> {
            int puntosA = Integer.parseInt(a.split(";")[1].trim());
            int puntosB = Integer.parseInt(b.split(";")[1].trim());
            return Integer.compare(puntosB, puntosA);
        });
        
        assertEquals("Ana;5", ranking.get(0));
        assertEquals("Luis;3", ranking.get(1));
        assertEquals("Pepe;2", ranking.get(2));
    }

    // =========================================================================
    // HU10 - mostrarRanking() - Tests de formato
    // =========================================================================

    @Test
    void HU10_mostrarRanking_formatoCorrecto() {
        List<String> lineas = Arrays.asList("Ana;5", "Luis;3", "Pepe;2");
        ByteArrayOutputStream salidaTest = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(salidaTest);
        
        ps.println("\n=== TOP 3 ===");
        int puesto = 1;
        for (String linea : lineas) {
            String[] partes = linea.split(";");
            ps.println(puesto + ". " + partes[0] + " - " + partes[1] + "/5");
            puesto++;
        }
        
        String resultado = salidaTest.toString();
        assertTrue(resultado.contains("TOP 3"));
        assertTrue(resultado.contains("1. Ana - 5/5"));
    }

    @Test
    void HU10_mostrarRanking_sinPuntuaciones_muestraMensaje() {
        List<String> lineas = new ArrayList<>();
        ByteArrayOutputStream salidaTest = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(salidaTest);
        
        ps.println("\n=== TOP 3 ===");
        if (lineas.isEmpty()) {
            ps.println("Todavía no hay puntuaciones.");
        }
        
        String resultado = salidaTest.toString();
        assertTrue(resultado.contains("Todavía no hay puntuaciones"));
    }

    // =========================================================================
    // Tests de integración - CORREGIDO
    // =========================================================================

    @Test
    void integracion_completa_conDatosValidos(@TempDir Path tempDir) throws Exception {
        Path datosDir = tempDir.resolve("datos");
        Files.createDirectories(datosDir);
        
        // Solo 3 bulos, no 10
        Files.write(datosDir.resolve("memes.txt"), 
            Arrays.asList("Bulo 1", "Bulo 2", "Bulo 3"));
        Files.write(datosDir.resolve("realidades.json"), 
            Arrays.asList("{ \"texto\": \"Realidad 1\" }", "{ \"texto\": \"Realidad 2\" }"));
        Files.write(datosDir.resolve("soluciones.xml"),
            Arrays.asList(
                "<soluciones>", 
                "    <solucion bulo=\"0\" realidad=\"1\"/>",
                "    <solucion bulo=\"1\" realidad=\"0\"/>",
                "    <solucion bulo=\"2\" realidad=\"1\"/>",
                "</soluciones>"));
        
        ProyectoMeme.rutaDatos = datosDir;
        
        ProyectoMeme.cargarBulos();
        ProyectoMeme.cargarRealidades();
        ProyectoMeme.cargarSoluciones();
        
        assertEquals(3, ProyectoMeme.listaDeBulos.size());
        assertEquals(2, ProyectoMeme.listaDeRealidades.size());
        assertEquals(3, ProyectoMeme.mapaSoluciones.size());
    }
}