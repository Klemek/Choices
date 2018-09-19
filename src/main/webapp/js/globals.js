/* exported ajax, mapping, globals, lang, utils */
const debug = false;

const globals = {
    //application configuration
    minQuestions: 10, //minimal question number for a question pack
    setSizeMin: 6, //minimal mastery target
    setSizeDefault: 10, //default mastery target
    setSizeStep: 2, //mastery target step
    questionPenalty: 3, //question penalty when not right
    cameraValidation: 4, //number of time a QR code must be detected to be accounted
    lastAnswerTimer: 5000, //milliseconds, length of timer when everyone answered
    messageTimer: 5000, //milliseconds, length of timer when automatic message is shown
    particlesTime: 5000, //milliseconds, particles screen time
    //developement configuration
    appPath: '',
    cameraScripts: ['js/camera/QRCode.js', 'js/camera/detector.js', 'js/camera/cards.js', 'js/camera/decoder.js', 'js/camera/findpat.js', 'js/camera/camcanvas2.js'],
    defaultScripts: ['js/ui.js', 'js/room.js', 'js/padRoom.js', 'js/camera.js'],
    mobileScripts: ['js/mobile-ui.js', 'js/padRoom.js'],
    mathjaxCDN: 'https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.4/latest.js?config=TeX-AMS_CHTML-full',
    roomScripts: ['https://cdnjs.cloudflare.com/ajax/libs/flot/0.8.3/jquery.flot.min.js'],
    flotOptions: {
        yaxis: {min: 0, max: 1, ticks: [[0, '0%'], [0.25, '25%'], [0.5, '50%'], [0.75, '75%'], [1, '100%']]},
        xaxis: {tickDecimals: 0},
        legend: {show: false},
        series: {shadowSize: 0, lines: {lineWidth: 3,}, curvedLines: {active: true}},
        colors: ['#28a745', '#dc3545'],
    },
    xlsxCDN: 'https://unpkg.com/xlsx/dist/xlsx.full.min.js',
    xlsxFileFilter: '.csv, text/csv, .xls,.xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel'
};

const mapping = {
    /**
     * All available effects with name, text/emoji & speed
     * @type Object<string,Object[]>
     */
    nameToEffect: {
        'confetti': [null, 1],
        'heart': ['❤️', 1],
        'stars': ['⭐', 0.3],
        'muscle': ['💪', 0.5],
        'like': ['👍', 0.5],
        'rain': ['💧', 1],
        'snow': ['❄️', 0.25],
        'victory': ['✌️', 0.5],
        'correct': ['👌', 0.5],
        'keep1': ['Keep calm...', 0.5],
        'keep2': ['Keep at it...', 0.5],
        'openmouth': ['😮', 0.5],
        'disappointed': ['😞', 0.5],
    },
    /**
     * @type Object<int,string>
     */
    answerToColor: {
        0: 'secondary',
        1: 'success',
        2: 'danger',
        3: 'info',
        4: 'warning'
    },
    /**
     * @type Object<string,int>
     */
    letterToAnswer: {
        A: 1,
        B: 2,
        C: 3,
        D: 4
    },
    /**
     * @param {string} letter
     * @returns {string}
     */
    letterToColor: function (letter) {
        return this.answerToColor[this.letterToAnswer[letter]];
    },
    /**
     * @type Object<int,string>
     */
    answerToLetter: {
        1: 'A',
        2: 'B',
        3: 'C',
        4: 'D'
    },
    /**
     * @Type Object<string,string[]>
     */
    categoryToLangPrefix: {
        'General': [],
        'State messages': ['state'],
        'Secondary Messages': ['text'],
        'UI components': ['btn', 'select', 'cb', 'label', 'input'],
        'Alerts': ['ask', 'warn', 'error', 'info'],
    },
    /**
     * @param {string} langKey
     * @return {string}
     */
    langToCategory: function (langKey) {
        let output = 'General';
        Object.keys(this.categoryToLangPrefix).forEach(function (key) {
            mapping.categoryToLangPrefix[key].forEach(function (prefix) {
                if (langKey.startsWith(prefix))
                    output = key;
            });
        });
        return output;
    }
};

/*
 Prototypes & Utils
 */

