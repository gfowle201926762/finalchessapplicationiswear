
import { game_board } from "./Board.js";

// var image_cache = {}
// create_cache();

// function create_cache() {
//     for (const col of ['white', 'black']) {
//         for (const type of ['king', 'queen', 'bishop', 'knight', 'castle', 'pawn']) {
//             image_cache[`/images/pieces/${col}_${type}.png`] = [];
//             if (type == 'king') {
//                 image_cache[`/images/pieces/${col}_${type}.png`].push(document.createElement("img"));
//                 image_cache[`/images/pieces/${col}_${type}.png`][0].src = `/images/pieces/${col}_${type}.png`;
//             }
//             else {
//                 for (let i = 0; i < 8; i++) {
//                     image_cache[`/images/pieces/${col}_${type}.png`].push(document.createElement("img"));
//                     image_cache[`/images/pieces/${col}_${type}.png`][i].src = `/images/pieces/${col}_${type}.png`;
//                 }
//             }
//         }
//     }
// }

please_wait();
initialiseButtons();

const playerId = document.getElementById('bottom_player_info').firstElementChild.innerHTML;
console.log(document.getElementById('bottom_player_info').firstElementChild);
console.log(document.getElementById('bottom_player_info').firstElementChild.innerHTML);

const regex = "[^/]+$";
const game_id = window.location.href.match(regex)[0];
console.log(`game_id: ${game_id}`);


// need to pass in the address here...
// gus-chess-1347475692.eu-west-2.elb.amazonaws.com
console.log(`window.location.hostname: ${window.location.hostname}`);
const socket = new WebSocket(`wss://${window.location.hostname}:443/websockets/game`);


var lastClickedOwn = null;

var game_state = 'initialising';
var game_ended = false;
var menu_down = false;
var promotion_selection_down = false;
var currentPlayer = 'white';
var clientPlayer = null;
var replaying = false;
var autoQueen = false;

  
socket.onopen = function(event) {
    console.log("WebSocket connection established.");
    sendMessage({
        'messageType' : 'INITIALISE',
        'playerId' : playerId,
        'gameId' : game_id
    });
};

socket.onerror = function(error) {
    console.error("WebSocket error: ", error);
};

socket.onclose = function(event) {
    console.log("WebSocket connection closed:", event);
};

function isClientVictory(status) {
    return ((status == 'WHITE_VICTORY' && clientPlayer == 'white') || (status == 'BLACK_VICTORY' && clientPlayer == 'black'));
}

function opponentCanMove(messageData) {
    return messageData['response']['origin'] != 0 || messageData['response']['destination'] != 0;
}

function isGettingLegalMovesOnly(messageData) {
    return messageData['fenStringClient'] == null || messageData['fenStringEngine'] == null;
}

function isReplay(messageData) {
    if (messageData['uuid'] != undefined) {
        return true;
    }
    return false;
}

