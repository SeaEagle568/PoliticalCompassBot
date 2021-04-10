package com.newsforright.bot.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsforright.bot.Bot;
import com.newsforright.bot.entities.Question;
import com.newsforright.bot.persistence.DBManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommonUtils {
    public BufferedImage compass;
    public List<Question> questionList = new ArrayList<>();

    public Long LAST_QUESTION;
    public int MAX_SCORE_ECON;
    public int MAX_SCORE_POLI;

    @Value("${bot.resources.questions}")
    private String questionsFile;
    @Value("${bot.resources.image}")
    private String imageFilePath;

    @Value("${bot.resources.greeting}")
    @Getter
    private String greetingFile;

    @Value("${bot.devChatId}")
    @Getter
    private String devChatId;
    private ObjectMapper objectMapper;
    private DBManager dbManager;
    private Bot bot;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setBot(Bot bot) {
        this.bot = bot;
    }

    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }



    public Pair<Integer, Integer> parseResults(String str){
        String x = str.substring(0, str.indexOf(','));
        String y = str.substring(str.indexOf(',')+1);
        return new Pair<Integer, Integer>(Integer.parseInt(x), Integer.parseInt(y));
    }

    public String resultsToString(Pair<Integer, Integer> results){
        return results.getFirst().toString() +
                "," +
                results.getSecond().toString();
    }


    @PostConstruct
    public void init(){
        System.err.println(questionsFile + " from utils");

        try {
            questionList = objectMapper.readValue(new FileReader(questionsFile), new TypeReference<ArrayList<Question>>(){});
        } catch (IOException e) {
            String error = "Error updating questions";
            System.err.println(error);
            printErrorToDev(error);
            e.printStackTrace();
        }
        LAST_QUESTION = (long) questionList.size();
        //TODO: Make a normal counter
        MAX_SCORE_ECON = MAX_SCORE_POLI = questionList.size();
        dbManager.saveQuestions(questionList);

        //Getting image
        try {
            File imageFile = new File(imageFilePath);
            compass = ImageIO.read(imageFile);
        } catch (IOException e) {
            String error = "Error uploading image";
            System.err.println(error);
            printErrorToDev(error);
            e.printStackTrace();
        }
    }

    public void printErrorToDev(String error){
        SendMessage message = new SendMessage();
        message.setText(error);
        message.setChatId(devChatId);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getEmptyList(){
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < LAST_QUESTION; i++){
            list.add(0);
        }
        return list;
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null)
                .getSubimage(0, 0, bi.getWidth(), bi.getHeight());
    }

    public File getCompassWithDot(Pair<Double, Double> finalResults) {
        BufferedImage finalCompass = deepCopy(compass);
        Graphics2D graphics2D = finalCompass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillOval(
                (int)(finalResults.getFirst() * 8),
                (int)(finalResults.getSecond() * 8),
                26,
                26
        );
        graphics2D.setColor(Color.RED);
        graphics2D.fillOval(
                (int)(finalResults.getFirst() * 8) + 3,
                (int)(finalResults.getSecond() * 8) + 3,
                20,
                20
        );
        graphics2D.dispose();
        File tempFile = new File("tempImage.png");
        try {
            ImageIO.write(finalCompass, "png", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            printErrorToDev("Error saving new compass");
        }
        return tempFile;
    }
}
