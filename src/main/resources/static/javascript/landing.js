import { game_board } from "./Board.js";
// import * as module from './setup.js';

start_screen();

function handle_login_to_play(event) {
    close_start_screen();
    window.location.href = "/login";
}

function handle_play_as_guest(event) {
    close_start_screen();
    window.location.href = "/start-game";
}


function start_screen(){
    const board = document.getElementById('board')
    const end_screen = document.createElement('div')
    end_screen.setAttribute('id', 'end_screen')
    end_screen.style.gridColumnStart = 2
    end_screen.style.gridColumnEnd = 8
    end_screen.style.gridRowStart = 2
    end_screen.style.gridRowEnd = 8
    end_screen.classList.add('end_screen')
    board.appendChild(end_screen)


    // const wrapper = document.createElement('div')
    // wrapper.classList.add('initial_option')
    // end_screen.appendChild(wrapper)

    const welcome = document.createElement('div')
    welcome.classList.add('start_text_wrapper')
    welcome.classList.add('header')
    welcome.innerHTML = 'Welcome!'
    end_screen.appendChild(welcome)


    const new_game = document.createElement('div')
    new_game.setAttribute('id', 'new_game')
    new_game.classList.add('text_wrapper') 
    new_game.classList.add('hoverable')
    new_game.innerHTML = 'Play as Guest'
    new_game.addEventListener("click", handle_play_as_guest);
    end_screen.appendChild(new_game)

    const login_to_play = document.createElement('div')
    login_to_play.setAttribute('id', 'login_to_play')
    login_to_play.classList.add('text_wrapper')
    login_to_play.classList.add('hoverable')
    login_to_play.innerHTML = 'Login to Play'
    login_to_play.addEventListener("click", handle_login_to_play);
    end_screen.appendChild(login_to_play)
}

function close_start_screen() {
    const end_screen = document.getElementById("end_screen");
    // const board = document.getElementById('board');
    end_screen.remove();
}