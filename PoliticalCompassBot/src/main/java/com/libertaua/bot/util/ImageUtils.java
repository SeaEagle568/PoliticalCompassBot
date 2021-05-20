package com.libertaua.bot.util;

import com.libertaua.bot.Bot;
import com.libertaua.bot.entities.TelegramUser;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Class that is responsible for all operations with images
 */
@Component
public class ImageUtils {

    protected final BufferedImage[] achievments = new BufferedImage[6];
    protected BufferedImage compass;
    private final double COMPASS_CX = 408d;
    private final double COMPASS_CY = 441d;
    private final double COMPASS_MULT = 7.7d;
    private final double NOLAN_CX = 480d;
    private final double NOLAN_CY = 400d;
    private final double NOLAN_MULT = 5.37d;
    private final double R = 69d;
    private final double r = 55d;
    protected BufferedImage true_compass;
    public File ideologies_pic;
    public ArrayList<File> memes = new ArrayList<>();

    private Bot bot;
    @Autowired
    public void setBot(Bot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void init(){
        ideologies_pic = new File("target/classes/ideologies.png");
        File memesFolder = new File("target/classes/memes");
        for (final File fileEntry : Objects.requireNonNull(memesFolder.listFiles())) {
            if (!fileEntry.isDirectory()) memes.add(fileEntry);
        }
        memes.sort(Comparator.comparing(File::getName));

        try {
            compass = ImageIO.read(new File("target/classes/compass.png"));
            true_compass = ImageIO.read(new File("target/classes/true.jpg"));
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
    public Pair<File, Integer> getResultsImage(BufferedImage userPic, Pair<Double, Double> finalResults, boolean allZeros, boolean trueCompass) {
        BufferedImage finalCompass;
        Integer resultType;
        if (trueCompass){
            resultType = -1;
            finalResults.first -= 50;
            finalResults.second -= 50;
            if (userPic == null) {
                finalCompass = getCompassWithDot(deepCopy(true_compass),
                        rotateCoords(finalResults, 135),
                        NOLAN_CX, NOLAN_CY, R, r, NOLAN_MULT);
            }
            else {
                finalCompass = getCompassWithPic(userPic,
                        deepCopy(true_compass),
                        rotateCoords(finalResults, 135),
                        NOLAN_CX, NOLAN_CY, R, NOLAN_MULT);
            }
        }
        else if (finalResults.first.intValue() == 100 && finalResults.second.intValue() == 100){
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
            finalResults.first -= 50;
            finalResults.second -= 50;
            resultType = 5;
            if (userPic == null) {
                finalCompass = getCompassWithDot(deepCopy(compass),
                        finalResults,
                        COMPASS_CX, COMPASS_CY, R, r, COMPASS_MULT);
            }
            else {
                finalCompass = getCompassWithPic(userPic,
                        deepCopy(compass),
                        finalResults,
                        COMPASS_CX, COMPASS_CY, R, COMPASS_MULT);
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
    private static Pair<Double, Double> rotateCoords(Pair<Double, Double> input, double degree){
        Pair<Double, Double> result = new Pair<>(0d,0d);
        input.second = -input.second;
        result.first = Math.cos(Math.toRadians(degree)) * input.first + (-Math.sin(Math.toRadians(degree))) * input.second;
        result.second = Math.sin(Math.toRadians(degree)) * input.first + (Math.cos(Math.toRadians(degree))) * input.second;
        input.second = -input.second;
        result.first = -result.first;
        result.second = -result.second;
        return result;
    }
    private BufferedImage resizeImage(BufferedImage originalImage, double targetWidth, double targetHeight) {
        BufferedImage resizedImage = new BufferedImage((int)Math.round(targetWidth), (int)Math.round(targetHeight), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, (int)Math.round(targetWidth),  (int)Math.round(targetHeight), null);
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

    private BufferedImage getCompassWithPic(BufferedImage userPic, BufferedImage compass, Pair<Double, Double> finalResults, double centerX, double centerY, double R, double multiplier) {
        BufferedImage finalPic = resizeImage(getCircle(userPic),R, R);
        Graphics2D graphics2D = compass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.drawImage(finalPic,
                (int) (centerX - R + Math.round(finalResults.first * multiplier)),
                (int) (centerY - R + Math.round(finalResults.second * multiplier)),
                null
        );
        graphics2D.dispose();
        return compass;
    }

    private BufferedImage getCompassWithDot(BufferedImage compass, Pair<Double, Double> finalResults, double centerX, double centerY, double R, double r, double multiplier)
    {
        Graphics2D graphics2D = compass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(Color.BLACK); //Outer circle
        graphics2D.fillOval(
                (int) (centerX - R + Math.round(finalResults.first * multiplier)),
                (int) (centerY - R + Math.round(finalResults.second * multiplier)),
                (int) R,
                (int) R
        );
        graphics2D.setColor(Color.RED); //Inner circle
        graphics2D.fillOval(
                (int) (centerX - R + Math.round(finalResults.first * multiplier) + (R-r) / 2),
                (int) (centerY - R + Math.round(finalResults.second * multiplier) + (R-r) / 2),
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
