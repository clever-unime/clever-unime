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
package org.clever.Common.VEInfo;

import java.util.List;
import java.util.ArrayList;



public class VEDescription
{

  private CpuSettings cpu;
  private MemorySettings mem;
  private ArrayList storage;
  private List network;
  private String name;
  private DesktopVirtualization desktop;
  
  //Per VMWare
  private String os_guest_id;

// metodo di prova
   public VEDescription(final MemorySettings mem)
  {

    this.mem = mem;

  }

  public VEDescription( final List storage, final List network, final String name,
                        final CpuSettings cpu, final MemorySettings mem, final DesktopVirtualization desktop)
  {
    this.name = name;
    this.network = network;
    this.storage = new ArrayList( storage );
    this.cpu = cpu;
    this.mem = mem;
    this.desktop = desktop;
  }
  
  public VEDescription( final List storage, final List network, final String name,
                        final CpuSettings cpu, final MemorySettings mem, final DesktopVirtualization desktop, final String vm_guest_id)
  {
    this.name = name;
    this.network = network;
    this.storage = new ArrayList( storage );
    this.cpu = cpu;
    this.mem = mem;
    this.desktop = desktop;
    this.os_guest_id = vm_guest_id;
  }



  public CpuSettings getCpu()
  {
    return cpu;
  }


  public void setCpu( CpuSettings cpu )
  {
    this.cpu = cpu;
  }



  public MemorySettings getMemorySettings()
  {
    return mem;
  }



  public void setMemorySettings( MemorySettings mem )
  {
    this.mem = mem;
  }



  public List getNetwork()
  {
    return network;
  }



  public void setNetwork( List network )
  {
    this.network = network;
  }



  public List getStorage()
  {
    return storage;
  }



  public void setStorage( List storage )
  {
    this.storage = new ArrayList( storage );
  }



  public String getName()
  {
    return name;
  }



  public void setName( String name )
  {
    this.name = name;
  }

  public DesktopVirtualization getDesktopVirtualization()
  {
    return this.desktop;
  }

  public void setDesktopVirtualization(DesktopVirtualization desktop)
  {
    this.desktop = desktop;
  }
  
  public void setOSGuestID(String vm_guest_id){
      this.os_guest_id = vm_guest_id;
  }
  
  public String getOSGuestID(){
      return this.os_guest_id;
  }


  public void toFile( String path ) throws Exception
  {
    throw new Exception( "not supported yet" );
  }


  static public VEDescription ReadFromFile( String path ) throws Exception
  {
    throw new Exception( "not supported yet" );
  }
  
}