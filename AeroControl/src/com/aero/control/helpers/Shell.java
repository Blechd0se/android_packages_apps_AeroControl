package com.aero.control.helpers;

import android.os.Looper;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexander Christ on 11.11.14.
 * Heavily based on the shell by Chainfire.
 */
public class Shell {

    private static final String LOG_TAG = Shell.class.getName();

    private List<String> mCommands;
    private Process mProcess = null;
    private DataOutputStream mShellOutput = null;

    /**
     * Starts a background thread which creates a new shell. Also
     * queues up the first startup command for the shell (can be either
     * sh or su).
     * The main process is also initiated together with the output
     * stream. There are no threads for the input/error stream from the
     * process since it adds an unnecessary overhead to the shell.
     *
     * @param commands String, which can be either "sh" or "su"
     * @param runOnOwnThread boolean, should run in its own thread?
     */
    public Shell(final String commands, boolean runOnOwnThread) {

        mCommands = new ArrayList<String>();

        if (!runOnOwnThread) {
            initInteractive(commands);
        } else {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        initInteractive(commands);
                    } catch (ShellException e) {
                        Log.e(LOG_TAG, "No shell was created.", e);
                    }
                }
            };
            Thread worker = new Thread(run);
            worker.start();
        }
    }

    private void checkUIThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // We are on our main UI thread, crash!
            throw new ShellException(ShellException.MAIN_UI_EXCEPTION);
        }
    }

    private synchronized void initInteractive(String su) {

        checkUIThread();

        try {
            mProcess = Runtime.getRuntime().exec(su);
            mShellOutput = new DataOutputStream(mProcess.getOutputStream());
        } catch (IOException e) {
            throw new ShellException(ShellException.NO_INTERACTIVE_SHELL);
        }
    }

    /**
     * Adds a single command to the command list, which will be executed
     * and cleaned in the next run.
     *
     * @param cmd String, which contains the full command
     */
    public synchronized void addCommand(String cmd) {
        this.mCommands.add(cmd);
    }

    /**
     * Adds a string list to the command list, which will be executed and
     * cleaned in the next run.
     *
     * @param cmds List<String>, which contains the full command(s).
     */
    public synchronized void addCommand(List<String> cmds) {
        this.mCommands.addAll(cmds);
    }

    /**
     * Adds a string list to the command list, which will be executed and
     * cleaned in the next run.
     *
     * @param cmds List<String>, which contains the full command(s).
     */
    public synchronized void addCommand(String[] cmds) {
        for (String cmd : cmds) {
            if (cmd != null)
                this.mCommands.add(cmd);
        }
    }

    /**
     * Executes the collected commands and cleans the command list afterwards.
     * After Execution, the shell stays alive until it is killed.
     */
    public void runInteractive() {

        List<String> commands = Collections.synchronizedList(mCommands);

        try {
            for (String cmd : commands) {
                mShellOutput.write((cmd + "\n").getBytes("UTF-8"));
                mShellOutput.flush();
            }
            try {
                mShellOutput.flush();
            } catch (IOException e) {}
        } catch (IOException e) {
            Log.e(LOG_TAG, "Something interrupted our operations...", e);
        }
        mCommands.clear();
    }

    /**
     * Actually "kills" the interactive shell because waitFor() might
     * need some time until its "killed".
     */
    public void closeInteractive() {

        try {
            mShellOutput.close();
        } catch (IOException e) {}
        mProcess.destroy();
    }

    /**
     * Exception-Class
     */
    public static class ShellException extends RuntimeException {

        public static final String MAIN_UI_EXCEPTION = "You have tried to execute your commands in the" +
                " main UI Thread. Consider using async-tasks or a thread instead.";

        public static final String NO_INTERACTIVE_SHELL = "The interactive shell couldn't be created";

        public ShellException(String message) {
            super(message);
        }
    }

}