const utils = {
    /**
     * Return the root path of the app (for example '/test')
     * @returns {string} the computed rootpath
     */
    getRootPath: function () {
        let rootPath = window.location.pathname;
        if (rootPath.endsWith('/'))
            rootPath = rootPath.substr(0, rootPath.length - 1);
        return rootPath;
    },
    /**
     * Change the page title and url
     * @param {string} name - the page name
     * @param {string} [query] - the query to add after path uri
     */
    setPage: function (name, query) {
        document.title = name;
        window.history.pushState({}, name, window.location.protocol + '//' + window.location.host + '/' + globals.appPath + (query ? ('?' + query) : ''));
    },
    /**
     * Load MathJax script then register utils.updateMath when possible
     * @param {function} [callback] - called when finished
     */
    loadMathJax: function (callback) {
        if (typeof MathJax === 'undefined')
            $.getScript(globals.mathjaxCDN).done(function () {
                utils.updateMath = function () {
                    MathJax.Hub.Queue(['Typeset', MathJax.Hub]);
                };
                console.log('MathJax is ready');
                if (callback)
                    setTimeout(callback, 1000);
            });
    },
    /**
     * Update all equations available in the screen
     */
    updateMath: function () {
    },
    /**
     * Process any youtube link into a correct embed link
     * @param {string} baseLink - the original link
     * @param {boolean} [autoplay] - if the link allows autoplaying
     * @return {string|undefined} the correct embed link or undefined if not youtube link
     */
    getYoutubeEmbeddedLink: function (baseLink, autoplay) {
        try {
            let v, start;
            const url = new URL(baseLink);
            if (url.host === 'youtu.be') {
                v = url.pathname.substr(1);
                start = /t=([^&]+)/.exec(url.search);
            } else if (url.host === 'www.youtube.com') {
                if (url.pathname.includes('/embed/')) {
                    v = url.pathname.split('/embed/')[1];
                    start = /start=([^&]+)/.exec(url.search);
                } else {
                    v = /v=([^&]+)/.exec(url.search)[1];
                    start = /t=([^&]+)/.exec(url.search);
                }
            } else {
                return undefined;
            }
            if (start)
                start = start[1];
            return 'https://www.youtube.com/embed/{0}?rel=0&start={1}&t={1}&autoplay={2}'.format(v, start ? start : 0, autoplay ? 1 : 0);
        }
        catch (err) {
            return undefined;
        }
    },
    /**
     * Check if a text is an URL
     * @param {string} text
     * @return {*} true if it's an url
     */
    matchUrl: function (text) {
        const regex = new RegExp(/[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi);
        return text.match(regex);
    },
    /**
     * Get a list of key-value and order them by value descending
     * @param {Object} o - object
     * @returns {Array}
     */
    getOrderedDict: function (o) {
        let a = Object.keys(o).map(function (key) {
            return [key, o[key]];
        });
        a = a.sort(function (a, b) {
            return (a[1] < b[1]) ? 1 : ((b[1] < a[1]) ? -1 : 0);
        });
        return a;
    },
    /**
     * Shuffle an array
     * @param {Object[]} array
     * @returns {Array} the modified array
     */
    shuffle: function (array) {
        let i, j, x;
        for (let u = 0; u < array.length * 2; u++) {
            i = Math.floor(Math.random() * (array.length));
            do {
                j = Math.floor(Math.random() * (array.length));
            } while (i === j);
            x = array[i];
            array[i] = array[j];
            array[j] = x;
        }
        return array;
    },
    /**
     * Remove an index from an array
     * @param {Object[]} array
     * @param {int} index
     * @returns {Array} the modified array
     */
    removeIndex: function (array, index) {
        return array.slice(0, index).concat(array.slice(index + 1));
    },
    /**
     * Return a random item from an array
     * @param {Object[]} array
     * @returns {Object} a random object
     */
    getRandom: function (array) {
        if (array.length === 1)
            return array[0];
        return array[Math.floor(Math.random() * (array.length))];
    },
    /**
     * Check if an html code is valid
     * @param {string} html
     * @returns {boolean}
     */
    checkHTML: function (html) {
        const doc = document.createElement('div');
        doc.innerHTML = html;
        return ( doc.innerHTML === html );
    },
    /**
     * Return the sum of all hashes in a dict
     * @param {Object.<string,string>} dict
     * @return {int}
     */
    dictHash: function (dict) {
        let hash = 0;
        for (let key in dict) {
            if (dict.hasOwnProperty(key)) {
                hash += dict[key].hashCode();
            }
        }
        return hash;
    },
    /**
     * Split a CSV line into a array of Strings
     * @param line
     * @return {Array}
     */
    splitCSVLine: function (line) {
        let out = [];
        while (line.length > 0) {
            const semicolon = line.indexOf(';');
            const quote = line.indexOf('"');
            if (semicolon < 0) {
                out.push(line);
                line = '';
            } else if (semicolon === 0) {
                out.push('');
                line = line.substr(1);
            } else if (quote === -1 || semicolon < quote) {
                out.push(line.substr(0, semicolon));
                line = line.substr(semicolon + 1);
            } else {
                let endQuote = line.indexOf('";') - 1;
                if (endQuote < 0)
                    endQuote = line.length - 1;
                out.push(line.substr(quote + 1, endQuote));
                line = line.substr(endQuote + 2);
            }
        }
        return out;
    },
    /**
     * Return a random integer between min and max
     * @param {int} min - minimum value (included)
     * @param {int} max - maximum value (excluded)
     * @returns {int} the random integer
     */
    randInt: function (min, max) {
        return Math.floor(Math.random() * (max - min)) + min;
    }
};

/** @namespace navigator.vendor */
/**
 * jQuery.browser.mobile (http://detectmobilebrowser.com/)
 *
 * jQuery.browser.mobile will be true if the browser is a mobile device
 *
 **/
(function (a) {
    (jQuery.browser = jQuery.browser || {}).mobile = /(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4));
})(navigator.userAgent || navigator.vendor || window.opera);

