package tClient;

import org.pmw.tinylog.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Random;
import java.util.Date;

import static java.lang.Math.abs;

/*
 * Created by G.Sargeant on and around 23/10/16.
 * This is the mysql database layer.
 * This handles all reads and writes to the DB.
 * No Logic in the DB!
 *
 */
public class DataInterface {

    private Properties prop = new Properties();

    //Create Database layer,
    //Read in .properties files with db connection information.
    public DataInterface(){
        try{
            loadProperties();
        }catch (Exception e){
            Logger.error("There was an exception reading the properties file.");
        }


    }

    private void loadProperties() throws IOException {
        InputStream in = getClass().getResourceAsStream("dataInterface.properties");
        prop.load(in);
        in.close();
    }

    //Connect ot mysql db specified in dataInterface.properties fi147.3267628 -42.8822598le.
    private Connection getConnection(){
        String host = prop.getProperty("host");
        String port = prop.getProperty("port");

        String db_name = prop.getProperty("db_name");
        String db_user= prop.getProperty("db_user");
        String psw = prop.getProperty("psw");
        Connection con = null;
        String connection_details;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection_details = "jdbc:mysql://" + host + ":" + port + "/" + db_name+"?allowMultiQueries=true";
            con = DriverManager.getConnection(connection_details, db_user, psw);

        } catch (Exception e) {
            Logger.error("There be an exceptions " + e);
        }

        return con;
    }

    public boolean testConnect(){
        Connection con = getConnection();
        if(con == null){
            return false;
        }else{
            return true;
        }
    }


    public double getDistance(double lng,double lat, double lng1, double lat1 ){

        double dist=0;

        Connection con = getConnection();
        try {

            Statement stmt = con.createStatement();
            String q1="SET @pt1 = ST_GeomFromText('POINT("+lng+" "+lat+")');";
            String q2="SET @pt2 = ST_GeomFromText('POINT("+lng1+" "+lat1+")');";
            String q3="SELECT ST_Distance(@pt1, @pt2);";
            Logger.debug("SQL = "+ q1+q2+q3);
            PreparedStatement ps1 = con.prepareStatement(q1);
            ps1.executeQuery();
            PreparedStatement ps2 = con.prepareStatement(q2);
            ps2.executeQuery();
            PreparedStatement ps3 = con.prepareStatement(q3);
            ResultSet rs = ps3.executeQuery();
            //rs2.beforeFirst();
            while(rs.next()){
                dist = rs.getDouble(1);
            }
            Logger.debug("This is the distance in cartesian coords from mysql :::::" + dist);
            dist = dist * 111000;

            con.close();

        }catch (Exception e){
            Logger.error("there was an error with with map distance" + e);
        }

        return dist;
    }

    //take user point, create a target point,
    //return a distance to the target point.
    //is user in active game?
    public boolean startGame(String uid, double lng, double lat){
        boolean ans = true;
        String result;
        double coords[];

        //enters points into db and first ping.
        result = makeTargetPoint(lng,lat);
        coords = extractCoord(result);
        boolean stat1 = insertStartGame(uid, coords[0], coords[1], lng,lat);

        boolean stat2 = ping(uid, lng, lat);


        if(stat1 == false || stat2 == false){
            ans = false;
        }
        Logger.debug("The user can start a new game? = " + ans);
        return ans;

    }

    //String in, points out.
    private double[] extractCoord(String point){

        double coords[] = new double[2];

        //POINT(147.31626933207124 -43.00241337891196)
        String lng, lat;

        lng = point.substring(point.indexOf("(") + 1, point.indexOf(" "));
        lat = point.substring(point.indexOf(" ") + 1, point.indexOf(")"));

        coords[0] = Double.parseDouble(lng);
        coords[1] = Double.parseDouble(lat);

        return coords;
    }

