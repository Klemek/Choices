/* exported cookies */

/**
 * Useful class to manage cookies without importing anything too heavy
 * Defines set, get and delete and is accessible everywhere via the 'cookies' global
 * @author mortrevere, 2018
 */
const cookies = {
        set: function (name, value, days, path) {
            if (days === undefined)
                days = 7;
            if (path === undefined)
                path = '/';

            const expires = new Date(Date.now() + days * 864e5).toUTCString();
            document.cookie = name + '=' + value + '; expires=' + expires + '; path=' + path;
        },
        get: function (name) {
            return document.cookie.split('; ').reduce(function (r, v) {
                const pos = v.indexOf('=');
                return v.substr(0, pos) === name ? v.substr(pos + 1) : r;
            }, '');
        },
        delete: function (name, path) {
            if (path === undefined)
                path = '/';
            this.set(name, '', -1, path);
        },
    clear: function () {
        const cookies = document.cookie.split(';');
        for (let i = 0; i < cookies.length; i++) {
            const cookie = cookies[i];
            const eqPos = cookie.indexOf('=');
            const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
            document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
        }
    }
    }
;

//# sourceURL=js/cookies.js