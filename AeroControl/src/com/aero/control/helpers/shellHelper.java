package com.aero.control.helpers;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alexander Christ on 18.09.13.
 * Summary of various shell-Methods, for easy method calling
 */
public final class shellHelper {

    // Buffer length;
    private static final int BUFF_LEN = 8192;
    private static final byte[] buffer = new byte[BUFF_LEN];
    private static final String LOG_TAG = shellHelper.class.getName();
    private ShellWorkqueue shWork = new ShellWorkqueue();
    private static shellHelper mShellHelper;

    private List<String> mCommands;
    private Process mProcess = null;
    private DataOutputStream mShellOutput = null;
    private BufferedReader mOutput = null;

    private final static String NO_DATA_FOUND = "Unavailable";


    private shellHelper() {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                openShell();
            }
        };
        Thread worker = new Thread(run);
        worker.start();
    }

    /**
     * Returns a interactive shell. If no shell objects existed previously,
     * a new one is created.
     * @return shellHelper
     */
    public static synchronized shellHelper instance() {
        if (mShellHelper == null) {
            mShellHelper = new shellHelper();
        }

        return mShellHelper;
    }

    /**
     * Forcefully creates a new shell, this shouldn't be used.
     * @return shellHelper
     */
    public static synchronized shellHelper forceInstance() {

        mShellHelper = new shellHelper();

        return mShellHelper;
    }

    /**
     * Adds commands to our queue for our shell.
     * @param commands String[]
     */
    private synchronized void addCommands(String[] commands) {
        for (String cmd : commands) {
            if (cmd != null)
                this.mCommands.add(cmd);
        }
    }

    /**
     * Adds a single command to our queue for our shell.
     * @param cmd String
     */
    public synchronized void addCommand(String cmd) {
        this.mCommands.add(cmd);
    }

    /**
     * If necessary, initiates the relevant parts for the shell to work.
     * This is only used internally.
     */
    private synchronized void openShell() {

        if (mCommands == null) {
            mCommands = new ArrayList<String>();
        }
        try {
            if (mProcess == null) {
                mProcess = Runtime.getRuntime().exec("su");
            }
            if (mShellOutput == null) {
                mShellOutput = new DataOutputStream(mProcess.getOutputStream());
            }
            if (mOutput == null) {
                mOutput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "We were not able to create a shell!", e);
        }

    }

    /**
     * Runs our commands without checking the output inside the shell.
     * This method is only used internally.
     */
    private synchronized void runCommands() {

        openShell();

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
     * Returns the output of *all* previously added commands. If one wants
     * to get the output of the shell, one should call getRootResult() directly
     * after addCommand() or addCommands().
     * @return String
     */
    private String getRootResult() {

        List<String> commands = Collections.synchronizedList(mCommands);
        char[] buf = new char[BUFF_LEN];
        int read;
        StringBuilder response = new StringBuilder();

        try {
            for (String cmd : commands) {
                mShellOutput.write((cmd + "\n").getBytes("UTF-8"));

                while(true){
                    read = mOutput.read(buf);
                    if (read == -1) {
                        return null;
                    }

                    response.append(buf, 0, read);

                    if(read < BUFF_LEN){
                        //we have read everything
                        break;
                    }
                }

                mShellOutput.flush();
            }
            try {
                mShellOutput.flush();
            } catch (IOException e) {}
        } catch (IOException e) {
            Log.e(LOG_TAG, "Something interrupted our operations...", e);
            return null;
        } finally {
            mCommands.clear();
        }
        return response.toString();
    }

    /*
     * Allows to simply query up commands to execute by the root process
     */
    private class ShellWorkqueue {

        private ArrayList<String> mWorkItems;

        private void addToWork(String work) {

            if (mWorkItems == null)
                initWork();
            mWorkItems.add(work);
        }

        private String[] execWork() {
            return mWorkItems.toArray(new String[0]);
        }

        private void initWork() {
            mWorkItems = new ArrayList<String>();
        }

        private void flushWork() {
            // Be super save here and check for null
            if (mWorkItems != null) {
                mWorkItems.clear();
                mWorkItems = null;
            }
        }

    }

    /**
     * Adds a work item to the current workqueue
     *
     * @param work   => work item (command for shell)
     *
     * @return nothing
     */
    public void queueWork(String work) {
        shWork.addToWork(work);
    }

    /**
     * Executes the current workqueue
     *
     * @return nothing
     */
    public void execWork() {
        shWork.addToWork("echo ");
        setRootInfo(shWork.execWork());
    }

    /**
     * Flushes the workqueue with its items
     *
     * @return nothing
     */
    public void flushWork() {
        shWork.flushWork();
    }

    /**
     * Gets the current Kernel Version + some useful information
     *
     * @return String
     */
    public final String getKernel() {
        // Taken from Androids/CM Gingerbread Branch:
        String procVersionStr;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), BUFF_LEN);
            try {
                procVersionStr = reader.readLine();
            } finally {
                reader.close();
            }

            final String PROC_VERSION_REGEX =
                    "\\w+\\s+" + /* ignore: Linux */
                            "\\w+\\s+" + /* ignore: version */
                            "([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
                            "\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /* group 2: (xxxxxx@xxxxx.constant) */
                            "\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /* ignore: (gcc ..) */
                            "([^\\s]+)\\s+" + /* group 3: #26 */
                            "(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
                            "(.+)"; /* group 4: date */

            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);

            if (!m.matches()) {
                Log.e(LOG_TAG, "Regex did not match on /proc/version: " + procVersionStr);
                return NO_DATA_FOUND;
            } else if (m.groupCount() < 4) {
                Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                        + " groups");
                return NO_DATA_FOUND;
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG,
                    "IO Exception when getting kernel version for Device Info screen",
                    e);

            return NO_DATA_FOUND;
        }
    }

    /**
     * Gets information from the filesystem with a given path
     *
     * @param s   => path (with filename)
     *
     * @return String
     */
    public final String getInfo(String s) {

        String info = NO_DATA_FOUND;

        if (s == null || !(new File(s).exists()))
            return info;

        try {
            final BufferedReader reader = new BufferedReader(new FileReader(s), BUFF_LEN);
            try {
                info = reader.readLine();
            } finally {
                reader.close();
            }

            if (info == null)
                info = NO_DATA_FOUND;

            return info;
        } catch (IOException e) {

            // Make sure that the shell is open;
            openShell();

            // At least try to read it via root, but check for permissions;
            if (!(getLegacyRootInfo("ls -l", s).substring(0, 10).equals("--w-------"))) {
                info = getLegacyRootInfo("cat", s);
            }

            if (info.equals(NO_DATA_FOUND))
                Log.e(LOG_TAG,
                        "IO Exception when trying to get information.",
                        e);

            return info;
        }
    }

    /**
     * Reads a file from a given path. It has not sanity checks and uses FileInputStream
     * to read the file.
     * @param path String, the filepath to read
     * @return String, the output from the file
     */
    public final String getFastInfo(final String path) {
        String tmp;
        try {
            final FileInputStream fis = new FileInputStream(path);
            final BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            tmp = br.readLine();

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception when trying to get information. Fallback to getInfo()", e);
            tmp = getInfo(path);
        }

        return tmp;

    }

    /**
     * Gets information from the filesystem with a given path
     *
     * @param s           => path (with filename)
     * @param deepsleep   => Should current deepsleep value be added?
     *
     * @return String[]
     */
    public final String[] getInfo(String s, boolean deepsleep) {

        String info;
        ArrayList<String> al = new ArrayList<String>();

        if (deepsleep) {
            long sleepTime = (SystemClock.elapsedRealtime()
                    - SystemClock.uptimeMillis()) / 10;
            al.add(Long.toString(sleepTime));
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(s), BUFF_LEN);
            try {
                info = reader.readLine();

                while (info != null) {
                    al.add(info);
                    info = reader.readLine();
                }

            } finally {
                reader.close();
            }
            return al.toArray(new String[0]);
        } catch (IOException e) {
            Log.e(LOG_TAG,
                    "IO Exception when trying to get information.",
                    e);

            return null;
        }
    }

    /**
     * Gets all files in a given dictionary
     *
     * @param s    => path to read the files
     * @param flag => for files or directory
     *
     * @return String[]
     */
    public final String[] getDirInfo(String s, boolean flag) {

        String[] result;

        // Return null, if the file doesn't exist
        if (!(new File(s).exists()))
            return null;

        // Handle case if files are needed;
        if (flag) {
            List<String> results = new ArrayList<String>();
            File[] files = new File(s).listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    results.add(file.getName());
                }
            }

            result = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                result[i] = results.get(i);
            }
            Arrays.sort(result);

            return result;

        } else {
            // Handle case if directory is needed;
            File file = new File (s);
            result = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return new File(file, s).isDirectory();
                }
            });
            return result;
        }
    }

    /**
     * Splits up the information from a string in more usable values
     * and returns this array.
     *
     * @param s         => string used (from a given path)
     * @param flag      => set to 1 to convert it with @toMHZ
     * @param flag_io   => set to 1 to read the io_schedulers
     * @return String[]
     */

    private String[] buildArray(String s, int flag, int flag_io) {

        String[] completeString = new String[0];
        String[] output;

        // Make sure the last value is not a line feed;
        if ((int)s.charAt(s.length() - 1) == 10) {
            s = s.replace(Character.toString((char)10), "");
        }

        if (flag_io == 1)
            completeString = s.replace("[", "").replace("]", "").split(" ");
        else if (flag_io == 0)
            completeString = s.split(" ");

        output = new String[completeString.length];
        output[0] = NO_DATA_FOUND;

        for (int i = 0; i < output.length; i++) {
            if (flag == 1)
                output[i] = toMHz(completeString[i]);
            else
                output[i] = completeString[i];
        }

        return output;
    }

    /**
     * This Method returns an Array, useful for the frequency list of the kernel
     *
     * @param s       => string used (from a given path)
     * @param flag    => set to 1 to convert it with @toMHZ
     * @param flag_io => set to 1 to read the io_schedulers
     *
     * @return String[]
     */
    public final String[] getInfoArray(String s, int flag, int flag_io) {

        String[] output = new String[] { NO_DATA_FOUND };
        String tmp;

        try {
            // Try to read the given Path, if not available -> throw exception
            BufferedReader reader = new BufferedReader(new FileReader(s), BUFF_LEN);
            try {
                output = buildArray(reader.readLine(), flag, flag_io);
            } finally {
                reader.close();
            }

            return output;
        } catch (IOException e) {

            // Make sure that the shell is open;
            openShell();

            // At least try to read it via root, but check for permissions;
            if (!(getRootInfo("ls -l", s).substring(0, 10).equals("--w-------"))) {
                tmp = getRootInfo("cat", s);
                output = buildArray(tmp, flag, flag_io);
            }

            if (output[0].equals(NO_DATA_FOUND))
                Log.e(LOG_TAG,
                        "IO Exception when trying to get information.",
                        e);

            return output;
        }
    }

    /**
     * Finds a String between two values (for now only used in io_schedulers)
     *
     * @param s   => string used (from a given path)
     *
     * @return String
     */
    public final String getInfoString(String s) {

        int open, close;
        String finalString = NO_DATA_FOUND;

        open = s.indexOf("[");
        close = s.lastIndexOf("]");
        if (open >= 0 && close >= 0) {
            finalString = s.substring(open + 1, close);
            return finalString;
        } else {
            return finalString;
        }
    }

    /**
     * Converts raw frequencies to userfriendly values
     *
     * @param mhzString   => String with frequencies
     *
     * @return String
     */
    public final String toMHz(String mhzString) {

        if (mhzString.equals(NO_DATA_FOUND) ||
                mhzString.equals("Unavaila"))
            return NO_DATA_FOUND;

        try {
            if (mhzString.length() < 8)
                return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz")
                        .toString();
            else
                return new StringBuilder().append(Integer.valueOf(mhzString) / 1000000).append(" MHz")
                        .toString();
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG,
                    "Tried to add something to a non existing string.",
                    e);
            return NO_DATA_FOUND;
        }
    }

    /**
     * Gets the total amount of memory and the total amount
     * of truly free memory.
     *
     * @param s   => path to read
     *
     * @return String
     */
    public final String getMemory(String s) {
        String totalMemory;
        String totalFreeMemory;

        try {
            /* /proc/meminfo entries follow this format:
             * MemTotal:         362096 kB
             * MemFree:           29144 kB
             * Buffers:            5236 kB
             * Cached:            81652 kB
             */
            BufferedReader reader = new BufferedReader(new FileReader(s), BUFF_LEN);
            totalMemory = reader.readLine();
            totalFreeMemory = reader.readLine();

            if (totalMemory != null && totalFreeMemory != null) {
                String parts[] = totalMemory.split("\\s+");
                if (parts.length == 3) {
                    totalMemory = Long.parseLong(parts[1]) / 1024 + " MB";
                }
                parts = totalFreeMemory.split("\\s+");
                if (parts.length == 3) {
                    totalFreeMemory = Long.parseLong(parts[1]) / 1024 + " MB";
                }
            }

        } catch (IOException e) {
            Log.e(LOG_TAG,
                    "Yep, i can't read your memory stats :( .",
                    e);
            return NO_DATA_FOUND;
        }

        return totalFreeMemory + " / " + totalMemory;
    }

    /**
     * Generic Method for setting values in kernel with "echo" command.
     * This method is just a wrapper for runCommands().
     *
     * @param command   => (Object) and the value (echo value >)
     * @param content   => the path to set the value (echo > path)
     */
    public synchronized final void setRootInfo(final String command, final String content) {

        String tmp;
        String[] commands;
        // Check if last char is a whitespace;
        tmp = command.substring(command.length() - 1);
        if (tmp.matches("^\\s*$")) {
            tmp = command.substring(0, command.length() - 1);
        } else {
            tmp = command;
        }

        commands = new String[]{
                "chmod 0666 " + content,
                "echo \"" + tmp + "\" " + "> " + content
        };

        addCommands(commands);
        runCommands();
    }
    /**
     * Generic Method for setting a bunch of commands
     * Same as setRootInfo but with an array instead of objects.
     * This method is just a wrapper for runCommands().
     *
     * @param array   => Commands to execute in an array
     */
    public final void setRootInfo(final String array[]) {

        addCommands(array);
        runCommands();
    }

    /**
     * Remounts the system (on the Defy) via runCommands();
     * TODO: Adjust this for the according device.
     *
     */
    public final void remountSystem() {

        addCommand("mount -o remount,rw -t ext3 /dev/block/mmcblk1p21 /system");
        runCommands();
    }

    /**
     * Executes a command in Terminal and returns output.
     * This method is just a wrapper for getRootResult().
     *
     * @param command   => set the command to execute
     * @param parameter => set a parameter
     *
     * @return String
     */
    public final String getRootInfo(String command, String parameter) {

        String ret = null;

        addCommand(command + " " + parameter);
        ret = getRootResult();

        if (ret == null) {
            ret = NO_DATA_FOUND;
        }

        return ret;
    }


    /**
     * This method returns an Array, with root rights.
     * This method is just a wrapper for getRootResult().
     *
     * @param command   => the actual command which runs with root-rights
     * @param split     => delimiter, which seperates the strings (mostly \n)
     *
     * @return String[]
     */
    public final String[] getRootArray(String command, String split) {

        ArrayList<String> temp = new ArrayList<String>();
        String ret = null;

        addCommand(command);
        ret = getRootResult();

        for (String a : ret.split(split)) {
            temp.add(a);
        }

        return temp.toArray(new String[0]);
    }

    /**
     * Executes a command in Terminal and returns output. Its only used internally for "legacy" devices/operations.
     *
     * @param command   => set the command to execute
     * @param parameter => set a parameter
     *
     * @return String
     */
    public String getLegacyRootInfo(String command, String parameter) {

        Process rooting = null;

        try {
            rooting = Runtime.getRuntime().exec("su");

            DataOutputStream stdin = new DataOutputStream(rooting.getOutputStream());

            stdin.writeBytes(command + " " + parameter + "\n");
            InputStream stdout = rooting.getInputStream();
            int read;
            String output = "";
            while(true){
                read = stdout.read(buffer);
                if (read == -1)
                    return NO_DATA_FOUND;

                output += new String(buffer, 0, read);
                if(read<BUFF_LEN){
                    //we have read everything
                    break;
                }
            }
            stdin.writeBytes("exit\n");
            return output;

        } catch (IOException e) {
            Log.e(LOG_TAG, "Do you even root, bro? :/", e);
        } finally {
            if (rooting != null) {
                try {
                    rooting.waitFor();
                } catch (InterruptedException e) {}
                rooting.destroy();
            }
        }

        return NO_DATA_FOUND;
    }


    /**
     * Sets the proper base addresses for the overclocking module.
     *
     * @return boolean, if overclocking was successful
     */
    public final boolean setOverclockAddress() {

        if (new File("/proc/overclock/omap2_clk_init_cpufreq_table_addr").exists() &&
                new File("/proc/overclock/cpufreq_stats_update_addr").exists()) {

            // getRootInfo() is much faster for large strings compared to getInfo()
            final String kallsyms = "/proc/kallsyms";
            final String omap_address = getLegacyRootInfo("busybox egrep \"omap2_clk_init_cpufreq_table$\"", kallsyms).substring(0, 8);
            final String cpufreq_address = getLegacyRootInfo("busybox egrep \"cpufreq_stats_update$\"", kallsyms).substring(0, 8);

            final String [] commands = new String[] {
                    "echo " + "0x" + omap_address + " > " + "/proc/overclock/omap2_clk_init_cpufreq_table_addr",
                    "echo " + "0x" + cpufreq_address + " > " + "/proc/overclock/cpufreq_stats_update_addr",
            };

            setRootInfo(commands);
            return true;
        } else {
            // Return quickly;
            return false;
        }
    }


}