//****** This is not in use as the random method below doesn't user a point db to select random points.
//****** Just hanging onto it because the buffer query is interesting. maybe for future work.
//    private String makeTargetPoint(double lng,double lat){
//       ArrayList<String> list = new ArrayList<String>();
//
//        Connection con = getConnection();
//        try{
//            String q1="SET @pt = ST_GeomFromText('POINT("+lng+" "+lat+")');";
//            String q2="SET @geo = (ST_Buffer(@pt, 0.017966));";
//            String q3="SELECT ST_AsWKT(wkt) FROM points WHERE st_within(WKT, @geo);";
//            Logger.debug("SQL = " + q1+q2+q3);
//            PreparedStatement ps1 = con.prepareStatement(q1);
//            ps1.executeQuery();
//            PreparedStatement ps2 = con.prepareStatement(q2);
//            ps2.executeQuery();
//            PreparedStatement ps3 = con.prepareStatement(q3);
//            ResultSet rs = ps3.executeQuery();
//
//            while(rs.next()){
//                list.add(rs.getString(1));
//            }
//            con.close();
//
//        }catch (Exception e){
//            Logger.error("These was an error with getting close locatoins" + e);
//        }
//
//        Random r = new Random();
//
//        int High = list.size();
//        int result = r.nextInt(High);
//
//        String point = list.get(abs(result));
//
//        return point;
//    }


    //This generates a random point within a radius of ~1km
    //0.008983 is about 500m.
    //This means cartesian coords like 500 south and 500 to the east.
    //So a 1km square.
    private String makeTargetPoint(double lng,double lat){
        String point="";

        Random r = new Random();
        double d=0;
        d = -0.008983 + r.nextDouble() * 0.008983;
        lng += d;
        d = -0.008983 + r.nextDouble() * 0.008983;
        lat += d;
        Logger.info("Starting Long"+lng+"::::::: Starting Long"+lat+"\n");
        point = "POINT("+lng+" "+lat+")";
        Logger.info("This is the point of interest !!! = " + point);

        return point;
    }


    //"INSERT INTO game(user, point, gameNo) VALUES("+uid+",ST_GeomFromText('POINT("lng+" "+"lng")'), '1477440000');"
    private boolean insertStartGame(String uid, double lngTarget ,double latTarget, double lng,double lat){

        Date d = new Date();
        Connection con = getConnection();
        try{

            String q1="INSERT INTO game(user, point, found, gameNo) VALUES("+uid+",ST_GeomFromText('POINT("+lngTarget+" "+latTarget+")'),2,'"+  d.getTime()  +"');";
            Logger.debug("SQL = " + q1);
            Statement s1 = con.createStatement();

            s1.executeUpdate(q1);

            con.close();

        }catch (Exception e){
            Logger.error("These was an error with inserting the start game parameters. X" + e );
            return false;
        }
        return true;
    }


    //fetch starting point
    //calc distance between point and starting point.
    public boolean ping(String uid, double lng,double lat){
        double gameNo;
        double dist = -1;
        double coords[];
        boolean stat = activeGame(uid);
        if(stat == true){
            gameNo = getGameNo(uid);
            if(gameNo == -1){
                return false;
            }
        }else{
            return false; //break of out method if the game is over. mayber have a check here for winner
        }
        Connection con = getConnection();

        try{
            String q1="SELECT ST_AsWKT(point) FROM game WHERE gameNo=" + gameNo +";";
            Logger.debug("SQL = " + q1);
            Statement s1 = con.createStatement();

            ResultSet rs = s1.executeQuery(q1);
            rs.next();
            coords = extractCoord(rs.getString(1));
            dist = getDistance(lng,lat,coords[0],coords[1]);

            boolean result = insertPing(uid,lng,lat,dist,gameNo);
            if(result == false) {
                throw new Exception("Insert was faulty");
            }



        }catch (Exception e){
            Logger.error("There was an exception with getting the ping distance or a user");
        }

        return true;
    }

    private boolean insertPing(String uid, double lng,double lat, double dist, double gameNo){
        boolean result = true;
        Connection con = getConnection();
        try{
            String q1="INSERT INTO ping(user, point, game_gameNo, distToTarget) VALUES("+uid+",ST_GeomFromText('POINT("+lng+" "+lat+")'),'"+  gameNo  +"', "+ dist +");";;
            Logger.debug("SQL = " + q1);
            Statement s1 = con.createStatement();

            s1.executeUpdate(q1);

            con.close();

        }catch (Exception e){
            Logger.error("These was an error with inserting a ping from a user." + e);
            result = false;
        }

        return result;
    }

    private double getGameNo(String uid) {
        double gameNo=-1;
        Connection con = getConnection();
        try{

            String q1 = "SELECT gameNo FROM game WHERE user='"+uid+"' AND found='2';";
            Logger.debug("SQL = " + q1);
            Statement s1 = con.createStatement();
            ResultSet rs = s1.executeQuery(q1);
            rs.next();
            gameNo = rs.getDouble(1);
            con.close();

        }catch (Exception e){
            Logger.error("These was an error with getting the game number" + e);
        }

        return gameNo;
    }


    public boolean activeGame(String uid){
        int result=0;
        boolean ans = false;
        Connection con = getConnection();
        try{
            //this should either return 1 active game or zero actives games.

            String q1 = "SELECT count(found) FROM game WHERE user='"+uid+"' AND found='2';";
            Logger.debug("SQL = " + q1);

            Statement s1 = con.createStatement();
            ResultSet rs = s1.executeQuery(q1);
            rs.next();
            result = rs.getInt(1);
            con.close();

        }catch (Exception e){
            Logger.error("There was an error with getting active games." + e);
        }
        //2 = active game
        if(result == 1){
            ans = true;
        }

        return ans;
    }

    public String getRecentPings(String uid){
        String result="",tmp="";
        int num = 0;
        //select * from ping where user = 476 order by time desc limit 3;
        double gameNo = getGameNo(uid);
        Connection con = getConnection();
        try{
            String q1 = "SELECT X(point), Y(point), distToTarget FROM ping WHERE game_gameNo = "+gameNo+" ORDER BY time DESC LIMIT 3;";
            Logger.debug("SQL = " + q1);
            Statement s1 = con.createStatement();
            ResultSet rs = s1.executeQuery(q1);

            DecimalFormat df = new DecimalFormat("#.#########");
            //time = Double.valueOf(df.format(time));
            while(rs.next()){
                tmp += "long"+num+"="+df.format(rs.getDouble(1))+"&"+"lat"+num+"="+df.format(rs.getDouble(2))+"&"+"dist"+num+"="+df.format(rs.getDouble(3))+"&";
                num++;
            }
            con.close();

            String url = "@url:https://d13dlmp0uwngbc.cloudfront.net/loc.html?";
            result = url+tmp;
            Logger.debug(result);
        }catch (Exception e){
            Logger.error("There was and error getting recent pings \n" + result + "\n" + e );
        }

        return result;
    }

    public boolean winningPing(String uid){
        boolean result = false;

        double dist=-1;

        double gameNo = getGameNo(uid);
        Connection con = getConnection();
        try{
            String q1 = "SELECT distToTarget FROM ping WHERE game_gameNo = "+gameNo+" ORDER BY time DESC LIMIT 1;";
            Logger.debug("SQL = " + q1);
            Statement s1 = con.createStatement();
            ResultSet rs = s1.executeQuery(q1);

            rs.next();
            dist = rs.getDouble(1);

            if(dist <= 15){
                result = true;
                setWinner(gameNo);
            }


            con.close();
        }catch (Exception e){
            Logger.error("There was and error finding out if the latest ping was a winner " + e);
        }

        return result;
    }

    private void setWinner(double gameNo){

        Connection con = getConnection();
        try{
            Statement s1 = con.createStatement();
            String q1 = "UPDATE game SET found=8 where gameNo="+gameNo+";";
            Logger.debug("SQL = " + q1);
            s1.executeUpdate(q1);

            con.close();

        }catch (Exception e){
            Logger.error("There was and error setting a winner, Game no = " + gameNo +"\n"+ e);
        }
    }

    public boolean setQuit(double gameNo){
        boolean rslt=false;
        Connection con = getConnection();
        try{
            Statement s1 = con.createStatement();
            String q1 = "UPDATE game SET found=9 where gameNo="+gameNo+";";
            Logger.debug("SQL = " + q1);
            Logger.info("The player has quit Game Number = " + gameNo);
            s1.executeUpdate(q1);
            rslt = true;
            con.close();

        }catch (Exception e){
            Logger.error("There was and error quitting, Game no = " + gameNo +"\n"+ e);
            rslt=false;
        }
        return rslt;
    }

    public boolean quit(String uid){
        double gn = getGameNo(uid);

        boolean rslt = setQuit(gn);

        return rslt;

    }

}


