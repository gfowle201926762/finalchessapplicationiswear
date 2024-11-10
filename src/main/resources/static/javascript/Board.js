

export class Board {
    /*
    Requirements:
    keep track of clicks
    reverse orientation
    execute a move (origin, destination, promotion, castle, castleType)
    */

    constructor() {
        this.board = document.getElementById("board");
        this.currentMove = 0;
        this.moves = [];
        this.legalMoves = {};
        this.fenStrings = [];
        this.flipped = false;
        for (let x=1; x<9; x++){
            for (let y=1; y<9; y++){
                const board_element = document.createElement("div")
                const backing = document.createElement("div");
                board_element.style.gridColumnStart = x
                board_element.style.gridRowStart = y
                backing.style.gridColumnStart = x
                backing.style.gridRowStart = y
                backing.classList.add('backing');
                this.board.appendChild(backing);
                board.appendChild(board_element)
                if ((x % 2 == 0 && y % 2 == 0) || (x % 2 != 0 && y % 2 != 0)){
                    board_element.classList.add("lightsquare")
                }
                else{
                    board_element.classList.add("darksquare")
                }
                if (x == 1) {
                    // backing.innerHTML("a");
                }
            }
        }
    }

    doNothing() {

    }

    saveLegalMoves(messageData) {
        this.legalMoves = {};
        for (let i = 0; i < messageData['clientLegalMoves'].length; i++) {
            if (messageData['clientLegalMoves'][i] == null) {
                break;
            }
            if (messageData['clientLegalMoves'][i]['origin'] in this.legalMoves) {
                this.legalMoves[messageData['clientLegalMoves'][i]['origin']].push(messageData['clientLegalMoves'][i]);
            } 
            else {
                this.legalMoves[messageData['clientLegalMoves'][i]['origin']] = [messageData['clientLegalMoves'][i],];
            }
        }
    }

    getLegalMoveCoordinate(origin, i, attribute) {
        if (attribute != "origin" && attribute != "destination") {
            return null;
        }
        return this.flipMoveCoordinates(this.legalMoves[this.flipMoveCoordinates(origin)][i][attribute]);
    }

    getLegalMovesLength(origin) {
        var flipped = this.flipMoveCoordinates(origin);
        console.log(flipped)
        return this.legalMoves[flipped].length;
    }

    isCoordinateInLegalMoves(coordinate) {
        if ((this.flipMoveCoordinates(coordinate)) in this.legalMoves) {
            return true;
        }
        return false;
    }

    flipTakenPieces() {
        const top_board_wrapper = document.getElementById("top_board_wrapper");
        const top_player_wrapper = top_board_wrapper.firstChild;

        const bottom_board_wrapper = document.getElementById("bottom_board_wrapper");
        const bottom_player_wrapper = bottom_board_wrapper.firstChild;

        bottom_board_wrapper.insertBefore(top_player_wrapper, bottom_board_wrapper.firstChild);
        top_board_wrapper.insertBefore(bottom_player_wrapper, top_board_wrapper.firstChild);
    }

    flipBoard(flipInfo) {
        this.removeTracking();
        var savedElements = [];
        var toCoords = [];
        var fromCoords = [];
        var taken = false;
        for (let x=1; x<9; x++){
            for (let y=1; y<9; y++){
                const piece = this.board.querySelector(`.board > img[style*="grid-column-start: ${y}; grid-row-start: ${x};"]`);
                if (piece != null) {
                    this.board.removeChild(piece);
                    savedElements.push(piece);
                    piece.style.gridColumnStart = 9 - y;
                    piece.style.gridRowStart = 9 - x;
                }
                const fromBacking = this.board.querySelector(`.board > div.backing.original_square[style*="grid-column-start: ${y}; grid-row-start: ${x};"]`);
                const toFreeBacking = this.board.querySelector(`.board > div.backing.new_free_square[style*="grid-column-start: ${y}; grid-row-start: ${x};"]`);
                const toTakenBacking = this.board.querySelector(`.board > div.backing.new_taken_square[style*="grid-column-start: ${y}; grid-row-start: ${x};"]`);
                if (fromBacking != null) {
                    fromCoords.push(9 - y, 9 - x);
                    fromBacking.classList.remove("original_square");
                }
                if (toFreeBacking != null) {
                    toCoords.push(9 - y, 9 - x);
                    toFreeBacking.classList.remove("new_free_square");
                }
                if (toTakenBacking != null) {
                    toCoords.push(9 - y, 9 - x);
                    toTakenBacking.classList.remove("new_taken_square");
                    taken = true;
                }
            }
        }
        for (let i = 0; i < savedElements.length; i++) {
            this.board.appendChild(savedElements[i]);
        }
        if (toCoords.length == 2) {
            const backingTo = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${toCoords[0]}; grid-row-start: ${toCoords[1]};"]`);
            backingTo.classList.add(taken ? "new_taken_square" : "new_free_square");
            const backingFrom = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${fromCoords[0]}; grid-row-start: ${fromCoords[1]};"]`);
            backingFrom.classList.add("original_square");
        }
        if (flipInfo) {
            this.flipTakenPieces();
        }
        this.flipped = this.flipped ? false : true;
    }

