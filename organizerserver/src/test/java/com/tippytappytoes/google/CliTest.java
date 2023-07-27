package com.tippytappytoes.google;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CliTest {

    @Test
    public void standard(){
        try {
            ProcessBuilder pb = new ProcessBuilder("ado", "list");
            pb.inheritIO(); // redirect output to console
            Process process = pb.start();
            process.waitFor(); // wait for the process to finish
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
