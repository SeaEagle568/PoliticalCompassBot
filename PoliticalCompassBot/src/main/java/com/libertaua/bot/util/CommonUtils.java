package com.libertaua.bot.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libertaua.bot.Bot;
import com.libertaua.bot.entities.Question;
import com.libertaua.bot.entities.TelegramUser;
import com.libertaua.bot.enums.Axe;
import com.libertaua.bot.persistence.DBManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.UserProfilePhotos;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final BufferedImage[] achievments = new BufferedImage[6];
    public ArrayList<Question> questionList = new ArrayList<>();
    public ArrayList<Ideology> ideologiesList = new ArrayList<>();
    //constants
    public Long LAST_QUESTION;
    public int MAX_SCORE_ECON = 0;
    public int MAX_SCORE_POLI = 0;
    //resources filePaths
    @Value("${bot.resources.questions}")
    private String questionsFile;
    @Value("${bot.resources.ideologies}")
    private String ideologiesFile;
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
    private Bot bot;

    @Autowired
    public void setBot(Bot bot) {
        this.bot = bot;
    }
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Method that converts a Pair of integers (result) in string for
     * DB 'results' in format '(a,b)'
     * @param results Input Pair<Integer, Integer> of result
     * @return        String in format '(a,b)' where a = pair.first, b = pair.second
     */
    public String resultsToString(Pair<Double, Double> results){
        return results.first.toString() +
                "," +
                results.second.toString();
    }
    public Pair<Double, Double> resultsToPair(String str){
        String x = str.substring(0, str.indexOf(','));
        String y = str.substring(str.indexOf(',')+1);
        return new Pair<>(Double.parseDouble(x), Double.parseDouble(y));
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
                    new TypeReference<>(){}
            );
            ideologiesList = objectMapper.readValue(
                    new FileReader(ideologiesFile),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            String error = "Error updating questions or ideologies";
            System.err.println(error);
            e.printStackTrace();
        }
        dbManager.saveQuestions(questionList); //updating db with questions

        LAST_QUESTION = (long) questionList.size();
        for (Question question : questionList){
            if (question.getAxe() == Axe.POLITICAL) MAX_SCORE_POLI += 2;
            else MAX_SCORE_ECON += 2;
        }


        //Getting image
        try {
            compass = ImageIO.read(new File(imageFilePath));
            achievments[0] = ImageIO.read(new File("target/classes/ancap.jpg"));
            achievments[1] = ImageIO.read(new File("target/classes/ancom.jpg"));
            achievments[2] = ImageIO.read(new File("target/classes/authright.jpg"));
            achievments[3] = ImageIO.read(new File("target/classes/tankie.jpg"));
            achievments[4] = ImageIO.read(new File("target/classes/normie.jpg"));
            achievments[5] = ImageIO.read(new File("target/classes/gigachad.png"));
        } catch (IOException e) {
            String error = "Error uploading image";
            System.err.println(error);
            e.printStackTrace();
        }
    }

    /**
     * A method that takes one dot and returns list of 4 ideologies, nearest to that dot
     * @param dot A Pair of doubles - result dot on coordinates
     * @return Array List of 4 ideologies
     */
    public ArrayList<Ideology> getNearestDots(Pair<Double, Double> dot){
        ArrayList<Ideology> result = new ArrayList<>();
        ArrayList<Pair<Integer, Double>> distance = new ArrayList<>();
        for (int i = 0; i < ideologiesList.size(); i++){
            distance.add(new Pair<>(i, getDistance(dot, ideologiesList.get(i))));
        }
        distance.sort(Comparator.comparing(a -> a.second));
        for (int i = 0; i < 4; i++) {
            result.add(ideologiesList.get(distance.get(i).first));
        }
        return result;
    }

    private Double getDistance(Pair<Double, Double> dot, Ideology ideology) {
        return Math.sqrt(Math.pow(dot.first - ideology.coords.first, 2) + Math.pow(dot.second - ideology.coords.second, 2));
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
    public Pair<File, Integer> getResultsImage(BufferedImage userPic, Pair<Double, Double> finalResults, boolean allZeros) {
        BufferedImage finalCompass;
        Integer resultType;
        if (finalResults.first.intValue() == 100 && finalResults.second.intValue() == 100){
            finalCompass = deepCopy(achievments[0]);
            resultType = 0;
        }
        else if (finalResults.first.intValue() == 0 && finalResults.second.intValue() == 100){
            finalCompass = deepCopy(achievments[1]);
            resultType = 1;
        }
        else if (finalResults.first.intValue() == 100 && finalResults.second.intValue() == 0){
            finalCompass = deepCopy(achievments[2]);
            resultType = 2;
        }
        else if (finalResults.first.intValue() == 0 && finalResults.second.intValue() == 0){
            finalCompass = deepCopy(achievments[3]);
            resultType = 3;
        }
        else if (allZeros){
            finalCompass = deepCopy(achievments[4]);
            resultType = 4;
        }
        else if (finalResults.first.intValue() == 50 && finalResults.second.intValue() == 50){
            finalCompass = deepCopy(achievments[5]);
            resultType = 6;
        }
        else {
            resultType = 5;
            if (userPic == null) finalCompass = getCompassWithDot(deepCopy(compass), finalResults, 23, 56, 52, 42, 7.17d);
            else {
                finalCompass = getCompassWithPic(userPic,deepCopy(compass), finalResults, 23, 56, 52, 7.17d);
            }
        }
        //Creating a temp file and saving result there
        File tempFile = new File("tempImage.png");
        try {
            ImageIO.write(finalCompass, "png", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(tempFile, resultType);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private BufferedImage getCircle(BufferedImage original){
        int width = original.getWidth();
        BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, width, width));
        g2.drawImage(original, 0, 0, width, width, null);
        g2.dispose();
        return circleBuffer;
    }

    private BufferedImage getCompassWithPic(BufferedImage userPic, BufferedImage compass, Pair<Double, Double> finalResults, int shiftX, int shiftY, int R, double multiplier) {
        BufferedImage finalPic = resizeImage(getCircle(userPic),R, R);
        Graphics2D graphics2D = compass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.drawImage(finalPic,
                (int) (Math.round(finalResults.first * multiplier) + shiftX),
                (int) (Math.round(finalResults.second * multiplier) + shiftY),
                null
                );
        graphics2D.dispose();
        return compass;
    }

    private BufferedImage getCompassWithDot(BufferedImage compass, Pair<Double, Double> finalResults, double shiftX, double shiftY, double R, double r, double multiplier)
    {
        Graphics2D graphics2D = compass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(Color.BLACK); //Outer circle
        graphics2D.fillOval(
                (int) (Math.round(finalResults.first * multiplier) + shiftX),
                (int) (Math.round(finalResults.second * multiplier) + shiftY),
                (int) R,
                (int) R
        );
        graphics2D.setColor(Color.RED); //Inner circle
        graphics2D.fillOval(
                (int) (Math.round(finalResults.first * multiplier) + shiftX + (R-r) / 2),
                (int) (Math.round(finalResults.second * multiplier) + shiftY + (R-r) / 2),
                (int) r,
                (int) r
        );
        graphics2D.dispose();
        return compass;
    }

    public BufferedImage getUserPic(TelegramUser currentUser) {
        GetUserProfilePhotos getUserProfilePhotos = new GetUserProfilePhotos(Long.parseLong(currentUser.getUserId()), 0, 1);
        try {
            UserProfilePhotos userPhotos = bot.execute(getUserProfilePhotos);
            List<List<PhotoSize>> photoes = userPhotos.getPhotos();
            String fileId = photoes.get(0).get(0).getFileId();
            GetFile fileGetter = new GetFile();
            fileGetter.setFileId(fileId);
            File photoFile = bot.downloadFile(bot.execute(fileGetter));
            return ImageIO.read(photoFile);
        } catch (Exception e) {
           // e.printStackTrace();
        }
        return null;

    }

}
