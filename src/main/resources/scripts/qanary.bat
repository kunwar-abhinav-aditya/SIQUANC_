@echo off
for /F "tokens=1-5* delims=," %%A in (scripts/queries.csv) do (
   echo %%~B >> scripts/abc.csv
)