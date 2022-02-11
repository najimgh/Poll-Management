package app;

import java.util.*;
public class Poll {
    final int CREATED = 0;
    final int RUNNING = 1;
    final int RELEASED = 2;
    final int CLOSED = 3;


    private int status;
    private String ID;
    private String name;
    private String question;
    private String creator;
    private String[] choices;
    private Hashtable<String, Integer> results;

    /*
     * Default Poll() constructor
     */
    public Poll() {
        // predefine that choices will be limited to 4 choices at max
        this.choices = new String[4];
        this.ID = null;
        this.name = null;
        this.question = null;
        this.creator = null;
        this.status = CREATED;
        this.results = new Hashtable<String, Integer>();
    }

    /*
     * return the status of the poll
     */
    public int getStatus() {
        return status;
    }

    /*
     * set the status of the poll
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /*
     * return the name of the poll
     */
    public String getName() {
        return this.name;
    }

    /*
     * return the ID of the poll
     */
    public String getID(){ return this.ID;}

    /*
     * return the creator of the poll
     */
    public String getCreator(){ return this.creator;}
    /*
     * return the array of choices of the poll
     */
    public String[] getChoices() {
        return this.choices;
    }

    /*
     * return the choice at the index i
     */
    public String getChoiceAtIndex(int i) {
        return this.choices[i];
    }

    /*
     * set the ID
     */
    public void setID(String ID) { this.ID = ID;}

    /*
     * set the name of the poll
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * return the question of the poll
     */
    public String getQuestion() {
        return this.question;
    }

    /*
     * set the question of the poll
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /*
     * set the choices of the poll and results
     */
    public void setChoices(String[] choices) {
        for (int i = 0; i < choices.length; i++) {
            this.choices[i] = choices[i];
        }
    }

    /*
     * set the creator
     */
    public void setCreator(String creator){
        this.creator = creator;
    }

    /*
     * removes all instance in hashtable results
     */
    public void clearResult() {
        this.results.clear();
    }

    /*
     * add a choice/result to the hashtable results with a value of 0
     */
    public void addResult(String choice) {
        if (choice != null) {
            this.results.put(choice, 0);
        }
    }

    /*
     * add 1 to the choice passed in the results
     */
    public void addVote(String choice) {
        int number = this.results.get(choice);
        number++;
        this.results.replace(choice, number);
    }

    public Hashtable<String, Integer> getResults() {
        return this.results;
    }

    public int getNbVote(String key) {
        return this.results.get(key);
    }

    public String createID(){
        return "";
    }

    public boolean gotVoted(){
        int nbVote = 0;
        Set<String> keys = results.keySet();

        for(String key: keys){
            nbVote += results.get(key);
        }

        if (nbVote > 0) {
            return true;
        } else {
            return false;
        }
    }
}
