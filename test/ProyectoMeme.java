import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit 5 para las HU1 a HU5 de ProyectoMeme.
 */
class ProyectoMemeTest {

    @TempDir
    Path tempDir;

    // =========================================================================
    // HU1 - comprobarArchivosIniciales
    // =========================================================================

    @Nested
    @DisplayName("HU1 - Comprobar archivos iniciales")
    class HU1Test {

        /**
         * Replica la lógica de HU1 usando una ruta base inyectada
         * para no depender del sistema de ficheros real del proyecto.
         */
        private boolean comprobarArchivos(Path rutaDatos) {
            if (!Files.exists(rutaDatos))      return false;
            if (!Files.isDirectory(rutaDatos)) return false;
            for (String archivo : new String[]{"memes.txt", "realidades.json", "soluciones.xml"}) {
                Path ruta = rutaDatos.resolve(archivo);
                if (!Files.exists(ruta) || !Files.isRegularFile(ruta)) return false;
            }
            return true;
        }

        @Test
        @DisplayName("Devuelve true cuando existen carpeta y los 3 ficheros")
        void todoCorrecto() throws Exception {
            Path datos = tempDir.resolve("datos");
            Files.createDirectories(datos);
            Files.createFile(datos.resolve("memes.txt"));
            Files.createFile(datos.resolve("realidades.json"));
            Files.createFile(datos.resolve("soluciones.xml"));
            assertTrue(comprobarArchivos(datos));
        }

        @Test
        @DisplayName("Devuelve false cuando no existe la carpeta datos")
        void faltaCarpeta() {
            assertFalse(comprobarArchivos(tempDir.resolve("no_existe")));
        }

        @Test
        @DisplayName("Devuelve false cuando falta memes.txt")
        void faltaMemes() throws Exception {
            Path datos = tempDir.resolve("datos");
            Files.createDirectories(datos);
            Files.createFile(datos.resolve("realidades.json"));
            Files.createFile(datos.resolve("soluciones.xml"));
            assertFalse(comprobarArchivos(datos));
        }

        @Test
        @DisplayName("Devuelve false cuando falta realidades.json")
        void faltaRealidades() throws Exception {
            Path datos = tempDir.resolve("datos");
            Files.createDirectories(datos);
            Files.createFile(datos.resolve("memes.txt"));
            Files.createFile(datos.resolve("soluciones.xml"));
            assertFalse(comprobarArchivos(datos));
        }

        @Test
        @DisplayName("Devuelve false cuando falta soluciones.xml")
        void faltaSoluciones() throws Exception {
            Path datos = tempDir.resolve("datos");
            Files.createDirectories(datos);
            Files.createFile(datos.resolve("memes.txt"));
            Files.createFile(datos.resolve("realidades.json"));
            assertFalse(comprobarArchivos(datos));
        }
    }

    // =========================================================================
    // HU2 - hu2
    // =========================================================================

    @Nested
    @DisplayName("HU2 - Crear directorio y fichero de resultados")
    class HU2Test {

        private Path dir;
        private Path fichero;

        @BeforeEach
        void setUp() {
            dir     = tempDir.resolve("resultados");
            fichero = dir.resolve("mejores.txt");
        }

        /** Replica la lógica de hu2() usando rutas inyectadas. */
        private void hu2(Path d, Path f) throws Exception {
            if (!Files.exists(d)) Files.createDirectories(d);
            if (!Files.exists(f)) Files.createFile(f);
        }

        @Test
        @DisplayName("Crea el directorio si no existe")
        void creaDirectorio() throws Exception {
            assertFalse(Files.exists(dir));
            hu2(dir, fichero);
            assertTrue(Files.isDirectory(dir));
        }

        @Test
        @DisplayName("Crea mejores.txt si no existe")
        void creaFichero() throws Exception {
            hu2(dir, fichero);
            assertTrue(Files.isRegularFile(fichero));
        }

        @Test
        @DisplayName("El fichero nuevo está vacío")
        void ficheroVacio() throws Exception {
            hu2(dir, fichero);
            assertEquals(0, Files.size(fichero));
        }

        @Test
        @DisplayName("No sobreescribe mejores.txt si ya tiene contenido")
        void noSobreescribe() throws Exception {
            Files.createDirectories(dir);
            Files.writeString(fichero, "Ana;5");
            hu2(dir, fichero);
            assertEquals("Ana;5", Files.readString(fichero));
        }

