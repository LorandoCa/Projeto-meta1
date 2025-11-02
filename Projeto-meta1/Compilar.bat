@echo off
echo Compilando código...
javac -cp ".;lib/jsoup-1.21.2.jar" -d out *.java
if %errorlevel% equ 0 (
  echo Compilação concluída com sucesso.
) else (
  echo Erro na compilação.
)
pause
