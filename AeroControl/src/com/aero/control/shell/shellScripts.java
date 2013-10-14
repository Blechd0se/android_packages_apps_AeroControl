package com.aero.control.shell;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alexander Christ on 18.09.13.
 * Summary of various shell-Methods, for easy method calling
 */
public class shellScripts {

    private static final int BUFF_LEN = 1024;

    public String getKernel() {
        // Taken from Andriods/CM Gingerbread Branch:
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

    // Keep it generic for further use.
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

    public String[] getDirInfo(String s) {

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

    }

    /*
     * This Method returns an Array, useful for the frequency list of the kernel
     * Controllable with a flag; 1 -> Array with frequencies, everything else -> Normal Array
     * Last flag is for arrays with []
     */
    public String[] getInfoArray(String s, int flag, int flag_io) {

        String[] completeString = new String[0];
        String[] output = null;
        // Just make some gerneric error-code
        String[] error = new String[0];

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

    // Gets the amount of memory. Taken from CM's 10.2 Branch
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

    /*
     * Generic Method for setting values in kernel with "echo" command
     * First value sets the command(value), second sets it to the actual path
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
            dataStream.flush();

        } catch (IOException e) {
            Log.e("Aero", "Do you even root, bro? :/");
        }

    }

    public void remountSystem() {

        Process rooting;
        try {

            rooting = Runtime.getRuntime().exec("su");
            rooting.getOutputStream();

            rooting = Runtime.getRuntime().exec("mount -o remount,rw /system");
            rooting.getOutputStream();

        } catch (IOException e) {
            Log.e("Aero", "Do you even root, bro? :/");
        }

    }

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

        } catch (IOException e) {
            Log.e("Aero", "Do you even root, bro? :/");
        }
        return "Something went wrong..";
    }


}
