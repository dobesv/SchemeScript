/*
 * Copyright (c) 2004 Nu Echo Inc.
 * 
 * This is free software. For terms and warranty disclaimer, see ./COPYING
 */
package org.schemeway.plugins.schemescript.interpreter;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.schemeway.plugins.schemescript.*;
import org.schemeway.plugins.schemescript.preferences.*;

/**
 * @author Nu Echo Inc.
 */
public class SchemeInterpreterDelegate implements ILaunchConfigurationDelegate {
    
    private static String[] mEnvironment;

    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        try {
            String[] cmdline = getCommandLine();
            if (cmdline == null || cmdline.length == 0)
                throw new CoreException(new Status(Status.ERROR,
                                                   SchemeScriptPlugin.getDefault().getBundle().getSymbolicName(),
                                                   Status.OK,
                                                   "Scheme interpreter not set in the preferences",
                                                   null));

            Process inferiorProcess = DebugPlugin.exec(getCommandLine(), getWorkingDirectory(), getEnvironment());
            Map attributes = new HashMap();
            attributes.put(IProcess.ATTR_PROCESS_TYPE, "scheme");
            DebugPlugin.newProcess(launch, inferiorProcess, getInterpreterName(), attributes);
        }
        catch (CoreException exception) {
            SchemeScriptPlugin.logException("Unable to start interpreter", exception);
        }
    }

    private String[] getCommandLine() {
        String command = InterpreterPreferences.getCommandLine();
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        String[] cmdline = new String[tokenizer.countTokens()];

        for (int i = 0; i < cmdline.length; i++)
            cmdline[i] = tokenizer.nextToken();

        return cmdline;
    }
    
    private synchronized String[] getEnvironment() {
        if (mEnvironment == null) {
            Map systemEnvironment = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
            mEnvironment = new String[systemEnvironment.size() + 1];
            int index = 0;
            Iterator iterator = systemEnvironment.keySet().iterator();
            while (iterator.hasNext()) {
                String name = (String)iterator.next();
                String value = (String)systemEnvironment.get(name);
                mEnvironment[index++] = name + "=" + value;
            }
            mEnvironment[index] = "ECLIPSE=true";
        }
        return mEnvironment;
    }

    private String getInterpreterName() {
        return InterpreterPreferences.getInterpreterName();
    }

    private File getWorkingDirectory() {
        return InterpreterPreferences.getWorkingDirectory();
    }
}