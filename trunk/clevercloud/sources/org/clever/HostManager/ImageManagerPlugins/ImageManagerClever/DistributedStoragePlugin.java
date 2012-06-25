
package org.clever.HostManager.ImageManagerPlugins.ImageManagerClever;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/*
 * @author valerione
 */

public interface DistributedStoragePlugin {

    public boolean mountPath(int clusterID, String host, String remotePath, String path) throws Exception;

    public void umountPath(String path) throws Exception;

    public void allowPathToHost(List hostname, String path) throws IOException,InterruptedException;

    public void denytPath(String path) throws FileNotFoundException, IOException;

    public boolean isMounted(int clusterID, String host, String remotePath) throws Exception;

    public boolean isRemote(String path, String mountPoint);

    public boolean isLocal(String path, String savePoint);
}