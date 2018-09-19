/* exported QRCode */
/*
 Cleanup by Klemek, July 2018
 Modified for multiple detection by Melanie MALFIONE, August 2013
 Ported to JavaScript by Lazar Laszlo 2011
 Copyright 2007 ZXing authors
 */
const QRCode = {
    imagedata: null,
    width: 0,
    height: 0,
    qrCodeSymbol: null,
    debug: false,
    grayscaled: [],
    sizeOfDataLengthInfo: [[10, 9, 8, 8], [12, 11, 16, 10], [14, 13, 16, 12]],
    callback: null,

    decodeCards: function (canvas) {

        /**
         * The HTMLCanvasElement.getContext() method returns a drawing context on the canvas, here in 2D
         */
        const context = canvas.getContext('2d');
        this.width = canvas.width;
        this.height = canvas.height;

        /**
         * The CanvasRenderingContext2D.getImageData() method of the Canvas 2D API returns
         * an ImageData object representing the underlying pixel data for the area of the
         * canvas denoted by the rectangle
         */
        this.imagedata = context.getImageData(0, 0, this.width, this.height);

        this.results = this.processCards(this, context);

        if (this.callback !== null) {
            this.callback(this.results);
        }

        return this.results;
    },

    getPixel: function (x, y) {
        if (this.width < x) {
            throw 'point error';
        }
        if (this.height < y) {
            throw 'point error';
        }
        const point = (x * 4) + (y * this.width * 4);
        return (this.imagedata.data[point] * 0.299 + this.imagedata.data[point + 1] * 0.587 + this.imagedata.data[point + 2] * 0.114);
    },

    getMiddleBrightnessPerArea: function (image) {
        const numSqrtArea = 8;
        //obtain middle brightness((min + max) / 2) per area
        const areaWidth = Math.floor(this.width / numSqrtArea);
        const areaHeight = Math.floor(this.height / numSqrtArea);
        const minmax = new Array(numSqrtArea);

        for (let i = 0; i < numSqrtArea; i++) {
            minmax[i] = new Array(numSqrtArea);

            for (let i2 = 0; i2 < numSqrtArea; i2++) {
                minmax[i][i2] = [0, 0];
            }
        }

        for (let ay = 0; ay < numSqrtArea; ay++) {
            for (let ax = 0; ax < numSqrtArea; ax++) {
                minmax[ax][ay][0] = 0xFF;

                for (let dy = 0; dy < areaHeight; dy++) {

                    for (let dx = 0; dx < areaWidth; dx++) {
                        const target = image[areaWidth * ax + dx + (areaHeight * ay + dy) * this.width];
                        if (target < minmax[ax][ay][0]) {
                            minmax[ax][ay][0] = target;
                        }
                        if (target > minmax[ax][ay][1]) {
                            minmax[ax][ay][1] = target;
                        }
                    }
                }
                //minmax[ax][ay][0] = (minmax[ax][ay][0] + minmax[ax][ay][1]) / 2;
            }
        }

        const middle = new Array(numSqrtArea);

        for (let i3 = 0; i3 < numSqrtArea; i3++) {
            middle[i3] = new Array(numSqrtArea);
        }

        for (let ay = 0; ay < numSqrtArea; ay++) {
            for (let ax = 0; ax < numSqrtArea; ax++) {
                middle[ax][ay] = Math.floor((minmax[ax][ay][0] + minmax[ax][ay][1]) / 2);
            }
        }

        return middle;
    },

    /**
     * Put the grayscaled image in bitmap
     * @param grayScale
     * @returns {Array}
     */
    grayScaleToBitmap: function (grayScale) {
        const middle = this.getMiddleBrightnessPerArea(grayScale);
        const sqrtNumArea = middle.length;
        const areaWidth = Math.floor(this.width / sqrtNumArea);
        const areaHeight = Math.floor(this.height / sqrtNumArea);
        const bitmap = new Array(this.height * this.width);

        for (let ay = 0; ay < sqrtNumArea; ay++) {

            for (let ax = 0; ax < sqrtNumArea; ax++) {

                for (let dy = 0; dy < areaHeight; dy++) {

                    for (let dx = 0; dx < areaWidth; dx++) {

                        bitmap[areaWidth * ax + dx + (areaHeight * ay + dy) * this.width] = (grayScale[areaWidth * ax + dx + (areaHeight * ay + dy) * this.width] < middle[ax][ay]);
                    }
                }
            }
        }
        return bitmap;
    },

    /**
     * Fout le bordel en échelle de gris
     * @returns {Array}
     */
    grayscale: function () {
        const ret = new Array(this.width * this.height);
        for (let y = 0; y < this.height; y++) {

            for (let x = 0; x < this.width; x++) {

                ret[x + y * this.width] = this.getPixel(x, y);
            }
        }
        return ret;
    },

    processCards: function (qrcode, ctx) {

        let start;
        if (qrcode.debug) {
            start = new Date().getTime();
        }

        /**
         * Passe l'image en N&B
         */
        const grayscaled = qrcode.grayscale();
        const image = qrcode.grayScaleToBitmap(grayscaled);

        const detector = new Detector(image);
        const cardsMatrix = detector.detectMultiCards(this); // this is now an array

        if (qrcode.debug) {
            ctx.putImageData(qrcode.imagedata, 0, 0);
        }

        const cards = Decoder.decodeMultipleCards(cardsMatrix); // an array
        if (qrcode.debug) {
            const end = new Date().getTime();
            const time = end - start;
            console.log(time);
        }
        return cards;
    }
};