socket.onmessage = function(event) {
    const messageData = JSON.parse(event.data);
    // Handle incoming message data
    console.log("Received message:", messageData);
    console.log("gamestate: ", game_state);

    if (isReplay(messageData)) {
        replaying = true;
        for (let i = 0; i < messageData['moves'].length; i++) {
            var move = messageData['moves'][i];
            console.log(move);
            var [origin, destination] = game_board.flipMoveCoordinates(move['origin'], move['destination']);
            game_board.executeMove(origin, destination, move['promotion'],
                move['castle'], move['castleType'], currentPlayer
            );
            invert_current_player();
        }
        game_finished(messageData['status'], messageData['reason'], true);
        game_ended = true;
        socket.close();
        return;
    }

    if (game_state == 'initialising') {
        clientPlayer = messageData['colour'].toLowerCase();
        game_board.saveFenString(messageData['fenString']);
        game_board.processFEN(messageData['fenString']);
        var opponentId = messageData['opponentUsername']
        var top_player_info = document.getElementById('top_player_info')
        top_player_info.innerHTML = `Opponent: ${opponentId}`
        game_state = 'in_play';
        if (clientPlayer == 'black') {
            game_board.flipBoard(false);
        }
        remove_please_wait();
        console.log("ON_MESSAGE INITIALISE... currentPlayer: ", currentPlayer, "; clientPlayer: ", clientPlayer);
        addEventListener("click", handleClick);
        return;
    }

    console.log(`status: ${messageData['status']}`);
        
    if (!game_ended && !isGettingLegalMovesOnly(messageData) && opponentCanMove(messageData)) {
        if (messageData['fenStringClient'].length > 0) {
            game_board.saveFenString(messageData['fenStringClient']);
        }
        if (messageData['fenStringEngine'].length > 0) {
            game_board.saveFenString(messageData['fenStringEngine']);
        }
        if (!game_board.isSafeToMove()) {
            lastClickedOwn = null;
            game_board.goMostRecent();
        }
        var [origin, destination] = game_board.flipMoveCoordinates(messageData['response']['origin'], messageData['response']['destination']);
        // console.log(`origin: ${origin}, destination: ${destination}`);

        game_board.executeMove(origin, destination, messageData['response']['promotion'],
            messageData['response']['castle'], messageData['response']['castleType'], game_board.invertColour(clientPlayer)
        );
        invert_current_player();
    }
    
    if (messageData['status'] != "ONGOING") {
        // GAME OVER
        game_finished(messageData['status'], messageData['reason'], true);
        game_ended = true;
        socket.close();
    }
        
    console.log("SAVING LEGAL MOVES... currentPlayer: ", currentPlayer, "; clientPlayer: ", clientPlayer);
    game_board.saveLegalMoves(messageData);
    console.log("legal moves:");
    console.log(game_board.legalMoves);
};

function sendMessage(message) {
    if (socket.readyState === WebSocket.OPEN) {
        console.log("SENDING MESSAGE:");
        console.log(message);
        socket.send(JSON.stringify(message));
    } else {
        console.error("WebSocket connection is not open.");
    }
}

function handleClick(event) {
    // console.log("handleClick; flipped: ", game_board.flipped);
    const piece = event.target;

    if (menu_down || promotion_selection_down) {
        return;
    }

    if (replaying || !game_board.isSafeToMove()) {
        return;
    }
    // console.log("one");
    if (!piece.classList.contains("backing") && !piece.classList.contains("piece")) {
        return;
    }
    // console.log("two");
    console.log("HANDLE CLICK... currentPlayer: ", currentPlayer, "; clientPlayer: ", clientPlayer);
    // console.log("piece: ", piece, "; lastClickedOwn: ", lastClickedOwn);
    game_board.removeTracking();

    if (currentPlayer != clientPlayer || piece == lastClickedOwn) {
        lastClickedOwn = null;
        return;
    }
    // console.log("three");

    const computedStyle = window.getComputedStyle(piece);
    const row = Number(computedStyle.gridRow.charAt(0));
    const col = Number(computedStyle.gridColumn.charAt(0));
    var origin = ((row - 1) * 8) + (col - 1);

    // send move if already clicked own piece, and you have selected empty square, or a piece which is not yours
    if (lastClickedOwn != null && (!piece.classList.contains('piece') || (piece.classList.contains('piece') && !piece.classList.contains(clientPlayer)))) {
        sendMoveToServerIfValid(piece, origin, row, col);
        lastClickedOwn = null;
        return;
    }

    
    // show legal moves, if you selected your own piece (and it's not what you last selected)
    // console.log(piece);
    // console.log(`origin: ${origin}, row: ${row}, col: ${col}`);
    if (piece.classList.contains(clientPlayer) && game_board.isCoordinateInLegalMoves(origin)) {
        lastClickedOwn = piece;
        for (let i = 0; i < game_board.getLegalMovesLength(origin); i++) {
            const backing = game_board.board.querySelector(`.board > div.backing[style*="grid-column-start: ${(game_board.getLegalMoveCoordinate(origin, i, "destination") % 8) + 1}; grid-row-start: ${Math.floor(game_board.getLegalMoveCoordinate(origin, i, "destination") / 8) + 1};"]`);
            backing.style.gridColumnStart = (game_board.getLegalMoveCoordinate(origin, i, "destination") % 8) + 1;
            backing.style.gridRowStart = Math.floor(game_board.getLegalMoveCoordinate(origin, i, "destination") / 8) + 1;
            backing.classList.add("takeable");
            game_board.board.appendChild(backing);
        }
        return;
    }
}

