import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class HU2Test {

    @TempDir
    Path tempDir;

    private Path rutaDirectorio;
    private Path rutaFichero;

    @BeforeEach
    void setUp() throws Exception {
        // Redirigimos las rutas a un directorio temporal para no ensuciar el proyecto
        rutaDirectorio = tempDir.resolve("resultados");
        rutaFichero = rutaDirectorio.resolve("mejores.txt");
    }

    // -----------------------------------------------------------------------
    // Test 1: Ni directorio ni fichero existen → se crean ambos
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Crea el directorio 'resultados' si no existe")
    void creaDirectorioCuandoNoExiste() throws Exception {
        assertFalse(Files.exists(rutaDirectorio), "El directorio NO debe existir antes del test");

        hu2(rutaDirectorio, rutaFichero);

        assertTrue(Files.exists(rutaDirectorio), "El directorio debe existir tras llamar a hu2");
        assertTrue(Files.isDirectory(rutaDirectorio), "Debe ser un directorio");
    }

    @Test
    @DisplayName("Crea 'mejores.txt' si no existe")
    void creaFicheroCuandoNoExiste() throws Exception {
        assertFalse(Files.exists(rutaFichero), "El fichero NO debe existir antes del test");

        hu2(rutaDirectorio, rutaFichero);

        assertTrue(Files.exists(rutaFichero), "El fichero debe existir tras llamar a hu2");
        assertTrue(Files.isRegularFile(rutaFichero), "Debe ser un fichero regular");
    }

    // -----------------------------------------------------------------------
    // Test 2: El directorio ya existe → no lanza excepción y el fichero se crea
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("No lanza excepción si el directorio ya existe")
    void noLanzaExcepcionSiDirectorioExiste() throws Exception {
        Files.createDirectories(rutaDirectorio);
        assertTrue(Files.exists(rutaDirectorio));

        assertDoesNotThrow(() -> hu2(rutaDirectorio, rutaFichero));
    }

    @Test
    @DisplayName("Crea 'mejores.txt' aunque el directorio ya existiera")
    void creaFicheroSiDirectorioYaExistia() throws Exception {
        Files.createDirectories(rutaDirectorio);

        hu2(rutaDirectorio, rutaFichero);

        assertTrue(Files.exists(rutaFichero));
    }

    // -----------------------------------------------------------------------
    // Test 3: Directorio y fichero ya existen → no los sobreescribe
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("No sobreescribe 'mejores.txt' si ya existe con contenido")
    void noSobreescribeFicheroExistente() throws Exception {
        Files.createDirectories(rutaDirectorio);
        Files.writeString(rutaFichero, "contenido previo");

        hu2(rutaDirectorio, rutaFichero);

        String contenido = Files.readString(rutaFichero);
        assertEquals("contenido previo", contenido,
                "El contenido del fichero no debe cambiar si ya existía");
    }

    @Test
    @DisplayName("No lanza excepción si directorio y fichero ya existen")
    void noLanzaExcepcionSiTodoExiste() throws Exception {
        Files.createDirectories(rutaDirectorio);
        Files.createFile(rutaFichero);

        assertDoesNotThrow(() -> hu2(rutaDirectorio, rutaFichero));
    }

    // -----------------------------------------------------------------------
    // Test 4: El fichero creado está vacío
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("El fichero 'mejores.txt' creado está vacío")
    void ficheroNuevoEstaVacio() throws Exception {
        hu2(rutaDirectorio, rutaFichero);

        assertEquals(0, Files.size(rutaFichero), "El fichero recién creado debe estar vacío");
    }

    // -----------------------------------------------------------------------
    // Versión parametrizada de hu2 que acepta rutas externas (para tests)
    // -----------------------------------------------------------------------
    private static void hu2(Path rutaDirectorioResultados, Path rutaFicheroMejores) throws Exception {
        if (!Files.exists(rutaDirectorioResultados))
            Files.createDirectories(rutaDirectorioResultados);
        if (!Files.exists(rutaFicheroMejores))
            Files.createFile(rutaFicheroMejores);
    }
}