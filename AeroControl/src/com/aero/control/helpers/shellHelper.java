package com.aero.control.helpers;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alexander Christ on 18.09.13.
 * Summary of various shell-Methods, for easy method calling
 */
public final class shellHelper {

    // Buffer length;
    private static final int BUFF_LEN = 512;
    private static final byte[] buffer = new byte[BUFF_LEN];
    private static final String LOG_TAG = shellHelper.class.getName();
    private ShellWorkqueue shWork = new ShellWorkqueue();


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
            mWorkItems.clear();
            mWorkItems = null;
        }

    };

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
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                        + " groups");
                return "Unavailable";
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG,
                    "IO Exception when getting kernel version for Device Info screen",
                    e);

            return "Unavailable";
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

        String info;

        if (s == null || (!new File(s).exists()))
            return "Unavailable";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(s), BUFF_LEN);
            try {
                info = reader.readLine();
            } finally {
                reader.close();
            }
            return info;
        } catch (IOException e) {
            Log.e(LOG_TAG,
                    "IO Exception when trying to get information.",
                    e);

            return "Unavailable";
        }
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
        // Just make some gerneric error-code
        String[] error = new String[0];
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

            return error;
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

        // Handle case if files are needed;
        if (flag) {
            List<String> results = new ArrayList<String>();
            File[] files = new File(s).listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    results.add(file.getName());
                }
            }

            String[] result = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                result[i] = results.get(i).toString();
            }
            Arrays.sort(result);

            return result;

        } else {
            // Handle case if directory is needed;
            File file = new File (s);
            String[] result = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return new File(file, s).isDirectory();
                }
            });
            return result;
        }
    }

    /**
     * This Method returns an Array, useful for the frequency list of the kernel
     *
     * @param s   => string used (from a given path)
     * @param flag => set to 1 to convert it with @toMHZ
     * @param flag_io => set to 1 to read the io_schedulers
     *
     * @return String[]
     */
    public final String[] getInfoArray(String s, int flag, int flag_io) {

        String[] completeString = new String[0];
        String[] output = new String[] { "Unavailable" };

        try {
            // Try to read the given Path, if not available -> throw exception
            BufferedReader reader = new BufferedReader(new FileReader(s), BUFF_LEN);
            try {
                if (flag_io == 1)
                    completeString = reader.readLine().replace("[", "").replace("]", "").split(" ");
                else if (flag_io == 0)
                    completeString = reader.readLine().split(" ");
                output = new String[completeString.length];
                for (int i = 0; i < output.length; i++) {
                    if (flag == 1)
                        output[i] = toMHz(completeString[i]);
                    else
                        output[i] = completeString[i];
                }
            } finally {
                reader.close();
            }

            return output;
        } catch (IOException e) {
            Log.e(LOG_TAG,
                    "IO Exception when trying to get information with an Array.",
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
        String finalString;

        open = s.indexOf("[");
        close = s.lastIndexOf("]");
        if (open >= 0 && close >= 0) {
            finalString = s.substring(open + 1, close);
            return finalString;
        } else {
            return "Unavailable";
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

        if (mhzString.equals("Unavailable"))
            return "Unavailable";

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
            return "Unavailable";
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
            return "Unavailable";
        }

        return totalFreeMemory + " / " + totalMemory;
    }

    /**
     * Generic Method for setting values in kernel with "echo" command
     *
     * @param command   => (Object) and the value (echo value >)
     * @param content   => the path to set the value (echo > path)
     *
     * @return nothing
     */
    public final void setRootInfo(final String command, final String content) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Process rooting;
                String tmp;
                try {

                    rooting = Runtime.getRuntime().exec("su");

                    DataOutputStream dataStream = new DataOutputStream(rooting.getOutputStream());

                    // Check if last char is a whitespace;
                    tmp = command.substring(command.length() - 1);
                    if (tmp.matches("^\\s*$")) {
                        tmp = command.substring(0, command.length() - 1);
                    } else {
                        tmp = command;
                    }

                    // Doing some String-puzzle;
                    dataStream.writeBytes("chmod 0666 " + content + "\n");
                    dataStream.writeBytes("echo \"" + tmp + "\" " + "> " + content + "\n");
                    dataStream.writeBytes("exit\n");
                    dataStream.flush();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Do you even root, bro? :/");
                }
            }
        };
        Thread rootThread = new Thread(runnable);
        rootThread.start();

    }
    /**
     * Generic Method for setting a bunch of commands
     * Same as setRootInfo but with an array instead of objects
     *
     * @param array   => Commands to execute in a array
     *
     * @return nothing
     */
    public final void setRootInfo(final String array[]) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {

                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream dataStream = new DataOutputStream(process.getOutputStream());
                    for (String commands : array) {
                        dataStream.writeBytes(commands + "\n");
                        dataStream.flush();
                    }
                    dataStream.writeBytes("exit\n");
                    dataStream.flush();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Do you even root, bro? :/");
                }
            }
        };
        Thread rootThread = new Thread(runnable);
        rootThread.start();

    }

    /**
     * Remounts the system
     *
     * @return nothing
     */
    public final void remountSystem() {

        Process rooting;
        try {

            rooting = Runtime.getRuntime().exec("su");
            DataOutputStream dataStream = new DataOutputStream(rooting.getOutputStream());
            dataStream.writeBytes("mount -o remount,rw -t ext3 /dev/block/mmcblk1p21 /system" + "\n");
            dataStream.flush();
            dataStream.writeBytes("exit\n");
            dataStream.flush();
            rooting.waitFor();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Do you even root, bro? :/", e);
        }

    }

    /**
     * Check if a method set a value correctly
     * (Needs to be reworked/reviewed)
     *
     * @param oldPath   => the old path (+file)
     * @param newPath   => the new path (+file)
     *
     * @return boolean
     */
    public final boolean checkPath(String oldPath, String newPath) {

        if(!oldPath.equals(newPath))
            return true;
        else
            return false;
    }


    /**
     * Executes a command in Terminal and returns output
     *
     * @param command   => set the command to execute
     * @param parameter => set a parameter
     *
     * @return String
     */
    public final String getRootInfo(String command, String parameter) {

        try {
            final Process rooting = Runtime.getRuntime().exec("su");

            DataOutputStream stdin = new DataOutputStream(rooting.getOutputStream());

            stdin.writeBytes(command + " " + parameter + "\n");
            InputStream stdout = rooting.getInputStream();
            int read;
            String output = new String();
            while(true){
                read = stdout.read(buffer);
                if (read == -1)
                    return "Unavailable";

                output += new String(buffer, 0, read);
                if(read<BUFF_LEN){
                    //we have read everything
                    break;
                }
            }
            Log.e(LOG_TAG, "Output from su-Operation: " + output);
            return output;

        } catch (IOException e) {
            Log.e(LOG_TAG, "Do you even root, bro? :/", e);
        }
        return "Unavailable";
    }

    /**
     * Sets the proper base addresses for the overclocking module
     *
     * @return nothing
     */

    public final void setOverclockAddress() {

        if (new File("/proc/overclock/omap2_clk_init_cpufreq_table_addr").exists() &&
                new File("/proc/overclock/cpufreq_stats_update_addr").exists()) {

            // getRootInfo() is much faster for large strings compared to getInfo()
            final String kallsyms = "/proc/kallsyms";
            final String omap_address = getRootInfo("busybox egrep \"omap2_clk_init_cpufreq_table$\"", kallsyms).substring(0, 8);
            final String cpufreq_address = getRootInfo("busybox egrep \"cpufreq_stats_update$\"", kallsyms).substring(0, 8);

            final String [] commands = new String[] {
                    "echo " + "0x" + omap_address + " > " + "/proc/overclock/omap2_clk_init_cpufreq_table_addr",
                    "echo " + "0x" + cpufreq_address + " > " + "/proc/overclock/cpufreq_stats_update_addr",
            };

            setRootInfo(commands);
        } else {
            // Return quickly;
            return;
        }
    }


}
