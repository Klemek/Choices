/* exported Decoder */
/*
 Cleanup by Klemek, July 2018
 Modified for multiple detection by Melanie MALFIONE, August 2013
 Ported to JavaScript by Lazar Laszlo 2011
 Copyright 2007 ZXing authors
 */
const Decoder = {
    /**
     * Make each matrix we found go through a test in order to find which card it is
     * @param cardsToCompare
     */
    decodeMultipleCards: function (cardsToCompare) {
        const results = [];
        cardsToCompare.forEach(function (item) {
            const cardFound = Decoder.whichCard(item);
            if (cardFound !== null) {
                results.push(cardFound);
            }
        });
        return results;

    },

    /**
     * Determine which card the array passed as a parameter is representing
     * @param cardToCompare The "card" which we are decoding
     */
    whichCard: function (cardToCompare) {
        let response,
            found = false,
            index = 0;

        /**
         * For each card in the bank, we compare it to the card we've detected in the picture, and for each rotation
         */
        while (!found && index < cards.length) {
            const card = cards[index];
            if (Decoder.compareCard(card.matrix, cardToCompare)) {
                response = {number: index + 1, response: 'A'};
                found = true;
            } else if (Decoder.compareCard(Decoder.rotateCard(card.matrix, 90), cardToCompare)) {
                response = {number: index + 1, response: 'B'};
                found = true;
            } else if (Decoder.compareCard(Decoder.rotateCard(card.matrix, 180), cardToCompare)) {
                response = {number: index + 1, response: 'C'};
                found = true;
            } else if (Decoder.compareCard(Decoder.rotateCard(card.matrix, 270), cardToCompare)) {
                response = {number: index + 1, response: 'D'};
                found = true;
            }
            index++;
        }
        if (found) {
            return response;
        } else {
            return null;
        }
    },

    /**
     * Compare two array representing two cards, returning true if they are the same
     * @param card The card we took from the bank
     * @param cardToCompare The "card" we want to compare with the one from the bank
     * @return {boolean}
     */
    compareCard: function (card, cardToCompare) {
        return card[0][0] === cardToCompare[0][0] &&
            card[0][1] === cardToCompare[0][1] &&
            card[0][2] === cardToCompare[0][2] &&
            card[0][3] === cardToCompare[0][3] &&
            card[0][4] === cardToCompare[0][4] &&

            card[1][0] === cardToCompare[1][0] &&
            card[1][4] === cardToCompare[1][4] &&

            card[2][0] === cardToCompare[2][0] &&
            card[2][4] === cardToCompare[2][4] &&

            card[3][0] === cardToCompare[3][0] &&
            card[3][4] === cardToCompare[3][4] &&

            card[4][0] === cardToCompare[4][0] &&
            card[4][1] === cardToCompare[4][1] &&
            card[4][2] === cardToCompare[4][2] &&
            card[4][3] === cardToCompare[4][3] &&
            card[4][4] === cardToCompare[4][4];
    },

    /**
     * Rotate the matrix of the given angle, or of 90 degrees clockwise if angle is not set
     * @param card
     * @param angle
     */
    rotateCard: function (card, angle) {

        let workingOn = card;

        if (angle === null || angle === undefined) {
            angle = 90;
        }

        const authorizedAngles = [90, 180, 270];
        if (authorizedAngles.indexOf(angle) === -1) {
            throw new TypeError();
        }

        const rotatedCard = [];
        for (let x = 0; x < 5; x++) {
            rotatedCard[x] = [];
            for (let y = 0; y < 5; y++) {
                rotatedCard[x][y] = null;
            }
        }

        switch (angle) {
            case 180:
                workingOn = Decoder.rotateCard(workingOn, 90);
                break;
            case 270:
                workingOn = Decoder.rotateCard(workingOn, 180);
                break;
            default:
                break;
        }

        for (let line = 0; line < 5; line++) {
            for (let column = 0; column < 5; column++) {
                rotatedCard[line][column] = workingOn[column][4 - line];
            }
        }
        return rotatedCard;
    }
};