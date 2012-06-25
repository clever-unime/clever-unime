/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.clever.ClusterManager.StorageManagerPlugins.StorageManagerClever;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HLComparer implements HostLoadComparer, Comparator<HostInfo>{

    public int compare(HostInfo hostA, HostInfo hostB) {
        HostLoad hla_current = hostA.getCurrentLoad();
        HostLoad hlb_current = hostB.getCurrentLoad();
        HostLoad hla_estimated = hostA.getEstimatedLoad();
        HostLoad hlb_estimated = hostB.getEstimatedLoad();
        float hostA_current_coefficient = (float) (0.5*hla_current.getStorage() + 0.3*hla_current.getMemory() + 0.2*hla_current.getCpu()) / 100;
        float hostA_estimated_coefficient = (float) (0.5*hla_estimated.getStorage() + 0.3*hla_estimated.getMemory() + 0.2*hla_estimated.getCpu()) / 100;
        float hostB_current_coefficient = (float) (0.5*hlb_current.getStorage() + 0.3*hlb_current.getMemory() + 0.2*hlb_current.getCpu()) / 100;
        float hostB_estimated_coefficient = (float) (0.5*hlb_estimated.getStorage() + 0.3*hlb_estimated.getMemory() + 0.2*hlb_estimated.getCpu()) / 100;
        float hostA_final_coeff = (float) (0.2*hostA_estimated_coefficient + 0.8*hostA_current_coefficient);
        float hostB_final_coeff = (float) (0.2*hostB_estimated_coefficient + 0.8*hostB_current_coefficient);
        if(hostA_final_coeff > hostB_final_coeff){
            return 1;
        }
        else{
            return -1;
        }
    }

    public HostInfo getMinLoaded(List host) {
        Collections.sort(host, new HLComparer());
        return(HostInfo) (host.get(0));
    }

    public List orderByAscending(List hosts) {
        Collections.sort( hosts, new HLComparer() );
        return(hosts);
    }



}