/**
 * Generate an hashcode unique for this String
 * (https://stackoverflow.com/a/8831937)
 * @returns {number} the hashcode of this String
 */
String.prototype.hashCode = function () {
    let hash = 0;
    if (this.length === 0) {
        return hash;
    }
    for (let i = 0; i < this.length; i++) {
        const char = this.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
};

/**
 * Replace all occurences in the string
 * @param search
 * @param replacement
 * @returns {string}
 */
String.prototype.replaceAll = function (search, replacement) {
    const target = this;
    return target.split(search).join(replacement);
};

/**
 * Simple string format which replace {n} by argument n
 * @returns {String} the formated result
 */
String.prototype.format = function () {
    let tmpstr = this;
    for (let i = 0; i < arguments.length; i++) {
        tmpstr = tmpstr.replaceAll('{' + i + '}', arguments[i]);
    }

    return tmpstr;
};

/**
 * https://stackoverflow.com/questions/11803215/how-to-include-multiple-js-files-using-jquery-getscript-method
 * @param {string[]} arr
 * @param {string} [path]
 * @returns {*}
 */
$.getMultiScripts = function (arr, path) {
    const _arr = $.map(arr, function (scr) {
        if (debug)
            console.log('loading script ' + (path || '') + scr);
        return $.getScript((path || '') + scr);
    });

    _arr.push($.Deferred(function (deferred) {
        $(deferred.resolve);
    }));

    return $.when.apply($, _arr);
};

/*
    Global constants and functions
 */

/**
 * ajax global to handle all ajax calls
 * @author mortrevere, 2018
 */
const ajax = {
    /**
     *
     * @param {string} method
     * @param {string} URI
     * @param {Object | function} [data] - request data or callback
     * @param {function} [callback] - callback or onerror if data is callback
     * @param {function} [onerror]
     */
    call: function (method, URI, data, callback, onerror) {
        const o = {
            method: method,
            url: globals.appPath + '/api' + URI
        };

        if (typeof data === 'function') {
            onerror = callback;
            callback = data;
        } else if (typeof data === 'object') {
            o.data = data;
        }

        function cbwrap(data) {
            if (debug)
                console.log('Received response from ' + o.method + ' request to ' + o.url + ' : ', data, 'sent data :', o.data);

            data = data.value ? data.value : data;
            if (callback)
                callback(data);
        }

        function failwrap(data) {
            if (data.status === 401)
                window.location.href = globals.appPath + '/login?redirect=' + encodeURI(window.location.href);

            console.error('Error in ' + o.method + ' request to ' + o.url + ' (status : ' + data.status + ') : ', data);

            if (onerror)
                onerror(data);
        }

        $.ajax(o).done(cbwrap).fail(failwrap);
    }
};

const lang = {
    strings: {},
    /**
     * Init the lang for the app
     * @param {{langHash:int}} data
     */
    init: function (data) {
        let tmpLang = {};
        try {
            tmpLang = JSON.parse(cookies.get('lang'));
        } catch (e) {
            console.error('Cannot parse JSON from cookie');
        }
        const localHash = utils.dictHash(tmpLang);
        if (localHash === data.langHash) {
            lang.strings = tmpLang;
            ui.setLangReady();
        } else {
            console.log('local:', localHash, 'online:', data.langHash);
            console.log('hashCode mismatch, reloading lang strings');
            ajax.call('GET', '/lang',
                /**
                 * @param {{lang:Object, hash:int}}  data
                 */
                function (data) {
                    lang.setStrings(data);
                    ui.setLangReady();
                });
        }
    },
    /**
     * Get a lang string by its key
     * @param {string} key
     * @returns {string}
     */
    getString: function (key) {
        if (lang.strings[key])
            return utils.getRandom(lang.strings[key].trim().split('\|'));
        return key;
    },
    /**
     * Load data from request
     * @param {{lang:Object, hash:int}} data
     */
    setStrings: function (data) {
        lang.strings = data.lang;
        const newHash = utils.dictHash(lang.strings);
        if (newHash !== data.hash) {
            console.error('hash mismatch');
            console.error('new hash:', newHash, 'indicated:', data.hash);
        }
        cookies.set('lang', JSON.stringify(lang.strings));
    },
    /**
     * Update lang data
     * @param {Object} newLang
     * @param {function} callback
     */
    update: function (newLang, callback) {
        const data = {
            lang: JSON.stringify(newLang)
        };
        ajax.call('POST', '/lang', data,
            /**
             * @param {{lang:Object, hash:int}}  value
             */
            function (value) {
                lang.setStrings(value);
                ui.alert('success', 'Texts updated');
                if (callback)
                    callback();
            }, function () {
                ui.alert('danger', 'Cannot update texts, please retry');
                if (callback)
                    callback();
            });
    },
    /**
     * Join strings in a readable way
     * @param {*[]} array
     * @return {string}
     */
    join: function (array) {
        switch (array.length) {
            case 0:
                return '';
            case 1:
                return array[0].toString();
            case 2:
                return array.join(' and ');
            default:
                return array.slice(0, -1).join(', ') + ' and ' + array[array.length - 1];
        }
    }
};

//# sourceURL=js/globals.js