function handle_promotion_selection(promotion, lastClickedOrigin, origin, castle, castleType) {
    console.log(`handle_promotion_selection-- promotion: ${promotion}, origin: ${lastClickedOrigin}, destination: ${origin}`);
    handle_close_promotion_screen();
    game_board.executeMove(lastClickedOrigin, origin, promotion, castle, castleType, clientPlayer);
    lastClickedOwn = null;
    invert_current_player();
    sendMessage({
        'messageType' : 'SEND_MOVE',
        'uuid' : game_id,
        'origin' : game_board.flipMoveCoordinates(lastClickedOrigin)[0],
        'destination' : game_board.flipMoveCoordinates(origin)[0],
        'promotion' : promotion,
        'castle' : castle,
        'castleType' : castleType,
    });
}

function handle_promotion(origin, destination, rowDestination, colDestination, castle, castleType) {
    if (autoQueen) {
        handle_promotion_selection(2, origin, destination, castle, castleType);
        return;
    }
    promotion_selection_down = true;
    var rowStart = rowDestination === 8 ? rowDestination - 5 : rowDestination + 1;
    var rowEnd = rowDestination === 8 ? rowDestination - 1 : rowDestination + 5;

    console.log(`handle_promotion-- origin: ${origin}, destination: ${destination}, rowDestination: ${rowDestination}, rowStart: ${rowStart}, rowEnd: ${rowEnd}, colDestination: ${colDestination}`);

    const promotion_selection = document.createElement('div')
    promotion_selection.setAttribute('id', 'promotion_selection')
    promotion_selection.classList.add('promotion_selection')
    promotion_selection.style.gridColumnStart = colDestination;
    promotion_selection.style.gridColumnEnd = colDestination;
    promotion_selection.style.gridRowStart = rowStart;
    promotion_selection.style.gridRowEnd = rowEnd;
    board.appendChild(promotion_selection)

    const close_container = document.createElement('div')
    close_container.classList.add('close_container')
    promotion_selection.appendChild(close_container)

    const close_button = document.createElement('img')
    close_button.setAttribute('id', 'close_option_screen')
    // close_button.src = '/images/buttons/cross.png'
    game_board.setImageSource(close_button, '/images/buttons/cross.png');
    close_button.classList.add('close_end_screen')
    close_container.appendChild(close_button)
    close_button.addEventListener('click', handle_close_promotion_screen, {once: true})

    const img_queen = document.createElement("img");
    // img_queen.src = `/images/pieces/${clientPlayer}_queen.png`;
    game_board.setImageSource(img_queen, `/images/pieces/${clientPlayer}_queen.png`);
    img_queen.classList.add('piece');
    img_queen.classList.add('promotion_piece');
    img_queen.addEventListener("click", handle_promotion_selection.bind(null, 2, origin, destination, castle, castleType));
    promotion_selection.appendChild(img_queen);

    const img_castle = document.createElement("img");
    // img_castle.src = `/images/pieces/${clientPlayer}_castle.png`;
    game_board.setImageSource(img_castle, `/images/pieces/${clientPlayer}_castle.png`);
    img_castle.classList.add('piece');
    img_castle.classList.add('promotion_piece');
    img_castle.addEventListener("click", handle_promotion_selection.bind(null, 3, origin, destination, castle, castleType));
    promotion_selection.appendChild(img_castle);

    const img_bishop = document.createElement("img");
    // img_bishop.src = `/images/pieces/${clientPlayer}_bishop.png`;
    game_board.setImageSource(img_bishop, `/images/pieces/${clientPlayer}_bishop.png`);
    img_bishop.classList.add('piece');
    img_bishop.classList.add('promotion_piece');
    img_bishop.addEventListener("click", handle_promotion_selection.bind(null, 4, origin, destination, castle, castleType));
    promotion_selection.appendChild(img_bishop);

    const img_knight = document.createElement("img");
    // img_knight.src = `/images/pieces/${clientPlayer}_knight.png`;
    game_board.setImageSource(img_knight, `/images/pieces/${clientPlayer}_knight.png`);
    img_knight.classList.add('piece');
    img_knight.classList.add('promotion_piece');
    img_knight.addEventListener("click", handle_promotion_selection.bind(null, 5, origin, destination, castle, castleType));
    promotion_selection.appendChild(img_knight);
}

