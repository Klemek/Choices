/* exported camera */
/**
 * All functions about the camera handling
 */
const camera = {
    cam2canvas: undefined,
    interval: undefined,
    canvas: undefined,
    lastCapture: {},
    stopCapture: false,
    /**
     * @type {Object.<number, number>}
     */
    members: {},
    lock: false,
    /**
     * Initialize the camera div
     * @param elem - the jQuery div element
     */
    init: function (elem) {
        const video = elem.find('video')[0];
        this.canvas = elem.find('canvas')[0];
        this.cam2canvas = new Cam2Canvas(video, this.canvas);
        this.cam2canvas.init(function () {
            setTimeout(camera.loop, 500);
        });

        this.stopCapture = false;

        elem.on('destroy', function () {
            camera.endCapture();
        });
    },
    /**
     * Main loop function
     */
    loop: function () {
        QRCode.decodeCards(camera.canvas).forEach(function (e) {
            const val = e.number + '-' + e.response;
            if (!camera.lastCapture[val]) {
                camera.lastCapture[val] = [true, 1];
            } else {
                camera.lastCapture[val] = [true, camera.lastCapture[val][1] + 1];
            }
        });
        Object.keys(camera.lastCapture).forEach(function (val) {
            if (!camera.lastCapture[val][0]) { //not seen this time
                delete camera.lastCapture[val];
            } else {
                camera.lastCapture[val][0] = false;
                if (!camera.lock && camera.lastCapture[val][1] >= globals.cameraValidation) {
                    const spl = val.split('-');
                    camera.members[spl[0]] = mapping.letterToAnswer[spl[1]];
                }
            }
        });
        if (!room.autoRefresh)
            room.refreshMembers();
        if (!this.stopCapture)
            setTimeout(camera.loop);
    },
    /**
     * Reset members answer to 0 (undefined)
     */
    resetAnswers: function () {
        Object.keys(camera.members).forEach(function (k) {
            camera.members[k] = 0;
        });
    },
    /**
     * Delete a member
     * @param {string} key - the member key
     */
    deleteMember: function (key) {
        delete camera.members[key];
    },
    /**
     * Lock or unlock the room to prevent answering
     * @param {boolean} lock
     */
    lockChange: function (lock) {
        this.lock = lock;
    },
    /**
     * Stop capture
     */
    endCapture: function () {
        this.stopCapture = true;
    }

};

//# sourceURL=js/camera.js