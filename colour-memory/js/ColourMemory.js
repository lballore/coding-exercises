/*!
 * Colour Memory
 *
 * @author: Luca Ballore <lballore@lucaballore.com>
 * @version: 1.0.1beta
 *
 */
var CONFIG_URL = 'colors.conf';
var COLORS_NUMBER = 8;

var boxopened = "";
var coloropened = "";
var count = 0;
var points = 0;
var found = 0;

/**
 * Semi-casual numbers generator
 *
 * @param {int} from
 * @param {int} to
 * @returns {int}
 */
function randomFromTo(from, to) {
	return Math.floor(Math.random() * (to - from + 1) + from);
}

/**
 * Retrieve the color values from a conf file (defined by the constant CONFIG_URL).
 *
 * @returns {Promise}
 */
function retrieveColors() {

	return new Promise(function(resolve, reject) {
		var xhr = new XMLHttpRequest();
		xhr.onload = function() {
			resolve(this.responseText);
		};
		xhr.onerror = reject;
		xhr.open('GET', CONFIG_URL);
		xhr.send();
	});
}

/**
 * Randomize color disposition on card grid
 *
 * @returns {void}
 */
function shuffle() {
	var children = $("#game_board").children();
	var child = $("#game_board div div.colour:first-child");
	var colorsArray = new Array;
	var tempArray = null;
	retrieveColors().then(function(result) {
		tempArray = result.match(/#\b[0-9A-F]{6}\b/gi);

		if (tempArray.length != COLORS_NUMBER) {
			alert("Invalid number of colors on config file");
			return;
		}
		for (var i = 0; i < COLORS_NUMBER; i++) {
			colorsArray.push(tempArray[i]);
			colorsArray.push(tempArray[i]);
		}

		var child = $("#game_board div:first-child");

		for (z = 0; z < children.length; z++) {
			randIndex = randomFromTo(0, colorsArray.length - 1);

			// set new image
			$("#" + child.attr("id") + " .colour").css("background-color", colorsArray[randIndex]);
			colorsArray.splice(randIndex, 1);

			child = child.next();
		}
	});
}

/**
 * Restart game and reset points
 *
 * @returns {void}
 */
function resetGame() {
	count = 0;
	points = 0;
	found = 0;
	boxopened = "";
	coloropened = "";

	$("#msg").hide();
	shuffle();
	$("#game_messages").removeClass();
	$(".colorcard").css('visibility', 'visible');
	$(".colour").hide();
	$("#msg").hide();
	$("#count").html("Attempts: " + count);
	$("#points").html("Score: " + points);
}

/**
 * Select and pair the cards.
 *
 * @returns {void}
 */
function openCard() {
	var bindmethod = 'click';
	var id = $(".playable.selected").attr("id");
	var divid = ".playable.selected";

	if ($("#" + id + " .colour").is(":hidden")) {
		$(divid).unbind(bindmethod, openCard);
		$("#" + id + " .colour").slideToggle('fast');

		if (coloropened === "") {
			boxopened = id;
			coloropened = $("#" + id + " .colour").css("background-color");
			setTimeout(function() {
				$(divid).bind(bindmethod, openCard);
			}, 300);
		} else {
			currentopened = $("#" + id + " .colour").css("background-color");
			if (coloropened !== currentopened) {
				// not found (close again)
				setTimeout(function() {
					$("#" + id + " .colour").fadeOut(1000);
					$("#" + boxopened + " .colour").fadeOut(1000);
					boxopened = "";
					coloropened = "";
				}, 400);
				assignPoints('negative');
			} else {
				// found (hide cards)
				$("#" + boxopened + ".colorcard").css('visibility', 'hidden');
				$("#" + id + ".colorcard").css('visibility', 'hidden');
				found++;
				assignPoints('positive');
			}
			setTimeout(function() {
				$(divid).bind(bindmethod, openCard);
			}, 400);
		}
		addCount();
		checkVictory()
	}
}

/**
 * Reset game after a confirm dialog
 *
 * @returns {void}
 */
function resetConfirm() {
	var answer = confirm("Are you sure you want to restart?");
	if (answer) {
		resetGame();
	}
	else {
	}
}

/**
 * Play a sound depending on an event.
 * Cross-browser solution, due to the <audio> tag not supported by all browsers.
 * May require a plugin for browsers that doesn't support wav playing natively
 *
 * @param {string} sound
 * @returns {void}
 */
function playSound(sound) {
	switch (sound) {
		case "applause":
			audioElement = document.createElement('audio');
			audioElement.src = 'snd/applause.mp3';
			audioElement.type = 'audio/mpeg';
			audioElement.setAttribute('hidden', true);
			audioElement.setAttribute('autoplay', 'autoplay');
			break;
		case "victory":
			audioElement = document.createElement('audio');
			audioElement.src = 'snd/victory.mp3';
			audioElement.type = 'audio/mpeg';
			audioElement.setAttribute('hidden', true);
			audioElement.setAttribute('autoplay', 'autoplay');
			break;
		default:
			break;
	}
	audioElement.removed = false;
	document.body.appendChild(audioElement);
}

/**
 * Point assignment. Negative or positive, according to the parameter value.
 *
 * @param {string} assignment
 * @returns {void}
 */
function assignPoints(assignment) {
	if (assignment === 'negative') {
		points--;
		$("#points").html("Score: " + points);
		$("#game_messages").removeClass();
		$("#game_messages").addClass("sad");
	} else {
		points += 2;
		boxopened = "";
		coloropened = "";
		$("#points").html("Score: " + points);
		$("#game_messages").removeClass();
		$("#game_messages").addClass("happy");
		playSound("applause");
	}
}

/**
 * Update the attempt counter
 *
 * @returns {void}
 */
function addCount() {
	count++;
	$("#count").html("Attempts: " + count);
}

/**
 * Check if all the colours have been paired. If yes, show the victory.
 *
 * @returns {void}
 */
function checkVictory() {
	if (found === COLORS_NUMBER) {
		$("#game_messages").removeClass();
		$("#game_messages").addClass("yeah");
		$("#msg").slideDown("slow");
		playSound("victory");
	}
}


$(document).ready(function() {

	$(".colour").hide(); // hide colours
	$('#msg span').click(function() { // restart option after a victory
		resetGame();
	});

	shuffle();

	$(".playable").hover(
		function(){
			$(this).addClass('selected');
			$(this).css( 'cursor', 'pointer');
		}, function(){
			$(this).removeClass('selected');
			$(this).css( 'cursor', 'default');
		}
	);

	$(".playable").click(function(){
		if ($(this).attr('id') === "restart_button") {
			// reset button action
			resetConfirm();
		} else {
			// card action
			openCard();
		}
	});


});