function sendMoveToServerIfValid(piece, origin, rowDestination, colDestination) {
    const lastClickedStyle = window.getComputedStyle(lastClickedOwn);
    const lastClickedRow = lastClickedStyle.gridRow.charAt(0);
    const lastClickedCol = lastClickedStyle.gridColumn.charAt(0);
    var lastClickedOrigin = ((lastClickedRow - 1) * 8) + (lastClickedCol - 1);

    for (let i = 0; i < game_board.getLegalMovesLength(lastClickedOrigin); i++) {
        if (game_board.getLegalMoveCoordinate(lastClickedOrigin, i, "destination") == origin) {

            game_board.removeTracking();

            var castle = 0;
            var castleType = 0;
            var promotion = 0;
            if (lastClickedOwn.classList.contains("king") && game_board.flipMoveCoordinates(lastClickedOrigin)[0] == 60 && clientPlayer == "white" && game_board.flipMoveCoordinates(origin)[0] == 62) {
                castle = 1;
            }
            if (lastClickedOwn.classList.contains("king") && game_board.flipMoveCoordinates(lastClickedOrigin)[0] == 60 && clientPlayer == "white" && game_board.flipMoveCoordinates(origin)[0] == 58) {
                castle = 1;
                castleType = 1;
            }
            if (lastClickedOwn.classList.contains("king") && game_board.flipMoveCoordinates(lastClickedOrigin)[0] == 4 && clientPlayer == "black" && game_board.flipMoveCoordinates(origin)[0] == 6) {
                castle = 1;
            }
            if (lastClickedOwn.classList.contains("king") && game_board.flipMoveCoordinates(lastClickedOrigin)[0] == 4 && clientPlayer == "black" && game_board.flipMoveCoordinates(origin)[0] == 2) {
                castle = 1;
                castleType = 1;
            }
            if (lastClickedOwn.classList.contains("pawn") && clientPlayer == "white" && game_board.flipGridRow(rowDestination) == 1) {
                handle_promotion(lastClickedOrigin, origin, rowDestination, colDestination, castle, castleType);
                return;
            }
            if (lastClickedOwn.classList.contains("pawn") && clientPlayer == "black" && game_board.flipGridRow(rowDestination) == 8) {
                handle_promotion(lastClickedOrigin, origin, rowDestination, colDestination, castle, castleType);
                return;
            }
            handle_promotion_selection(0, lastClickedOrigin, origin, castle, castleType);
            break;
        }
    }
}

function invert_current_player() {
    if (currentPlayer == "white") {
        currentPlayer = "black";
    }
    else {
        currentPlayer = "white";
    }
    console.log("CURRENT PLAYER INVERTED TO: ", currentPlayer);
}

function handle_start_arrow() {
    lastClickedOwn = null;
    game_board.goFirstMove();
}

