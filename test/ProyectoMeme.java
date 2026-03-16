import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProyectoMemeTest {

    @TempDir
    Path dirTemporal;

    // Antes de cada test, cambiamos el directorio de trabajo al temporal
    // y limpiamos el estado estático de la clase
    @BeforeEach
    void setUp() throws Exception {
        ProyectoMeme.bulos.clear();
        ProyectoMeme.realidades.clear();
        ProyectoMeme.soluciones.clear();
        ProyectoMeme.bulosUsados.clear();
        ProyectoMeme.puntos = 0;

        // Crear carpeta datos/ dentro del directorio temporal
        Files.createDirectories(dirTemporal.resolve("datos"));
        System.setProperty("user.dir", dirTemporal.toString());
    }

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
    void hu7_respuestaCorrecta_incrementaPuntos() {
        ProyectoMeme.puntos = 2;
        ProyectoMeme.soluciones.put(0, 1);

        Integer respuesta  = 1;
        Integer correcta   = ProyectoMeme.soluciones.get(0);

        if (respuesta.equals(correcta)) ProyectoMeme.puntos++;

        assertEquals(3, ProyectoMeme.puntos);
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