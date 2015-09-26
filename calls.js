var figo = require("figo");
var open = require("open");
var clientId = "CMpd1Z4YK98zWLeZyr-9A4Q-YO_AFrALkSz85ABpHFsc";
var secret = "SINAbQb46PiQNkPTGOxxMOd2wD9orWnO8AA58_42F6rg";

var connection = new figo.Connection(clientId, secret, "http://my-domain.org/redirect-url");

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
    $( "#create" ).onclick( function() { $(this).post("https://api.figo.me/auth/user", $( "#new_user" ).serializeToString(),
        function(data, result) {
            if (result.equals("error")) {
                alert("User with that email already exists");
            } else if (result.equals("success")) {
                window.location.href = "userHome.html";
            }})});

    $("#unlock").onclick( function() { $(this).post("https://api.figo.me/auth/unlock", "{\"username\": \""+$( "#logform").elements["email"]+"\"}",
        function(data, result) {
            if (result.equals("error")) {
                alert("No user associated with given email");
            } else if (result.equals("success")) {
                window.location.href = "#";
                alert("Reset Password email sent");
            }})});

    $("#login").onclick( function() { $(this).post("https://api.figo.me/rest/user", $( "#logform" ).serializeToString(),
        function(data, result) {
            if (result.equals("error")) {
                alert("Incorrect email or password");
            } else if (result.equals("success")) {
                alert("It worked");
                window.location.href = "userHome.html";
            } else {
                alert("Failed");
            }})});
});