function handle_end_arrow() {
    lastClickedOwn = null;
    game_board.goMostRecent();
}

function handle_back_arrow() {
    lastClickedOwn = null;
    game_board.undoMove();
}

function handle_forward_arrow() {
    lastClickedOwn = null;
    game_board.goForward();
}

function handle_flip() {
    lastClickedOwn = null;
    game_board.flipBoard(true);
}

function handle_close_promotion_screen() {
    const promotion_selection = document.getElementById('promotion_selection')
    if (promotion_selection){
        promotion_selection.remove()
    }

    promotion_selection_down = false
}

function handle_close_option_screen() {
    const drop_down = document.getElementById('drop_down')
    if (drop_down){
        drop_down.remove()
    }

    menu_down = false
}

function handle_abort_game(){
    // handle_close_option_screen();
    // var reason = 'RESIGNATION';
    // game_ended = true;
    // game_finished(clientPlayer == "black" ? "WHITE_VICTORY" : "BLACK_VICTORY", reason);
    sendMessage({
        'messageType' : 'GAME_STATUS_UPDATE',
        'uuid' : game_id,
        'reason' : "RESIGNATION"
    });
}

function handle_menu(event) {

    if (menu_down == true){
        handle_close_option_screen()
    }

    else if (menu_down == false){

        const drop_down = document.createElement('div')
        drop_down.setAttribute('id', 'drop_down')
        drop_down.classList.add('drop_down')
        drop_down.style.gridColumnStart = 3
        drop_down.style.gridColumnEnd = 7
        drop_down.style.gridRowStart = 2
        drop_down.style.gridRowEnd = 7
        board.appendChild(drop_down)

        const close_container = document.createElement('div')
        close_container.classList.add('close_container')
        drop_down.appendChild(close_container)

        const close_button = document.createElement('img')
        close_button.setAttribute('id', 'close_option_screen')
        // close_button.src = '/images/buttons/cross.png'
        game_board.setImageSource(close_button, '/images/buttons/cross.png');
        close_button.classList.add('close_end_screen')
        close_container.appendChild(close_button)
        close_button.addEventListener('click', handle_close_option_screen, {once: true})

        const button_container = document.createElement('div')
        button_container.classList.add('drop_down_container')
        drop_down.appendChild(button_container)

        const abort_button = document.createElement('div')
        abort_button.setAttribute('id', 'abort_button')
        abort_button.classList.add('drop_down_button')
        if (game_ended == false){
            abort_button.innerHTML = 'Abort game'
        }
        if (game_ended == true){
            abort_button.innerHTML = 'New game'
        }

        button_container.appendChild(abort_button)
        abort_button.addEventListener('click', handle_abort_game, {once: true})

        const pause_button = document.createElement('div')
        pause_button.setAttribute('id', 'pause_button')
        pause_button.classList.add('drop_down_button')
        // if (pause == false){
        //     pause_button.innerHTML = 'Pause'
        // }
        // if (pause == true){
        //     pause_button.innerHTML = 'Resume'
        // }
        button_container.appendChild(pause_button)
        // pause_button.addEventListener('click', handle_pause_game)


        menu_down = true
    }
    lastClickedOwn = null;
}

