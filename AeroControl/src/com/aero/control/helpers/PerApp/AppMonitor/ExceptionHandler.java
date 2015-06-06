package com.aero.control.helpers.PerApp.AppMonitor;

/**
 * Created by Alexander Christ on 30.04.15.
 */
public class ExceptionHandler extends RuntimeException {

    public static String EX_APP_NAME_OVERRIDE = "AppName already initialized, overriding is forbidden!";
    public static String EX_NO_IDENTIFIER_FOUND = "There was no valid identifier found for this module!";
    public static String EX_IDENTIFIER_ALREADY_DEFINED =  "The module identifier was already defined, overriding is forbidden!";

    public ExceptionHandler() {
        super();
    }

    public ExceptionHandler(String message) {
        super(message);
    }

    public ExceptionHandler(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionHandler(Throwable cause) {
        super(cause);
    }
}
