package com.dezq;

public class Launcher {
    public static void main(String[] args) {
        // This redirects to your Main class but tricks the JVM 
        // into ignoring the missing JavaFX module-path check.
        Main.main(args);
    }
}