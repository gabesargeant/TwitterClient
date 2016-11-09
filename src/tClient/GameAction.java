package tClient;

import org.pmw.tinylog.Logger;

import java.util.Random;

/**
 * Created by GSargeant on 27/10/16.
 */
public class GameAction implements Runnable {

    private final String ACTION;
    private final String uid;
    private final double lng;
    private final double lat;
    private final String uname;
    private DataInterface db;
    private TwitterLayer twitterLayer;

    public GameAction(String action, String id, double longditude, double latitude, String userName){
        ACTION = action.toLowerCase();
        uid = id;
        lng = longditude;
        lat = latitude;
        uname = userName;
        db = new DataInterface();
        twitterLayer = new TwitterLayer();
    }

    @Override
    public void run() {

        switch (ACTION){
            case "play" :
                createGame();
                break;
            case "quit":
                endGame();
                break;
            case "ping":
                ping();
                break;
            case "rules":
                rules();
                break;
            default:
                rules();
                break;

        }

    }

    private boolean postStatus(String text){
        boolean result = false;
        try{
            Logger.debug("@"+uname+" " + text);

            Random r = new Random();
            String rnd_text = Integer.toString(r.nextInt());

            twitterLayer.postStatus("@"+uname+" " + text + rnd_text);
            result = true;
        }catch (Exception e){
            Logger.error("There was an error posting a status" + e);
        }
        return result;
    }

    private void createGame() {

        if(db.activeGame(uid) == false){
            boolean stat1 = db.startGame(uid, lng,lat);

            if(stat1 == true){
                String map = db.getRecentPings(uid);
                postStatus(map);

            }

        }else{
            postStatus(" you have and active Game. Read the rules, maybe try ping!");
        }


    }
    private void ping() {


        boolean stat1 = db.ping(uid, lng, lat);

        if(stat1 == true){
            String map = db.getRecentPings(uid);
            if(db.winningPing(uid) == true){
                postStatus(map + " You found it good Work! Game Over");
            }
            else{
                postStatus(map);
            }
        }

    }

    private void endGame() {
        if(db.activeGame(uid) == true)
        {
            db.quit(uid);
            postStatus("Game Over. Try Again sometime");

        }

    }

    private void rules() {
        String rulesURL = " @url:https://d13dlmp0uwngbc.cloudfront.net/rules.html ";
        postStatus("read the rules.... Have a Glance at" + rulesURL);

    }


}
