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
    protected BufferedImage compass_woman;
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
            if (userPic == null) {
                finalCompass = getCompassWithDot(deepCopy(compass),
                        finalResults,
                        23, 56, 69, 55, 7d);
            }
            else {
                finalCompass = getCompassWithPic(userPic,
                        deepCopy(compass),
                        finalResults,
                        23, 56, 69, 7d);
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
