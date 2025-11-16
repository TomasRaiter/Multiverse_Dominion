@echo off
echo Ejecutando Multiverse Dominion...

REM Verificar que existe build/
if not exist "build" (
    echo ❌ No se encontro el directorio build/
    echo Ejecuta compile.bat primero para compilar el proyecto
    pause
    exit /b 1
)

REM Ejecutar el juego con classpath apuntando a build/ y src/ para recursos
java -cp "build;src" main.Main

pause
