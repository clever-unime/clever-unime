

OCCIDIR=$(pwd)/../clever-occi-plugins/
librerie=${OCCIDIR}/lib/*.jar
librerie=$(echo $librerie|tr ' ' :)
echo "Lancio: java -cp $librerie:dist/clever.jar:${OCCIDIR}/dist/clever-occi-plugins.jar org.clever.Common.Initiator.Main"
java -cp $librerie:dist/clever.jar:${OCCIDIR}/dist/clever-occi-plugins.jar org.clever.Common.Initiator.Main


#aggiunto per creare le dir principali di log
mkdir -p LOGS
cd LOGS
mkdir -p ClusterManager Common HostManager
######################################




