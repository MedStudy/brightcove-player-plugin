cordova.define("cordova-plugin-bcplayer.player", function(require, exports, module) {
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

    BrightcovePlayerPlugin.enable = function() {
        exec(
            successHandler,
            errorHandler,
            "BCPlayerPlugin",
            "enable",
            []
        );
    };

    BrightcovePlayerPlugin.disable = function() {
        exec(
            successHandler,
            errorHandler,
            "BCPlayerPlugin",
            "disable",
            []
        );
    };

    BrightcovePlayerPlugin.hide = function() {
    exec(
        successHandler,
        errorHandler,
        "BCPlayerPlugin",
        "hide",
        []
        );
    };

    BrightcovePlayerPlugin.show = function() {
    exec(
        successHandler,
        errorHandler,
        "BCPlayerPlugin",
        "show",
        []
        );
    };

    BrightcovePlayerPlugin.rate = function(rate) {
        exec(
            successHandler,
            errorHandler,
            "BCPlayerPlugin",
            "rate",
            [rate ? rate.toString() : null]
        );
    };

    BrightcovePlayerPlugin.seek = function(position) {
    exec(
        successHandler,
        errorHandler,
        "BCPlayerPlugin",
        "seek",
        [position ? position.toString() : null]
        );
    };

    BrightcovePlayerPlugin.pause = function() {
    exec(
        successHandler,
        errorHandler,
        "BCPlayerPlugin",
        "pause",
        []
        );
    };

    BrightcovePlayerPlugin.play = function() {
    exec(
        successHandler,
        errorHandler,
        "BCPlayerPlugin",
        "play",
        []
        );
    };

    BrightcovePlayerPlugin.reposition = function(x, y, width, height) {
    exec(
        successHandler,
        errorHandler,
        "BCPlayerPlugin",
        "reposition",
        [x ? x.toString() : null, y ? y.toString() : null, width ? width.toString() : null, height ? height.toString() : null]
        );
    };

    function successHandler(success) {
      //console.log("[BrightcovePlayerPlugin] OK: " + success);
    }

    function errorHandler(error) {
      //console.error("[BrightcovePlayerPlugin] Error: " + error);
    }

module.exports = BrightcovePlayerPlugin;

});
