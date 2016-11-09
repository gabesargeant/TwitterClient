package tClient;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import org.pmw.tinylog.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;


/**
 * Created by G.sargeant on 11/10/16.
 */
public class TwitterLayer {
    Properties prop = new Properties();
    Twitter twitter = null;

    TwitterLayer(){
        try{
            loadProperties();
            Logger.info("The properties files loaded without issues");
        }catch (Exception e){
            Logger.error("There was an error with the properties file");
        }
        createTwitterConnection();
    }

    private void loadProperties() throws IOException {

        InputStream in = getClass().getResourceAsStream("twitter4j.properties");
        prop.load(in);
        in.close();

    }

    private void createTwitterConnection(){

        TwitterFactory twitterFactory = new TwitterFactory();
        twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(prop.getProperty("consumerKey"), prop.getProperty("consumerSecret"));
        twitter.setOAuthAccessToken(new AccessToken(prop.getProperty("accessToken"), prop.getProperty("accessTokenSecret")));

    }

    public boolean isTwitter(){

        return (twitter != null);

    }

    public Twitter getTwitterHandle(){
        return twitter;
    }

    public String postStatus(String post) throws TwitterException {
        Logger.info(post);
        Status s = twitter.updateStatus(post);
        return s.getText();
    }

    public ArrayList<String> getTweets() throws TwitterException
    {
        ResponseList<Status> rlist = twitter.getUserTimeline();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < rlist.size(); i++){
            list.add(i, rlist.get(i).getText() + " " + rlist.get(i).getUser().getId());
        }
        return list;
    }

}
