/*
 * Copyright (c) 2002-2003 Nu Echo Inc. All rights reserved.
 */
package org.schemeway.plugins.schemescript.interpreter;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.*;
import org.eclipse.ui.console.*;
import org.schemeway.plugins.schemescript.*;

/**
 * @author Nu Echo Inc.
 */
public class KawaInterpreter implements Interpreter
{
    public static final String CONFIG_TYPE = SchemeScriptPlugin.PLUGIN_NS + ".kawaInterpreter";

    public KawaInterpreter()
    {
        super();
    }

    public void showConsole()
    {
        IConsole console = DebugUITools.getConsole(KawaProcess.getInstance());
        ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
    }

    public boolean isRunning()
    {
        return true;
    }

    public void start()
    {
        try
        {
            ILaunchConfigurationType configType = DebugPlugin.getDefault()
                                                             .getLaunchManager()
                                                             .getLaunchConfigurationType(CONFIG_TYPE);
            ILaunchConfigurationWorkingCopy copy = configType.newInstance(null, "");
            copy.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
            showConsole();
        }
        catch (CoreException exception)
        {
        }
    }

    public void stop()
    {
    }

    public void restart()
    {
        start();
    }

    public boolean supportInterruption()
    {
        return false;
    }

    public void interrupt()
    {
    }

    public void eval(String code)
    {
        KawaProcess.eval(code);
    }

    public void load(IFile file)
    {
        String filename = file.getRawLocation().toString();
        eval("(load \"" + filename + "\")");
    }
}