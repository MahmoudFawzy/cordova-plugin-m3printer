var exec = require('cordova/exec');

var printer = {
    platforms: ['android'],

    isSupported: function() {
        if (window.device) {
            var platform = window.device.platform;
            if ((platform !== undefined) && (platform !== null)) {
                return (this.platforms.indexOf(platform.toLowerCase()) >= 0);
            }
        }
        return false;
    },
    printText: function(text, onSuccess, onError) {
        exec(onSuccess, onError, 'M3Printer', 'printText', [text]);
    },
    printBase64: function(base64, onSuccess, onError) {
        alert(0)
        exec(onSuccess, onError, 'M3Printer', 'printBase64', [base64]);
    },
    printPath: function(base64, onSuccess, onError) {
        alert(0)
        exec(onSuccess, onError, 'M3Printer', 'printPath', [base64]);
    }
};
module.exports = printer;