/* exported FinderPattern, FinderPatternFinder */
/*
 Cleanup by Klemek, July 2018
 Modified for multiple detection by Melanie MALFIONE, August 2013
 Ported to JavaScript by Lazar Laszlo 2011
 Copyright 2007 ZXing authors
 */
const MIN_SKIP = 3;
const INTEGER_MATH_SHIFT = 8;

function FinderPattern(posX, posY, estimatedModuleSize) {
    this.x = posX;
    this.y = posY;
    this.count = 1;
    this.estimatedModuleSize = estimatedModuleSize;

    this.__defineGetter__('EstimatedModuleSize', function () {
        return this.estimatedModuleSize;
    });
    this.__defineGetter__('Count', function () {
        return this.count;
    });
    this.__defineGetter__('X', function () {
        return this.x;
    });
    this.__defineGetter__('Y', function () {
        return this.y;
    });
    this.incrementCount = function () {
        this.count++;
    };
    this.aboutEquals = function (moduleSize, i, j) {
        if (Math.abs(i - this.y) <= moduleSize && Math.abs(j - this.x) <= moduleSize) {
            let moduleSizeDiff = Math.abs(moduleSize - this.estimatedModuleSize);
            return moduleSizeDiff <= 1.0 || moduleSizeDiff / this.estimatedModuleSize <= 1.0;
        }
        return false;
    };

}

