package org.clever.HostManager.ImageManagerPlugins.ImageManagerClever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/*
 * @author valerione
 */
/*
 * Dalla classe ImageManager mi viene passato il punto di mount radice dentro al quale monto
 * utilizzando delle cartelle rinominate allo stesso modo dell'hostname da cui devo montare
 * in modo da generare una nuova cartella per ogni montaggio
 *
 * La gestione degli accessi avviene principalmente attraverso la gestione del file /etc/exports.
 * Per evitare inconvenienti con le varie versioni dei demoni che gestiscono NFS su sistemi linux
 * è stata aggiunta la gestione dei file /etc/hosts.allow, /etc/hosts.deny
 */

public class LinuxNFS implements DistributedStoragePlugin {
    private Runtime shell;

    //  ------COSTRUTTORE------
    public LinuxNFS() throws IOException {
        shell = Runtime.getRuntime();        
        //CleanExport();
        //CleanAccess();
    }

    @Override
    public boolean mountPath(int clusterID, String host, String remotePath, String path) throws IOException, InterruptedException {
        /*
         * La variabile "String host" è l'hostname dell'host sulla rete
         * Il montaggio del remotePath avviene in un cartella rinominata col nome
         * dell'hostname dentro al mountPoint, che è il percorso "radice" di tutti i mount
         *
         * Nel comando mount non sono specificate opzioni (parametro -o) così quando viene
         * montato il path, vengono ereditati i permessi che il server ha deciso di concedere.
         */

        String[] command = new String[2];
        //Creo la cartella che ospiterà il montaggio
        command[0] = "mkdir " + path + "/" + host;
        //Monto il percorso
        command[1] = "mount -t nfs " + host + ":" + remotePath + " " + path + "/" + host;

        Process p = this.shell.exec(command);        
        p.waitFor();        
        return isMounted(clusterID, host, remotePath);
    }

    @Override
    public void umountPath(String path) throws IOException, InterruptedException {
        String[] command = new String[2];
        //Smonto il percorso
        command[0] = "umount " + path;
        //Cancello la cartella
        command[1] = "rmdir " + path;
        
        Process p = this.shell.exec(command);
        p.waitFor();
    }

    @Override
    //Lettura dell'output del comando 'df' da shell Linux
    //ClusterID non so che farmene
    public boolean isMounted(int clusterID, String host, String remotePath) throws IOException, InterruptedException {
        Process p = this.shell.exec("df");

        //Preparo i buffer per la lettura sul processo
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        p.waitFor();

        //Scorro il file righa dopo righa
        String s = "";
        while ((s = br.readLine()) != null)
        {
            if (SearchOnString(s, host + ":" + remotePath))
            {
                //chiudo il buffer
                br.close();
                return true;
            }
        }
        //chiudo il buffer
        br.close();
        //Se sono qui è perchè la ricerca ha dato esito negativo quindi FALSE
        return false;
    }
    
    @Override
    public boolean isRemote(String path, String mountPoint) {
         // Verifico che nel percorso da verificare sia inclusa la radice di montaggio
        return (path.indexOf(mountPoint) != -1) ? true : false;
    }
    
    @Override
    public boolean isLocal(String path, String savePoint) {
         return (path.indexOf(savePoint) != -1) ? true : false;
    }

    @Override
    //Rende un path locale montabile dall'esterno
    //Modifica del file /etc/exports
    public void allowPathToHost(List hostname, String path) throws IOException,InterruptedException {
        /*
         * E necessario un controllo che l'array IP non sia vuoto
         * in quanto l'inserimento del path senza indicazione sulle macchine
         * rende il path stesso pubblico e disponibile al mount per qualunque macchina sulla rete mondiale
        */
        if (!(hostname.size() > 0))
            return;

        //Preparo il file per la scrittura
        PrintWriter pw = new PrintWriter(new FileWriter("/etc/exports", true));

        //Costruisco la stringa che andrà inserita nel file /etc/exports
        String record_export = path;
        for (int i = 0; i < hostname.size(); i++)
        {
            //Le indicazioni sui permessi (rw,hard,intr) sono consigliate dal manuale
            record_export = record_export + " " + hostname.get(i) + "(rw,hard,intr)";
        }

        //Scrivo la stringa come nuova righa nel file
        pw.println(record_export);
        //Chiudo
        pw.close();

        //Inserisco gli IP nella lista dei permessi qualora non ci fossero
        AllowAccessIP(hostname);

        //Questo comando aggiorna il file /etc/export e rende effettive le modifiche
        Process p = this.shell.exec("exportfs -ra");
        p.waitFor();
    }