    removeTracking() {
        for (let x=1; x<9; x++){
            for (let y=1; y<9; y++){
                const backing = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${y}; grid-row-start: ${x};"]`);
                backing.classList.remove("takeable");
            }
        }
    }

    removeToFrom() {
        for (let x=1; x<9; x++){
            for (let y=1; y<9; y++){
                const backing = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${y}; grid-row-start: ${x};"]`);
                backing.classList.remove("new_free_square", "original_square", "new_taken_square");
            }
        }
    }

    removePieces() {
        for (let x=1; x<9; x++){
            for (let y=1; y<9; y++){
                const piece = this.board.querySelector(`.board > img[style*="grid-column-start: ${y}; grid-row-start: ${x};"]`);
                if (piece != null) {
                    piece.remove();
                }
            }
        }
    }

    removeEverything() {
        this.removeToFrom();
        this.removeTracking();
        this.removePieces();
    }

    flipGridRow(row) {
        if (this.flipped) {
            return (9 - row);
        }
        return row;
    }

    flipMoveCoordinates() {
        console.log(`arguments: ${arguments}`);
        var flippedCoordinates = [];
        for (let i = 0; i < arguments.length; i++) {
            console.log(arguments[i]);
            if (this.flipped) {
                flippedCoordinates.push(((7 - Math.floor(arguments[i] / 8)) * 8) + (7 - (arguments[i] % 8)));
            }
            else {
                flippedCoordinates.push(arguments[i]);
            }
        }
        return flippedCoordinates;
    }

    processFEN(string) {
        this.removeEverything();
        var cell = 0;
        for (let i = 0; i < string.length; i++) {
            if (string[i] == ' ') {
                break;
            }
            if (string[i] == '/') {
                continue;
            }
            if (string[i] > '0' && string[i] <= '9') {
                cell += Number.parseInt(string[i]);
                continue;
            }
            const imageElement = document.createElement("img");
            imageElement.classList.add('piece');
            board.appendChild(imageElement);
            var colour;
            var type;

            if (string[i] == 'K') {
                colour = 'white';
                type = 'king';
            }
            if (string[i] == 'Q') {
                colour = 'white';
                type = 'queen';
            }
            if (string[i] == 'R') {
                colour = 'white';
                type = 'castle';
            }
            if (string[i] == 'B') {
                colour = 'white';
                type = 'bishop';
            }
            if (string[i] == 'N') {
                colour = 'white';
                type = 'knight';
            }
            if (string[i] == 'P') {
                colour = 'white';
                type = 'pawn';
            }
            if (string[i] == 'k') {
                colour = 'black';
                type = 'king';
            }
            if (string[i] == 'q') {
                colour = 'black';
                type = 'queen';
            }
            if (string[i] == 'r') {
                colour = 'black';
                type = 'castle';
            }
            if (string[i] == 'b') {
                colour = 'black';
                type = 'bishop';
            }
            if (string[i] == 'n') {
                colour = 'black';
                type = 'knight';
            }
            if (string[i] == 'p') {
                colour = 'black';
                type = 'pawn';
            }

            imageElement.src = `/images/pieces/${colour}_${type}.png`;
            imageElement.classList.add(colour);
            imageElement.classList.add(type);
            imageElement.setAttribute('name', type);
            imageElement.style.gridColumnStart = (cell % 8) + 1;
            imageElement.style.gridRowStart = Math.floor(cell / 8) + 1;
            cell += 1;
        }
    }

