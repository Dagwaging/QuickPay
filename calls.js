var figo = require("figo");
var open = require("open");

var clientId = "CMpd1Z4YK98zWLeZyr-9A4Q-YO_AFrALkSz85ABpHFsc";
var secret = "SINAbQb46PiQNkPTGOxxMOd2wD9orWnO8AA58_42F6rg";

var connection = new figo.Connection(clientId, secret, "http://localhost:3000/");

var start_login = function() {
    // Open web browser to kick of the login process.
    open(connection.login_url("qweqwe"));
};

var process_redirect = function(authorization_code, state) {
    // Handle the redirect URL invocation from the initial start_login call.

    // Ignore bogus redirects.
    if (state !== "qweqwe") {
        return;
    }

    // Trade in authorization code for access token.
    var token_dict = connection.obtain_access_token(authorization_code, null, function(error, token_dict) {
        if (!error) {

            // Start session.
            var session = new figo.Session(token_dict.access_token);

            // Print out list of account numbers.
            session.get_accounts(function(error, accounts) {
                if (!error) {
                    accounts.forEach(function(account) {
                        console.log(account.account_number);
                    })
                }
            });
        }
    });
};




$(document).ready(function() {
    $( "#create" ).onclick( function() {
        connection.query_api("/auth/user", $( "#new_user").serialize(),
            function(data, result) {
                if (result === "error") {
                    alert("User with that email already exists");
                } else if (result === "success") {
                    window.location.href = "userHome.html";}})});

    $("#unlock").onclick( function() {
        connection.query_api("/auth/unlock", "{\"username\": \""+$( "#logform").elements["email"]+"\"}",
            function(data, result) {
                if (result === "error") {
                    alert("No user associated with given email");
                } else if (result === "success") {
                    window.location.href = "#";
                    alert("Reset Password email sent");}})});

    $("#login").onclick( function() {
        start_login();
        connection.query_api("/rest/user", $( "#logform").serialize(),
            function(data, result) {
                if (result === "error") {
                    alert("Incorrect email or password");
                } else if (result === "success") {
                    alert("It worked");
                    window.location.href = "userHome.html";
                } else {
                    alert("Failed");}})});
});