
# REQUIRIMIENTOS:

Tienes que tener instalado jdk-25 y junit standalone-1.9.3.java. Con esto podrás compilar y ejecutar el código incluido los test.

Adjunto enlaces si no lo tienes descargado.

# JDK-25 :

https://www.oracle.com/es/java/technologies/downloads/

Entrando a este enlace y buscando más abajo lo puedes descargar. Y depende del sistema operativo que tengas.

# junit standalone 1.9.3 :

https://mvnrepository.com/artifact/org.junit.platform/junit-platform-console-standalone/1.9.3

¡IMPORTANTE! Cuando entres en la página en el apartado FILES tienes que pulsar jar.

Para instarlarlo tienes que tener GIT y para poder tenerlo tendras que usar el comando git clone

git clone https://github.com/jrmartinezs01/MEMES8M.git


# VERIFICACIÓN (Para verificar si es la ultima version del java y git. Tienes que ir al cmd y utilizar estos comandos.)

java -version

# Debe mostrar: java version "25" o superior

git --version
# Debe mostrar la versión instalada


ESTRUCTURA DE ARCHIVOS

MEMES8M/
├── datos/                          
│   ├── memes.txt                    
│   ├── realidades.json               
│   └── soluciones.xml                
├── fuentes/
│   └── ProyectoMeme.java
├── test/                           
│   └── ProyectoMemeTest.java         
└── lib/                             
    └── junit-platform-console-standalone-1.9.3.jar



COMPILACIÓN (Recomendamos compilar los test primero y ejecutarlos)

# Compilar incluyendo JUnit para los tests
javac -cp "lib/junit-platform-console-standalone-1.9.3.jar" fuentes/*.java test/*.java

# Compilar sin tests (solo el programa principal)
javac fuentes/ProyectoMeme.java

EJECUCIÓN

# Ejecutar los tests
java -jar lib/junit-platform-console-standalone-1.9.3.jar --class-path . --scan-class-path

# Ejecutar el programa principal
java -cp . fuentes.ProyectoMeme

DESISTALACIÓN (Desde el cmd)

# Opción 1: Borrar la carpeta del proyecto
rm -rf MEMES8M/           # En Linux/macOS
rmdir /s MEMES8M          # En Windows

# Opción 2: Solo borrar archivos compilados (limpieza)
rm -rf fuentes/*.class    # En Linux/macOS
del /s fuentes\*.class    # En Windows


VERIFICACIÓN DE INSTALACIÓN CORRECTA

# Compilar y ejecutar en un solo comando
javac fuentes/ProyectoMeme.java && java -cp . fuentes.ProyectoMeme

