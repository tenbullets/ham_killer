package org.example;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        int availableTaps = 1000;

        while(true) {
            long unixTime = getUnixTime();

            URL url = new URL("https://api.hamsterkombatgame.io/clicker/tap");
            String body = "{\"count\":40,\"availableTaps\":" + availableTaps + ",\"timestamp\":" + unixTime + "}";

            // code, available, max, balance
            List<Integer> data = sendRequest(url, body);
            String s = String.format("RESPONSE CODE: %d, Available Taps: %d, Max Taps: %d, Balance: %d", data.get(0), data.get(1), data.get(2), data.get(3));
            System.out.println(s);

            if(data.get(1) <= 100) {
                int sleepTime = (data.get(2) / 3) + 5;

                for (int i = sleepTime; i >= 0; i--) {
                    System.out.print("\r" + "SLEEP TIME: " + secToTime(i));
                    Thread.sleep(1000);
                }
                System.out.println();
            }
            availableTaps = data.get(2);
        }

    }

    public static long getUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static List<Integer> sendRequest(URL url, String body) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Authorization", "Bearer 1721561330770UqJuGpvspLcDm5WJx6WIGBBtB8TRKAXV4YX6H3sqlWngcwCYAEhYtcDnMEHdEI3W1264156311");

        try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
            dos.writeBytes(body);
        }

        String json = null;
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = bf.readLine()) != null) {
//                System.out.println(line);
                json = line;
            }
        }

        List<Integer> data = new ArrayList<>();
        data.add(conn.getResponseCode());
        data.add(getAvailableTaps(json));
        data.add(getMaxTaps(json));
        data.add(getBalance(json));

        return data;
    }

    public static int getAvailableTaps(String json) {
        Pattern pattern = Pattern.compile("\"availableTaps\":(\\d+),");
        Matcher matcher = pattern.matcher(json);

        String availableTapsValue = null;
        while (matcher.find()) {
            availableTapsValue = matcher.group(1);
        }

        return Integer.parseInt(availableTapsValue);
    }

    public static int getMaxTaps(String json) {
        Pattern pattern = Pattern.compile("\"maxTaps\":(\\d+),");
        Matcher matcher = pattern.matcher(json);

        String maxTapsValue = null;
        while (matcher.find()) {
            maxTapsValue = matcher.group(1);
        }

        return Integer.parseInt(maxTapsValue);
    }

    public static int getBalance(String json) {
        Pattern pattern = Pattern.compile("\"totalCoins\":(\\d+),");
        Matcher matcher = pattern.matcher(json);

        String totalCoinsValue = null;
        while (matcher.find()) {
            totalCoinsValue = matcher.group(1);
        }

        return Integer.parseInt(totalCoinsValue);
    }

    public static String secToTime(int sec) {
        int second = sec % 60;
        int minute = sec / 60;
        if (minute >= 60) {
            int hour = minute / 60;
            minute %= 60;
            return hour + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
        }
        return minute + ":" + (second < 10 ? "0" + second : second);
    }
}