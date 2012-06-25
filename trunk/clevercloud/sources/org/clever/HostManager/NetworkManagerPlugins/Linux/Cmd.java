/*
 *  Copyright (c) 2010 Patrizio Filloramo
 *  Copyright (c) 2010 Salvatore Barbera
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
package org.clever.HostManager.NetworkManagerPlugins.Linux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class Cmd
{
	static private String errorStr;
	static List executeCommand(String command)
	{
		String line = "";
		errorStr = new String();
		List execStr = new ArrayList();
		try
		{
			//System.out.println("INPUT >>> " + command);
			Process proc = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			while ((line = input.readLine()) != null)
			{
				//System.out.println("OUTPUT >>> " + line);
				execStr.add(line);
			}
			input.close();
			while ((line = error.readLine()) != null)
			{
				errorStr = errorStr.concat(line + "\n");
				execStr.add(line);
			}
			if(errorStr.equals("")) errorStr = "Unknown";
			NetworkManagerLinux.setErrorStr(errorStr);
			error.close();
		}
		catch (Exception err)
		{
			err.printStackTrace();
		}
		return execStr;
	}

	static Process runProcess(String command)
	{
		Process proc = null;
		try
		{
			proc = Runtime.getRuntime().exec(command);
		}
		catch (Exception err)
		{
			err.printStackTrace();
		}
		return proc;
	}

}
