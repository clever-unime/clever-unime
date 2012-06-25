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

package org.clever.HostManager.MonitorPlugins.Sigar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.ProcTime;
import org.hyperic.sigar.ProcUtil;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.SigarCommandBase;


public class ProcessInfo extends SigarCommandBase {

    private static long[] ids;
    private static ProcCredName cred;
    private static ProcState statepid;
    private static ProcTime STime;
    private static ProcMem mem;
    private static String name;

    public ProcessInfo() throws SigarException  {

        ids =sigar.getProcList();
    }

    public List listProcesses() {

            List procNames = new ArrayList();

            for (int i = 0; i < ids.length; i++) {
                try {

                  procNames.add( getPID(i) +
                                    " " + getUser((int) ids[i]) +
                                    " " + getStartTime((int) ids[i]) +
                                    " " + Sigar.formatSize((long) getMemoryUsage((int) ids[i]))+
                                    " " + Sigar.formatSize((long) getSwapDimension((int) ids[i])) +
                                    " " + getState((int) ids[i]) +
                                    " " + getPriority((int) ids[i]) +
                                    " " + getExecutionTime((int) ids[i]) +
                                    " " + getCommand((int) ids[i]));

                } catch (SigarException e) {
                    procNames.add(ids[i] + ":" + e.getMessage());
                }
            }

            return procNames;

    }


    public int getPID() {

         return (int) sigar.getPid();

    }

    public int getPID(int i) {

         return (int) ids [i];

    }

    public String getUser(int pid) throws SigarException{

         try {
                cred = sigar.getProcCredName(pid);
                name = cred.getUser();
         } catch (Exception e) {}

        return name;

    }

    public char getState(int pid) throws SigarException{
        statepid = sigar.getProcState(pid);
        return statepid.getState();

    }

    public int getPriority(int pid) throws SigarException{
        statepid = sigar.getProcState(pid);
        return statepid.getPriority();

    }


    public float getMemoryUsage(int pid) throws SigarException{
        mem = sigar.getProcMem(pid);
        return new Float (mem.getSize());

    }



    public float getSwapDimension(int pid) throws SigarException{
        mem = sigar.getProcMem(pid);
        return new Float (mem.getVsize()-mem.getResident());

    }


    public String getStartTime(int pid) throws SigarException{
        STime = sigar.getProcTime(pid);
        return StartTime(STime.getStartTime());

    }

    public String getExecutionTime(int pid) throws SigarException{
        STime = sigar.getProcTime(pid);
        return getCpuTime(STime);

    }

    public String getCommand(int pid) throws SigarException{
        name = ProcUtil.getDescription(sigar, pid);
        return name;
    }


    public static String getCpuTime(long total) {
        long t = total / 1000;
        return t/60 + ":" + t%60;
    }

    public static String getCpuTime(ProcTime time) {
        return getCpuTime(time.getTotal());
    }


    public static String StartTime(long time) {
        if (time == 0) {
            return "00:00";
        }
        long timeNow = System.currentTimeMillis();
        String fmt = "MMMd";

        if ((timeNow - time) < ((60*60*24) * 1000)) {
            fmt = "HH:mm";
        }

        return new SimpleDateFormat(fmt).format(new Date(time));
    }

    @Override
    public void output(String[] strings) throws SigarException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}