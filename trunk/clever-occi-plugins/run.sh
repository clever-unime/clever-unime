DIR=$(pwd)
librerie=${DIR}/lib/*.jar
librerie=$(echo $librerie|tr ' ' :)
java -cp ./:$librerie:${DIR}/../CleverCommon/dist/CleverCommon.jar:dist/clever-occi-plugins.jar org.clever.HostManager.HyperVisorPlugins.OCCI.TestPlugin ${DIR}/../clevercloud/cfg/configuration_hypervisor.xml

