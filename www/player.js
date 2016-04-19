var exec = require('cordova/exec');

    var BrightcovePlayerPlugin = function() {
    };

    BrightcovePlayerPlugin.init = function(token) {
        exec(
            successHandler,
            errorHandler,
            "BCPlayerPlugin",
            "init",
            [token || null]
        );
    };

    BrightcovePlayerPlugin.load = function(id) {
      exec(
        successHandler,
        errorHandler,
        "BCPlayerPlugin",
        "load",
        [id ? id.toString() : null]
      );
    };


    function successHandler(success) {
      //console.log("[BrightcovePlayerPlugin] OK: " + success);
    }

    function errorHandler(error) {
      //console.error("[BrightcovePlayerPlugin] Error: " + error);
    }

module.exports = BrightcovePlayerPlugin;