const FinderPatternFinder = {
    image: null,
    possibleCenters: [],
    hasSkipped: false,
    crossCheckStateCount: [0, 0, 0, 0, 0],
    verticalCheckStateCount: [0, 0, 0, 0, 0],
    resultPointCallback: null,

    foundPatternCross: function (stateCount) {
        let totalModuleSize = 0;
        for (let i = 0; i < 5; i++) {
            const count = stateCount[i];
            if (count === 0) {
                return false;
            }
            totalModuleSize += count;
        }
        if (totalModuleSize < 7) {
            return false;
        }
        const moduleSize = Math.floor((totalModuleSize << INTEGER_MATH_SHIFT) / 7);
        const maxconstiance = Math.floor(moduleSize / 2);
        // Allow less than 50% constiance from 1-1-3-1-1 proportions
        return Math.abs(moduleSize - (stateCount[0] << INTEGER_MATH_SHIFT)) < maxconstiance && Math.abs(moduleSize - (stateCount[1] << INTEGER_MATH_SHIFT)) < maxconstiance && Math.abs(3 * moduleSize - (stateCount[2] << INTEGER_MATH_SHIFT)) < 3 * maxconstiance && Math.abs(moduleSize - (stateCount[3] << INTEGER_MATH_SHIFT)) < maxconstiance && Math.abs(moduleSize - (stateCount[4] << INTEGER_MATH_SHIFT)) < maxconstiance;
    },

    foundCardPatternCross: function (stateCount) {
        let totalModuleSize = 0;
        for (let i = 0; i < 3; i++) {
            const count = stateCount[i];
            if (count === 0) {
                return false;
            }
            totalModuleSize += count;
        }
        if (totalModuleSize < 7) {
            return false;
        }
        const moduleSize = Math.floor((totalModuleSize << INTEGER_MATH_SHIFT) / 3);
        const maxconstiance = Math.floor(moduleSize);
        const meanconstiance = Math.floor(moduleSize / 2);
        // Allow less than 50% constiance from 1-1-1 proportions
        return Math.abs(moduleSize - (stateCount[0] << INTEGER_MATH_SHIFT)) < maxconstiance &&
            Math.abs(moduleSize - (stateCount[1] << INTEGER_MATH_SHIFT)) < meanconstiance &&
            Math.abs(moduleSize - (stateCount[2] << INTEGER_MATH_SHIFT)) < maxconstiance;


        //return Math.abs(moduleSize - (stateCount[0] << INTEGER_MATH_SHIFT)) < maxconstiance && Math.abs(moduleSize - (stateCount[1] << INTEGER_MATH_SHIFT)) < maxconstiance && Math.abs(3 * moduleSize - (stateCount[2] << INTEGER_MATH_SHIFT)) < 3 * maxconstiance && Math.abs(moduleSize - (stateCount[3] << INTEGER_MATH_SHIFT)) < maxconstiance && Math.abs(moduleSize - (stateCount[4] << INTEGER_MATH_SHIFT)) < maxconstiance;
    },

    centerFromCardEnd: function (stateCount, end) {
        return (end - stateCount[2] - stateCount[1] / 2.0);
    },

    crossCheckCardVertical: function (qrcode, startI, centerJ, maxCount, originalStateCountTotal) {
        const image = this.image;

        const maxI = qrcode.height;
        const stateCount = this.CrossCheckStateCount;

        // We start counting up from center
        let i = startI;

        //Counting the white pixels up
        while (i >= 0 && !image[centerJ + i * qrcode.width]) {
            stateCount[1]++;
            i--;
        }
        if (i < 0) {
            return NaN;
        }

        //Counting black pixels up
        while (i >= 0 && image[centerJ + i * qrcode.width] && stateCount[0] <= maxCount) {
            stateCount[0]++;
            i--;
        }
        if (stateCount[0] > maxCount) {
            return NaN;
        }

        // Now also count down from center
        i = startI + 1;

        //Counting white pixels down
        while (i < maxI && !image[centerJ + i * qrcode.width]) {
            stateCount[1]++;
            i++;
        }
        if (i === maxI) {
            return NaN;
        }

        //Counting black pixels down
        while (i < maxI && image[centerJ + i * qrcode.width] && stateCount[2] < maxCount) {
            stateCount[2]++;
            i++;
        }
        if (stateCount[2] >= maxCount) {
            return NaN;
        }


        // If we found a finder-pattern-like section, but its size is more than 40% different than
        // the original, assume it's a false positive
        const stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2];
        const centerToEdgeconstiance = 5 * (stateCount[0] - stateCount[1]);
        if (5 * Math.abs(stateCountTotal - originalStateCountTotal) >= 2 * originalStateCountTotal || (centerToEdgeconstiance < -2 * stateCount[1])) {
            return NaN;
        }


        return this.foundCardPatternCross(stateCount) ? this.centerFromCardEnd(stateCount, i) : NaN;
    },


    crossCheckCardHorizontal: function (qrcode, startJ, centerI, maxCount, originalStateCountTotal) {
        const image = this.image;

        const maxJ = qrcode.width;
        const stateCount = this.CrossCheckStateCount;

        //We start counting left from the center
        let j = startJ;

        //Counting the white pixels on the left
        while (j >= 0 && !image[j + centerI * qrcode.width]) {
            stateCount[1]++;
            j--;
        }
        if (j < 0) {
            return NaN;
        }

        //Counting the black pixels on the left
        while (j >= 0 && image[j + centerI * qrcode.width] && stateCount[0] <= maxCount) {
            stateCount[0]++;
            j--;
        }
        if (stateCount[0] > maxCount) {
            return NaN;
        }

        //Now also count to the right
        j = startJ + 1;

        //Counting white pixels to the right
        while (j < maxJ && !image[j + centerI * qrcode.width]) {
            stateCount[1]++;
            j++;
        }
        if (j === maxJ) {
            return NaN;
        }

        //Counting black pixels to the right
        while (j < maxJ && image[j + centerI * qrcode.width] && stateCount[2] < maxCount) {
            stateCount[2]++;
            j++;
        }
        if (stateCount[2] >= maxCount) {
            return NaN;
        }

        // If we found a finder-pattern-like section, but its size is significantly different than
        // the original, assume it's a false positive
        const stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2];
        const centerToEdgeconstiance = 5 * (stateCount[0] - stateCount[1]);
        const constiance = 5 * Math.abs(stateCountTotal - originalStateCountTotal);
        if (constiance >= originalStateCountTotal || (centerToEdgeconstiance < -2 * stateCount[1])) {
            return NaN;
        }

        return this.foundCardPatternCross(stateCount) ? this.centerFromCardEnd(stateCount, j) : NaN;
    },

    crossCheckCardDiagonal: function (qrcode, startI, centerJ, maxCount, originalStateCountTotal) {
        const image = this.image;

        const stateCount = this.CrossCheckStateCount;

        // Start counting up, left from center finding white center mass
        let i = 0;
        while (startI >= i && centerJ >= i && !image[centerJ - i + (startI - i) * qrcode.width]) {
            stateCount[1]++;
            i++;
        }

        if (startI < i || centerJ < i) {
            return false;
        }

        // Continue up, left finding black space
        while (startI >= i && centerJ >= i && image[centerJ - i + (startI - i) * qrcode.width] &&
        stateCount[0] <= maxCount) {
            stateCount[0]++;
            i++;
        }

        // If already too many modules in this state or ran off the edge:
        if (startI < i || centerJ < i || stateCount[0] > maxCount) {
            return false;
        }

        const maxI = qrcode.height;
        const maxJ = qrcode.width;

        // Now also count down, right from center
        i = 1;
        while (startI + i < maxI && centerJ + i < maxJ && !image[centerJ + i + (startI + i) * qrcode.width]) {
            stateCount[1]++;
            i++;
        }

        // Ran off the edge ?
        if (startI + i >= maxI || centerJ + i >= maxJ) {
            return false;
        }

        while (startI + i < maxI && centerJ + i < maxJ && image[centerJ + i + (startI + i) * qrcode.width] && stateCount[2] < maxCount) {
            stateCount[2]++;
            i++;
        }

        if (startI + i >= maxI || centerJ + i >= maxJ || stateCount[2] >= maxCount) {
            return false;
        }

        // If we found a finder-pattern-like section, but its size is more than 100% different than
        // the original, assume it's a false positive
        const stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2];
        const centerToEdgeconstiance = 5 * (stateCount[0] - stateCount[1]);
        return Math.abs(stateCountTotal - originalStateCountTotal) < 2 * originalStateCountTotal && centerToEdgeconstiance >= -2 * stateCount[1] && this.foundCardPatternCross(stateCount);
    },


    calcEstimatedCardModuleSize: function (stateCount) {
        /* Not the best working case, too much error. Instead we use the size of the white square,
        which is more accurate despite not being divided to reduce error. */

        return stateCount[1] * 0.92;
    },

    handlePossibleCardCenter: function (qrcode, stateCount, i, j) {
        const stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2];
        let centerJ = this.centerFromCardEnd(stateCount, j); //float
        let centerI = this.crossCheckCardVertical(qrcode, i, Math.floor(centerJ), stateCount[1] * 2, stateCountTotal); //float
        if (!isNaN(centerI)) {
            // Re-cross check
            centerJ = this.crossCheckCardHorizontal(qrcode, Math.floor(centerJ), Math.floor(centerI), stateCount[1] * 2, stateCountTotal);
            if (!isNaN(centerJ) && this.crossCheckCardDiagonal(qrcode, Math.floor(centerI), Math.floor(centerJ), stateCount[1] * 2, stateCountTotal)) {

                const estimatedModuleSize = this.calcEstimatedCardModuleSize(stateCount, stateCountTotal);

                let found = false;
                const max = this.possibleCenters.length;
                for (let index = 0; index < max; index++) {
                    const center = this.possibleCenters[index];
                    // Look for about the same center and module size:
                    if (center.aboutEquals(estimatedModuleSize, centerI, centerJ)) {
                        center.incrementCount();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    const point = new FinderPattern(centerJ, centerI, estimatedModuleSize);
                    this.possibleCenters.push(point);
                    /*if (this.resultPointCallback != null) {
                        this.resultPointCallback.foundPossibleResultPoint(point);
                    }*/
                }
                return true;
            }
        }
        return false;
    },

    selectMultipleBestCardPatterns: function (qrcode, image) {
        const possibleCenters = this.possibleCenters;

        const results = [];

        possibleCenters.forEach(function (center) {
            const centerMatrix = [];
            for (let x = 0; x < 5; x++) {
                centerMatrix[x] = [];
                for (let y = 0; y < 5; y++) {
                    centerMatrix[x][y] = null;
                }
            }

            const moduleSize = Math.floor(center.estimatedModuleSize), centerX = Math.floor(center.x),
                centerY = Math.floor(center.y);
            for (let line = 0; line < 5; line++) {
                for (let column = 0; column < 5; column++) {
                    const pos = (centerX + (column - 2) * moduleSize + (centerY + (line - 2) * moduleSize) * qrcode.width);
                    centerMatrix[line][column] = image[pos];
                }
            }

            if (centerMatrix[1][1] && centerMatrix[1][2] && centerMatrix[1][3] &&
                centerMatrix[2][1] && !centerMatrix[2][2] && centerMatrix[2][3] &&
                centerMatrix[3][1] && centerMatrix[3][2] && centerMatrix[3][3]) {
                results.push(centerMatrix);
            }
        });
        return results;
    },

    //New function for multiple detection
    findMultiFinderCardPattern: function (qrcode, image) {
        const tryHarder = false;
        this.image = image;

        /**
         * Hauteur et largeur de l'image de la caméra ?
         * @type {number|*}
         */
        const maxI = qrcode.height;
        const maxJ = qrcode.width;

        let iSkip = Math.floor(maxI / (5 * 20.0));

        if (iSkip < MIN_SKIP || tryHarder) {
            iSkip = MIN_SKIP;
        }

        const stateCount = new Array(3);
        for (let i = iSkip - 1; i < maxI; i += iSkip) {
            // Get a row of black/white values
            stateCount[0] = 0;
            stateCount[1] = 0;
            stateCount[2] = 0;
            let currentState = 0;
            for (let j = 0; j < maxJ; j++) {
                if (image[j + i * qrcode.width]) {
                    // Black pixel
                    if ((currentState & 1) === 1) {
                        // Counting white pixels
                        currentState++;
                    }
                    stateCount[currentState]++;
                } else {
                    // White pixel
                    if ((currentState & 1) === 0) {
                        // Counting black pixels
                        if (currentState === 2) {

                            if (this.foundCardPatternCross(stateCount, i, j) && this.handlePossibleCardCenter(qrcode, stateCount, i, j)) {
                                // Yes, but shift counts back by two, just in case
                                stateCount[0] = stateCount[2];
                                stateCount[1] = 1;
                                stateCount[2] = 0;
                                currentState = 1;
                            } else {
                                // No, shift counts back by two
                                stateCount[0] = stateCount[2];
                                stateCount[1] = 1;
                                stateCount[2] = 0;
                                currentState = 1;
                            }
                        } else {
                            stateCount[++currentState]++;
                        }
                    } else {
                        // Counting white pixels
                        stateCount[currentState]++;
                    }
                }
            }
            if (this.foundPatternCross(stateCount)) {
                this.handlePossibleCardCenter(qrcode, stateCount, i, maxJ);
            }
        }
        return this.selectMultipleBestCardPatterns(qrcode, image);
    }
};

FinderPatternFinder.__defineGetter__('CrossCheckStateCount', function () {
    this.crossCheckStateCount[0] = 0;
    this.crossCheckStateCount[1] = 0;
    this.crossCheckStateCount[2] = 0;
    this.crossCheckStateCount[3] = 0;
    this.crossCheckStateCount[4] = 0;
    return this.crossCheckStateCount;
});

FinderPatternFinder.__defineGetter__('VerticalCheckStateCount', function () {
    this.verticalCheckStateCount[0] = 0;
    this.verticalCheckStateCount[1] = 0;
    this.verticalCheckStateCount[2] = 0;
    this.verticalCheckStateCount[3] = 0;
    this.verticalCheckStateCount[4] = 0;
    return this.verticalCheckStateCount;
});