package app;

import org.h2.util.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.json.*;
import local.DatabaseConnection;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PollManager {
    private final int CREATED = 0;
    private final int RUNNING = 1;
    private final int RELEASED = 2;
    private final int CLOSED = 3;
    private Poll poll;
    private List<String> listPollID;
    private List<String> listPinNumbers;
    protected int pollCount = 0;

    public LocalDateTime date;
    public DateTimeFormatter dtf;

    public PollManager() {
        this.poll = new Poll();
        this.listPollID = new ArrayList<String>();
        this.listPinNumbers = new ArrayList<String>();
        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    }

    /*
     * Create a Poll if it's not running
     * return the poll ID
     */
    public String CreatePoll(String name, String question, String[] choices, String user, Connection conn) throws Exception {
        if (poll.getStatus() != RUNNING) {
            this.pollCount++;
            this.poll.setID(createID());
            this.poll.setName(name);
            this.poll.setQuestion(question);
            this.poll.setChoices(choices);
            this.poll.clearResult();
            // add the user instead of 'Andre'
            String query = "INSERT INTO POLL.poll VALUES('" + poll.getID() + "', '" + poll.getName() + "', 'CREATED','" + user + "');";
            try(Statement stmt = conn.createStatement()){
                stmt.execute(query);
                query = "INSERT INTO POLL.POLL_QUESTION VALUES('" + poll.getID() + "', '" + poll.getQuestion() + "');";
                stmt.execute(query);
                for(int i = 0; i < choices.length; i++){
                    query = "INSERT INTO POLL.POLL_ANSWER VALUES('" + poll.getID() + "', '" + poll.getQuestion() + "','" + choices[i] + "');";
                    stmt.execute(query);
                }
                System.out.println("Success");
            } catch (Exception e){
                System.out.println(e);
            }
            for (int i = 0; i < choices.length; i++) {
                this.poll.addResult(this.poll.getChoiceAtIndex(i));
            }
            date = LocalDateTime.now();
            System.out.println("Poll is created");
            return this.poll.getID();
        } else throw new Exception("Cannot create a poll when a poll is already running");
    }

    /*
     * return the question of the poll
     */
    public String getQuestion() {
        return this.poll.getQuestion();
    }

    /*
     * return the name of the poll
     */
    public String getName() {
        return this.poll.getName();
    }

    /*
     * return the list of choices
     */
    public String[] getChoices() {
        return this.poll.getChoices();
    }

    /*
     * return the status
     */
    public int getStatus() {
        return this.poll.getStatus();
    }

    /*
     * return the poll count
     */
    public int getpollCount() {
        return this.pollCount;
    }

    /*
     * Update the poll and clear previous results
     */
    public void UpdatePoll(String name, String question, String[] choices) throws Exception {
        if (poll.getStatus() == CREATED | poll.getStatus() == RUNNING) {
            if (!name.equals("")) {
                this.poll.setName(name);
            }
            if (!question.equals("")) {
                this.poll.setQuestion(question);
            }

            //assumes if first choice is empty then user did not intend to update choices, updates choices otherwise
            if (!choices[0].equals("")) {
                this.poll.setChoices(choices);
            }
            this.poll.setStatus(CREATED);
            this.poll.clearResult();
            for (int i = 0; i < poll.getChoices().length; i++) {
                this.poll.addResult(this.poll.getChoiceAtIndex(i));
            }
            System.out.println("Poll is updated");
        } else throw new Exception("Poll is not newly created or running. Impossible to update it");
    }

    /*
     * results will be cleared(set all results equal to 0) but for released the status will be change to created
     */
    public void ClearPoll() throws Exception {
        int ChoicesLength = this.poll.getChoices().length;
        if (poll.getStatus() == RUNNING || poll.getStatus() == RELEASED) {
            this.poll.clearResult();
            for (int i = 0; i < ChoicesLength; i++) {
                this.poll.addResult(this.poll.getChoiceAtIndex(i));
            }

            if (poll.getStatus() == RELEASED) {
                this.poll.setStatus(CREATED);
            }
        } else throw new Exception("Poll is not running nor released");
    }

    /*
     * Close poll if status is equal release
     */
    public void ClosePoll(Connection conn) throws Exception {
        if (poll.getStatus() == RELEASED) {
            this.poll.setStatus(CLOSED);
            this.pollCount--;
            this.poll = null;
            String query = "UPDATE POLL SET STATUS='CLOSED' WHERE ID='" + poll.getID() + "';";
            try(Statement stmt = conn.createStatement()){
                stmt.execute(query);
                System.out.println("Poll is now closed");
            } catch (Exception e){
                System.out.println(e);
            }
        } else throw new Exception("Cannot close an unreleased poll");
    }

    /*
     * set status to running if poll is created
     */
    public void RunPoll(Connection conn) throws Exception {
        if (poll.getStatus() == CREATED) {
            this.poll.setStatus(RUNNING);
            String query = "UPDATE POLL SET STATUS='RUNNING' WHERE ID='" + poll.getID() + "';";
            try(Statement stmt = conn.createStatement()){
                stmt.execute(query);
                System.out.println("Poll is now running");
            } catch (Exception e){
                System.out.println(e);
            }
        } else throw new Exception("Cannot run a non-created poll");
    }

    /*
     * set poll status to release
     */
    public void ReleasePoll(Connection conn) throws Exception {
        if (poll.getStatus() == RUNNING) {
            this.poll.setStatus(RELEASED);
            String query = "UPDATE POLL SET STATUS='RELEASED' WHERE ID='" + poll.getID() + "';";
            try(Statement stmt = conn.createStatement()){
                stmt.execute(query);
                date = LocalDateTime.now();
                System.out.println("Poll is now released");
            } catch (Exception e){
                System.out.println(e);
            }
        } else throw new Exception("Poll was not in running state");
    }

    /*
     * create a unique ID
     * @return the unique ID
     */
    public String createID(){
        String characters = "ABCDEFGHJKMNPQRSTVWXYZ0123456789";
        Random random = new Random();
        String ID = "";
        boolean isOk = false;
        while(!isOk){
            isOk = true;
            for (int i = 0; i < 10; i++){
                int randomInt = random.nextInt(characters.length());
                ID += characters.charAt(randomInt);
            }
            // check if unique
            for (int i = 0; i < listPollID.size(); i++){
                if(ID.equalsIgnoreCase(listPollID.get(i))){
                    isOk = false;
                }
            }
        }
        listPollID.add(ID);
        return ID;
    }

    /*
     * create a unique ID
     * @return the unique ID
     */
    public String requestPIN(){
        String characters = "0123456789";
        Random random = new Random();
        String PIN = "";
        boolean isOk = false;
        while(!isOk){
            isOk = true;
            for (int i = 0; i < 6; i++){
                int randomInt = random.nextInt(characters.length());
                PIN += characters.charAt(randomInt);
            }
            // check if unique
            for (int i = 0; i < listPinNumbers.size(); i++){
                if(PIN.equalsIgnoreCase(listPinNumbers.get(i))){
                    isOk = false;
                }
            }
        }
        listPinNumbers.add(PIN);
        return PIN;
    }

    /*
     * either view or vote on a poll
     */
    public Hashtable<String, Integer> accessPoll(String option, String pollID, String userID, String choice, Connection conn) throws Exception{
        switch(option){
            case "Vote":
                if (poll.getStatus()== RUNNING){
                    Vote(choice, pollID, userID, conn);
                } else throw new Exception("Cannot vote on a poll that is not running");
                break;
            case "View":
                if (poll.getStatus() == RELEASED) {
                    return getPollResults();
                } else throw new Exception("Poll is not in released state");
            default:
                break;
        }
        return null;
    }

    /*
     * set poll back to running
     */
    public void UnreleasePoll(Connection conn) throws Exception {
        if (poll.getStatus() == RELEASED) {
            this.poll.setStatus(RUNNING);
            String query = "UPDATE POLL SET STATUS='RUNNING' WHERE ID='" + poll.getID() + "';";
            try(Statement stmt = conn.createStatement()){
                stmt.execute(query);
                System.out.println("Poll is now running");
            } catch (Exception e){
                System.out.println(e);
            }
        } else throw new Exception("Poll was not in released state");
    }

    /*
     * delete a poll if it is the creator
     */
    public void DeletePoll(String user, String pollID, Connection conn) throws Exception {
        //String query = "SELECT COUNT(*) FROM PIN WHERE ID='1BNUDC208A';";
        String query = "SELECT CREATEDBY,ID FROM POLL.POLL WHERE ID='" + pollID + "';";
        try(Statement stmt = conn.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            String creator = rs.getString("CREATEDBY");
            String id = rs.getString("ID");
            if (user.equalsIgnoreCase(creator)){
                if (pollID.equalsIgnoreCase(id)){
                    query = "DELETE FROM POLL.POLL WHERE ID='" + id + "';";
                    stmt.executeQuery(query);
                } else throw new Exception("It is not the right ID for the poll");
            } else throw new Exception("It is not the right owner of the created poll");
            System.out.println("Success");
        } catch (Exception e){
            System.out.println(e);
        }
    }

    /*
     * let a participant vote and increase the number in results
     */
    public void Vote(String modChoice, String pollID, String PIN, Connection conn) throws Exception {
        if (poll.getStatus() == RUNNING) {
            if (PINexist(PIN)){
                String query = "UPDATE POLL.POLL_VOTE SET ANSWER_ID =(SELECT ID FROM POLL.POLL_ANSWER WHERE POLL_ID='" +
                        pollID + "' AND ANSWER='" + modChoice + "') WHERE PIN='" + PIN + "' AND POLL_ID='" +
                        pollID + "';";
                try(Statement stmt = conn.createStatement()){
                    stmt.executeQuery(query);
                    System.out.println("Vote is a success");
                } catch (Exception e){
                    System.out.println(e);
                }
            } else {
                String newPIN = requestPIN();
                String query = "INSERT INTO POLL.POLL_VOTE VALUES('"+ poll.getID() +
                        "', (SELECT ID FROM POLL.POLL_QUESTION WHERE POLL_ID='" + poll.getID() +
                        "'), (SELECT ID FROM POLL.POLL_ANSWER WHERE POLL_ID='" + poll.getID() +
                        "'),'" + newPIN + "',1);";
                try(Statement stmt = conn.createStatement()){
                    stmt.executeQuery(query);
                    System.out.println("Vote is a success");
                } catch (Exception e){
                    System.out.println(e);
                }
            }
        } else throw new Exception("Cannot vote on a poll that is not running");
    }

    /*
     * return the hash table of the results of the poll
     */
    public Hashtable<String, Integer> getPollResults() throws Exception {
        if (poll.getStatus() == RELEASED) {
            return this.poll.getResults();
        } else throw new Exception("Cannot access results of a unreleased poll");
    }

    public boolean PINexist(String PIN){
        for (int i = 0; i<listPinNumbers.size(); i++){
            if (PIN.equalsIgnoreCase(listPinNumbers.get(i))){
                return true;
            }
        }
        return false;
    }
    /*
     * return a list of String to print of all poll created by this user
     */
    public List<String> getListPoll(String user, Connection conn){
        List<String> listPoll = new ArrayList<String>();
        String query = "SELECT ID,NAME,STATUS,DISTINCT(QUESTION) FROM POLL.POLL,POLL.QUESTION WHERE CREATEDBY='" + user + "';";
        System.out.println("All the poll created by " + user + ":{ ");
        try(Statement stmt = conn.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                String status =  rs.getString("STATUS");
                String question = rs.getString("QUESTION");

                question = question.replaceAll("[?]", "");
                String line = id + " - " + name + " [" + status + "] (" + question + "?)";
                listPoll.add(line);
            }
            System.out.println("list of polls created by this user has been returned");
        } catch (Exception e){
            System.out.println(e);
        }
        return listPoll;
    }

    /*
     * download results of the poll
     */
    public void downloadPollDetails(PrintWriter output, String format) throws Exception {
        if (poll.getStatus() == RELEASED) {
            try {
                switch (format) {
                    case "text": {
                        output.append(this.getName() + "\n");
                        output.append(this.getQuestion() + "\n");
                        output.println("Number of vote for each choices:");
                        Set<String> setOfChoices = poll.getResults().keySet();
                        for (String key : setOfChoices) {
                            output.println(key + " " + Integer.toString(poll.getNbVote(key)));
                        }
                        break;
                    }
                    case "xml":{
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                        Document doc = docBuilder.newDocument();
                        Element rootElement = doc.createElement("POLL");
                        doc.appendChild(rootElement);

                        Element title= doc.createElement("TITLE");
                        title.appendChild(doc.createTextNode(this.getName()));
                        rootElement.appendChild(title);

                        Element question = doc.createElement("QUESTION");
                        question.appendChild(doc.createTextNode(this.getQuestion()));
                        rootElement.appendChild(question);

                        Element choices = doc.createElement("CHOICES");
                        Set<String> setOfChoices = poll.getResults().keySet();
                        for (String key : setOfChoices) {
                            Element choice = doc.createElement("CHOICE");
                            choice.appendChild(doc.createTextNode(key));
                            choices.appendChild(choice);

                            Element numVotes = doc.createElement("NUMBER_OF_VOTES");
                            numVotes.appendChild(doc.createElement(Integer.toString(poll.getNbVote(key))));
                            choices.appendChild(numVotes);
                        }


                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer transformer = tf.newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        StringWriter writer = new StringWriter();
                        transformer.transform(new DOMSource(doc), new StreamResult(writer));
                        String xmlOutput = writer.getBuffer().toString().replaceAll("\n|\r", "");
                        output.println(xmlOutput);
                    }

                    case "json":{
                        JsonArrayBuilder choicesArray = Json.createArrayBuilder();
                        JsonArrayBuilder resultsArray = Json.createArrayBuilder();
                        Set<String> setOfChoices = poll.getResults().keySet();
                        for (String key : setOfChoices){
                            choicesArray.add(key);
                            resultsArray.add(Integer.toString(poll.getNbVote(key)));
                        }


                        String jsonString = Json.createObjectBuilder()
                                .add( "Title", this.getName())
                                .add( "Question", this.getQuestion())
                                .add("Choices", choicesArray)
                                .add("Results", resultsArray)
                                .build()
                                .toString();
                        output.println(jsonString);
                    }
                }
            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            throw new Exception("Cannot download from an unreleased poll");
        }
    }
}
