import { game_board } from "./Board.js";
import * as module from './setup.js';


// const playerId = document.getElementById('bottom_player_info').firstElementChild.innerHTML;
// console.log(document.getElementById('bottom_player_info').firstElementChild);
// console.log(document.getElementById('bottom_player_info').firstElementChild.innerHTML);

const playerId = await getPlayerId();
console.log(`playerId: ${playerId}`);

// need to pass in address here
console.log(`window.location.hostname: ${window.location.hostname}`);
const socket = new WebSocket(`ws://${window.location.hostname}:8080/websockets/game-setup`);

var game_request_sent = false;


async function getPlayerId() {
    const response = await fetch("/auth/getUsername", {
        method: "GET",
    }).then(response => {
        console.log(`response.status: ${response.status}`)
        if (response.ok) {
            var username = response.text();
            return username;
        } else {
            throw new Error(`HTTP error: ${response.status}`);
        }
    }).then(response => {
        console.log(`response: ${response}`)
        return response;
    })
    .catch(error => console.log(error));
    return response;
}


function handle_new_game() {
    console.log("handle_new_game");
    if (module.opposition != null && module.colour != null && game_request_sent == false) {
        console.log("STARTING!");
        module.close_start_screen();
        please_wait();
        game_request_sent = true;
        var colour = module.colour;
        if (colour == 'random') {
            colour = Math.floor((Math.random() * 10)) % 2 == 0 ? 'white' : 'black';
        }
        console.log("COLOUR: ", colour);
        
        sendMessage({
            'messageType' : 'INITIALISE',
            'playerId' : playerId,
            'opponentId' : 'placementOpponentId', // not needed
            'opponentType' : module.opposition,
            'breadth' : 5,
            'colour' : colour,
            'timeLimit' : module.compute_time
        });        
    }
}

socket.onopen = function(event) {
    console.log("WebSocket connection established.");
    module.start_screen(handle_new_game);
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

    socket.close();
    window.location.href = `game/${messageData}`;
};

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

function sendMessage(message) {
    if (socket.readyState === WebSocket.OPEN) {
        console.log("SENDING MESSAGE:");
        console.log(message);
        socket.send(JSON.stringify(message));
    } else {
        console.error("WebSocket connection is not open.");
    }
}