    queryBoard(string) {
        return this.board.querySelector(string);
    }

    invertColour(colour) {
        if (colour == "white") {
            return "black";
        }
        return "white";
    }

    undoMove() {
        this.removeTracking();
        if (this.currentMove <= 0 || this.currentMove > this.moves.length) {
            return;
        }
        this.removeToFrom();

        var [origin, destination, promotion, castle, castleType, colour, killedType] = this.moves[this.currentMove - 1];
        [origin, destination] = this.flipMoveCoordinates(origin, destination);

        console.log(`undoMove origin ${origin}, destination ${destination}, colour ${colour}`);

        var imageElement = this.board.querySelector(`.board > img[style*="grid-column-start: ${(destination % 8) + 1}; grid-row-start: ${Math.floor(destination / 8) + 1};"]`);
        if (promotion != 0) {
            imageElement.src = `/images/pieces/${colour}_pawn.png`;
        }

        imageElement.style.gridColumnStart = (origin % 8) + 1;
        imageElement.style.gridRowStart = Math.floor(origin / 8) + 1;

        if (killedType != null) {
            const killedElement = document.createElement("img");
            killedElement.classList.add('piece');
            killedElement.src = `/images/pieces/${this.invertColour(colour)}_${killedType}.png`;
            killedElement.classList.add(this.invertColour(colour));
            killedElement.classList.add(killedType);
            killedElement.setAttribute('name', killedType);
            killedElement.style.gridColumnStart = (destination % 8) + 1;
            killedElement.style.gridRowStart = Math.floor(destination / 8) + 1;
            board.appendChild(killedElement);
            this.removeTakenPiece(this.invertColour(colour), killedType);
        }
        this.currentMove -= 1;

        if (castle == 1) {
            if (castleType == 0 && colour == "white") {
                // king side
                console.log("castle 1");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(6)}; grid-row-start: ${this.flipGridRow(8)};"]`);
                castlePiece.style.gridColumnStart = this.flipGridRow(8);
                castlePiece.style.gridRowStart = this.flipGridRow(8);
            }
            if (castleType == 1 && colour == "white") {
                console.log("castle 2");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(4)}; grid-row-start: ${this.flipGridRow(8)};"]`);
                castlePiece.style.gridColumnStart = this.flipGridRow(1);
                castlePiece.style.gridRowStart = this.flipGridRow(8);
            }
            if (castleType == 0 && colour == "black") {
                console.log("castle 3");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(6)}; grid-row-start: ${this.flipGridRow(1)};"]`);
                console.log(castlePiece);
                castlePiece.style.gridColumnStart = this.flipGridRow(8);
                castlePiece.style.gridRowStart = this.flipGridRow(1);
                console.log(castlePiece);
            }
            if (castleType == 1 && colour == "black") {
                console.log("castle 4");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(4)}; grid-row-start: ${this.flipGridRow(1)};"]`);
                castlePiece.style.gridColumnStart = this.flipGridRow(1);
                castlePiece.style.gridRowStart = this.flipGridRow(1);
            }
        }


        if (this.currentMove > 0) {
            [origin, destination, promotion, castle, castleType, colour, killedType] = this.moves[this.currentMove - 1];
            [origin, destination] = this.flipMoveCoordinates(origin, destination);
            const backingFrom = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${(origin % 8) + 1}; grid-row-start: ${Math.floor(origin / 8) + 1};"]`);
            const backingTo = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${(destination % 8) + 1}; grid-row-start: ${Math.floor(destination / 8) + 1};"]`);
            backingFrom.classList.add("original_square");

            if (killedType == null) {
                backingTo.classList.add("new_free_square");
            }
            else {
                backingTo.classList.add("new_taken_square");
            }
        }
    }

    isSafeToMove() {
        if (this.currentMove == this.moves.length) {
            return true;
        }
        return false;
    }

    goForward() {
        this.removeTracking();
        if (this.currentMove < 0 || this.currentMove >= this.moves.length) {
            return;
        }
        this.removeToFrom();

        var [origin, destination, promotion, castle, castleType, colour, killedType] = this.moves[this.currentMove];
        [origin, destination] = this.flipMoveCoordinates(origin, destination);
        this.executeMove(origin, destination, promotion, castle, castleType, colour);
        this.moves.pop();
    }

    goMostRecent() {
        while (!(this.currentMove < 0 || this.currentMove >= this.moves.length)) {
            this.goForward();
        }
    }

    goFirstMove() {
        while (!(this.currentMove <= 0 || this.currentMove > this.moves.length)) {
            this.undoMove();
        }
    }

    saveFenString(fenString) {
        this.fenStrings.push(fenString);
    }

    removeTakenPiece(colour, killedType) {
        const top_take_board = document.getElementById("top_board_wrapper").firstChild.lastChild;
        const bottom_take_board = document.getElementById("bottom_board_wrapper").firstChild.lastChild;

        var take_board = null;
        if ((colour == 'white' && this.flipped == false) || (colour == 'black' && this.flipped == true)) {
            take_board = top_take_board;
        }
        else {
            take_board = bottom_take_board;
        }

        console.log(`image_type_div id: ${colour}_${killedType}_div`)

        const image_type_div = document.getElementById(`${colour}_${killedType}_div`);

        if (image_type_div.childNodes.length <= 1) {
            take_board.removeChild(image_type_div);
            return;
        }

        image_type_div.removeChild(image_type_div.firstChild);
    }

    isMoreValuable(pieceType1, pieceType2) {
        if (pieceType1 == 'pawn') {
            return false;
        }
        if (pieceType1 == 'knight' && pieceType2 != 'pawn') {
            return false;
        }
        if (pieceType1 == 'bishop' && pieceType2 != 'knight' && pieceType2 != 'pawn') {
            return false;
        }
        if (pieceType1 == 'castle' && pieceType2 != 'bishop' && pieceType2 != 'knight' && pieceType2 != 'pawn') {
            return false;
        }
        if (pieceType1 == 'queen' && pieceType2 != 'castle' && pieceType2 != 'bishop' && pieceType2 != 'knight' && pieceType2 != 'pawn') {
            return false;
        }
        if (pieceType1 == 'king' && pieceType2 == 'king') {
            return false;
        }
        return true;
    }

    insertTakeDiv(take_board, image_type_div, killedType) {
        for (let i = 0; i < take_board.childNodes.length; i++) {
            const childNodeType = take_board.childNodes[i].getAttribute("id").match("(?<=[a-z]+_)[a-z]+(?=_div)")
            console.log(`childNodeType: ${childNodeType}`);
            if (this.isMoreValuable(killedType, childNodeType)) {
                take_board.insertBefore(image_type_div, take_board.childNodes[i]);
                return;
            }
        }
        take_board.appendChild(image_type_div);
    }

    addTakenPiece(colour, killedType) {

        const top_take_board = document.getElementById("top_board_wrapper").firstChild.lastChild;
        const bottom_take_board = document.getElementById("bottom_board_wrapper").firstChild.lastChild;

        var image_type_div = document.getElementById(`${colour}_${killedType}_div`);

        if (image_type_div == null) {
            image_type_div = document.createElement('div');
            image_type_div.setAttribute('id', `${colour}_${killedType}_div`);
            image_type_div.classList.add('taken_piece_wrapper');

            if ((colour == 'white' && this.flipped == false) || (colour == 'black' && this.flipped == true)) {
                this.insertTakeDiv(top_take_board, image_type_div, killedType);
            }
            else {
                this.insertTakeDiv(bottom_take_board, image_type_div, killedType);
            }
        }

        const image = document.createElement('img')
        image.src = `/images/pieces/${colour}_${killedType}.png`;

        if (killedType == 'pawn'){
            image.classList.add('small_pawn_image')
        }
        else{
            image.classList.add('small_image')
        }
        image_type_div.appendChild(image);

    }

    executeMove(origin, destination, promotion, castle, castleType, colour) {
        console.log("executeMove: origin: ", origin, "; destination: ", destination, "flipped: ", this.flipped, "; promotion: ", promotion);

        this.removeToFrom();
        this.removeTracking();

        if (promotion == 2) {
            promotion = "queen";
        }
        if (promotion == 3) {
            promotion = "castle";
        }
        if (promotion == 4) {
            promotion = "bishop";
        }
        if (promotion == 5) {
            promotion = "knight";
        }

        const killedElement = this.board.querySelector(`.board > img[style*="grid-column-start: ${(destination % 8) + 1}; grid-row-start: ${Math.floor(destination / 8) + 1};"]`);
        var killedType = null;
        if (killedElement != null) {
            killedType = killedElement.getAttribute('name');
            this.board.removeChild(killedElement);
            this.addTakenPiece(this.invertColour(colour), killedType);
        }

        var imageElement;
        imageElement = this.board.querySelector(`.board > img[style*="grid-column-start: ${(origin % 8) + 1}; grid-row-start: ${Math.floor(origin / 8) + 1};"]`);
        
        if (promotion != 0) {
            imageElement.src = `/images/pieces/${colour}_${promotion}.png`;
        }

        imageElement.style.gridColumnStart = (destination % 8) + 1;
        imageElement.style.gridRowStart = Math.floor(destination / 8) + 1;

        if (castle == 1) {
            console.log("castle == 1");
            if (castleType == 0 && colour == "white") {
                // king side
                console.log("castle 1");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(8)}; grid-row-start: ${this.flipGridRow(8)};"]`);
                castlePiece.style.gridColumnStart = this.flipGridRow(6);
                castlePiece.style.gridRowStart = this.flipGridRow(8);
            }
            if (castleType == 1 && colour == "white") {
                console.log("castle 2");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(1)}; grid-row-start: ${this.flipGridRow(8)};"]`);
                castlePiece.style.gridColumnStart = this.flipGridRow(4);
                castlePiece.style.gridRowStart = this.flipGridRow(8);
            }
            if (castleType == 0 && colour == "black") {
                console.log("castle 3");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(8)}; grid-row-start: ${this.flipGridRow(1)};"]`);
                console.log(castlePiece);
                castlePiece.style.gridColumnStart = this.flipGridRow(6);
                castlePiece.style.gridRowStart = this.flipGridRow(1);
                console.log(castlePiece);
            }
            if (castleType == 1 && colour == "black") {
                console.log("castle 4");
                var castlePiece = this.board.querySelector(`.board > img[style*="grid-column-start: ${this.flipGridRow(1)}; grid-row-start: ${this.flipGridRow(1)};"]`);
                castlePiece.style.gridColumnStart = this.flipGridRow(4);
                castlePiece.style.gridRowStart = this.flipGridRow(1);
            }
        }

        if (imageElement.classList.contains("pawn") && imageElement.style.gridRowStart == this.flipGridRow(4) && colour == "white" && (origin % 8 != destination % 8) && killedElement == null) {
            // must be white taking en passant
            var takenEnPassant = this.board.querySelector(`.board > img[style*="grid-column-start: ${(destination % 8) + 1}; grid-row-start: ${Math.floor(destination / 8) + (this.flipped ? 0 : 2)};"]`);
            this.board.removeChild(takenEnPassant);
        }
        if (imageElement.classList.contains("pawn") && imageElement.style.gridRowStart == this.flipGridRow(5) && colour == "black" && (origin % 8 != destination % 8) && killedElement == null) {
            // must be black taking en passant
            var takenEnPassant = this.board.querySelector(`.board > img[style*="grid-column-start: ${(destination % 8) + 1}; grid-row-start: ${Math.floor(destination / 8) + (this.flipped ? 2 : 0)};"]`);
            this.board.removeChild(takenEnPassant);
        }

        const backingFrom = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${(origin % 8) + 1}; grid-row-start: ${Math.floor(origin / 8) + 1};"]`);
        const backingTo = this.board.querySelector(`.board > div.backing[style*="grid-column-start: ${(destination % 8) + 1}; grid-row-start: ${Math.floor(destination / 8) + 1};"]`);
        backingFrom.classList.add("original_square");
        if (killedElement == null) {
            backingTo.classList.add("new_free_square");
        }
        else {
            backingTo.classList.add("new_taken_square");
        }
        this.currentMove += 1;
        [origin, destination] = this.flipMoveCoordinates(origin, destination);
        console.log(`pushing origin: ${origin}, destination: ${destination}`);
        this.moves.push([origin, destination, promotion, castle, castleType, colour, killedType]);
    }
}

export var game_board = new Board('white');