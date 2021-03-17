package ru.kontur.intern.shortener.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandReader extends Thread {

    private static final String EXIT_CODE = "exit";
    private static final String CLEAR_CODE = "clear";

    private final Links links;

    public CommandReader(Links links) {
        this.links = links;
    }

    @Override
    public void run() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String string = "";
        try {
            while (!string.equals(EXIT_CODE)) {
                string = reader.readLine();
                if (string.equals(CLEAR_CODE)) {
                    links.clear();
                    System.out.println("===== DATABASE CLEARED =====");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
