package app;
import local.DatabaseConnection;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
public class PollDriver {
    public static void main(String[] args){
        String characters = "ABCDEFGHJKMNPQRSTVWXYZ0123456789";
        Random random = new Random();
        String ID = "";
        for (int i = 0; i < 10; i++){
            int randomInt = random.nextInt(characters.length());
            ID += characters.charAt(randomInt);
        }

        System.out.println("your ID is: " + ID);

        int Sum = 0;
        Hashtable<String,Integer> testHash = new Hashtable<String,Integer>();
        testHash.put("choice1", 1);
        testHash.put("choice2", 2);
        Set<String> keys = testHash.keySet();
        for(String key: keys){
            Sum += testHash.get(key);
        }
        System.out.println("Sum is " + Sum);
        String hello = "Hello?";
        hello = hello.replaceAll("[?]", "");
        System.out.println(hello);
        //====================================================
        /*
        DatabaseConnection openConnection;
        openConnection = DatabaseConnection.start();
        //System.out.println("everything above is ok?");
        Connection conn = DatabaseConnection.start().getConnection();
        String query = "INSERT INTO POLL VALUES('ABCDE12345', 'testPollDriver', 'CREATED', 'Andre', 'DriverQuestion', 'C1', 'C2', 0,0);";
        try(Statement stmt = conn.createStatement()){
            stmt.execute(query);
            System.out.println("Success");
        } catch (Exception e){
            System.out.println("Problem occured");
        }
        openConnection.stop();
         */
    }
}