        @Test
        @DisplayName("No lanza excepción si el directorio y el fichero ya existen")
        void noLanzaExcepcion() throws Exception {
            Files.createDirectories(dir);
            Files.createFile(fichero);
            assertDoesNotThrow(() -> hu2(dir, fichero));
        }
    }

    // =========================================================================
    // HU3 - leer memes.txt
    // =========================================================================

    @Nested
    @DisplayName("HU3 - Leer memes.txt")
    class HU3Test {

        /** Replica la lógica de hu3() usando una ruta inyectada. */
        private ArrayList<String> leerMemes(Path ruta) throws Exception {
            ArrayList<String> lista = new ArrayList<>(Files.readAllLines(ruta));
            lista.removeIf(String::isBlank);
            return lista;
        }

        @Test
        @DisplayName("Lee correctamente todas las líneas no vacías")
        void leeBulos() throws Exception {
            Path memes = tempDir.resolve("memes.txt");
            Files.writeString(memes, "Bulo 1\nBulo 2\nBulo 3\n");
            ArrayList<String> resultado = leerMemes(memes);
            assertEquals(3, resultado.size());
            assertEquals("Bulo 1", resultado.get(0));
            assertEquals("Bulo 3", resultado.get(2));
        }

        @Test
        @DisplayName("Ignora líneas vacías")
        void ignoraLineasVacias() throws Exception {
            Path memes = tempDir.resolve("memes.txt");
            Files.writeString(memes, "Bulo 1\n\n\nBulo 2\n");
            assertEquals(2, leerMemes(memes).size());
        }

        @Test
        @DisplayName("Devuelve lista vacía si el fichero está vacío")
        void ficheroVacio() throws Exception {
            Path memes = tempDir.resolve("memes.txt");
            Files.createFile(memes);
            assertTrue(leerMemes(memes).isEmpty());
        }

        @Test
        @DisplayName("Lanza excepción si el fichero no existe")
        void noExiste() {
            assertThrows(Exception.class, () -> leerMemes(tempDir.resolve("no_existe.txt")));
        }
    }

    // =========================================================================
    // HU4 - leerRealidades
    // =========================================================================

    @Nested
    @DisplayName("HU4 - Leer realidades.json")
    class HU4Test {

        /** Replica la lógica de leerRealidades() usando una ruta inyectada. */
        private ArrayList<String> leerRealidades(Path ruta) throws Exception {
            List<String> lineas = Files.readAllLines(ruta);
            ArrayList<String> realidades = new ArrayList<>();
            for (String linea : lineas) {
                linea = linea.trim();
                if (linea.contains("\"texto\"")) {
                    int inicio         = linea.indexOf("\"texto\"") + "\"texto\"".length();
                    int primerComilla  = linea.indexOf('"', inicio + 1) + 1;
                    int segundaComilla = linea.indexOf('"', primerComilla);
                    if (primerComilla > 0 && segundaComilla > primerComilla) {
                        String texto = linea.substring(primerComilla, segundaComilla).trim();
                        if (!texto.isBlank()) realidades.add(texto);
                    }
                }
            }
            return realidades;
        }

        @Test
        @DisplayName("Extrae los textos correctamente de un JSON válido")
        void extraeTextos() throws Exception {
            Path json = tempDir.resolve("realidades.json");
            Files.writeString(json,
                "[\n" +
                "  { \"texto\": \"Brecha salarial del 18%\", \"fuente\": \"INE\" },\n" +
                "  { \"texto\": \"80% de víctimas son mujeres\", \"fuente\": \"Interior\" }\n" +
                "]\n");
            ArrayList<String> resultado = leerRealidades(json);
            assertEquals(2, resultado.size());
            assertEquals("Brecha salarial del 18%", resultado.get(0));
            assertEquals("80% de víctimas son mujeres", resultado.get(1));
        }

        @Test
        @DisplayName("Devuelve lista vacía si no hay campos texto")
        void sinCamposTexto() throws Exception {
            Path json = tempDir.resolve("realidades.json");
            Files.writeString(json, "[ { \"fuente\": \"INE\" } ]\n");
            assertTrue(leerRealidades(json).isEmpty());
        }

        @Test
        @DisplayName("Devuelve lista vacía con fichero vacío")
        void ficheroVacio() throws Exception {
            Path json = tempDir.resolve("realidades.json");
            Files.createFile(json);
            assertTrue(leerRealidades(json).isEmpty());
        }

