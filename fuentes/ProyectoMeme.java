package fuentes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProyectoMeme {
    
    public static void main(String[] args) {
        System.out.println("=== BIENVENIDO A MEMES8M ===");
        
        // HU1 - Comprobar archivos iniciales
        if (!comprobarArchivosIniciales()) {
            System.out.println("❌ El programa no puede continuar. Corrige los errores.");
            return;
        }
        
        System.out.println("✅ Archivos verificados correctamente. El programa puede continuar.");
        
        // Aquí tus compañeros añadirán el resto del código (HU2, HU3, etc.)
    }
    
    /**
     * HU1 - Comprueba si existe el directorio datos con los ficheros:
     * memes.txt, realidades.json y soluciones.xml
     * @return true si todo existe, false si falta algo
     */
    public static boolean comprobarArchivosIniciales() {
        // Ruta a la carpeta datos (está al mismo nivel que fuentes)
        Path rutaDatos = Paths.get("datos");
        
        // Comprobar si existe la carpeta datos
        if (!Files.exists(rutaDatos)) {
            System.out.println("ERROR: No existe la carpeta 'datos'");
            return false;
        }
        
        if (!Files.isDirectory(rutaDatos)) {
            System.out.println("ERROR: 'datos' no es una carpeta");
            return false;
        }
        
        // Lista de archivos necesarios
        String[] archivosNecesarios = {"memes.txt", "realidades.json", "soluciones.xml"};
        boolean todoCorrecto = true;
        
        // Comprobar cada archivo
        for (String archivo : archivosNecesarios) {
            Path rutaArchivo = rutaDatos.resolve(archivo);
            
            if (!Files.exists(rutaArchivo)) {
                System.out.println("ERROR: No existe el archivo: " + archivo);
                todoCorrecto = false;
            }
            else if (!Files.isRegularFile(rutaArchivo)) {
                System.out.println("ERROR: No es un archivo válido: " + archivo);
                todoCorrecto = false;
            }
            else {
                System.out.println("✓ Encontrado: " + archivo);
            }
        }
        
        return todoCorrecto;
    }
}