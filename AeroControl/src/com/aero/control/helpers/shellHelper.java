package com.aero.control.helpers;

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
public class shellHelper {

    // Buffer length;
    private static final int BUFF_LEN = 1024;

    /**
     * Gets the current Kernel Version + some useful information
     *
     * @return String
     */
    public String getKernel() {
        // Taken from Androids/CM Gingerbread Branch:
        String procVersionStr;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
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
                Log.e("Aero", "Regex did not match on /proc/version: " + procVersionStr);
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                Log.e("Aero", "Regex match on /proc/version only returned " + m.groupCount()
                        + " groups");
                return "Unavailable";
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {
            Log.e("Aero",
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
    public String getInfo(String s) {

        String info;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(s), 256);
            try {
                info = reader.readLine();
            } finally {
                reader.close();
            }
            return info;
        } catch (IOException e) {
            Log.e("Aero",
                    "IO Exception when trying to get information.",
                    e);

            return "Unavailable";
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
    public String[] getDirInfo(String s, boolean flag) {

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
            List<String> sortedList = Arrays.asList(result);

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
    public String[] getInfoArray(String s, int flag, int flag_io) {

        String[] completeString = new String[0];
        String[] output = null;
        // Just make some gerneric error-code
        String[] error = new String[0];
        int bropen, brclose;

        try {
            // Try to read the given Path, if not available -> throw exception
            BufferedReader reader = new BufferedReader(new FileReader(s), 256);
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
            Log.e("Aero",
                    "IO Exception when trying to get information with an Array.",
                    e);

            return error;
        }
    }

    /**
     * Finds a String between two values (for now only used in io_schedulers)
     *
     * @param s   => string used (from a given path)
     *
     * @return String
     */
    public String getInfoString(String s) {

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
    public String toMHz(String mhzString) {
        try {
            return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz")
                    .toString();
        } catch (Exception e) {
            Log.e("Aero",
                    "Tried to add something to a non existing string.",
                    e);
            return "Unavailable";
        }
    }

    /**
     * Gets the total amount of memory
     *
     * @param s   => path to read
     *
     * @return String
     */
    public String getMemory(String s) {
        String result = null;

        try {
            /* /proc/meminfo entries follow this format:
             * MemTotal:         362096 kB
             * MemFree:           29144 kB
             * Buffers:            5236 kB
             * Cached:            81652 kB
             */
            BufferedReader reader = new BufferedReader(new FileReader(s), 256);
            result = reader.readLine();
            if (result != null) {
                String parts[] = result.split("\\s+");
                if (parts.length == 3) {
                    result = Long.parseLong(parts[1]) / 1024 + " MB";
                }
            }
        } catch (IOException e) {
            Log.e("Aero",
                    "Yep, i can't read your memory stats :( .",
                    e);
            return "Unavailable";
        }

        return result;
    }

    /**
     * Generic Method for setting values in kernel with "echo" command
     *
     * @param command   => (Object) and the value (echo value >)
     * @param content   => the path to set the value (echo > path)
     *
     * @return nothing
     */
    public void setRootInfo(Object command, String content) {

        // Casting object to string;
        // This is probably deprecated, remove and change Parameter to string...
        String s = (String) command;

        Process rooting;
        try {

            rooting = Runtime.getRuntime().exec("su");

            DataOutputStream dataStream = new DataOutputStream(rooting.getOutputStream());
            // Doing some String-puzzle;
            dataStream.writeBytes("echo \"" + s + "\" " + "> " + content + "\n");
            dataStream.writeBytes("exit\n");
            dataStream.flush();

        } catch (IOException e) {
            Log.e("Aero", "Do you even root, bro? :/");
        }

    }
    /**
     * Generic Method for setting a bunch of commands
     * Same as setRootInfo but with an array instead of objects
     *
     * @param array   => Commands to execute in a array
     *
     * @return nothing
     */
    public void setRootInfo(String array[]) {

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
            Log.e("Aero", "Do you even root, bro? :/");
        }

    }

    /**
     * Remounts the system
     *
     * @return nothing
     */
    public void remountSystem() {

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
            Log.e("Aero", "Do you even root, bro? :/", e);
        }

    }

    /**
     * Check if a method set a value correctly
     *
     * @param oldPath   => the old path (+file)
     * @param newPath   => the new path (+file)
     *
     * @return boolean
     */
    public boolean checkPath(String oldPath, String newPath) {

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
    public String getRootInfo(String command, String parameter) {

        try {
            Process rooting = Runtime.getRuntime().exec("su");

            DataOutputStream stdin = new DataOutputStream(rooting.getOutputStream());

            stdin.writeBytes(command + " " + parameter + "\n");
            InputStream stdout = rooting.getInputStream();
            byte[] buffer = new byte[BUFF_LEN];
            int read;
            String output = new String();
            while(true){
                read = stdout.read(buffer);
                output += new String(buffer, 0, read);
                if(read<BUFF_LEN){
                    //we have read everything
                    break;
                }
            }
            Log.e("Aero", "Output from su-Operation: " + output);
            return output;

        } catch (Exception e) {
            Log.e("Aero", "Do you even root, bro? :/", e);
        }
        return "Unavailable";
    }


}
