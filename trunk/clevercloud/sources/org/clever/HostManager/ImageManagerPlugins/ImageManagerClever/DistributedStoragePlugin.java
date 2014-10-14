/*
 * Copyright 2014 Università di Messina
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
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