function initialiseButtons() {
    const board_wrapper = document.getElementById('board_wrapper')

    // create top and bottom board wrapper
    const top_board_wrapper = document.createElement('div')
    top_board_wrapper.classList.add('above_below_board_wrapper')
    top_board_wrapper.setAttribute('id', 'top_board_wrapper')
    board_wrapper.insertBefore(top_board_wrapper, board_wrapper.firstChild)


    const bottom_board_wrapper = document.createElement('div')
    bottom_board_wrapper.classList.add('above_below_board_wrapper')
    bottom_board_wrapper.setAttribute('id', 'bottom_board_wrapper')
    board_wrapper.appendChild(bottom_board_wrapper)

    // creating top and bottom player wrappers
    const top_player_wrapper = document.createElement('div')
    top_player_wrapper.classList.add('player_wrapper')
    top_player_wrapper.setAttribute('id', 'top_player_wrapper')
    top_board_wrapper.appendChild(top_player_wrapper)

    const bottom_player_wrapper = document.createElement('div')
    bottom_player_wrapper.classList.add('player_wrapper')
    bottom_player_wrapper.setAttribute('id', 'bottom_player_wrapper')
    bottom_board_wrapper.appendChild(bottom_player_wrapper)

    // creating top and bottom player info containers
    const top_player_info = document.createElement('div')
    top_player_info.classList.add('player_info')
    top_player_info.classList.add('no_wrap')
    top_player_info.setAttribute('id', 'top_player_info')
    top_player_info.innerHTML = `Opponent: loading`
    top_player_wrapper.appendChild(top_player_info)

    // const bottom_player_info = document.createElement('div')
    const bottom_player_info = document.getElementById('bottom_player_info')
    bottom_player_info.removeAttribute("style");
    bottom_player_info.classList.add('player_info') 
    bottom_player_info.classList.add('no_wrap')
    bottom_player_info.setAttribute('id', 'bottom_player_info')
    // bottom_player_info.innerHTML = `You: myself`
    bottom_player_wrapper.appendChild(bottom_player_info)

    // creating top and bottom takeboards
    const top_take_board = document.createElement('div')
    top_take_board.setAttribute('id', 'top_take_board')
    top_take_board.classList.add('take_board')
    top_player_wrapper.appendChild(top_take_board)

    const bottom_take_board = document.createElement('div')
    bottom_take_board.setAttribute('id', 'bottom_take_board')
    bottom_take_board.classList.add('take_board')
    bottom_player_wrapper.appendChild(bottom_take_board)

    // creating back / forward arrows to go in the bottom board wrapper.
    const start_arrow = document.createElement('img')
    start_arrow.setAttribute('id', 'start_arrow')
    start_arrow.classList.add('board_buttons')
    // start_arrow.src = '/images/buttons/start-arrow.png'
    game_board.setImageSource(start_arrow, '/images/buttons/start-arrow.png');
    // start_arrow.src = '/images/pieces/white_king.png'
    bottom_board_wrapper.appendChild(start_arrow)
    start_arrow.addEventListener('click', handle_start_arrow)

    const left_arrow = document.createElement('img')
    left_arrow.setAttribute('id', 'left_arrow')
    left_arrow.classList.add('board_buttons')
    // left_arrow.src = '/images/buttons/left-arrow.png'
    game_board.setImageSource(left_arrow, '/images/buttons/left-arrow.png');
    // var colour1 = "black";
    // var type1 = "king";
    // left_arrow.src = `/images/pieces/${colour1}_${type1}.png`
    bottom_board_wrapper.appendChild(left_arrow)
    left_arrow.addEventListener('click', handle_back_arrow)

    const right_arrow = document.createElement('img')
    right_arrow.setAttribute('id', 'right_arrow')
    right_arrow.classList.add('board_buttons')
    // right_arrow.src = '/images/buttons/right-arrow.png'
    game_board.setImageSource(right_arrow, '/images/buttons/right-arrow.png');
    bottom_board_wrapper.appendChild(right_arrow)
    right_arrow.addEventListener('click', handle_forward_arrow)

    const end_arrow = document.createElement('img')
    end_arrow.setAttribute('id', 'end_arrow')
    end_arrow.classList.add('board_buttons')
    // end_arrow.src = '/images/buttons/end-arrow.png'
    game_board.setImageSource(end_arrow, '/images/buttons/end-arrow.png');
    bottom_board_wrapper.appendChild(end_arrow)
    end_arrow.addEventListener('click', handle_end_arrow)





    // create buttons to go in the side menu (settings and flip board) -- and a container to orgaise them horizontally
    const menu_collection = document.createElement('div')
    menu_collection.classList.add('menu_collection')
    top_board_wrapper.appendChild(menu_collection)

    const menu_button = document.createElement('img')
    menu_button.setAttribute('id', 'menu_button')
    // menu_button.src = '/images/buttons/settings.png'
    game_board.setImageSource(menu_button, '/images/buttons/settings.png');
    menu_button.classList.add('board_buttons')
    menu_collection.appendChild(menu_button)
    menu_button.addEventListener('click', handle_menu)

    const flip_button = document.createElement('img')
    flip_button.setAttribute('id', 'flip_button')
    flip_button.classList.add('board_buttons')
    // flip_button.src = '/images/buttons/flip_button.png'
    game_board.setImageSource(flip_button, '/images/buttons/flip_button.png');
    menu_collection.appendChild(flip_button)
    flip_button.addEventListener('click', handle_flip)
}

