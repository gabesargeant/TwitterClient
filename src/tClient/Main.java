package tClient;

import org.pmw.tinylog.Logger;
/*
*  This Bot? is a state controller for a game.
*  In short, it assigns points for people to find
*  They start a game via the tweet Play Game.
*  The Bot generates a point about ~ 1km away
*  They are provided an area where this point is.
*  They then go looking. Tweeting ping! to get more details.
*  As they get closer and narrow their points down they
*  either tweet quit to leave or keep pinging to find the location.
*
*  If they are within 15 meters of the point they win.
*
*  Created By G.Sargeant to have a play with the MySQL spatial functions.
*  Contact Gabe.Sargeant@gmail.com for details etc.
*
*  The Web Component Is a fairly heavy ESRI Web Map. All Static :)
*
* */


public class Main {

    private TwitterStreamLayer twitterStreamLayer;

    public static void main(String[] args) {
        DataInterface db = new DataInterface();
        if(db.testConnect() == true){


        TwitterStreamLayer twitterStreamLayer;
        twitterStreamLayer = new TwitterStreamLayer();
        twitterStreamLayer.setStream();

        while(twitterStreamLayer.isTwitter()){

        }
        }else {
            Logger.error("Cant connect to db");
        }

        Logger.info("The program has ended");
    }
}
