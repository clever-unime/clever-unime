package org.clever.administration.api;

import org.clever.administration.annotations.GetShellModule;
import org.clever.administration.api.modules.AdministrationModule;
import org.clever.administration.api.modules.BigDataModule;
import org.clever.administration.api.modules.ObjectManagerModule;
import org.clever.administration.api.modules.HostAdministrationModule;
import org.clever.administration.api.modules.MonitoringAdministrationModule;
import org.clever.administration.api.modules.StrorageAdministrationModule;
import org.clever.administration.api.modules.VMAdministrationModule;
import org.clever.administration.api.modules.SensingAdministrationModule;
import org.clever.administration.api.modules.ObjectStorageSwiftModule;
/**
 * Classe che permette di invocare le API
 * @author maurizio
 * @author giuseppe
 * @author antonio
 * @author riccardo
 * 
 */
final public class Session {
    
    final AdministrationModule module;
    final HostAdministrationModule hostAdministrationModule;
    final VMAdministrationModule vMAdministrationModule;
    final StrorageAdministrationModule SMAdministrationModule;
    final MonitoringAdministrationModule monitoringAdministrationModule;
    final SensingAdministrationModule sensingAdministrationModule;
    final BigDataModule bigDataModule;
    final ObjectStorageSwiftModule  swiftAdministrationModule;
    final Settings settings;
    final ObjectManagerModule objectManagerModule;

    public Settings getSettings() {
        return settings;
    }
    
    
    
    //per ora faccio un meccanismo molto spartano non considerando il modulo passato
    public Session(Settings s)
    {
        settings = s;
        module = new AdministrationModule(this);
        hostAdministrationModule = new HostAdministrationModule(this);
        vMAdministrationModule = new VMAdministrationModule(this);
        SMAdministrationModule= new StrorageAdministrationModule(this);
        monitoringAdministrationModule = new MonitoringAdministrationModule(this);
        sensingAdministrationModule= new SensingAdministrationModule(this);
        swiftAdministrationModule= new ObjectStorageSwiftModule(this);
        objectManagerModule=new ObjectManagerModule(this);
        bigDataModule=new BigDataModule(this);
    }
    
    /**
     * Ritorna l'administrationmodule per invocare le funzionalita' 
     * per ora ritorna un solo modulo ignorando il nome passato. Si potra' invocare un metodo generico di un agente di un'entita' (CM o HM), passandogli i parametri
     * @param moduleName
     * @return 
     */
    public AdministrationModule getModule(String moduleName)
    {
        return module;
        
    }

    
    /**
     * Ritorna un HostAdministrationModule per gestirele entita' clever a livello di host
     * per es.: getActiveCM, listHostManagers,ecc.
     * @return 
     */
    @GetShellModule(name="ham", comment="Low level module for clever host administration")
    public HostAdministrationModule getHostAdministrationModule() {
        return hostAdministrationModule;
    }
    
    
    /**
     * Ritorna un VMAdministrationModule per gestire le VM
     * 
     * @return 
     */
    @GetShellModule(name="vmm", comment="Low level module for VM administration")
    public VMAdministrationModule getVMAdministrationModule() {
        return vMAdministrationModule;
    }
    
    /**
     * Return a StrorageAdministrationModule for Virtual File System Management
     * 
     * @return 
     */
    @GetShellModule(name="sma", comment="Low level module for SM administration")
    public StrorageAdministrationModule getSMAdministrationModule() {
        return SMAdministrationModule;
    }
    
    /**
     * Return a MonitoringAdministrationModule for CLEVER Monitoring
     * 
     * @return 
     */
    @GetShellModule(name="mam", comment="Low level module for Monitoring administration")
    public MonitoringAdministrationModule getMonitoringAdministrationModule() {
        return monitoringAdministrationModule;
    }
    /**
     * Ritorna un HostAdministrationModule per gestirele entita' clever a livello di host
     * per es.: getActiveCM, listHostManagers,ecc.
     * @return 
     */
    @GetShellModule(name="seam", comment="Low level module for Sensing Administration Module")
    public SensingAdministrationModule getSensingAdministrationModule() {
        return sensingAdministrationModule;
    }
    /**
     * Ritorna un BIGAdministrationModule per gestire le entita' clever a livello di host
     * per es.: getActiveCM, listHostManagers,ecc.
     * @return 
     */ 
    @GetShellModule(name="bdm", comment="Low level module for clever host administration")
    public BigDataModule getBIGAdministrationModule() {
        return bigDataModule;
    }
    
    /**
     * Ritorna un BIGAdministrationModule per gestire le entita' clever a livello di host
     * per es.: getActiveCM, listHostManagers,ecc.
     * @return 
     */ 
    @GetShellModule(name="omm", comment="Low level module for clever host administration")
    public ObjectManagerModule getOMMAdministrationModule() {
        return objectManagerModule;
    }
    
    
    
    @GetShellModule(name="swf", comment="Low level module for Swift module")
    public ObjectStorageSwiftModule getSWFdministrationModule() {
        return swiftAdministrationModule;
    }
}
