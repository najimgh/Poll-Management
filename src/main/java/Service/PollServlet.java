package Service;

import app.PollManager;
import local.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "Service.PollServlet")
public class PollServlet extends HttpServlet {
    PollManager pollManager;
    private static DatabaseConnection openConnection;


    @Override
    public void init() {
        pollManager = new PollManager();
        openConnection = DatabaseConnection.start(); //Start the connection to the database
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Disable caching
        response.setHeader("Expires", "-1");
        //Setting response content type to .txt
        response.setContentType("text/plain");
        Connection conn = DatabaseConnection.start().getConnection(); //Get the Connection object
        try {
            switch (request.getParameter("Command")) {
                case "GetPoll": {

                    int status = pollManager.getStatus();
                    if (status == 1) {
                        String title = pollManager.getName();
                        String question = pollManager.getQuestion();
                        String[] choices = pollManager.getChoices();
                        try (PrintWriter output = response.getWriter()) {
                            output.println(title);
                            output.println(question);
                            for (int i = 0; i < choices.length; i++) {
                                output.println(choices[i]);
                            }

                        } catch (Exception e) {
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                        }
                    } else throw new Exception("Cannot vote on non-running poll");
                    break;

                }
                case "GetResults": {
                    String pollId = request.getParameter("PollId");
                    String title = pollManager.getName();
                    String results = pollManager.accessPoll("View", pollId, "", "", conn).toString();
                    try (PrintWriter output = response.getWriter()) {
                        output.println(title);
                        output.println(results);

                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                    break;
                }
                case "DownloadResults": {
                    try (PrintWriter output = response.getWriter()) {

                        String date = pollManager.dtf.format(pollManager.date);
                        String fileName = pollManager.getName() + " - " + date;
                        switch (request.getParameter("Format")) {
                            case "text": {
                                response.setHeader("Content-disposition", fileName + ".txt");
                                pollManager.downloadPollDetails(output, "text");
                                break;
                            }
                            case "xml": {
                                response.setHeader("Content-disposition", fileName + ".xml");
                                pollManager.downloadPollDetails(output, "xml");
                                break;
                            }
                            case "json": {
                                response.setHeader("Content-disposition", fileName + ".json");
                                pollManager.downloadPollDetails(output, "json");
                                break;
                            }
                        }
                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                     break;
                    }
                case "SignIn": {
                    String username = request.getParameter("Username");
                    String hashedPassword = request.getParameter("Password");
                    try (PrintWriter output = response.getWriter()) {

                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                    break;
                }
                case "RequestPin":{
                    //TODO: REQUEST PIN LOGIC
                    String pollId = request.getParameter("PollId");
                    String requestedPin = pollManager.requestPIN();
                    try (PrintWriter output = response.getWriter()) {
                            output.println(requestedPin);
                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                    break;
                }
                case "ViewAllPolls":{
                    //TODO: REQUEST PIN LOGIC
                    String userId = request.getParameter("UserId");
                    List<String> pollIds = pollManager.getListPoll(userId, conn);
                    try (PrintWriter output = response.getWriter()) {

                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                    break;
                }

                default:
                    throw new Exception("No match");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(); //Get the session object.
            Connection conn = DatabaseConnection.start().getConnection(); //Get the Connection object
            String pollId;
            String userId;

            //Based on the button clicked, the following methods will execute
            switch (request.getParameter("Command")) {
                case "CreatePoll":
                    userId = request.getParameter("UserId");
                    pollManager.CreatePoll(request.getParameter("PollName"), request.getParameter("PollQuestion"), request.getParameterValues("PollChoices[]"), userId, conn);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("<!DOCTYPE html>");
                        out.println("<html>");
                        out.println("<head>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<p>Poll created successfully</p>");
                        out.println("<a href=\"pollManager.jsp\">Return to Poll Manager</a>");
                        out.println("</body>");
                        out.println("</html>");
                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                    break;
                case "UpdatePoll":
                    pollManager.UpdatePoll(request.getParameter("PollName"), request.getParameter("PollQuestion"), request.getParameterValues("PollChoices[]"));
                    try (PrintWriter out = response.getWriter()) {
                        out.println("<!DOCTYPE html>");
                        out.println("<html>");
                        out.println("<head>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<p>Poll updated successfully</p>");
                        out.println("<a href=\"pollManager.jsp\">Return to Poll Manager</a>");
                        out.println("</body>");
                        out.println("</html>");
                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                    break;
                case "ClearPoll":
                    pollManager.ClearPoll();
                    break;
                case "RunPoll":
                    pollManager.RunPoll(conn);
                    break;
                case "ReleasePoll":
                    pollManager.ReleasePoll(conn);
                    break;
                case "UnreleasePoll":
                    pollManager.UnreleasePoll(conn);
                    break;
                case "ClosePoll":
                    pollManager.ClosePoll(conn);
                    break;
                case "DeletePoll":
                    pollId = request.getParameter("PollId");
                    userId = request.getParameter("UserId");
                    pollManager.DeletePoll(userId, pollId, conn);
                    break;
                case "Vote":
                    pollId = request.getParameter("PollId");
                    userId = request.getParameter("UserId");
                    String participantID = session.getId(); //Get session ID
                    String[] choiceList = pollManager.getChoices();
                    String choice = request.getParameter("VoteChoice");
                    //Determine if the choice is valid by comparing the value to the list choices in the Poll.
                    boolean validChoice = Arrays.stream(choiceList).anyMatch(choice::equals);
                    if (validChoice) {
                        pollManager.accessPoll("Vote", pollId, userId, choice, conn);
                    } else {
                        throw new Exception("Invalid choice");
                    }
                    try (PrintWriter out = response.getWriter()) {
                        out.println("<!DOCTYPE html>");
                        out.println("<html>");
                        out.println("<head>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<p>Vote recorded successfully</p>");
                        out.println("<a href=\"index.jsp\">Return to main page</a>");
                        out.println("</body>");
                        out.println("</html>");
                    } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    }
                    break;
                default:
                    throw new Exception("No match");
            }
            openConnection.stop(); //Stop the connection to the database
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
