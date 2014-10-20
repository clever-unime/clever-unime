echo "Elimino il log"
rm -f cleverAdministration.log
echo "Lancio: java -jar dist/cleveradministration.jar cfg/clever_client.xml"
java -jar dist/cleveradministration.jar cfg/clever_client.xml
