package tClient;

import org.pmw.tinylog.Logger;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by G.Sargeat on 15/10/16.
 */
public class TwitterStreamLayer {

    Properties prop = new Properties();
    TwitterStream twitterStream;
    TwitterLayer twitterLayer = null;

    TwitterStreamLayer(){
        try{
            loadProperties();

            twitterStream = new TwitterStreamFactory().getInstance();
            twitterStream.setOAuthConsumer(prop.getProperty("consumerKey"), prop.getProperty("consumerSecret"));
            twitterStream.setOAuthAccessToken(new AccessToken(prop.getProperty("accessToken"), prop.getProperty("accessTokenSecret")));

            Logger.info("The properties files loaded without issues");

        }catch (Exception e){
            Logger.error("There was an error with the properties file");
        }
    }

    private void loadProperties() throws IOException {
        InputStream in = getClass().getResourceAsStream("twitter4j.properties");
        prop.load(in);
        in.close();
    }

    public boolean isTwitter(){
        return (twitterStream != null);
    }

    public void setTwitterLayerHandle(TwitterLayer twit){
        twitterLayer = twit;
    }

    public void setStream() {

        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer(prop.getProperty("consumerKey"), prop.getProperty("consumerSecret"));
        twitterStream.setOAuthAccessToken(new AccessToken(prop.getProperty("accessToken"), prop.getProperty("accessTokenSecret")));

        StatusListener listener = new StatusListener(){

            @Override
            public void onException(Exception e) {
                Logger.error("There was an exception with the Twitter Stream" +  e);

            }

            @Override
            public void onStatus(Status status) {
                String action;
                String text = status.getText();
                Logger.info("From : " + status.getUser().getScreenName() + ":::> " + text);
                GameAction gameAction;
                text.toLowerCase();
                action = processStatus(text);

                if (status.getUser().getScreenName().compareTo("gonzo4111") != 0){
                    if(status.getGeoLocation() != null){

                        gameAction = new GameAction(action, Long.toString(status.getUser().getId()), status.getGeoLocation().getLongitude(), status.getGeoLocation().getLatitude(), status.getUser().getScreenName());

                        Thread t = new Thread(gameAction);
                        t.start();
                    }else if(status.getGeoLocation() == null){
                        gameAction = new GameAction("rules", Long.toString(status.getUser().getId()), 0,0, status.getUser().getScreenName());

                        Thread t = new Thread(gameAction);
                        t.start();
                    }
                }


            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }
        };

        twitterStream.addListener(listener);
        FilterQuery query = new FilterQuery();
        query.follow(785064602002268160L); //This should be the user who is the bot
        twitterStream.filter(query);

//        twitterStream.sample();
    }

    public void closeStream(){
        twitterStream.cleanUp();
    }

    //the order of these if statements indicate their precedence to each other.
    //quitting is most imporant
    //ping is second importatnt
    //starting a game is third most important.
    //rules is least least important
    private String processStatus(String status){
        String action = "rules";
        status = status.toLowerCase();
        if(status.contains("quit")){
            action = "quit";

        }else if(status.contains("ping")){
            action = "ping";

        }else if(status.contains("play game")){
            action = "play";

        }else if(status.contains("rules")){
            action = "rules";
        }

        return action;
    }

}