        @Test
        @DisplayName("Lanza excepción si el fichero no existe")
        void noExiste() {
            assertThrows(Exception.class, () -> leerRealidades(tempDir.resolve("no.json")));
        }
    }

    // =========================================================================
    // HU5 - seleccionarBuloAleatorio / mostrarBuloYRealidades
    // =========================================================================

    @Nested
    @DisplayName("HU5 - Mostrar bulo aleatorio y lista de realidades")
    class HU5Test {

        private Set<Integer> usados;

        @BeforeEach
        void setUp() {
            usados = new HashSet<>();
            // Limpiamos el estado estático de ProyectoMeme antes de cada test
            ProyectoMeme.bulosUsados.clear();
        }

        /** Replica la lógica de seleccionarBuloAleatorio() con el Set inyectado. */
        private int seleccionar(int total) {
            if (usados.size() >= total) return -1;
            Random r = new Random();
            int indice;
            do { indice = r.nextInt(total); } while (usados.contains(indice));
            usados.add(indice);
            return indice;
        }

        // --- seleccionarBuloAleatorio ---

        @Test
        @DisplayName("El índice devuelto está dentro del rango válido")
        void indiceEnRango() {
            int indice = seleccionar(5);
            assertTrue(indice >= 0 && indice < 5);
        }

        @Test
        @DisplayName("No se repite ningún bulo en llamadas sucesivas")
        void sinRepeticion() {
            Set<Integer> vistos = new HashSet<>();
            for (int i = 0; i < 5; i++) {
                int indice = seleccionar(5);
                assertFalse(vistos.contains(indice), "El bulo " + indice + " se ha repetido");
                vistos.add(indice);
            }
        }

        @Test
        @DisplayName("Devuelve -1 cuando ya se usaron todos los bulos")
        void devuelveMinusUnoSiAgotados() {
            for (int i = 0; i < 3; i++) seleccionar(3);
            assertEquals(-1, seleccionar(3));
        }

        @Test
        @DisplayName("El índice seleccionado queda registrado como usado")
        void indiceRegistrado() {
            int indice = seleccionar(5);
            assertTrue(usados.contains(indice));
        }

        @Test
        @DisplayName("Funciona correctamente con un único bulo disponible")
        void unSoloBulo() {
            assertEquals(0, seleccionar(1));
            assertEquals(-1, seleccionar(1));
        }

        // --- mostrarBuloYRealidades ---

        @Test
        @DisplayName("Muestra el bulo seleccionado y todas las realidades numeradas")
        void muestraContenido() {
            List<String> bulos      = List.of("Bulo A", "Bulo B");
            List<String> realidades = List.of("Realidad 1", "Realidad 2", "Realidad 3");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream original = System.out;
            System.setOut(new PrintStream(baos));

            int indice = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);

            System.setOut(original);
            String salida = baos.toString();

            assertTrue(indice >= 0 && indice < bulos.size());
            assertTrue(salida.contains(bulos.get(indice)));
            assertTrue(salida.contains("1. Realidad 1"));
            assertTrue(salida.contains("2. Realidad 2"));
            assertTrue(salida.contains("3. Realidad 3"));
        }

        @Test
        @DisplayName("Devuelve el índice del bulo mostrado")
        void devuelveIndiceValido() {
            List<String> bulos      = List.of("Bulo A", "Bulo B", "Bulo C");
            List<String> realidades = List.of("Realidad 1");
            int indice = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
            assertTrue(indice >= 0 && indice < bulos.size());
        }

        @Test
        @DisplayName("Devuelve -1 si no quedan bulos disponibles")
        void devuelveMinusUnoSinBulos() {
            List<String> bulos      = List.of("Bulo A");
            List<String> realidades = List.of("Realidad 1");

            ProyectoMeme.mostrarBuloYRealidades(bulos, realidades); // consume el único bulo
            int resultado = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);

            assertEquals(-1, resultado);
        }

        @Test
        @DisplayName("No repite bulos en llamadas sucesivas a mostrarBuloYRealidades")
        void noRepiteBulos() {
            List<String> bulos      = List.of("Bulo A", "Bulo B", "Bulo C");
            List<String> realidades = List.of("Realidad 1");

            Set<Integer> vistos = new HashSet<>();
            for (int i = 0; i < bulos.size(); i++) {
                int indice = ProyectoMeme.mostrarBuloYRealidades(bulos, realidades);
                assertFalse(vistos.contains(indice), "El bulo " + indice + " se ha repetido");
                vistos.add(indice);
            }
        }
    }
}