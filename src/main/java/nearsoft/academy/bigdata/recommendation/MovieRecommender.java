package nearsoft.academy.bigdata.recommendation;




import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import static javax.imageio.ImageIO.getCacheDirectory;

public class MovieRecommender {

    private int userIndex,movieIndex,numReviews;
    private String path,pathCsv;
    private File fileMovie;
    private Hashtable<String,Integer> users;
    private Hashtable<String,Integer> movies;

    public MovieRecommender(String path) throws IOException {
        this.path = path;
        this.fileMovie = new File(this.path);
        users = new Hashtable<String,Integer>();
        movies = new Hashtable<String,Integer>();
        movieIndex = 1;
        userIndex = 1;
        numReviews = 0;
        this.pathCsv = this.convertToCsv();

    }

    public List<String> getRecommendationsForUser(String user) throws TasteException, IOException {
        DataModel model = new FileDataModel(new File(pathCsv));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        int idUser = searchUser(user);
        List<RecommendedItem> recommendations = recommender.recommend(idUser, 3);
        List<String> output = new ArrayList<>();
        for (RecommendedItem recommendation : recommendations) {
            output.add(getProductID((int)recommendation.getItemID()));
        }
        return output;
    }

    public int getTotalReviews() throws IOException {
        return numReviews;
    }

    public int getTotalProducts(){
        return movieIndex-1;
    }

    public int getTotalUsers(){
        return userIndex-1;
    }

    public int searchUser(String user){
        int idUser = users.get(user);
        return idUser;

    }

    public String getProductID(int value){
        Enumeration e = movies.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (movies.get(key)==value) {
                return key;
            }
        }
        return null;
    }

    public String convertToCsv() throws IOException {


        File moviesCsv = new File(getCacheDirectory(),"movies.csv");
        if(!moviesCsv.exists()){
            moviesCsv.createNewFile();
        }else {
            moviesCsv.delete();
            moviesCsv.createNewFile();
        }
        FileReader fileReader = new FileReader(fileMovie);
        BufferedReader bufferReader = new BufferedReader(fileReader);
        FileWriter fileWriter = new FileWriter(moviesCsv);
        Writer writer = new BufferedWriter(fileWriter);
        String [] lineParts;
        String tempLine,titleLine,textLine,score;
        int currentUser = 0,currentMovie=0;
        while ((tempLine = bufferReader.readLine())!=null ){
            if(tempLine.length()!=0){
                lineParts = tempLine.split(":");
                titleLine = lineParts[0];
                switch (titleLine){
                    case "product/productId":
                        textLine = lineParts[1].trim();
                        if(!movies.containsKey(textLine)){
                            movies.put(textLine,movieIndex);
                            currentMovie = movieIndex;
                            movieIndex++;
                        }
                        else{
                            currentMovie = movies.get(textLine);
                        }
                        break;

                    case "review/userId":
                        textLine = lineParts[1].trim();
                        if(!users.containsKey(textLine)){
                            users.put(textLine,userIndex);
                            currentUser = userIndex;
                            userIndex++;
                        }
                        else{
                            currentUser = users.get(textLine);
                        }
                        break;

                    case "review/score":
                        score = lineParts[1].trim();
                        writer.write(currentUser+","+currentMovie+","+score+"\n");
                        numReviews++;
                        break;
                }

            }



        }
        bufferReader.close();
        writer.close();
        return moviesCsv.getAbsolutePath();

    }
}