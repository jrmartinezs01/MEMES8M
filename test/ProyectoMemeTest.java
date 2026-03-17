import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProyectoMemeTest {

    static Path rutaDatosReal;

    @BeforeAll
    static void setup() {
        // Usamos directamente tu carpeta real "datos"
        rutaDatosReal = Paths.get("datos");

        // Forzamos al programa a usar esta ruta
        ProyectoMeme.rutaDatos = rutaDatosReal;
    }

    // -------------------------------
    // HU1
    // -------------------------------
    @Test
    void testExistenFicheros() {
        boolean resultado = ProyectoMeme.existenFicheros();
        assertTrue(resultado);
    }

    // -------------------------------
    // HU3
    // -------------------------------
    @Test
    void testCargarBulos() throws IOException {
        ProyectoMeme.listaDeBulos.clear();

        ProyectoMeme.cargarBulos();

        assertTrue(ProyectoMeme.listaDeBulos.size() > 0);
    }

    // -------------------------------
    // HU4
    // -------------------------------
    @Test
    void testCargarRealidades() throws IOException {
        ProyectoMeme.listaDeRealidades.clear();

        ProyectoMeme.cargarRealidades();

        assertTrue(ProyectoMeme.listaDeRealidades.size() > 0);
    }

    // -------------------------------
    // XML
    // -------------------------------
    @Test
    void testLeerAtributo() {
        String linea = "<solucion bulo=\"1\" realidad=\"2\"/>";

        int bulo = ProyectoMeme.leerAtributo(linea, "bulo");

        assertEquals(1, bulo);
    }

    // -------------------------------
    // SOLUCIONES
    // -------------------------------
    @Test
    void testCargarSoluciones() throws IOException {
        ProyectoMeme.mapaSoluciones.clear();

        ProyectoMeme.cargarSoluciones();

        assertTrue(ProyectoMeme.mapaSoluciones.size() > 0);
    }

    // -------------------------------
    // LÓGICA
    // -------------------------------
    @Test
    void testElegirBulo() {
        ProyectoMeme.listaDeBulos = new ArrayList<>(List.of("B1", "B2", "B3"));
        ProyectoMeme.bulosUsados.clear();

        int bulo = ProyectoMeme.elegirBuloAlAzar();

        assertTrue(bulo >= 0 && bulo < 3);
    }
}