    @Override
    //Rimuove un path locale dalla lista di quelli condivisibili per il mount
    //Modifica del file /etc/exports
    public void denytPath(String path) throws FileNotFoundException, IOException {
        //Cancellare il record da /etc/exports
        //Uso un ArrayList

        ArrayList righe = new ArrayList();  //Conterrà le righe del file exports
        String str = "";    //Variabile di appoggio

        Scanner sc = new Scanner(new File("/etc/exports"));
        while (sc.hasNextLine())
        {
            str = sc.nextLine();
            if (!SearchOnString(str, path))
                righe.add(str);
        }
        //Chiudo
        sc.close();

        //Apro il file cancellandone il contenuto
        PrintWriter pw = new PrintWriter(new FileWriter("/etc/exports"));
        //Riscrivo il file
        for (int i = 0; i < righe.size(); i++)
            pw.println(righe.get(i));

        pw.close();
    }

    //Concede l'accesso agli host indicati nella lista che prende come parametro
    //aggiungendoli al file /etc/hosts.allow
    public void AllowAccessIP(List hostname) throws FileNotFoundException, IOException {
        if (!(hostname.size() > 0))
            return;

        //Preparo il file per la scrittura
        PrintWriter pw = new PrintWriter(new FileWriter("/etc/hosts.allow", true));
        
        //Preparo il file per l'analisi
        Scanner sc = new Scanner(new File("/etc/hosts.allow"));

        //Se il file non è vuoto mi conservo in str la stringa che lo compone
        //proseguo con l'analisi e man mano scrivo
        String str = "";
        if (sc.hasNextLine())
        {
            str = sc.nextLine();
            
            for (int i = 0; i < hostname.size(); i++)
            {
                //Se l'IP non è presente lo inserisco
                if(!SearchOnString(str, (String) hostname.get(i)))
                    pw.write(" , " + hostname.get(i));
            }
        }
        //Altrimenti inizio a comporre il record da zero
        else
        {
            str = "ALL:";
            for (int i = 0; i < hostname.size(); i++)
            {
                if (i == 0)
                    str = str + " " + hostname.get(i);
                else
                    str = str + " , " + hostname.get(i);
            }
            //Scrivo str sul file
            pw.write(str);
        }

        //Chiudo
        sc.close();
        pw.close();
    }

    //Metodo non utilizzato
    //Nega l'accesso agli host indicati nella lista che prende in ingresso
    //Modificando il file /etc/hosts.allow
    public void DenyAccessIP(List hostname) throws FileNotFoundException, IOException {
        if (!(hostname.size() > 0))
            return;

        Scanner sc = new Scanner(new File("/etc/hosts.allow"));

        //Se il file è vuoto sono già tutti negati, quindi return
        if (!sc.hasNextLine())
            return;

        StringTokenizer token = new StringTokenizer(sc.nextLine());
        String str = "ALL:";
        String tk = "";
        
        for (int k = 0; k < token.countTokens(); k++)
        {
            tk=token.nextToken();                
            if (tk.equals(",") || tk.equals("ALL:"))
                continue;

            for (int i = 0; i < hostname.size(); i++)
            {
                if (!tk.equals((String) hostname.get(i)))
                {
                    if (k == 0)
                        str = " " + tk;
                    else
                        str = str + " , " + tk;
                }
            }
        }
        sc.close();

        //Preparo il file per la scrittura cancellando il contenuto
        PrintWriter pw = new PrintWriter(new FileWriter("/etc/hosts.allow"));
        //Riscrivo il contenuto modificato
        pw.write(str);
        pw.close();
    }

    //  ------Resetta il file /etc/exports------
    public void CleanExport() throws IOException {
        //Eseguo il settaggio di /etc/exports cellandone il contenuto
        PrintWriter pw = new PrintWriter(new FileWriter("/etc/exports"));
        pw.close();
    }

    //  ------Resetta i file /etc/hosts.deny ed /etc/hosts.allow------
    public void CleanAccess() throws IOException {
        //Eseguo il settaggio di /etc/hosts.deny
        PrintWriter pw_deny = new PrintWriter(new FileWriter("/etc/hosts.deny"));
        pw_deny.write("ALL: ALL");
        pw_deny.close();

        //Eseguo il settaggio di /etc/hosts.allow cellandone il contenuto
        PrintWriter pw_allow = new PrintWriter(new FileWriter("/etc/hosts.allow"));
        pw_allow.close();
    }

    public boolean SearchOnString(String str, String path) throws IOException{
        String tk;
        StringTokenizer token = new StringTokenizer(str);

        while (token.hasMoreTokens())
        {
            tk = token.nextToken();
            //Se trovo il path torno TRUE
            if (tk.equals(path))
                return true;
        }
        //Se sono qui è perchè la ricerca ha dato esito negativo quindi FALSE
        return false;
    }
}