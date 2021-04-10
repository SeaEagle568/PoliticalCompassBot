package com.newsforright.bot.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsforright.bot.Bot;
import com.newsforright.bot.entities.Question;
import com.newsforright.bot.persistence.DBManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

/**
 * A piece of human metabolic output
 * Here are different useful functions (such as conversions)
 * As well as commonly used constants and resources
 *
 * @author seaeagle
 */
@Component
public class CommonUtils {
    //resources
    private BufferedImage compass;
    public List<Question> questionList = new ArrayList<>();
    //constants
    public Long LAST_QUESTION;
    public int MAX_SCORE_ECON;
    public int MAX_SCORE_POLI;
    //resources filePaths
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
    //dependencies
    private ObjectMapper objectMapper;
    private DBManager dbManager;
    private Bot bot; //TODO: delete dependency on release

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

    /**
     * Method that converts DB 'results' string in format (a,b) into Pair of integers
     * @param str Input string in format '(a,b)'
     * @return    Pair<Integer, Integer> where pair.first = a, pair.second = b
     */
    public Pair<Integer, Integer> parseResults(String str){
        String x = str.substring(0, str.indexOf(','));
        String y = str.substring(str.indexOf(',')+1);
        return new Pair<>(Integer.parseInt(x), Integer.parseInt(y));
    }

    /**
     * Method that converts a Pair of integers (result) in string for
     * DB 'results' in format '(a,b)'
     * @param results Input Pair<Integer, Integer> of result
     * @return        String in format '(a,b)' where a = pair.first, b = pair.second
     */
    public String resultsToString(Pair<Integer, Integer> results){
        return results.first.toString() +
                "," +
                results.second.toString();
    }

    /**
     * Spring post-construct method that loads all resources and constants
     * DO NOT call manually
     * I avoided just making a static class because Sanya Balashov said it is anti-pattern and i trusted
     */
    @PostConstruct
    public void initializeUtils(){
        try {
            //Reading a json an deserializing questions
            questionList = objectMapper.readValue(
                    new FileReader(questionsFile),
                    new TypeReference<ArrayList<Question>>(){}
            );
        } catch (IOException e) {
            String error = "Error updating questions";
            System.err.println(error);
            printErrorToDev(error); //TODO: delete on release
            e.printStackTrace();
        }
        dbManager.saveQuestions(questionList); //updating db with questions

        LAST_QUESTION = (long) questionList.size();
        //TODO: Make a normal counter
        MAX_SCORE_ECON = MAX_SCORE_POLI = questionList.size();


        //Getting image
        try {
            compass = ImageIO.read(new File(imageFilePath));
        } catch (IOException e) {
            String error = "Error uploading image";
            System.err.println(error);
            printErrorToDev(error); //TODO: delete on release
            e.printStackTrace();
        }
    }

    /**
     * Temporary method on development/test phase
     * Prints error message directly to developer's telegram
     * TODO: delete on release
     * @param error String with error message
     */
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

    /**
     * Method to return ArrayList with N zeros (N = number of questions)
     * Used in new TelegramUser initialization for 'answers' DB field
     * @return Returns ArrayList<Integer> full of zeros
     */
    public ArrayList<Integer> getEmptyList(){
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < LAST_QUESTION; i++){
            list.add(0);
        }
        return list;
    }

    /**
     * Private method to make a deep copy of BufferedImage
     * @param bi Original BufferedImage
     * @return   Copied BufferedImage
     */
    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null)
                .getSubimage(0, 0, bi.getWidth(), bi.getHeight());
    }

    /**
     * Oh, that's a shitty one
     * A methods that generates image with dot at right place from final user results
     * Saves temp file btw
     * @param finalResults Pair<Double, Double> coordinates from (0,0) to (100, 100) representing results
     * @return             A temporary file with resulting image
     */
    public File getCompassWithDot(Pair<Double, Double> finalResults) {
        BufferedImage finalCompass = deepCopy(compass); //make a copy of blank compass
        //Some java graphics magic
        Graphics2D graphics2D = finalCompass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(Color.BLACK); //Outer circle
        graphics2D.fillOval(
                (int)(finalResults.first* 8),
                (int)(finalResults.second * 8),
                26,
                26
        );
        graphics2D.setColor(Color.RED); //Inner circle
        graphics2D.fillOval(
                (int)(finalResults.first * 8) + 3,
                (int)(finalResults.second * 8) + 3,
                20,
                20
        );
        graphics2D.dispose();
        //Creating a temp file and saving result there
        File tempFile = new File("tempImage.png");
        try {
            ImageIO.write(finalCompass, "png", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            printErrorToDev("Error saving new compass"); //TODO: delete on release
        }
        return tempFile;
    }
}
