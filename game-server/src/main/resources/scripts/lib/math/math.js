(function (exports) {

    exports.clamp = function (value, min, max) {
        return Math.max(min, Math.min(max, value));
    };

    exports.randomInt = function (min, max) {
        min = Math.ceil(min);
        max = Math.floor(max);
        return Math.floor(Math.random() * (max - min + 1)) + min;
    };

    exports.percent = function (value, percent) {
        return Math.floor(value * (percent / 100));
    };

    exports.round2 = function (value) {
        return Math.round(value * 100) / 100;
    };

    exports.distance2D = function (x1, y1, x2, y2) {
        const dx = x2 - x1;
        const dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    };

})(globalThis.MathLib = globalThis.MathLib || {});