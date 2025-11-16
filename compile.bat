@echo off
echo Compilando Multiverse Dominion...

REM Crear directorio build si no existe
if not exist "build" mkdir build

REM Compilar sin advertencias y dirigir .class a build/
javac -cp src -d build src/main/*.java

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilacion exitosa! Los archivos .class estan en build/
    echo Para ejecutar el juego, usa: run.bat
) else (
    echo ❌ Error en la compilacion
)

pause
