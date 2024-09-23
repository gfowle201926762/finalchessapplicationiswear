
import { game_board } from "./Board.js";

please_wait();
initialiseButtons();



var lastClickedOwn = null;
var currentPlayer = "black";
var clientPlayer = "black";
var game_state = 'initialising';
var game_id = null;
var game_ended = false;
var menu_down = false;



const socket = new WebSocket("ws://localhost:8080/websocket");
  
socket.onopen = function(event) {
    console.log("WebSocket connection established.");
    sendMessage({
        'messageType' : 'INITIALISE',
        'playerId' : 'placementPlayerId',
        'opponentId' : 'placementOpponentId',
        'opponentType' : 'COMPUTER',
        'breadth' : 100,
        'startPlayer' : 'white',
        'timeLimit' : 1
    });
};

socket.onerror = function(error) {
    console.error("WebSocket error: ", error);
};

socket.onclose = function(event) {
    console.log("WebSocket connection closed:", event);
};

socket.onmessage = function(event) {
    const messageData = JSON.parse(event.data);
    // Handle incoming message data
    console.log("Received message:", messageData);

    if (game_state == 'initialising') {
        game_board.saveFenString(messageData['fenString']);
        game_board.processFEN(messageData['fenString']);
        game_state = 'in_play';
        game_id = messageData['id'];
        remove_please_wait();
        return;
    }
    else {
        console.log(`status: ${messageData['status']}`);
         
        if (!game_ended) {
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
            console.log(`origin: ${origin}, destination: ${destination}`);
    
            game_board.executeMove(origin, destination, messageData['response']['promotion'],
                messageData['response']['castle'], messageData['response']['castleType'], game_board.invertColour(clientPlayer)
            );
        }
        
        if (messageData['status'] != "ONGOING") {
            // GAME OVER
            game_finished(messageData['status'], true);
            game_ended = true;
        }
        
    }
    game_board.saveLegalMoves(messageData);
    console.log("legal moves:");
    console.log(game_board.legalMoves);
    invert_current_player();
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
    console.log("handleClick; flipped: ", game_board.flipped);
    const piece = event.target;

    if (!game_board.isSafeToMove()) {
        return;
    }
    if (!piece.classList.contains("backing") && !piece.classList.contains("piece")) {
        return;
    }
    
    game_board.removeTracking();

    if (currentPlayer != clientPlayer || piece == lastClickedOwn) {
        lastClickedOwn = null;
        return;
    }

    const computedStyle = window.getComputedStyle(piece);
    const row = computedStyle.gridRow;
    const col = computedStyle.gridColumn;
    var origin = ((row - 1) * 8) + (col - 1);

    // send move if already clicked own piece, and you have selected empty square, or a piece which is not yours
    if (lastClickedOwn != null && (!piece.classList.contains('piece') || (piece.classList.contains('piece') && !piece.classList.contains(clientPlayer)))) {
        sendMoveToServerIfValid(piece, origin);
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

function sendMoveToServerIfValid(piece, origin) {
    const lastClickedStyle = window.getComputedStyle(lastClickedOwn);
    const lastClickedRow = lastClickedStyle.gridRow;
    const lastClickedCol = lastClickedStyle.gridColumn;
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
            if (lastClickedOwn.classList.contains("pawn") && clientPlayer == "white" && game_board.flipGridRow(lastClickedRow) == 1) {
                promotion = 2;
            }
            if (lastClickedOwn.classList.contains("pawn") && clientPlayer == "black" && game_board.flipGridRow(lastClickedRow) == 8) {
                promotion = 2;
            }
            game_board.executeMove(lastClickedOrigin, origin, promotion, castle, castleType, clientPlayer);
            lastClickedOwn = null;
            invert_current_player();
            sendMessage({
                'messageType' : 'SEND_MOVE',
                'uuid' : game_id,
                'origin' : game_board.flipMoveCoordinates(lastClickedOrigin)[0],
                'destination' : game_board.flipMoveCoordinates(origin)[0],
                'promotion' : '',
                'castle' : castle,
                'castleType' : castleType,
            });

            break;
        }
    }
}

function invert_current_player() {
    if (currentPlayer == "white") {
        currentPlayer = "black";
    }
    else {
        currentPlayer = "black";
    }
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
    game_board.flipBoard();
}

function handle_close_option_screen() {
    const drop_down = document.getElementById('drop_down')
    if (drop_down){
        drop_down.remove()
    }

    menu_down = false
}

function handle_abort_game(){
    handle_close_option_screen();
    var result = 'game aborted';
    game_ended = true;
    game_finished(result);
    sendMessage({
        'messageType' : 'GAME_STATUS_UPDATE',
        'uuid' : game_id,
        'status' : result
    });
    // need confirmation that the abortion has been completed.
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
        close_button.src = '/images/buttons/cross.png'
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
    top_player_info.setAttribute('id', 'top_player_info')
    top_player_info.innerHTML = `Opponent: opponent`
    top_player_wrapper.appendChild(top_player_info)

    const bottom_player_info = document.createElement('div')
    bottom_player_info.classList.add('player_info')
    bottom_player_info.setAttribute('id', 'bottom_player_info')
    bottom_player_info.innerHTML = `You: myself`
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
    start_arrow.src = '/images/buttons/start-arrow.png'
    bottom_board_wrapper.appendChild(start_arrow)
    start_arrow.addEventListener('click', handle_start_arrow)

    const left_arrow = document.createElement('img')
    left_arrow.setAttribute('id', 'left_arrow')
    left_arrow.classList.add('board_buttons')
    left_arrow.src = '/images/buttons/left-arrow.png'
    bottom_board_wrapper.appendChild(left_arrow)
    left_arrow.addEventListener('click', handle_back_arrow)

    const right_arrow = document.createElement('img')
    right_arrow.setAttribute('id', 'right_arrow')
    right_arrow.classList.add('board_buttons')
    right_arrow.src = '/images/buttons/right-arrow.png'
    bottom_board_wrapper.appendChild(right_arrow)
    right_arrow.addEventListener('click', handle_forward_arrow)

    const end_arrow = document.createElement('img')
    end_arrow.setAttribute('id', 'end_arrow')
    end_arrow.classList.add('board_buttons')
    end_arrow.src = '/images/buttons/end-arrow.png'
    bottom_board_wrapper.appendChild(end_arrow)
    end_arrow.addEventListener('click', handle_end_arrow)





    // create buttons to go in the side menu (settings and flip board) -- and a container to orgaise them horizontally
    const menu_collection = document.createElement('div')
    menu_collection.classList.add('menu_collection')
    top_board_wrapper.appendChild(menu_collection)

    const menu_button = document.createElement('img')
    menu_button.setAttribute('id', 'menu_button')
    menu_button.src = '/images/buttons/settings.png'
    menu_button.classList.add('board_buttons')
    menu_collection.appendChild(menu_button)
    menu_button.addEventListener('click', handle_menu)

    const flip_button = document.createElement('img')
    flip_button.setAttribute('id', 'flip_button')
    flip_button.classList.add('board_buttons')
    flip_button.src = '/images/buttons/flip_button.png'
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

function game_finished(result, disallowed){
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
        close_button.src = '/images/buttons/cross.png'
        close_button.classList.add('close_end_screen')
        close_container.appendChild(close_button)
        close_button.addEventListener('click', handle_close_end_screen, {once: true})

        const announcement = document.createElement('div')
        announcement.innerHTML = 'The game has ended!'
        announcement.classList.add('text_wrapper')
        end_screen.appendChild(announcement)

        const info = document.createElement('div')
        info.innerHTML = `${result}`
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

document.addEventListener('click', handleClick)
