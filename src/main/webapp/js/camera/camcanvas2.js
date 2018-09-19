/* exported Cam2Canvas */
/*
 Cleanup by Klemek, July 2018
 Modified for multiple detection by Melanie MALFIONE, August 2013
 Ported to JavaScript by Lazar Laszlo 2011
 Copyright 2007 ZXing authors
 */
function Cam2Canvas(video, canvas) {
    const cam2Canvas = this;
    cam2Canvas.requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame;
    window.requestAnimationFrame = cam2Canvas.requestAnimationFrame;

    cam2Canvas.video = video;
    cam2Canvas.canvas = canvas;
    cam2Canvas.gCtx = null;

    const fps = 3;
    const interval = 1000 / fps;
    let now, delta;
    let then = Date.now();

    function draw() {
        window.requestAnimationFrame(draw);

        now = Date.now();
        delta = now - then;

        if (delta > interval) {
            // update time stuffs

            // Just `then = now` is not enough.
            // Lets say we set fps at 10 which means
            // each frame must take 100ms
            // Now frame executes in 16ms (60fps) so
            // the loop iterates 7 times (16*7 = 112ms) until
            // delta > interval === true
            // Eventually this lowers down the FPS as
            // 112*10 = 1120ms (NOT 1000ms).
            // So we have to get rid of that extra 12ms
            // by subtracting delta (112) % interval (100).
            // Hope that makes sense.

            then = now - (delta % interval);

            const w = 1280; //value to verify/ajust
            const h = cam2Canvas.video.videoHeight / (cam2Canvas.video.videoWidth / w);
            //const w = canvas.clientWidth;
            //const h = canvas.clientHeight;
            cam2Canvas.canvas.width = w;
            cam2Canvas.canvas.height = h;
            cam2Canvas.gCtx.drawImage(cam2Canvas.video, 0, 0, w, h);
        }
    }

    this.init = function (callback) {
        console.log('Initializing camera');
        cam2Canvas.video.height = window.innerHeight;
        //v.width=window.innerWidth;
        cam2Canvas.gCtx = cam2Canvas.canvas.getContext('2d');

        if (!navigator.mediaDevices) {
            console.log('getUserMedia() not supported.');
            return;
        }

        navigator.mediaDevices.getUserMedia(
            {
                audio: false,
                video: {
                    width: 1280,
                    height: 720,
                    frameRate: 15
                }
            }
        ).then(function (stream) {
            cam2Canvas.video.srcObject = stream;
            cam2Canvas.stream = stream;
            cam2Canvas.video.onloadedmetadata = function () {
                cam2Canvas.video.play();

                if (callback) {
                    callback();
                }
            };
        })
            .catch(function (err) {
                console.log(err.name + ': ' + err.message);
            });
        window.requestAnimationFrame(draw); //undefined for Opera
    };


}