function remove_please_wait() {
    const please_wait = document.getElementById('please_wait')
    if (please_wait != null){
        please_wait.remove()
    }
}

function please_wait(){
    if (document.getElementById('please_wait') == null){

        const board = document.getElementById('board')
        const please_wait = document.createElement('div')

        please_wait.setAttribute('id', 'please_wait')
        please_wait.style.gridColumnStart = 3
        please_wait.style.gridColumnEnd = 7
        please_wait.style.gridRowStart = 3
        please_wait.style.gridRowEnd = 5
        please_wait.classList.add('please_wait')
        board.appendChild(please_wait)

        please_wait.innerHTML = 'Waiting for opponent...'
    }
}

function handle_close_end_screen(){
    const end_screen = document.getElementById('end_screen')
    end_screen.remove()
}

function handle_play_again(event){
    window.location.pathname = '/'
}

function handle_rematch(event){

    sendMessage({
        'messageType' : 'REQUEST_REMATCH',
        'uuid' : game_id
    });
    handle_close_end_screen();
    please_wait();
}

function game_finished(result, reason, disallowed){
    if (document.getElementById('end_screen') == null){

        const board = document.getElementById('board')
        const end_screen = document.createElement('div')

        end_screen.setAttribute('id', 'end_screen')
        end_screen.style.gridColumnStart = 3
        end_screen.style.gridColumnEnd = 7
        end_screen.style.gridRowStart = 2
        end_screen.style.gridRowEnd = 7
        end_screen.classList.add('end_screen')
        board.appendChild(end_screen)

        const close_container = document.createElement('div')
        close_container.classList.add('close_container')
        end_screen.appendChild(close_container)

        const close_button = document.createElement('img')
        close_button.setAttribute('id', 'close_end_screen')
        // close_button.src = '/images/buttons/cross.png'
        game_board.setImageSource(close_button, '/images/buttons/cross.png');
        close_button.classList.add('close_end_screen')
        close_container.appendChild(close_button)
        close_button.addEventListener('click', handle_close_end_screen, {once: true})

        const announcement = document.createElement('div')
        announcement.innerHTML = 'The game has ended!'
        announcement.classList.add('text_wrapper')
        end_screen.appendChild(announcement)

        const info = document.createElement('div')
        info.innerHTML = `${result} by ${reason}`
        info.classList.add('text_wrapper')
        end_screen.appendChild(info)

        if (disallowed != true){
            const rematch = document.createElement('div')
            rematch.setAttribute('id', 'rematch')
            rematch.innerHTML = 'Rematch'
            rematch.classList.add('text_wrapper')
            rematch.classList.add('play_again')
            end_screen.appendChild(rematch)
            rematch.addEventListener('click', handle_rematch, {once: true})
        }


        const play_again = document.createElement('div')
        play_again.innerHTML = "Back to lobby"
        play_again.classList.add('text_wrapper')
        play_again.classList.add('play_again')
        play_again.addEventListener('click', handle_play_again, {once: true})
        end_screen.appendChild(play_again)
    }
}
