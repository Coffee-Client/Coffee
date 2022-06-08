@echo off
echo Input changelog, enter "end" when done
rem. > ./src/main/resources/changelogLatest.txt

:loop
set /p "line=>"
if "%line%" == "end" goto loopend
echo %line% >> ./src/main/resources/changelogLatest.txt
goto loop
:loopend

set /p ver=<./src/main/resources/version.txt
set /a verNew=%ver%+1
echo Version: %ver% -^> %verNew%
echo %verNew% > ./src/main/resources/version.txt

echo Running build
./gradlew build
if not exist "./bin" mkdir bin

ren ./build/libs/coffee-1.0.0.jar ./bin/latest.jar
ren ./build/devlibs/coffee-1.0.0-dev.jar ./bin/latest-sdk.jar
ren ./build/devlibs/coffee-1.0.0-sources.jar ./bin/latest-sdk-sources.jar
echo Made release
