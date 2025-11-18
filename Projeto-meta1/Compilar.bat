@echo off
echo ======================================
echo  Compilando código fonte Java...
echo ======================================

REM Cria diretório de saída se não existir
if not exist out mkdir out

javac -encoding UTF-8 -cp ".;lib/jsoup-1.21.2.jar" -d out *.java
if %errorlevel% neq 0 (
    echo.
    echo ERRO: Falha na compilação! Verifique o código.
    pause
    exit /b
)

echo.
echo Compilação concluída com sucesso.


echo ======================================
echo  Gerando documentação Javadoc...
echo ======================================

REM Cria diretório docs se não existir
if not exist docs mkdir docs

javadoc -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 -d docs -cp ".;lib/jsoup-1.21.2.jar" *.java
if %errorlevel% neq 0 (
    echo.
    echo  Javadoc gerado com avisos/erros, mas pasta docs foi criada.
) else (
    echo Documentação Javadoc gerada com sucesso na pasta /docs
)

echo.
pause
