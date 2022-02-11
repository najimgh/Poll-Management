<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Poll Service</title>
    <link rel="stylesheet" href="pollStyle.css">
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="http://crypto-js.googlecode.com/svn/tags/3.0.2/build/rollups/md5.js"></script>
    <script type="text/javascript">
        $(function(){
            var resultsVisible = false;
            var username;

            $("#signInButton").on("click", function () {
                username = $("#usernameInput").val();
                var password = $("#passwordInput").val();
                var hashedPassword = CryptoJS.MD5(password).toString();
                var successful = false;
                $.get( "PollServlet", {Command: "SignIn", Username: username, Password: hashedPassword}, function( data ) {

                    //TODO: successful sign in logic
                    successful = true;

                }).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });

                if(successful) {
                    $("#pollManagerOptions").css("display", "block");
                }
            })

            $("#getPollIdResults").on("click", function () {
                $("#divPollIdForResult").css("display", "block");
            })
            $("#showResults").on("click", function(){
                resultsVisible = !resultsVisible;
                if(resultsVisible){
                    $("#piechart").css("display", "block");
                    google.charts.load('current', {'packages':['corechart']});
                    google.charts.setOnLoadCallback(drawChart);
                    var pollId = $("#resultsPollIdInput").val();
                    function drawChart() {
                        var chartData = new google.visualization.DataTable();
                        var title;
                        chartData.addColumn('string','choice');
                        chartData.addColumn('number','votes');

                        $.get( "PollServlet", {Command: "GetResults", PollId: pollId}, function( data ) {
                            var serverResult = data.split(/\r?\n/);

                            title = serverResult[0];
                            var pollResultsList = serverResult[1].split(",");

                            for(let i =0;i<pollResultsList.length;i++){
                                var formatedResult = pollResultsList[i].replace('{','');
                                var pollResult = formatedResult.split("=");
                                var resultArray = new Array();
                                resultArray.push(pollResult[0]);
                                resultArray.push(parseInt(pollResult[1]));
                                chartData.addRow(resultArray);
                            }
                            var options = {
                                title: title
                            };

                            var chart = new google.visualization.PieChart(document.getElementById('piechart'));

                            chart.draw(chartData, options);

                        }).fail(function (request, textStatus, errorThrown) {
                            $('#body').html(request.responseText);
                        });
                    }
                }
                else{
                    $("#piechart").css("display", "none");
                }


            });

            $("#getPollIdVote").on("click", function () {
                $("#divPollIdForResult").css("display", "block");
            });


            $("#participateInPoll").one("click",function(){
                    var pollId = $("#votePollIdInput").val();
                    $.get( "PollServlet", {Command: "GetPoll", PollId: pollId}, function( data ) {
                        var result = data.split(/\r?\n/);
                        $('#pollTitle').text(result[0]);
                        $('#pollQuestion').text(result[1]);

                        for(let i = 2; i<result.length;i++){

                            if(result[i] != "null" && result[i] != "") {
                                $("#pollForm").append('<label>' + result[i] + '</label>');
                                $("#pollForm").append('<input type="radio" value="' + result[i] + '" name="pollOption"><br>');
                            }
                        }
                        $("#pollForm").append('<input type="submit"  id="pollSubmit" value="Submit">');
                        $("#pollForm").append('<input type="hidden"  name="Command" value="Vote">');
                        $("#poll").css("display", "block");

                    }).fail(function (request, textStatus, errorThrown) {
                        $('#body').html(request.responseText);
                    });

            });

            $("#downloadButton").on("click", function (){
                $.get("PollServlet", { Command: "DownloadResults", Format: "Text" }, function (data, textStatus, response) {
                    var textToDownload = new Blob([data], {type: 'text/plain'});
                    var fileName = response.getResponseHeader("content-disposition");
                    var url = window.URL.createObjectURL(textToDownload);

                    $('#download_link').attr("href", url);
                    $('#download_link').attr("download", fileName);
                }).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
                $("#download_link").css("display", "block");
            });

            $("#xmlDownloadButton").on("click", function (){
                $.get("PollServlet", { Command: "DownloadResults", Format: "xml" }, function (data, textStatus, response) {
                    var textToDownload = new Blob([data], {type: 'text/plain'});
                    var fileName = response.getResponseHeader("content-disposition");
                    var url = window.URL.createObjectURL(textToDownload);

                    $('#download_link').attr("href", url);
                    $('#download_link').attr("download", fileName);
                }).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
                $("#download_link").css("display", "block");
            });

            $("#jsonDownloadButton").on("click", function (){
                $.get("PollServlet", { Command: "DownloadResults", Format: "json" }, function (data, textStatus, response) {
                    var textToDownload = new Blob([data], {type: 'text/plain'});
                    var fileName = response.getResponseHeader("content-disposition");
                    var url = window.URL.createObjectURL(textToDownload);

                    $('#download_link').attr("href", url);
                    $('#download_link').attr("download", fileName);
                }).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
                $("#download_link").css("display", "block");
            });


            $("#pollForm").on("submit", function () {
                var voteChoice =  $('input[name=pollOption]:checked', '#pollForm').val();
                $("#pollForm").append('<input type="hidden"  name="VoteChoice" value="' + voteChoice + '">');
            })



            $("#createPoll").on("click",function(){
                $("#pollCreation").css("display", "block");
            });

            $("#updatePoll").on("click", function () {

                $("#pollUpdate").css("display", "block");
            });

            $('#pollCreationForm').on("click",function() {
                $(this).append('<input type="hidden" name="Command" value="CreatePoll" /> ');
            });

            $('#pollUpdateForm').on("click",function() {
                var pollId = $("#pollId").val();
                $(this).append('<input type="hidden" name="Command" value="UpdatePoll" /> ');
                $(this).append('<input type="hidden" name="PollId" id="pollUpdateId" /> ');
                $("#pollUpdateID").val(pollId);
            });

            var numChoices = 2
            $("#addChoice").on("click", function(){
                if(numChoices < 4) {
                    $("#choiceTwo").after('<input type="text" name="PollChoices[]"/> ');
                    numChoices++;
                }
                else{
                    alert("Poll has maximum number of choices");
                }
            });

            var numChoicesUpdated = 2
            $("#addChoiceUpdated").on("click", function(){
                if(numChoicesUpdated < 4) {
                    $("#choiceTwoUpdated").after('<input type="text" name="PollChoices[]"/> ');
                    numChoicesUpdated++;
                }
                else{
                    alert("Poll has maximum number of choices");
                }
            });

            $("#runPoll").on("click",function(){
                var pollId = $("#pollId").val();
                $.post("PollServlet", { Command: "RunPoll", PollId: pollId }, function () {
                    $('#operationResult').css("color", "green");
                    $('#operationResult').text("Poll ran successfully");
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            });



            $("#clearPoll").on("click",function(){
                var pollId = $("#pollId").val();
                $.post("PollServlet", { Command: "ClearPoll", PollId: pollId  }, function () {
                    $('#operationResult').css("color", "green");
                    $('#operationResult').text("Poll cleared successfully");
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            });

            $("#releasePoll").on("click",function(){
                var pollId = $("#pollId").val();
                $.post("PollServlet", { Command: "ReleasePoll", PollId: pollId  }, function () {
                    $('#operationResult').css("color", "green");
                    $('#operationResult').text("Poll released successfully");
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            });

            $("#unreleasePoll").on("click",function(){
                var pollId = $("#pollId").val();
                $.post("PollServlet", { Command: "UnreleasePoll", PollId: pollId  }, function () {
                    $('#operationResult').css("color", "green");
                    $('#operationResult').text("Poll unreleased successfully");
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            });

            $("#closePoll").on("click",function(){
                var pollId = $("#pollId").val();
                $.post("PollServlet", { Command: "ClosePoll", PollId: pollId  }, function () {
                    $('#operationResult').css("color", "green");
                    $('#operationResult').text("Poll closed successfully");
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            });


            $("#deletePoll").on("click", function (){
                var pollId = $("#pollId").val();
                $.post("PollServlet", { Command: "Delete Poll", PollId: pollId  }, function (data) {
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            })

            $("#requestPin").on("click", function (){
                var pollId = $("#pollId").val();
                $.get("PollServlet", { Command: "RequestPin", PollId: pollId  }, function (data) {
                    var requestedPin = data;
                    $("#operationResult").html("Requested Pin is: " + requestedPin);
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            })


            $("#viewAllPolls").on("click", function (){
                $.get("PollServlet", { Command: "ViewAllPolls", UserId: username  }, function (data) {
                } ).fail(function (request, textStatus, errorThrown) {
                    $('#body').html(request.responseText);
                });
            })
            
        });
    </script>
</head>
<body id="body">
    <h1><%= "Poll Service" %>
    </h1>

    <br/>
    <a href=signup.jsp>Register new account</a>
    <br/>

    <label for="usernameInput">Username</label>
    <input type="text" id="usernameInput"><br>
    <label for="passwordInput">Password</label><br>
    <input type="password" id="passwordInput">
    <button id="signInButton">Sign In</button><br>
    <p id="operationResult"></p><br>


    <button id="getPollIdResults">View Poll Results</button>
    <button id="getPollIdVote">Participate In Poll</button>
    <button id="downloadButton">Download results as text file</button>
    <button id="xmlDownloadButton">Download results as XML file</button>
    <button id="jsonDownloadButton">Download results as JSON file</button>
    <button id="requestPin">Request User Pin</button>
    <a id="download_link" download="" href=”” >Click to download results</a>
    <div id="divPollIdForResult">
        <label for="resultsPollIdInput">Enter Poll Id For Results</label>
        <input  type= "text" id="resultsPollIdInput"><br>
        <button id="showResults">Enter</button>
    </div>
    <div id="divPollIdForVote">
        <label for="votePollIdInput">Enter Poll Id For Voting</label>
        <input  type= "text" id="votePollIdInput"><br>
        <button id="participateInPoll">Enter</button>
    </div>
    <div id="piechart"></div>
    <div id="poll">
        <p id="pollTitle"></p>
        <p id="pollQuestion"></p>
        <form id="pollForm" method="post" action="PollServlet">
            <label for="userId">User ID</label>
            <input type="text" id="userId">
        </form>
    </div>
    <div id="pollList"></div>
    <div id="pollManagerOptions">
    <button class ="authButtons" id="createPoll">Create Poll</button>
    <button class ="authButtons" id="runPoll">Run Poll</button>
    <button class ="authButtons" id="updatePoll">Update Poll</button>
    <button class ="authButtons" id="clearPoll">Clear Poll</button>
    <button class ="authButtons" id="releasePoll">Release Poll</button>
    <button class ="authButtons" id="unreleasePoll">Unrelease Poll</button>
    <button class ="authButtons" id="closePoll">Close Poll</button>
    <button class ="authButtons" id="deletePoll">Delete Poll</button>
    <button class ="authButtons" id="viewAllPolls">View All Polls</button>
    </div>
    <div id="pollCreation">
        <form id="pollCreationForm" action="PollServlet" method="post">
            <label></label>
            <input type="text" id="pollCreationUserId" name="userId">User PIN<br>
            <label for="name">Poll Name</label><br>
            <input type="text" id="name" name="PollName"><br>
            <label for="question">Question</label><br>
            <input type="text" id="question" name="PollQuestion"><br>
            <label id="choiceLabel" for="choiceOne">Choices: </label><br>
            <input type="text" id="choiceOne" name="PollChoices[]">
            <input type="text" id="choiceTwo" name="PollChoices[]">
            <input type="submit" value="Submit">
        </form>
        <button id="addChoice">Add Choice</button>
    </div>
    <div id="pollUpdate">
        <form id="pollUpdateForm" action="PollServlet" method="post">
            <label for="name">Poll Name</label><br>
            <input type="text" id="updateName" name="PollName"><br>
            <label for="question">Question</label><br>
            <input type="text" id="updateQuestion" name="PollQuestion"><br>
            <label id="updateChoiceLabel" for="choiceOne">Choices: </label><br>
            <input type="text" id="choiceOneUpdated" name="PollChoices[]">
            <input type="text" id="choiceTwoUpdated" name="PollChoices[]">
            <input type="submit" value="Submit">
        </form>
        <button id="addChoiceUpdated">Add Choice</button>
    </div>
    <br/>


</body>
</html>