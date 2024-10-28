
/* 
Have a setup option
White or black or random?
Human or computer?
If computer:
maximum time to search for a move
*/

export var opposition = null;
export var colour = null;
export var compute_time = 1;


export function handle_human() {
    const human_button = document.getElementById('human');
    const computer_button = document.getElementById('computer');
    const new_game = document.getElementById('new_game');

    if (opposition == 'HUMAN'){
        human_button.classList.remove('selected');
        new_game.classList.remove('play_again');
        new_game.classList.add('reject_play');
        opposition = null;
        return;
    }

    human_button.classList.add('selected');
    computer_button.classList.remove('selected');
    opposition = 'HUMAN';
    if (colour != null) {
        new_game.classList.remove('reject_play');
        new_game.classList.add('play_again');
    }    
}

export function handle_computer() {
    const computer_button = document.getElementById('computer');
    const human_button = document.getElementById('human');
    const new_game = document.getElementById('new_game');

    if (opposition == "COMPUTER") {
        computer_button.classList.remove('selected');
        new_game.classList.remove('play_again');
        new_game.classList.add('reject_play');
        opposition = null;
        return;
    }
    computer_button.classList.add('selected');
    human_button.classList.remove('selected');
    opposition = 'COMPUTER';
    if (colour != null) {
        new_game.classList.remove('reject_play');
        new_game.classList.add('play_again');
    }    
}

export function handle_computer_options() {
    if (opposition == "computer") {

    }
}

export function handle_friend_challenge() {

}

export function handle_black() {
    const black_button = document.getElementById('black');
    const white_button = document.getElementById('white');
    const random_button = document.getElementById('random');
    const new_game = document.getElementById('new_game');

    if (colour == "black") {
        black_button.classList.remove('selected');
        new_game.classList.remove('play_again');
        new_game.classList.add('reject_play');
        colour = null;
        return;
    }
    black_button.classList.add('selected');
    white_button.classList.remove('selected');
    random_button.classList.remove('selected');
    colour = 'black';
    if (opposition != null) {
        new_game.classList.remove('reject_play');
        new_game.classList.add('play_again');
    }
}

export function handle_white() {
    const black_button = document.getElementById('black');
    const white_button = document.getElementById('white');
    const random_button = document.getElementById('random');
    const new_game = document.getElementById('new_game');

    if (colour == "white") {
        white_button.classList.remove('selected');
        new_game.classList.remove('play_again');
        new_game.classList.add('reject_play');
        colour = null;
        return;
    }
    white_button.classList.add('selected');
    black_button.classList.remove('selected');
    random_button.classList.remove('selected');
    colour = 'white'
    if (opposition != null) {
        new_game.classList.remove('reject_play');
        new_game.classList.add('play_again');
    }
}

export function handle_random() {
    const black_button = document.getElementById('black');
    const white_button = document.getElementById('white');
    const random_button = document.getElementById('random');
    const new_game = document.getElementById('new_game');

    if (colour == "random") {
        random_button.classList.remove('selected');
        new_game.classList.remove('play_again');
        new_game.classList.add('reject_play');
        colour = null;
        return;
    }
    white_button.classList.remove('selected');
    black_button.classList.remove('selected');
    random_button.classList.add('selected');
    colour = 'random'
    if (opposition != null) {
        new_game.classList.remove('reject_play');
        new_game.classList.add('play_again');
    }
}

export function update_compute_time() {
    const inputField = document.getElementById('number');
    compute_time = Number(inputField.value);
    if (typeof compute_time !== 'number') {
        compute_time = 1;
    }
    console.log(compute_time);
}

export function start_screen(handle_new_game){
    const board = document.getElementById('board')
    const end_screen = document.createElement('div')
    end_screen.setAttribute('id', 'end_screen')
    end_screen.style.gridColumnStart = 2
    end_screen.style.gridColumnEnd = 8
    end_screen.style.gridRowStart = 1
    end_screen.style.gridRowEnd = 9
    end_screen.classList.add('end_screen')
    board.appendChild(end_screen)


    const wrapper = document.createElement('div')
    wrapper.classList.add('initial_option')
    end_screen.appendChild(wrapper)

    const welcome = document.createElement('div')
    welcome.classList.add('start_text_wrapper')
    welcome.classList.add('header')
    welcome.innerHTML = 'New Game'
    wrapper.appendChild(welcome)

    const options = document.createElement('div')
    options.classList.add('option_wrapper')
    wrapper.appendChild(options)

    const human = document.createElement('div')
    human.setAttribute('id', 'human')
    human.classList.add('option_button')
    human.innerHTML = 'human'
    options.appendChild(human)
    human.addEventListener('click', handle_human)

    const computer = document.createElement('div')
    computer.setAttribute('id', 'computer')
    computer.classList.add('option_button')
    computer.innerHTML = 'computer'
    options.appendChild(computer)
    computer.addEventListener('click', handle_computer)

    const colours = document.createElement('div')
    colours.classList.add('option_wrapper')
    wrapper.appendChild(colours)

    const white = document.createElement('div')
    white.setAttribute('id', 'white')
    white.classList.add('option_button')
    white.innerHTML = 'White'
    colours.appendChild(white)
    white.addEventListener('click', handle_white)

    const random = document.createElement('div')
    random.setAttribute('id', 'random')
    random.classList.add('option_button')
    random.innerHTML = 'Random'
    colours.appendChild(random)
    random.addEventListener('click', handle_random)

    const black = document.createElement('div')
    black.setAttribute('id', 'black')
    black.classList.add('option_button')
    black.innerHTML = 'Black'
    colours.appendChild(black)
    black.addEventListener('click', handle_black)

    const info_wrapper = document.createElement('div')
    info_wrapper.classList.add('initial_option')
    info_wrapper.classList.add('info_wrapper')
    info_wrapper.innerHTML = 'Challenge a friend!'
    end_screen.appendChild(info_wrapper)
    info_wrapper.addEventListener('click', handle_friend_challenge)

    const form_container = document.createElement('div')
    form_container.classList.add('form')
    // end_screen.appendChild(form_container)

    const form = document.createElement('form');
    form.setAttribute("id", "compute_time_form");
    form.classList.add('form')
    end_screen.appendChild(form);

    const form_input = document.createElement('input');
    form_input.setAttribute("type", "number");
    form_input.setAttribute("id", "number");
    form_input.setAttribute("min", 1);
    form_input.addEventListener("input", update_compute_time);
    form_input.setAttribute("required", "true");
    form_input.setAttribute("placeholder", `Compute time seconds`);
    form.appendChild(form_input);


    const new_game = document.createElement('div')
    new_game.setAttribute('id', 'new_game')
    new_game.classList.add('text_wrapper')
    new_game.classList.add('reject_play')
    new_game.innerHTML = 'New Game'
    end_screen.appendChild(new_game)
    new_game.addEventListener('click', handle_new_game)
}

export function close_start_screen() {
    const end_screen = document.getElementById("end_screen");
    // const board = document.getElementById('board');
    end_screen.remove();
}