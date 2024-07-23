package ru.itis.HamKillerBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class Tapper {

    private int max = 0;

    public void launch(TelegramBot telegramBot, long chatId) throws IOException, InterruptedException {
        int it = 0;
        int firstBalance = 0;

        while(true) {
            List<Integer> pData = getData();
            if(it == 0) firstBalance = pData.get(3);

            long unixTime = getUnixTime();
            int availableTaps = pData.get(1);
            int count = 20 + randInt(-4, 8);

            URL url = new URL("https://api.hamsterkombatgame.io/clicker/tap");
            String body = "{\"count\":" + count + ",\"availableTaps\":" + availableTaps + ",\"timestamp\":" + unixTime + "}";

            List<Integer> data = sendRequest(url, body); // code, available, max, balance
            String s = String.format(
                    """
                    RESPONSE CODE: %d
                    Available Taps: %d
                    Max Taps: %d
                    Balance: %d
                    """,
                    data.get(0), data.get(1), data.get(2), data.get(3));

            telegramBot.sendMsg(chatId, s);

            if(data.get(1) < 100) {
                int sleepTime = ((data.get(2) - data.get(1)) / 3) + 10;
                int gain = data.get(3) - firstBalance;

                telegramBot.sendMsg(chatId, "<b>GAIN: " + gain + "\n" + "SLEEP TIME: " + secToTime(sleepTime) + "</b>");
                Thread.sleep(1000L * sleepTime);

                it = 0;
                firstBalance = 0;
            } else {
                it += 1;
                Thread.sleep(1000L * randInt(3, 5));
            }
        }

    }

    public List<Integer> getData() throws IOException, InterruptedException {
        int availableTaps = ((max == 0) ? 2000 : max);
        long unixTime = getUnixTime();

        URL url = new URL("https://api.hamsterkombatgame.io/clicker/tap");
        String body = "{\"count\":1,\"availableTaps\":" + availableTaps + ",\"timestamp\":" + unixTime + "}";

        List<Integer> data = sendRequest(url, body); // code, available, max, balance
        max = data.get(2);

        Thread.sleep(2000);
        return data;
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
        conn.setRequestProperty("Authorization", "Bearer " + "your token");

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
        Pattern pattern = Pattern.compile("\"balanceCoins\":(\\d+),");
        Matcher matcher = pattern.matcher(json);

        String balanceCoinsValue = null;
        while (matcher.find()) {
            balanceCoinsValue = matcher.group(1);
        }

        return Integer.parseInt(balanceCoinsValue);
    }

    public static String secToTime(int sec) {
        int second = sec % 60;
        int minute = sec / 60;
        if (minute >= 60) {
            int hour = minute / 60;
            minute %= 60;
            return hour + "hr " + (minute < 10 ? "0" + minute : minute) + "m " + (second < 10 ? "0" + second : second) + "s ";
        }
        return minute + "m " + (second < 10 ? "0" + second : second) + "s ";
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

}
